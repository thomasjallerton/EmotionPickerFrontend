package ichack18.emotionpicker;

import java.io.Serializable;

/**
 * Created by Thomas on 27/01/2018.
 */

public class Place implements Serializable {
    private String title;
    private String address;
    private String placeID;
    private float rating;
    private boolean opennow;
    private float lat;
    private float longi;

    public float getLat() {
        return lat;
    }

    public void setLat(float lat) {
        this.lat = lat;
    }

    public void setLat(double lat) {
        this.lat = (float) lat;
    }

    public void setLongi(double longi) {
        this.longi = (float) longi;
    }

    public float getLongi() {
        return longi;
    }

    public void setLongi(float longi) {
        this.longi = longi;
    }

    public Place() {

    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPlaceID() {
        return placeID;
    }

    public void setPlaceID(String placeID) {
        this.placeID = placeID;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public boolean isOpennow() {
        return opennow;
    }

    public void setOpennow(boolean opennow) {
        this.opennow = opennow;
    }
}
