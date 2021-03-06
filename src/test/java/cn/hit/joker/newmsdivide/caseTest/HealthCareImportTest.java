package cn.hit.joker.newmsdivide.caseTest;

import cn.hit.joker.newmsdivide.MainSystem;
import cn.hit.joker.newmsdivide.analyzer.MicroserviceAnalyzer;
import cn.hit.joker.newmsdivide.importer.ImporterUtils;
import cn.hit.joker.newmsdivide.importer.InputData;
import cn.hit.joker.newmsdivide.importer.classImporter.ClassDiagram;
import cn.hit.joker.newmsdivide.importer.classImporter.Deploy;
import cn.hit.joker.newmsdivide.importer.classImporter.UmlClass;
import cn.hit.joker.newmsdivide.importer.sequenceImporter.SequenceDiagram;
import cn.hit.joker.newmsdivide.model.MsDivideSystem;
import cn.hit.joker.newmsdivide.model.result.Microservice;
import cn.hit.joker.newmsdivide.solver.SolveSystem;
import cn.hit.joker.newmsdivide.utils.ChangeToServiceCutterInput;
import cn.hit.joker.newmsdivide.utils.WriteFile;
import com.debacharya.nsgaii.datastructure.Population;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class HealthCareImportTest
{
    public static InputData getInputData() {
        String path0 = "cases/healthPension/class.json";
        String path1 = "cases/healthPension/nurseServiceSequence.json";
        String path2 = "cases/healthPension/bodyInfoCollectSequence.json";
        String path3 = "cases/healthPension/homeDoctorServiceSequence.json";
        String path4 = "cases/healthPension/slowSickTreatmentSequence.json";

        try {
            ClassDiagram classDiagram = ImporterUtils.importClassDiagram(path0);
            System.out.println(classDiagram);

            SequenceDiagram sequenceDiagram1 = ImporterUtils.importSequenceDiagram(path1);
            SequenceDiagram sequenceDiagram2 = ImporterUtils.importSequenceDiagram(path2);
            SequenceDiagram sequenceDiagram3 = ImporterUtils.importSequenceDiagram(path3);
            SequenceDiagram sequenceDiagram4 = ImporterUtils.importSequenceDiagram(path4);

            List<SequenceDiagram> sequenceDiagrams = new ArrayList<>();
            sequenceDiagrams.add(sequenceDiagram1);
            sequenceDiagrams.add(sequenceDiagram2);
            sequenceDiagrams.add(sequenceDiagram3);
            sequenceDiagrams.add(sequenceDiagram4);

            InputData inputData = new InputData(classDiagram, sequenceDiagrams);
            return inputData;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Test
    public void getMsDivideSystemNormalResultTest() {
        InputData inputData = getInputData();
        List<UmlClass> classList = inputData.getClassDiagram().getClassList();
        // ??????????????????
        long startTime = System.currentTimeMillis();
        List<List<Microservice>> solutionList = MainSystem.getNoDeployDivideResult(SolveSystem.MODE_GEPHI, inputData, 1);
        long endTime = System.currentTimeMillis();
        System.out.printf("???????????????%d ??????.\n", (endTime - startTime));

        String path = "src/main/resources/cases/healthPension/divideResult/normalResult";
        // ????????????
        WriteFile.delAllFile(path);

        solutionList.forEach(microserviceList -> {
            MicroserviceAnalyzer.addAllToMs(microserviceList, inputData);

            double cohesionDegree = MicroserviceAnalyzer.getCohesionDegree(microserviceList, inputData.getClassDiagram());
            double coupingDegree = MicroserviceAnalyzer.getCoupingDegree(microserviceList, inputData.getClassDiagram());

            // check satisfy deployment constraint
            boolean meet = true;
            for (Microservice microservice : microserviceList) {
                if (microservice.getClassList().size() == 1) {
                    microservice.setDeployLocationSet(inputData.getClassDiagram().getUmlClassByName(microservice.getClassList().get(0).getName()).getDeploy().getLocations());
                } else if (microservice.getClassList().size() > 1) {
                    Set<Deploy.Location> locations = MainSystem.checkDeployLocation(microservice, classList);
                    if (locations.size() == 0) {
                        meet = false;
                        break;
                    } else {
                        microservice.setDeployLocationSet(locations);
                    }
                }
            }

            double communicatePrice = MicroserviceAnalyzer.getCommunicatePrice(microserviceList, inputData.getSequenceDiagramList());
            double[] value = MicroserviceAnalyzer.getAverageValueSupport(microserviceList);

            StringBuilder builder = new StringBuilder();
            builder.append("------------------------------\n")
                    .append("???????????????????????????\n")
                    .append("????????????????????????" + microserviceList.size() + "\n")
                    .append("???????????????" + microserviceList + "\n")
                    .append("???????????????" + cohesionDegree + "\n")
                    .append("???????????????" + coupingDegree + "\n")
                    .append("?????????????????????????????????" + meet + "\n")
                    .append("??????????????????" + communicatePrice + "\n")
                    .append("????????????????????????????????????????????????" + value[0] + "\n")
                    .append("????????????????????????????????????????????????" + value[1] + "\n")
                    .append("-------------------------------------------\n");

            String fileName = microserviceList.size() + ".txt";
            WriteFile.writeToFile(path, builder.toString(), fileName);
        });
    }

    @Test
    public void getMsDivideSystemQualityResultTest() {
        InputData inputData = getInputData();
        List<UmlClass> classList = inputData.getClassDiagram().getClassList();

        // ??????????????????
        long startTime = System.currentTimeMillis();
        List<List<Microservice>> solutionList = MainSystem.getNoDeployDivideResult(SolveSystem.MODE_GEPHI, inputData, 2);
        long endTime = System.currentTimeMillis();
        System.out.printf("???????????????%d ??????.\n", (endTime - startTime));

        String path = "src/main/resources/cases/healthPension/divideResult/qualityResult";
        // ????????????
        WriteFile.delAllFile(path);

        solutionList.forEach(microserviceList -> {
            MicroserviceAnalyzer.addAllToMs(microserviceList, inputData);

            double cohesionDegree = MicroserviceAnalyzer.getCohesionDegree(microserviceList, inputData.getClassDiagram());
            double coupingDegree = MicroserviceAnalyzer.getCoupingDegree(microserviceList, inputData.getClassDiagram());

            // check satisfy deployment constraint
            boolean meet = true;
            for (Microservice microservice : microserviceList) {
                if (microservice.getClassList().size() == 1) {
                    microservice.setDeployLocationSet(inputData.getClassDiagram().getUmlClassByName(microservice.getClassList().get(0).getName()).getDeploy().getLocations());
                } else if (microservice.getClassList().size() > 1) {
                    Set<Deploy.Location> locations = MainSystem.checkDeployLocation(microservice, classList);
                    if (locations.size() == 0) {
                        meet = false;
                        break;
                    } else {
                        microservice.setDeployLocationSet(locations);
                    }
                }
            }

            double communicatePrice = MicroserviceAnalyzer.getCommunicatePrice(microserviceList, inputData.getSequenceDiagramList());
            double[] value = MicroserviceAnalyzer.getAverageValueSupport(microserviceList);

            StringBuilder builder = new StringBuilder();
            builder.append("------------------------------\n")
                    .append("???????????????????????????\n")
                    .append("????????????????????????" + microserviceList.size() + "\n")
                    .append("???????????????" + microserviceList + "\n")
                    .append("???????????????" + cohesionDegree + "\n")
                    .append("???????????????" + coupingDegree + "\n")
                    .append("?????????????????????????????????" + meet + "\n")
                    .append("??????????????????" + communicatePrice + "\n")
                    .append("????????????????????????????????????????????????" + value[0] + "\n")
                    .append("????????????????????????????????????????????????" + value[1] + "\n")
                    .append("-------------------------------------------\n");

            String fileName = microserviceList.size() + ".txt";
            WriteFile.writeToFile(path, builder.toString(), fileName);
        });
    }

    @Test
    public void getMsDivideSystemDeployResultTest() {
        InputData inputData = getInputData();

        // ??????????????????
        long startTime = System.currentTimeMillis();
        List<List<Microservice>> solutionList = MainSystem.getDivideResult(SolveSystem.MODE_GEPHI, inputData, 3);
        long endTime = System.currentTimeMillis();
        System.out.printf("???????????????%d ??????.\n", (endTime - startTime));

        String path = "src/main/resources/cases/healthPension/divideResult/deployResult";
        // ????????????
        WriteFile.delAllFile(path);

        solutionList.forEach(microserviceList -> {
            MicroserviceAnalyzer.addAllToMs(microserviceList, inputData);

            double cohesionDegree = MicroserviceAnalyzer.getCohesionDegree(microserviceList, inputData.getClassDiagram());
            double coupingDegree = MicroserviceAnalyzer.getCoupingDegree(microserviceList, inputData.getClassDiagram());
            double communicatePrice = MicroserviceAnalyzer.getCommunicatePrice(microserviceList, inputData.getSequenceDiagramList());
            double[] value = MicroserviceAnalyzer.getAverageValueSupport(microserviceList);

            StringBuilder builder = new StringBuilder();
            builder.append("------------------------------\n")
                    .append("???????????????????????????\n")
                    .append("????????????????????????" + microserviceList.size() + "\n")
                    .append("???????????????" + microserviceList + "\n")
                    .append("???????????????" + cohesionDegree + "\n")
                    .append("???????????????" + coupingDegree + "\n")
                    .append("??????????????????" + communicatePrice + "\n")
                    .append("????????????????????????????????????????????????" + value[0] + "\n")
                    .append("????????????????????????????????????????????????" + value[1] + "\n")
                    .append("-------------------------------------------\n");

            String fileName = microserviceList.size() + ".txt";
            WriteFile.writeToFile(path, builder.toString(), fileName);
        });
    }

    @Test
    public void getMsDivideSystemNewResultTest() {
        InputData inputData = getInputData();
        // ??????????????????
        long startTime = System.currentTimeMillis();
        List<List<Microservice>> solutionList = MainSystem.getDivideResult(SolveSystem.MODE_GEPHI, inputData, 4);
        long endTime = System.currentTimeMillis();
        System.out.printf("???????????????%d ??????.\n", (endTime - startTime));

        String path = "src/main/resources/cases/healthPension/divideResult/newResult";
        // ????????????
        WriteFile.delAllFile(path);

        solutionList.forEach(microserviceList -> {
            MicroserviceAnalyzer.addAllToMs(microserviceList, inputData);

            double cohesionDegree = MicroserviceAnalyzer.getCohesionDegree(microserviceList, inputData.getClassDiagram());
            double coupingDegree = MicroserviceAnalyzer.getCoupingDegree(microserviceList, inputData.getClassDiagram());
            double communicatePrice = MicroserviceAnalyzer.getCommunicatePrice(microserviceList, inputData.getSequenceDiagramList());
            double[] value = MicroserviceAnalyzer.getAverageValueSupport(microserviceList);

            StringBuilder builder = new StringBuilder();
            builder.append("------------------------------\n")
                    .append("???????????????????????????\n")
                    .append("????????????????????????" + microserviceList.size() + "\n")
                    .append("???????????????" + microserviceList + "\n")
                    .append("???????????????" + cohesionDegree + "\n")
                    .append("???????????????" + coupingDegree + "\n")
                    .append("??????????????????" + communicatePrice + "\n")
                    .append("????????????????????????????????????????????????" + value[0] + "\n")
                    .append("????????????????????????????????????????????????" + value[1] + "\n")
                    .append("-------------------------------------------\n");

            String fileName = microserviceList.size() + ".txt";
            WriteFile.writeToFile(path, builder.toString(), fileName);
       });
    }

    @Test
    public void getRandomMsDivideSystemTest() {
        InputData inputData = getInputData();
        List<List<Microservice>> solutionList = MainSystem.getRandomDivideResult(inputData, 1, 4);
        String path = "src/main/resources/cases/healthPension/randomDivideResult";
        // ????????????
//        WriteFile.delAllFile(path);
        for (int i = 0; i < solutionList.size(); i++) {
            List<Microservice> microserviceList = solutionList.get(i);
            MicroserviceAnalyzer.addAllToMs(microserviceList, inputData);

            double cohesionDegree = MicroserviceAnalyzer.getCohesionDegree(microserviceList, inputData.getClassDiagram());
            double coupingDegree = MicroserviceAnalyzer.getCoupingDegree(microserviceList, inputData.getClassDiagram());
            double communicatePrice = MicroserviceAnalyzer.getCommunicatePrice(microserviceList, inputData.getSequenceDiagramList());
            double[] value = MicroserviceAnalyzer.getAverageValueSupport(microserviceList);

            StringBuilder builder = new StringBuilder();
            builder.append("------------------------------\n")
                    .append("???????????????????????????\n")
                    .append("????????????????????????" + microserviceList.size() + "\n")
                    .append("???????????????" + microserviceList + "\n")
                    .append("???????????????" + cohesionDegree + "\n")
                    .append("???????????????" + coupingDegree + "\n")
                    .append("??????????????????" + communicatePrice + "\n")
                    .append("????????????????????????????????????????????????" + value[0] + "\n")
                    .append("????????????????????????????????????????????????" + value[1] + "\n")
                    .append("-------------------------------------------\n");

            String fileName = "??????" + SolveSystem.MODE_FAST_NEWMAN + "_" + i  + "--" + microserviceList.size() + ".txt";
            WriteFile.writeToFile(path, builder.toString(), fileName);
        }

    }

    @Test
    public void FastNewManTest() {
        InputData inputData = getInputData();
        MsDivideSystem msDivideSystem = inputData.getMsDivideSystem();
        List<Microservice> msList = MainSystem.start(SolveSystem.MODE_FAST_NEWMAN, 0, msDivideSystem, 4);
        MicroserviceAnalyzer.addAllToMs(msList, inputData);

        double cohesionDegree = MicroserviceAnalyzer.getCohesionDegree(msList, inputData.getClassDiagram());
        double coupingDegree = MicroserviceAnalyzer.getCoupingDegree(msList, inputData.getClassDiagram());
        double[] value = MicroserviceAnalyzer.getAverageValueSupport(msList);
//        System.out.println("\n\n-----------------------------------");
//        System.out.println("????????????????????????" + msList.size());
//        System.out.println("???????????????" + cohesionDegree);
//        System.out.println("???????????????" + coupingDegree);

        StringBuilder builder = new StringBuilder();
        builder.append("------------------------------\n")
                .append("???????????????????????????\n")
                .append("????????????????????????" + msList.size() + "\n")
                .append("???????????????" + msList + "\n")
                .append("???????????????" + cohesionDegree + "\n")
                .append("???????????????" + coupingDegree + "\n")
                .append("????????????????????????????????????????????????" + value[0] + "\n")
                .append("????????????????????????????????????????????????" + value[1] + "\n")
                .append("-------------------------------------------\n");

        System.out.println(builder);
//        String path = "src/main/resources/cases/healthPension/divideResult";
//        String fileName = msList.size() + ".txt";
//        WriteFile.writeToFile(path, builder.toString(), fileName);
    }

    @Test
    public void resultAnalyze() {
        InputData inputData = getInputData();
        MsDivideSystem msDivideSystem = inputData.getMsDivideSystem();
        List<Microservice> msList = MainSystem.start(SolveSystem.MODE_FAST_NEWMAN, 0, msDivideSystem, 4);
        msList.forEach(microservice -> {
            microservice.setDeployLocationSet(MainSystem.checkDeployLocation(microservice, inputData.getClassDiagram().getClassList()));
        });
        System.out.println(msList);
        double cohesionDegree = MicroserviceAnalyzer.getCohesionDegree(msList, inputData.getClassDiagram());
        double coupingDegree = MicroserviceAnalyzer.getCoupingDegree(msList, inputData.getClassDiagram());
        double communicatePrice = MicroserviceAnalyzer.getCommunicatePrice(msList, inputData.getSequenceDiagramList());
        System.out.println("\n\n-----------------------------------");
        System.out.println("????????????????????????" + msList.size());
        System.out.println("???????????????" + cohesionDegree);
        System.out.println("???????????????" + coupingDegree);
        System.out.println("??????????????????" + communicatePrice);
    }
}
