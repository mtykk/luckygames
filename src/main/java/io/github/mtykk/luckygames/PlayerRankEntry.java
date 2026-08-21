package io.github.mtykk.luckygames;

public class PlayerRankEntry {
    private final String name;
    private final int team;
    PlayerRankEntry(String name, int team){
        this.name = name;
        this.team = team;
    }

    public String getName() {
        return name;
    }

    public int getTeam() {
        return team;
    }
}
