package org.jobhearted.crawler.objects;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Many2Many;
import org.jobhearted.crawler.processing.Education;
import org.jobhearted.crawler.processing.Skill;

/**
 * Created with IntelliJ IDEA for JobHearted.
 * User: Morlack
 * Date: 12/15/13
 * Time: 2:46 PM
 */

@Many2Many(other = Skill.class, join = "profiles_skills", sourceFKName = "profile_id", targetFKName = "skill_id")
public class Profile extends Model {

    private static final String COL_URL = "url";
    private static final String COL_NAME = "name";
    private static final String COL_LOCATION = "location";

    public void setName(String name) {
        this.setString(COL_NAME, name);
    }

    public void setLocation(String loc) {
        this.setString(COL_LOCATION, loc);
    }

    public void setUrl(String url) {
        this.setString(COL_URL, url);
    }

    public void addEducation(Education education) {
        if(!this.getAll(Education.class).contains(education)) {
            this.add(education);
        }
    }

    public void addSkill(Skill skill) {
        if(!this.getAll(Skill.class).contains(skill)) {
            this.add(skill);
        }
    }


}
