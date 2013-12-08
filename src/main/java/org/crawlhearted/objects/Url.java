package org.crawlhearted.objects;

import org.crawlhearted.management.Statistics;
import org.javalite.activejdbc.Model;

/**
 * Created with IntelliJ IDEA for JobHearted.
 * User: Morlack
 * Date: 12/8/13
 * Time: 7:53 PM
 */
public class Url extends Model {

    public Flag getFlag() {
        String flag = this.getString("flag");
        if(flag != null) return Flag.valueOf(flag);
        return null;
    }

    public void setFlag(Flag flag) {
        Statistics.getInstance().urlFlagChanged(getInteger("crawler_id"), getFlag(), flag);
        this.setString("flag", flag.toString());
    }
}
