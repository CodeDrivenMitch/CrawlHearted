package org.jobhearted.crawler.processing;

import org.javalite.activejdbc.Model;

/**
 * Class for the Skill model. Contains the fields id and skill. Used for identification of skill in vacancies and
 * profiles.
 */
public class Skill extends Model {
    // Database fields
    public static final String COL_SKILL = "skill";

    // Model validations
    static {
        validatePresenceOf(COL_SKILL);
    }

    /**
     * Getter for the skill field in the database
     *
     * @return Value of the skill field
     */
    public String getSkill() {
        return this.getString(COL_SKILL);
    }

    /**
     * Setter for the skill field in the database
     *
     * @param skill New value of the skill
     */
    public void setSkill(String skill) {
        this.setString(COL_SKILL, skill);
    }

    /**
     * Overrides equals to make sure skills are compared based on their skill fields, ignoring the case.
     *
     * @param obj Object to compare to. Should be a Skill
     * @return Equality of the objects. Always false if parameter is not Skill.class
     */

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass().equals(this.getClass())) {
            if (this.getSkill().equalsIgnoreCase(((Skill) obj).getSkill())) return true;
        }
        return false;
    }
}
