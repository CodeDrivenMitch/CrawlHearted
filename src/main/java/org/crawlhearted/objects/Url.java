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

    public void isTrulyDead() {
        if(this.getFlag() == Flag.RETRY) {
            int currentNumber = this.getInteger("number_of_retries");
            //TODO: add settings
            if(currentNumber + 1 >= 4) {
                this.setFlag(Flag.DEAD);
            } else {
                this.setInteger("number_of_retries", currentNumber + 1);
            }
        } else {
            this.setFlag(Flag.RETRY);
            this.setInteger("number_of_retries", 1);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if(obj.getClass().equals(Url.class)) {
            Url other = (Url) obj;
            return other.getString("url").equals(this.getString("url"));
        } else {
            return false;
        }
    }
}
