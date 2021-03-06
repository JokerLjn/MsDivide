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
import cn.hit.joker.newmsdivide.utils.ChangeToServiceCutterInput;
import cn.hit.joker.newmsdivide.utils.WriteFile;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.cglib.core.Local;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author joker
 * @version 1.0
 * @date 2021/6/16 10:04
 * @description
 */
public class ServiceCutter {

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
    public void changeToServiceCutterInput() {
        String data = ChangeToServiceCutterInput.Change(getInputData().getClassDiagram());
        String path = "src/main/resources/cases/healthPension/serviceCutter";
        WriteFile.writeToFile(path, data, "HealthCareClass.json");
    }

    @Test
    // analyze service cutter divide result
    public void analyzeServiceCutterResult() {
        String path = "cases/healthPension/serviceCutter/divideResult.json";
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

        String outPath = "src/main/resources/cases/healthPension/serviceCutter";
        String fileName = "serviceCutter-" + msList.size() + ".txt";
        WriteFile.writeToFile(outPath, builder.toString(), fileName);
    }
}
