package cn.hit.joker.newmsdivide.solver;

import cn.hit.joker.newmsdivide.model.MsDivideSystem;
import cn.hit.joker.newmsdivide.model.criteria.ClassPair;
import cn.hit.joker.newmsdivide.scorer.Score;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.nlpub.watset.graph.ChineseWhispers;
import org.nlpub.watset.graph.Clustering;
import org.nlpub.watset.graph.NodeWeighting;

import java.util.Map;

/**
 * @author joker
 * @version 1.0
 * @date 2021/6/3 16:33
 * @description chinese whisper clustering algorithm solver
 */
public class ChineseWhispersSolver extends WatsetSolver {
    private final NodeWeighting nodeWeighting;

    public ChineseWhispersSolver(MsDivideSystem msDivideSystem, Map<ClassPair, Map<String, Score>> scores, SolverConfig config) {
        super(msDivideSystem, scores);
        this.nodeWeighting = mapNodeWeightingConfig(config.getValueForAlgorithmParam("cwNodeWeighting").intValue());
    }

    @Override
    protected Clustering<String> getAlgorithm() {
        return new ChineseWhispers<String, DefaultWeightedEdge>(graph, nodeWeighting);
    }

    /**
     * Map integer value to nodeWeighting parameter here, because Service Cutter only supports numbers as algorithm parameters ...
     * <p>
     * 0 = top (default)
     * 1 = label
     * 2 = linear
     * 3 = log
     * <p>
     * https://github.com/nlpub/watset-java#chinese-whispers
     */
    private NodeWeighting mapNodeWeightingConfig(int nodeWeighting) {
        switch (nodeWeighting) {
            case 1:
                return NodeWeighting.label();
            case 2:
                return NodeWeighting.linear();
            case 3:
                return NodeWeighting.log();
            default:
                return NodeWeighting.top();
        }
    }
}
