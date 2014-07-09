package ro.bogdanmariesan.bugger.comparator;

import java.util.Comparator;

import ro.bogdanmariesan.bugger.model.HotSpotScore;

public class HotSpotScoreComparator implements Comparator<HotSpotScore> {

    public int compare(HotSpotScore o1, HotSpotScore o2) {
        return o1.getScore().compareTo(o2.getScore());
    }

}
