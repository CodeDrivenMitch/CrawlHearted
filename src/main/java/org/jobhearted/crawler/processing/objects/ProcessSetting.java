package org.jobhearted.crawler.processing.objects;

import org.javalite.activejdbc.Model;

/**
 * ActivJDBC model to represent the process settings of the crawlers
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
