package io.github.mtykk.luckygames;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class IndexedGameLocation {
    private final List<List<Location>> locationList = new ArrayList<>();
    public Location getLocation(int lane,int index) throws IllegalArgumentException{
        try{
            return locationList.get(lane).get(index);
        }catch (IndexOutOfBoundsException e){
            throw new IllegalArgumentException("No such lane or index");
        }
    }
    public void addLocation(int lane,Location location){
        locationList.get(lane).add(location);
    }
    public void clear(){
        locationList.clear();
    }
    public void setLanes(int lanes){
        clear();
        for(int i = 0;i < lanes;i++){
            locationList.add(new ArrayList<>());
        }
    }
}
