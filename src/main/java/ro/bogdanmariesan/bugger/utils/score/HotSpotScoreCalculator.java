package ro.bogdanmariesan.bugger.utils.score;

import org.eclipse.jgit.revwalk.RevCommit;
import org.springframework.stereotype.Component;
import ro.bogdanmariesan.bugger.comparator.CommitModelComparator;
import ro.bogdanmariesan.bugger.model.CommitModel;
import ro.bogdanmariesan.bugger.model.HotSpotScore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class HotSpotScoreCalculator {

    public List<HotSpotScore> evaluateHotSpotScoreForBranch(Map<String, List<CommitModel>> commitFilesMap, RevCommit firstCommit, RevCommit lastCommit) {
        final List<HotSpotScore> hotSpotScores = new ArrayList<HotSpotScore>();

        final Double min = (double) firstCommit.getCommitTime();
        final Double max = (double) lastCommit.getCommitTime();

        for (final Map.Entry<String, List<CommitModel>> stringListEntry : commitFilesMap.entrySet()) {
            final List<CommitModel> commitList = stringListEntry.getValue();
            Collections.sort(commitList, new CommitModelComparator());
            final Double score = extractBugScore(commitList, min, max);
            if (score >= 2) {
                hotSpotScores.add(new HotSpotScore(score, stringListEntry.getKey(),commitList.size()));
            }
        }
        return hotSpotScores;
    }

    public double extractBugScore(final List<CommitModel> list, final double min, final double max) {
        double score = 0;
        for (int k = 0; k < list.size(); k++) {
            score += computeSimpleScore(list.get(k), k, min, max);
        }
        return score;
    }

    private double computeSimpleScore(final CommitModel commitModel, final int i, final double min, final double max) {
        // (x-min)/(max-min) - normalization
        double normalizedIndex = (commitModel.getDate() - min) / (max - min);
        double exponent = Math.exp(-12 * normalizedIndex * i + 12);
        double denominator = 1 + exponent;
        double numerator = 1;
        return numerator / denominator;
    }
}