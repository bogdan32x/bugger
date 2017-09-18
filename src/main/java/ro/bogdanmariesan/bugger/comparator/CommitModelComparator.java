package ro.bogdanmariesan.bugger.comparator;

import ro.bogdanmariesan.bugger.model.CommitModel;

import java.util.Comparator;

/**
 * @author Bogdan Mariesan
 */
public class CommitModelComparator implements Comparator<CommitModel> {

    public int compare(CommitModel o1, CommitModel o2) {
        return o1.getDate().compareTo(o2.getDate());
    }
}
