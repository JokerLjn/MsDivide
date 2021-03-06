package cn.hit.joker.newmsdivide.solverTest;

import cn.hit.joker.newmsdivide.MainSystem;
import cn.hit.joker.newmsdivide.importer.ImporterUtils;
import cn.hit.joker.newmsdivide.importer.InputData;
import cn.hit.joker.newmsdivide.importer.classImporter.ClassDiagram;
import cn.hit.joker.newmsdivide.importer.sequenceImporter.SequenceDiagram;
import cn.hit.joker.newmsdivide.solver.SolveSystem;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author joker
 * @version 1.0
 * @date 2021/6/3 16:52
 * @description
 */
public class SolverTest {
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

    @Test
    public void ChineseWhisperSolverTest() {
        MainSystem.start(SolveSystem.MODE_CW, 0, getInputData().getMsDivideSystem(), 4);
    }

    @Test
    public void MarkovSolverTest() {
        MainSystem.start(SolveSystem.MODE_MARKOV, 0, getInputData().getMsDivideSystem(), 4);
    }

    @Test
    public void FastNewmanSolverTest() {
        MainSystem.start(SolveSystem.MODE_FAST_NEWMAN, 0, getInputData().getMsDivideSystem(), 4);
    }

    @Test
    public void GNSolverTest() {
        MainSystem.start(SolveSystem.MODE_GEPHI, 3, getInputData().getMsDivideSystem(), 4);
    }

    @Test
    public void DivideResultTest() {
        MainSystem.getDivideResult(SolveSystem.MODE_GEPHI, getInputData(), 4);
    }
}
