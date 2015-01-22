package ro.bogdanmariesan.bugger.model;

/**
 * 
 * @author Bogdan Mariesan
 *
 */
public final class CommitModel {

    private Double date;

    public CommitModel(Double date) {
        this.setDate(date);
    }

    public Double getDate() {
        return date;
    }

    public void setDate(Double date) {
        this.date = date;
    }

}
