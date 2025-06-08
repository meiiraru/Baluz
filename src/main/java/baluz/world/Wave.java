package baluz.world;

import baluz.world.entity.Balloon;

import java.util.ArrayList;
import java.util.List;

public class Wave {

    //awards
    private final int time, score;

    //balloons
    private final List<Balloon> ballons = new ArrayList<>();

    public Wave(int time, int score) {
        this.time = time;
        this.score = score;
    }

    public List<Balloon> getBallons() {
        return ballons;
    }

    public int getTimeReward() {
        return time;
    }

    public int getScoreReward() {
        return score;
    }

    public boolean isComplete() {
        for (Balloon ballon : ballons)
            if (!ballon.isRemoved())
                return false;
        return true;
    }
}
