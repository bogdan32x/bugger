package ro.bogdanmariesan.bugger.model;

import java.text.DecimalFormat;

/**
 * @author Bogdan Mariesan
 */
public final class HotSpotScore {

    private Double score;
    private String file;
    private int bugFixCommits;

    public HotSpotScore(Double score, String file, int bugFixCommits) {
        this.setScore(score);
        this.setFile(file);
        this.setBugFixCommits(bugFixCommits);
    }

    public int getBugFixCommits() {
        return bugFixCommits;
    }

    private void setBugFixCommits(int bugFixCommits) {
        this.bugFixCommits = bugFixCommits;
    }

    public Double getScore() {
        return score;
    }

    private void setScore(Double score) {
        this.score = score;
    }

    public String getFile() {
        return file;
    }

    private void setFile(String file) {
        this.file = file;
    }

    @Override
    public String toString() {
        final DecimalFormat df = new DecimalFormat("##0.000000");
        return "HotSpotScore [score=" + df.format(score) + ", bugFixCommits=" + bugFixCommits + ", file=" + file + "]";
    }

}
