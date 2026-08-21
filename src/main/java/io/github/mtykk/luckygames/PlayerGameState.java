package io.github.mtykk.luckygames;

public class PlayerGameState {
    public int respawnLocationIndex;
    public boolean[] locationVisited;
    PlayerGameState(int locationIndexCount){
        locationVisited = new boolean[locationIndexCount];
        locationVisited[0] = true;
    }
}
