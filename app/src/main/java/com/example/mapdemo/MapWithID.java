package com.example.mapdemo;
import com.google.android.gms.maps.GoogleMap;

/**
 * Created by mayajey on 7/14/17.
 */

// I have no idea if we need this
public class MapWithID {

    public GoogleMap map;
    // Typically concatenated group name for uniqueness
    public String ID;

    public MapWithID(GoogleMap map, String ID){
        this.map = map;
        this.ID = ID;
    }
    public MapWithID(){
    }

    public GoogleMap getMap() {
        return map;
    }

    public void setMap(GoogleMap map) {
        this.map = map;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }
}