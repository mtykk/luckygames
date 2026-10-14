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

    public void run(Player player, Location blockLocation) {
        ManagedLuckyRunnableSimple event = RandomPicker.randomPick(possibleEventsList);
        int times = event.maxTimes == event.minTimes
                ? event.maxTimes
                : PluginRandom.randomGenerator.nextInt(event.maxTimes - event.minTimes + 1) + event.minTimes;
        Location eventLocation = blockLocation.clone();
        if (event.onStart != null) event.onStart.run(player, eventLocation.clone());
        else classDefaultOnStart(player, eventLocation.clone());
        for (int i = 0; i < times; i++) {
            event.luckyRunnableSimple.run(player, eventLocation.clone());
        }
        if (event.onFinish != null) event.onFinish.run(player, eventLocation.clone());
        else classDefaultOnFinish(player, eventLocation.clone());
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
