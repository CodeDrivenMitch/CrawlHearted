package org.jobhearted.crawler;

import org.jobhearted.crawler.database.Database;
import org.jobhearted.crawler.processing.objects.*;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA for JobHearted.
 * User: Morlack
 * Date: 1/12/14
 * Time: 10:36 AM
 */

public class TestModelRelationships {
    @Before
    public void initializeDatabaseConnection() {
        Database.loadSettings();
        Database.openDatabaseConnection();
    }

    @Test
    public void testProfileRelationships() {
        assertTrue(Profile.belongsTo(Education.class));
        assertTrue(Profile.belongsTo(Skill.class));
        assertTrue(Profile.belongsTo(Location.class));
    }

    @Test
    public void testVacancyRelationships() {
        assertTrue(Vacature.belongsTo(Education.class));
        assertTrue(Vacature.belongsTo(Skill.class));
        assertTrue(Vacature.belongsTo(Location.class));
    }
}
