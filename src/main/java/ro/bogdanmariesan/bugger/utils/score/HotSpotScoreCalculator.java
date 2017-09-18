package ro.bogdanmariesan.bugger.utils.score;

import org.eclipse.jgit.revwalk.RevCommit;
import org.springframework.stereotype.Component;
import ro.bogdanmariesan.bugger.comparator.CommitModelComparator;
import ro.bogdanmariesan.bugger.model.CommitModel;
import ro.bogdanmariesan.bugger.model.HotSpotScore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class HotSpotScoreCalculator {

    public List<HotSpotScore> evaluateHotSpotScoreForBranch(Map<String, List<CommitModel>> commitFilesMap, RevCommit firstCommit, RevCommit lastCommit, Double bugScoreBoundary) {
        final List<HotSpotScore> hotSpotScores = new ArrayList<>();

        final Double min = (double) firstCommit.getCommitTime();
        final Double max = (double) lastCommit.getCommitTime();

        commitFilesMap.forEach((key, commitList) -> {
            commitList.sort(new CommitModelComparator());
            final Double actualBugScore = extractBugScore(commitList, min, max);
            if (actualBugScore >= bugScoreBoundary) {
                hotSpotScores.add(new HotSpotScore(actualBugScore, key, commitList.size()));
            }
        });

        return hotSpotScores;
    }

    private double extractBugScore(final List<CommitModel> list, final double min, final double max) {
        double score = 0;
        for (int k = 0; k < list.size(); k++) {
            score += computeSimpleScore(list.get(k), k, min, max);
        }
        return score;
    }

    private double computeSimpleScore(final CommitModel commitModel, final int i, final double min, final double max) {
        // (x-min)/(max-min) - normalization
        final double normalizedIndex = (commitModel.getDate() - min) / (max - min);
        final double exponent = Math.exp(-12 * normalizedIndex * i + 12);
        final double denominator = 1 + exponent;
        final double numerator = 1;
        return numerator / denominator;
    }
}