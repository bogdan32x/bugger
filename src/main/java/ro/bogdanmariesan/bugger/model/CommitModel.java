package ro.bogdanmariesan.bugger.model;

/**
 * 
 * @author Bogdan Mariesan
 *
 */
public class CommitModel {

    private Integer date;

    public CommitModel(Integer date) {
        this.setDate(date);
    }

    public Integer getDate() {
        return date;
    }

    public void setDate(Integer date) {
        this.date = date;
    }

}
