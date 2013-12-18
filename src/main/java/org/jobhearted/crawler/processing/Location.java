package org.jobhearted.crawler.processing;

import org.javalite.activejdbc.Model;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA for JobHearted.
 * User: Morlack
 * Date: 12/18/13
 * Time: 8:46 PM
 */
public class Location extends Model {
    private static Logger logger = LoggerFactory.getLogger(Location.class);

    // Database fields
    private static final String COL_NAME = "name";
    private static final String COL_LONGTITUDE = "longtitude";
    private static final String COL_LATITUDE = "latitude";

    // Model validators
    static {
        validateNumericalityOf(COL_LATITUDE, COL_LONGTITUDE);
        validatePresenceOf(COL_NAME);
    }

    public String getName() {
        return this.getString(COL_NAME);
    }

    public void setName(String name) {
        this.setString(COL_NAME, name.trim());
    }

    public void setLongtitude(Double longtitude) {
        this.setDouble(COL_LONGTITUDE, longtitude);
    }

    public void setLatitude(Double latitude) {
        this.setDouble(COL_LATITUDE, latitude);
    }

    public void getCoords() throws IOException {
        logger.info("getting location");
        String location = this.getName().replace(" ", "+");
        String url = "http://maps.googleapis.com/maps/api/geocode/json?address=" + location + "&sensor=false";

        String result = Jsoup.connect(url).ignoreContentType(true).execute().body();
        JSONObject json = new JSONObject(result)
                .getJSONArray("results")
                .getJSONObject(0)
                .getJSONObject("geometry")
                .getJSONObject("location");


        this.setLatitude(json.getDouble("lat"));
        this.setLongtitude(json.getDouble("lng"));
    }

    @Override
    public boolean equals(Object obj) {
        if(!obj.getClass().equals(this.getClass())) {
            return false;
        } else {
            return ((Location) obj).getName().equals(this.getName());
        }

    }
}
