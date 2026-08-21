package io.github.mtykk.luckygames;

import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;

public class LuckyEvents {
    private final List<List<LuckyRunnable>> luckyEventsRegistry = new ArrayList<>(); //A list for 4 lists, the 4 lists are for 4 types of events(UNLUCKY,NORMAL,LUCKY,CHALLENGE)
    private static final int[][] eventTypeWeightPerBlockType = new int[4][4]; //The first dimension represents the block type, the second dimension represents the lucky event type, the value represents the weight
    private int unluckyEventsTotalWeight = 0;
    private int normalEventsTotalWeight = 0;
    private int luckyEventsTotalWeight = 0;
    private int challengeEventsTotalWeight = 0;
    private final PlayerOngoingChallenges playerOngoingChallenges;

    LuckyEvents(){
        readEachTypeWeight();

        for(int i = 0;i < 4;i++){
            luckyEventsRegistry.add(new ArrayList<>());
        }

        playerOngoingChallenges = new PlayerOngoingChallenges();

        //Start registering lucky events
        registerLuckyEvent(new UnluckyLootLuckyEvents(),0);
        registerLuckyEvent(new NormalLootLuckyEvents(),1);
        registerLuckyEvent(new LuckyLootLuckyEvents(),2);

        registerLuckyEvent(new UnluckySpawnLuckyEvents(),0);
        registerLuckyEvent(new NormalSpawnLuckyEvents(),1);
        registerLuckyEvent(new LuckySpawnLuckyEvents(),2);

        registerLuckyEvent(new UnluckyStructureLuckyEvents(),0);
        registerLuckyEvent(new NormalStructureLuckyEvents(),1);
        registerLuckyEvent(new LuckyStructureLuckyEvents(),2);

        registerLuckyEvent(new BetterNotChallengeLuckyEvents(playerOngoingChallenges),3);

        if(Bukkit.getPluginManager().isPluginEnabled("QualityArmory")){
            registerLuckyEvent(new AmmoCustomLuckyEvents(),1);
            registerLuckyEvent(new WeaponCustomLuckyEvents(),2);
        }

        calculateLuckyEventsRegistrationStats();
    }

    private void calculateLuckyEventsRegistrationStats(){
        for(LuckyRunnable i: getLuckyEventsRegistry(0)){
            unluckyEventsTotalWeight += i.getWeight();
        }
        for(LuckyRunnable i: getLuckyEventsRegistry(1)){
            normalEventsTotalWeight += i.getWeight();
        }
        for(LuckyRunnable i: getLuckyEventsRegistry(2)){
            luckyEventsTotalWeight += i.getWeight();
        }
        for(LuckyRunnable i: getLuckyEventsRegistry(3)){
            challengeEventsTotalWeight += i.getWeight();
        }
    }

    private static void readEachTypeWeight(){
        for(int i = 0;i <= 3;i++){
            for(int j = 0;j <= 3;j++){
                eventTypeWeightPerBlockType[i][j] = LuckyGames.getInstance().getConfig().getInt("luckyblock."+LuckyBlock.luckyBlockTypesString[i]+"."+LuckyBlock.luckyEventTypesString[j]);
            }
        }
    }

    /**
     * Register a lucky event
     * @param luckyEvent The event runnable
     * @param eventType The type of the event (0:UNLUCKY, 1:NORMAL, 2:LUCKY, 3: CHALLENGE)
     */
    protected void registerLuckyEvent(LuckyRunnable luckyEvent,int eventType) throws IllegalArgumentException{
        if (eventType < 0 || eventType > 3) throw new IllegalArgumentException();

        luckyEventsRegistry.get(eventType).add(luckyEvent);
    }

    public LuckyRunnable getRandomLuckyRunnable(LuckyBlockTypes luckyBlockType){
        List<LuckyRunnable> secondaryLuckyEventRegistry = WeightedRandomPicker.randomPick(luckyEventsRegistry,eventTypeWeightPerBlockType[luckyBlockType.ordinal()]); //Random pick the lucky event type according to the block type
        return WeightedRandomPicker.randomPick(secondaryLuckyEventRegistry); //From the picked events, random pick a specific event
    }

    public List<LuckyRunnable> getLuckyEventsRegistry(int luckyEventsType) throws IllegalArgumentException{
        if(luckyEventsType < 0 || luckyEventsType > 3) throw new IllegalArgumentException();
        return luckyEventsRegistry.get(luckyEventsType);
    }

    public int getUnluckyEventsTotalWeight() {
        return unluckyEventsTotalWeight;
    }

    public int getNormalEventsTotalWeight() {
        return normalEventsTotalWeight;
    }

    public int getLuckyEventsTotalWeight() {
        return luckyEventsTotalWeight;
    }

    public int getChallengeEventsTotalWeight() {
        return challengeEventsTotalWeight;
    }

    public int[] getAllWeight(){
        int[] res = new int[4];
        res[0] = getUnluckyEventsTotalWeight();
        res[1] = getNormalEventsTotalWeight();
        res[2] = getLuckyEventsTotalWeight();
        res[3] = getChallengeEventsTotalWeight();
        return res;
    }

    public PlayerOngoingChallenges getPlayerOngoingChallenges() {
        return playerOngoingChallenges;
    }
}
