package org.jobhearted.crawler.processing;

import org.javalite.activejdbc.Model;

/**
 * Created with IntelliJ IDEA for JobHearted.
 * User: Morlack
 * Date: 12/10/13
 * Time: 12:41 PM
 */
public class ProcessSetting extends Model {
    // Database fields
    public static final String COL_CRAWLER_ID = "crawler_id";
    public static final String COL_SETTING_KEY = "setting_name";
    public static final String COL_SETTING_VALUE = "setting_value";

    // Model validators
    static {
        validatePresenceOf(COL_CRAWLER_ID, COL_SETTING_KEY, COL_SETTING_VALUE);
        validateNumericalityOf(COL_CRAWLER_ID);
    }
}
