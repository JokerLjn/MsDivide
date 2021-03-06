package cn.hit.joker.newmsdivide.caseTest;

import cn.hit.joker.newmsdivide.MainSystem;
import cn.hit.joker.newmsdivide.analyzer.MicroserviceAnalyzer;
import cn.hit.joker.newmsdivide.importer.ImporterUtils;
import cn.hit.joker.newmsdivide.importer.InputData;
import cn.hit.joker.newmsdivide.importer.ReadFile;
import cn.hit.joker.newmsdivide.importer.classImporter.ClassDiagram;
import cn.hit.joker.newmsdivide.importer.classImporter.Deploy;
import cn.hit.joker.newmsdivide.importer.classImporter.UmlClass;
import cn.hit.joker.newmsdivide.importer.sequenceImporter.SequenceDiagram;
import cn.hit.joker.newmsdivide.model.result.Microservice;
import cn.hit.joker.newmsdivide.solver.SolveSystem;
import cn.hit.joker.newmsdivide.utils.WriteFile;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author joker
 * @version 1.0
 * @date 2021/7/4 9:12
 * @description
 */
public class DDDCargoTest {
    // get input data
    private InputData getInputData() {
        String classPath = "cases/dddCargo/class.json";
        String sequencePath = "cases/dddCargo/sequence.json";
        InputData inputData = null;
        try {
            ClassDiagram classDiagram = ImporterUtils.importClassDiagram(classPath);
            SequenceDiagram sequenceDiagram = ImporterUtils.importSequenceDiagram(sequencePath);
            List<SequenceDiagram> sequenceDiagrams = new ArrayList<>();
            sequenceDiagrams.add(sequenceDiagram);
            inputData = new InputData(classDiagram, sequenceDiagrams);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return inputData;
    }

    // get ms divide result;
    @Test
    public void getMsDivideSystemNormalResultTest() {
        InputData inputData = getInputData();
        List<UmlClass> classList = inputData.getClassDiagram().getClassList();
        // ??????????????????
        long startTime = System.currentTimeMillis();
        List<List<Microservice>> solutionList = MainSystem.getNoDeployDivideResult(SolveSystem.MODE_GEPHI, inputData, 1);
        long endTime = System.currentTimeMillis();
        System.out.printf("???????????????%d ??????.\n", (endTime - startTime));

        String path = "src/main/resources/cases/dddCargo/divideResult/normalResult";
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
    public void getRandomMsDivideSystemTest() {
        InputData inputData = getInputData();
        // ??????????????????
        long startTime = System.currentTimeMillis();
        List<List<Microservice>> solutionList = MainSystem.getRandomDivideResult(inputData, 1, 4);
        long endTime = System.currentTimeMillis();
        System.out.printf("???????????????%d ??????.\n", (endTime - startTime));

        String path = "src/main/resources/cases/dddCargo/divideResult/normalResult";
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
    public void getMsDivideSystemQualityResultTest() {
        InputData inputData = getInputData();
        List<UmlClass> classList = inputData.getClassDiagram().getClassList();

        // ??????????????????
        long startTime = System.currentTimeMillis();
        List<List<Microservice>> solutionList = MainSystem.getNoDeployDivideResult(SolveSystem.MODE_GEPHI, inputData, 2);
        long endTime = System.currentTimeMillis();
        System.out.printf("???????????????%d ??????.\n", (endTime - startTime));

        String path = "src/main/resources/cases/dddCargo/divideResult/qualityResult";
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

        String path = "src/main/resources/cases/dddCargo/divideResult/deployResult";
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

        String path = "src/main/resources/cases/dddCargo/divideResult/newResult";
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
    // analyze service cutter divide result
    public void analyzeServiceCutterResult() {
        String path = "cases/dddCargo/serviceCutter/divideResult.json";
        String input;
        List<Microservice> msList = new ArrayList<>();
        // get msList
        try {
            input = ReadFile.readFromJsonFile(path);
//            System.out.println(input);
            JSONObject msJson = JSON.parseObject(input);
            msJson.entrySet().forEach(entry -> {
                String msName = entry.getKey();
                List<UmlClass> classList = new ArrayList<>();
                JSONArray classArray = (JSONArray) entry.getValue();
                for (int i = 0; i < classArray.size(); i++) {
                    classList.add(new UmlClass(classArray.getString(i)));
                }
                msList.add(new Microservice(msName, classList));
            });
            System.out.println(msList);
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }

        InputData inputData = getInputData();
        ClassDiagram classDiagram = inputData.getClassDiagram();
        boolean suit = true;
        for (Microservice microservice : msList) {
            Set<Deploy.Location> deploySet = MainSystem.checkDeployLocation(microservice, classDiagram.getClassList());
            if (deploySet.size() > 0) {
                microservice.setDeployLocationSet(deploySet);
            } else {
                suit = false;
            }
        }

        if (!suit) {
            System.out.println("??????????????????????????????????????????");
        } else {
            System.out.println("???????????????????????????????????????");
        }

        MicroserviceAnalyzer.addAllToMs(msList, inputData);

        double cohesionDegree = MicroserviceAnalyzer.getCohesionDegree(msList, inputData.getClassDiagram());
        double coupingDegree = MicroserviceAnalyzer.getCoupingDegree(msList, inputData.getClassDiagram());
        double communicatePrice = MicroserviceAnalyzer.getCommunicatePrice(msList, inputData.getSequenceDiagramList());
        double[] value = MicroserviceAnalyzer.getAverageValueSupport(msList);

        StringBuilder builder = new StringBuilder();
        builder.append("------------------------------\n")
                .append("???????????????????????????\n")
                .append("????????????????????????" + msList.size() + "\n")
                .append("???????????????" + msList + "\n")
                .append("???????????????" + cohesionDegree + "\n")
                .append("???????????????" + coupingDegree + "\n")
                .append("??????????????????" + communicatePrice + "\n")
                .append("????????????????????????????????????????????????" + value[0] + "\n")
                .append("????????????????????????????????????????????????" + value[1] + "\n")
                .append("-------------------------------------------\n");

        String outPath = "src/main/resources/cases/dddCargo/serviceCutter";
        String fileName = "serviceCutter-" + msList.size() + ".txt";
        WriteFile.writeToFile(outPath, builder.toString(), fileName);
    }
}
