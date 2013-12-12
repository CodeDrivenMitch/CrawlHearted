package org.crawlhearted.processing;

import org.javalite.activejdbc.Model;

/**
 * Created with IntelliJ IDEA for JobHearted.
 * User: Morlack
 * Date: 12/11/13
 * Time: 10:49 PM
 */
public class Skill extends Model {
    // Database fields
    public static final String COL_ID = "id";
    public static final String COL_SKILL = "skill";

    // Model validations
    static {
        validatePresenceOf(COL_SKILL);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj.getClass().equals(this.getClass())) {
            if(this.getString(COL_SKILL).equals(((Skill) obj).getString(COL_SKILL))) return true;
        }
        return false;

    }
}
