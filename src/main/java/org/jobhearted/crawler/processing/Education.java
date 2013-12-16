package org.jobhearted.crawler.processing;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Many2Many;
import org.jobhearted.crawler.objects.Vacature;

/**
 * Created with IntelliJ IDEA for JobHearted.
 * User: Morlack
 * Date: 12/12/13
 * Time: 2:50 PM
 */

@Many2Many(other = Vacature.class, join = "vacatures_educations", sourceFKName = "education_id", targetFKName = "vacature_id")
public class Education extends Model {
    // Database fields
    public static final String COL_EDUCATION = "education";

    // Model validators
    static {
        validatePresenceOf(COL_EDUCATION);
    }

    /**
     * Gets the value of the education field.
     *
     * @return Education string
     */
    public String getEducation() {
        return this.getString(COL_EDUCATION);
    }

    /**
     * Sets the education field of the model
     *
     * @param education new value of the education field
     */
    public void setEducation(String education) {
        this.setString(COL_EDUCATION, education);
    }

    /**
     * Override equals to always compare Educations based on their education field, ignoring case.
     * Will return false if the parameter is not an Education object
     *
     * @param obj Education to compare to
     * @return equality of the objects
     */
    @Override
    public boolean equals(Object obj) {
        if (!obj.getClass().equals(Education.class)) {
            return false;
        }
        return this.getEducation().equals(((Education) obj).getEducation());
    }
}
