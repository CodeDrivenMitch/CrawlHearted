package org.jobhearted.crawler.processing;

import org.jobhearted.crawler.objects.Vacature;
import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Many2Many;

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
    public static final String COL_LEVEL = "level";


    // Model validators
    static {
        validateNumericalityOf(COL_LEVEL);
        validatePresenceOf(COL_EDUCATION, COL_LEVEL);
    }

    @Override
    public boolean equals(Object obj) {
        if(!obj.getClass().equals(Education.class)) {
            return false;
        }

        Education education = (Education) obj;

        return education.getInteger(Education.COL_LEVEL).equals(this.getInteger(Education.COL_LEVEL));

    }
}
