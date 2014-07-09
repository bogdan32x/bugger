package ro.bogdanmariesan.bugger.model;

import java.text.DecimalFormat;

/**
 * 
 * @author Bogdan Mariesan
 *
 */
public class HotSpotScore {

    private Double score;
    private String file;

    public HotSpotScore(Double score, String file) {
        this.setScore(score);
        this.setFile(file);
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        DecimalFormat df = new DecimalFormat("##0.000000");
        builder.append("HotSpotScore [score=");
        builder.append(df.format(score));
        builder.append(", file=");
        builder.append(file);
        builder.append("]");
        return builder.toString();
    }

}
