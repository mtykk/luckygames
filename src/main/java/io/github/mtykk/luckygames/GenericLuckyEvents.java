package io.github.mtykk.luckygames;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class GenericLuckyEvents implements LuckyRunnable{
    protected List<ManagedLuckyRunnableSimple> possibleEventsList= new ArrayList<>();

    protected abstract void classDefaultOnStart(Player player,Location blockLocation);
    protected abstract void classDefaultOnFinish(Player player,Location blockLocation);

    public int getWeight(){
        return possibleEventsList.size();
    }

    public void run(Player player, Location blockLocation){
        ManagedLuckyRunnableSimple e = RandomPicker.randomPick(possibleEventsList);
        int times = e.maxTimes == e.minTimes ? e.maxTimes : PluginRandom.randomGenerator.nextInt(e.maxTimes-e.minTimes+1)+e.minTimes;
        if(e.onStart != null) e.onStart.run(player,blockLocation);
        else classDefaultOnStart(player,blockLocation);
        for(int i = 0;i < times;i++){
            e.luckyRunnableSimple.run(player,blockLocation);
        }
        if(e.onFinish != null) e.onFinish.run(player,blockLocation);
        else classDefaultOnFinish(player,blockLocation);
    }
    protected void registerGenericEvent(LuckyRunnableSimple genericEvent,
                                        @Nullable LuckyRunnableSimple onStart,
                                        @Nullable LuckyRunnableSimple onFinish,
                                        int minTimes,
                                        int maxTimes) throws IllegalArgumentException{
        possibleEventsList.add(new ManagedLuckyRunnableSimple(genericEvent,onStart,onFinish,minTimes,maxTimes));
    }
    protected void registerGenericEvent(LuckyRunnableSimple genericEvent,int minTimes,int maxTimes){
        this.registerGenericEvent(genericEvent,null,null,minTimes,maxTimes);
    }
    protected void registerGenericEvent(LuckyRunnableSimple genericEvent){
        this.registerGenericEvent(genericEvent,1,1);
    }
}
