package org.jobhearted.crawler.processing;

import org.javalite.activejdbc.Model;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Location model in the database. Any locaion can have multiple vacatures and profiles linked to it.
 * This is later used in the matcher of the system.
 */
public class Location extends Model {
    private static Logger logger = LoggerFactory.getLogger(Location.class);

    // Database fields
    private static final String COL_NAME = "name";
    private static final String COL_LONGITUDE = "longitude";
    private static final String COL_LATITUDE = "latitude";
    // Url for Geo API with $ as wildcard, can use String.replace("$", value) to get the right url
    private static final String URL_API = "http://maps.googleapis.com/maps/api/geocode/json?address=$&sensor=false";

    // Model validators
    static {
        validateNumericalityOf(COL_LATITUDE, COL_LONGITUDE);
        validatePresenceOf(COL_NAME);
    }

    /**
     * Getter for the Location name
     * @return Name of the location
     */
    public String getName() {
        return this.getString(COL_NAME);
    }

    /**
     * Sets the given parameter to the name field after trimming it (no preceding and trailing whitespaces).
     * @param name Name of location to set to
     */
    public void setName(String name) {
        this.setString(COL_NAME, name.trim());
    }

    /**
     * Setter for the longitude
     * @param longitude longitude to set to
     */
    public void setLongitude(Double longitude) {
        this.setDouble(COL_LONGITUDE, longitude);
    }

    /**
     * Sets the latitude of the location, should be a double
     * @param latitude Latitude to set to
     */
    public void setLatitude(Double latitude) {
        this.setDouble(COL_LATITUDE, latitude);
    }

    /**
     * This method contacts the google Geolocation api with the location name and gets the coordinates of that
     * place as a result. Those are set to the location.
     * @throws IOException If there is no connection to the internet
     */
    public void getCoords() throws IOException, JSONException {
        logger.info("getting location");

        String result = Jsoup.connect(URL_API.replace("$", getName().replace(" ", "+") ))
                .ignoreContentType(true).execute().body();

        // Get the right JSON information, takes a few steps to get there
        JSONObject json = new JSONObject(result)
                .getJSONArray("results")
                .getJSONObject(0)
                .getJSONObject("geometry")
                .getJSONObject("location");


        this.setLatitude(json.getDouble("lat"));
        this.setLongitude(json.getDouble("lng"));
    }

    /**
     * Returns wether the object is equal or not. Is overridden to represent the right information, which is the
     * location name field currently set.
     * @param obj Object to compare to
     * @return Equality
     */
    @Override
    public boolean equals(Object obj) {
        if(!obj.getClass().equals(this.getClass()) ) {
            return false;
        } else {
            return ((Location) obj).getName().equalsIgnoreCase(this.getName());
        }

    }

    /**
     * Overriding the hashcode to represent the right information
     * @return hashcode
     */
    @Override
    public int hashCode() {
        return getName().hashCode();
    }
}
