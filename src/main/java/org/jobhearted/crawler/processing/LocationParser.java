package org.jobhearted.crawler.processing;

import org.javalite.activejdbc.Model;
import org.jobhearted.crawler.processing.objects.Locatable;
import org.jobhearted.crawler.processing.objects.Location;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Parser for the locations associated with a class which implements Locatable.
 *
 * @see org.jobhearted.crawler.processing.objects.Locatable
 */
public class LocationParser {
    private static List<Location> allLocations;
    private static Logger logger = LoggerFactory.getLogger(LocationParser.class);

    public static void parseLocation(String loc, Locatable object) {
        initializeList();

        Location location = new Location();
        location.setName(loc);

        int index = allLocations.indexOf(location);
        if (index == -1) {
            // This location does not exist yet
            try {
                location.getCoords();
                location.saveIt();

                object.addLocation(location);
                allLocations.add(location);
            } catch (IOException e) {
                logger.info("Could not get location!", e);
            } catch (JSONException e) {
                logger.info("Failed to parse the JSON, is it valid?", e);
            }
        } else {
            location = allLocations.get(index);
            object.addLocation(location);
        }
    }

    private static void initializeList() {
        if (allLocations == null) {
            allLocations = new LinkedList<Location>();
            for (Model l : Location.findAll().load()) {
                allLocations.add((Location) l);
            }
            logger.info("Loaded {} Location entries!", allLocations.size());
        }
    }
}
