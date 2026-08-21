package io.github.mtykk.luckygames;

public enum GamePhase {
    /**
     * Everyone in the lobby, waiting for the game to run
     * All players joining should be sent to the lobby
     */
    WAITING,
    /**
     * `start` command issued, preparing the game world
     * Players should be assigned a team
     * Players joining should be sent to the lobby, and no team will be assigned, the player will become a spectator
     */
    PREPARING,
    /**
     * In game
     * Players joining should be sent to the game world, if a player has been assigned a team and is already in the game world, do nothing
     */
    RUNNING,
    /**
     * The last part of the game
     * Players dying in this phase will be removed of their team and turned into a spectator
     */
    EPILOGUE,
    /**
     * Game finished
     * Players joining should be sent to the game world
     */
    FINISHED

}
