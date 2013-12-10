package org.crawlhearted.objects;

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
        if(flag != null) {
            this.set("flag", flag.toString());
        } else {
            throw new NullPointerException("Flag can not be null!");
        }
    }

    public void failedConnection() {
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
    public int hashCode() {
        return this.getString("url").hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj.getClass().equals(Url.class)) {
            Url other = (Url) obj;
            return other.get("url").equals(this.getString("url"));
        } else {
            return false;
        }
    }
}
