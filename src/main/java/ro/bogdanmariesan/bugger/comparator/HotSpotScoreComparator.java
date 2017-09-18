package ro.bogdanmariesan.bugger.comparator;

import ro.bogdanmariesan.bugger.model.HotSpotScore;

import java.util.Comparator;

public class HotSpotScoreComparator implements Comparator<HotSpotScore> {

    public int compare(HotSpotScore o1, HotSpotScore o2) {
        return o2.getScore().compareTo(o1.getScore());
    }

}
