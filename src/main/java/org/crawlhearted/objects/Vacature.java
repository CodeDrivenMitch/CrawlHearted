package org.crawlhearted.objects;

import org.crawlhearted.processing.ProcessData;
import org.javalite.activejdbc.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA for JobHearted.
 * User: Morlack
 * Date: 12/10/13
 * Time: 1:29 PM
 */
public class Vacature extends Model {
    private static final Map<ProcessData, String> databaseMap = createDatabaseMap();
    private static Logger logger = LoggerFactory.getLogger(Vacature.class);


    private static Map<ProcessData, String> createDatabaseMap() {
        Map<ProcessData, String> map = new HashMap<ProcessData, String>();

        map.put(ProcessData.VAC_BEDRIJF, "bedrijf");
        map.put(ProcessData.VAC_DIENSTVERBAND, "dienstverband");
        map.put(ProcessData.VAC_OMSCHRIJVING, "omschrijving");
        map.put(ProcessData.VAC_TITLE, "title");
        map.put(ProcessData.VAC_PLAATS, "plaats");

        return map;
    }

    public void putProperty(ProcessData data, String value) {
        this.setString(databaseMap.get(data), value);
    }

    public void generateHash() {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(this.getString("omschrijving").getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte anArray : array) {
                sb.append(Integer.toHexString((anArray & 0xFF) | 0x100).substring(1, 3));
            }
            this.setString("hash", sb.toString());
        } catch (java.security.NoSuchAlgorithmException e) {
            logger.warn("No algorithm!", e);
        }
    }

    public void saveSafely() {

        List<Vacature> result = Vacature.find("url_id = ?", this.getString("url_id")).load();
        if (!result.isEmpty()) {
            // already one!
            Vacature vacature = result.get(1);
            if (!vacature.getString("hash").equals(this.getString("hash"))) {
                // we got a new version! Set the old one inactive
                vacature.setInteger("active", 0);
                vacature.save();

                this.setInteger("version", vacature.getInteger("version") + 1);
                this.save();
            }
        } else {
            List<Vacature> list = Vacature.find("hash = ?", this.getString("hash")).load();
            if (list.isEmpty()) {
                this.setInteger("version", 1);
                this.setInteger("active", 1);
                this.saveIt();
            }
        }
    }



}
