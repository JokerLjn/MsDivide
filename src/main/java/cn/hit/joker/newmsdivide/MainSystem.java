package cn.hit.joker.newmsdivide;

import cn.hit.joker.newmsdivide.importer.ImporterUtils;
import cn.hit.joker.newmsdivide.importer.InputData;
import cn.hit.joker.newmsdivide.importer.classImporter.ClassDiagram;
import cn.hit.joker.newmsdivide.importer.classImporter.Deploy;
import cn.hit.joker.newmsdivide.importer.classImporter.UmlClass;
import cn.hit.joker.newmsdivide.importer.sequenceImporter.SequenceDiagram;
import cn.hit.joker.newmsdivide.model.MsDivideSystem;
import cn.hit.joker.newmsdivide.model.result.Microservice;
import cn.hit.joker.newmsdivide.solver.SolveSystem;
import cn.hit.joker.newmsdivide.solver.SolverConfig;
import lombok.extern.slf4j.Slf4j;

import javax.xml.stream.Location;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author joker
 * @version 1.0
 * @date 2021/6/4 21:08
 * @description
 */
@Slf4j
public class MainSystem {
    public static InputData getInputData() {
        String classPath = "cases/case1 ddd cargo transport/class.json";
        String sequencePath = "cases/case1 ddd cargo transport/sequence.json";
        InputData inputData = null;
        try {
            ClassDiagram classDiagram = ImporterUtils.importClassDiagram(classPath);
            SequenceDiagram sequenceDiagram = ImporterUtils.importSequenceDiagram(sequencePath);
            inputData = new InputData(classDiagram, sequenceDiagram);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return inputData;
    }

    /**
     * get microservice solution of input algorithm
     *
     * @param algorithm
     * @param clusterNum
     * @return
     */
    public static List<Microservice> start(String algorithm, int clusterNum) {
        InputData inputData = getInputData();
        MsDivideSystem divideSystem = inputData.getMsDivideSystem();
        SolverConfig solverConfig;
        switch (algorithm) {
            case SolveSystem.MODE_CW:
                solverConfig = SolverConfig.getChineseWhisperConfig();
                break;
            case SolveSystem.MODE_MARKOV:
                solverConfig = SolverConfig.getMarkovConfig();
                break;
            case SolveSystem.MODE_FAST_NEWMAN:
                solverConfig = SolverConfig.getFastNewmanConfig();
                break;
            case SolveSystem.MODE_GEPHI:
                solverConfig = SolverConfig.getGephiConfig(clusterNum);
                break;
            default:
                throw new IllegalArgumentException("输入的算法不支持");
        }
        List<Microservice> msList = new SolveSystem().solveSystem(divideSystem, solverConfig, SolveSystem.TYPE_NEW);
        System.out.println(msList);
        return msList;
    }

    /**
     * get all microservice solution in deployment constraint of input algorithm
     *
     * @param algorithm
     * @return
     */
    public static List<List<Microservice>> getDivideResult(String algorithm) {
        List<List<Microservice>> msSolutionList = new ArrayList<>();
        InputData inputData = getInputData();
        List<UmlClass> classList = inputData.getClassDiagram().getClassList();
        for (int i=1; i<=classList.size(); i++) {
            List<Microservice> msList = start(algorithm, i);
            if (msList.size() == i) {
                msSolutionList.add(msList);
            }
        }

        // check deployLocation constraint
        for (int i=0; i< msSolutionList.size(); i++) {
            boolean meet = true;
            for (Microservice microservice : msSolutionList.get(i)) {
                if (microservice.getClassList().size() > 1) {
                    if (!checkDeployLocation(microservice, classList)) {
                        log.warn("微服务划分方案：[" + msSolutionList.get(i) + "] 不符合部署位置约束！");
                        msSolutionList.remove(i);
                        i--;
                        meet = false;
                        
                        break;
                    }
                }
            }
            if (meet) {
                log.info("微服务划分方案：[" + msSolutionList.get(i) + "] 符合部署位置约束！");
            }
        }
        return msSolutionList;
    }

    public static boolean checkDeployLocation(Microservice microservice, List<UmlClass> classList) {
        Set<Deploy.Location> locations = new HashSet<>();
        locations.add(Deploy.Location.Cloud);
        locations.add(Deploy.Location.Edge);
        locations.add(Deploy.Location.End);

        microservice.getClassList().forEach(umlClass -> {
            umlClass = getClass(classList, umlClass);
            if (umlClass == null) {
                throw new IllegalArgumentException("检查部署位置约束时找不到类名，出现错误！");
            }
            locations.retainAll(umlClass.getDeploy().getLocations());
        });

        return locations.size() > 0 ;
    }

    private static UmlClass getClass(List<UmlClass> classList, UmlClass emptyClass) {
        for (UmlClass umlClass : classList) {
            if (umlClass.equals(emptyClass)) {
                return umlClass;
            }
        }
        return null;
    }
}