package cn.hit.joker.newmsdivide.functionDecomposition;

import cn.hit.joker.newmsdivide.MainSystem;
import cn.hit.joker.newmsdivide.analyzer.MicroserviceAnalyzer;
import cn.hit.joker.newmsdivide.importer.InputData;
import cn.hit.joker.newmsdivide.importer.ReadFile;
import cn.hit.joker.newmsdivide.importer.classImporter.ClassDiagram;
import cn.hit.joker.newmsdivide.importer.classImporter.Deploy;
import cn.hit.joker.newmsdivide.importer.classImporter.UmlClass;
import cn.hit.joker.newmsdivide.model.result.Microservice;
import cn.hit.joker.newmsdivide.utils.WriteFile;
import cn.hit.joker.nsga2.Nsga2DDDCargoDemo;
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
 * @date 2021/7/4 15:15
 * @description
 */
public class DDDCargoTest {
    @Test
    public void analyzeDivideResult() {
        String path = "cases/dddCargo/functionalDecomposition/divideResult.json";
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

        InputData inputData = Nsga2DDDCargoDemo.getInputData();
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
        double communicatePrice = 0;
        if (suit) {
            communicatePrice = MicroserviceAnalyzer.getCommunicatePrice(msList, inputData.getSequenceDiagramList());
        }
        double[] value = MicroserviceAnalyzer.getAverageValueSupport(msList);

        StringBuilder builder = new StringBuilder();
        builder.append("------------------------------\n")
                .append("???????????????????????????\n")
                .append("????????????????????????" + msList.size() + "\n")
                .append("???????????????" + msList + "\n")
                .append("???????????????" + cohesionDegree + "\n")
                .append("???????????????" + coupingDegree + "\n")
                .append("?????????????????????????????????" + suit + "\n")
                .append("??????????????????" + communicatePrice + "\n")
                .append("????????????????????????????????????????????????" + value[0] + "\n")
                .append("????????????????????????????????????????????????" + value[1] + "\n")
                .append("-------------------------------------------\n");

        String outPath = "src/main/resources/cases/dddCargo/functionalDecomposition";
        String fileName = "functionDecomposition-" + msList.size() + ".txt";
        WriteFile.writeToFile(outPath, builder.toString(), fileName);
    }
}
