package org.crawlhearted.management;

import org.crawlhearted.database.Database;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;

/**
 *
 */
public class ProcessManager {
    private static Logger logger = LoggerFactory.getLogger(ProcessManager.class);

    private ProcessManager() throws SQLException, IOException {
        logger.info("Initializing the ProcessManager module");
        Database.loadSettings();
        Database.openDatabaseConnection();
    }

    public static ProcessManager createProcessManager() throws Exception {
        return new ProcessManager();
    }
}
