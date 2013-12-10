package org.crawlhearted.objects;

import org.crawlhearted.database.UniquenessValidator;
import org.javalite.activejdbc.Model;

import java.util.Date;

/**
 * Created with IntelliJ IDEA for JobHearted.
 * User: Morlack
 * Date: 12/8/13
 * Time: 7:53 PM
 */
public class Url extends Model {
    // Database field names
    public static final String COL_ID = "id";
    public static final String COL_URL = "url";
    public static final String COL_FLAG = "flag";
    public static final String COL_RETRIES = "number_of_retries";
    public static final String COL_FIRST_SEEN = "first_visited";
    public static final String COL_LAST_SEEN = "last_visited";

    // Model validors
    static {
        validatePresenceOf(COL_ID, COL_URL, COL_FLAG);
        validateNumericalityOf(COL_RETRIES);
        validateWith(new UniquenessValidator(COL_URL));
    }

    /**
     * Getter for the Flag field
     * @return Flag of the url
     * @see Flag
     */
    public Flag getFlag() {
        return Flag.valueOf(this.getString(COL_FLAG));
    }

    /**
     *  Sets the flag of the url, also updates the first and last visited fields if the flag indicates the visit has
     *  been successful
     * @param flag The new flag of the url
     */
    public void setFlag(Flag flag) {
        if(flag != null) {
            this.set(COL_FLAG, flag.toString());

            if(flag == Flag.VISITED) {
                Date now = new Date();
                this.setDate(COL_LAST_SEEN, now);
                if(this.getDate(COL_FIRST_SEEN) == null) {
                    this.setDate(COL_FIRST_SEEN, now);
                }
            }
        } else {
            throw new NullPointerException("Flag can not be null!");
        }
    }

    public void failedConnection() {
        if(this.getFlag() == Flag.RETRY) {
            int currentNumber = this.getInteger(COL_RETRIES);
            if(currentNumber + 1 >= 4) {
                this.setFlag(Flag.DEAD);
            } else {
                this.setInteger(COL_RETRIES, currentNumber + 1);
            }
        } else {
            this.setFlag(Flag.RETRY);
            this.setInteger(COL_RETRIES, 1);
        }
    }

    @Override
    public int hashCode() {
        return this.getString(COL_URL).hashCode();
    }

    @Override
    public String toString() {
        return this.getString(COL_URL);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj.getClass().equals(Url.class)) {
            Url other = (Url) obj;
            return other.get(COL_URL).equals(this.getString(COL_URL));
        } else {
            return false;
        }
    }
}
