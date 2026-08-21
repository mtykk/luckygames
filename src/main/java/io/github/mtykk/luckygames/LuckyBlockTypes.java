package io.github.mtykk.luckygames;

public enum LuckyBlockTypes {
    UNLUCKY , //Black Stained Glass
    NORMAL, //Yellow Stained Glass
    LUCKY, //Lime Stained Glass
    CHALLENGE; //Light Blue Stained Glass

    @Override
    public String toString(){
        return name().toLowerCase();
    }
}