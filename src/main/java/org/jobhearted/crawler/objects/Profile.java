package org.jobhearted.crawler.objects;

import org.javalite.activejdbc.Model;
import org.jobhearted.crawler.processing.Education;
import org.jobhearted.crawler.processing.Skill;

/**
 * This is the Profile Data-object, an ActiveJDBC model. Used in the JSON parser for saving LinkedIn profiles.
 */

public class Profile extends Model {
    // Database fields
    private static final String COL_URL = "url";
    private static final String COL_NAME = "name";
    private static final String COL_LOCATION = "location";

    /**
     * Sets the person's name on the profile
     * @param name Name to set
     */
    public void setName(String name) {
        this.setString(COL_NAME, name);
    }

    /**
     * Sets the location field of the profile
     * TODO: Add functionality similar to vacature location
     * @param loc Location to set
     */
    public void setLocation(String loc) {
        this.setString(COL_LOCATION, loc);
    }

    /**
     * Sets the Url of the LinkedIn profile
     * @param url Url to set
     */
    public void setUrl(String url) {
        this.setString(COL_URL, url);
    }

    /**
     * Add an education to the profile. A Many2Many ActiveJDBC model is used to represent this.
     * @param education Education to add
     */
    public void addEducation(Education education) {
        if(!this.getAll(Education.class).contains(education)) {
            this.add(education);
        }
    }

    /**
     * Add a skill to the profile. A Many2Many ActiveJDBC model is used to represent this.
     * @param skill Skill to add
     */
    public void addSkill(Skill skill) {
        if(!this.getAll(Skill.class).contains(skill)) {
            this.add(skill);
        }
    }


}
