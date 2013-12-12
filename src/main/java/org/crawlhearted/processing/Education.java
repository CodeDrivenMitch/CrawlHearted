package org.crawlhearted.processing;

import org.crawlhearted.objects.Vacature;
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
}
