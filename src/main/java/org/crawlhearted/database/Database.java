package org.crawlhearted.database;

import org.javalite.activejdbc.Base;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.*;

/**
 * The Database class is used for opening a database connection and maintaining it.
 * Upon making a connection it will first verify the integrity of the database.
 * When the database structure is correct, according to databasestructure.cfg, it will initalize
 * the ActiveRecord connection.
 * If not, an exception will be thrown and the application will exit.
 */
public class Database {
    private static final Logger logger = LoggerFactory.getLogger(Database.class);
    public static HashMap<String, List<String>> databaseRequirements;
    public static Connection CONNECTION;
    public static String USER;
    public static String PASS;
    public static int PORT;
    public static String HOST;
    public static String NAME;

    /**
     * Opens a JDBC database connection as well as active JDBC connection if the database structure is correct
     *
     * @throws SQLException Thrown when connection failed or database structure is incorrect
     */
    public static void openDatabaseConnection() throws SQLException {

        // prepare for making the connection
        Properties connectionProps = new Properties();
        connectionProps.put("user", USER);
        connectionProps.put("password", PASS);


        //make the connection
        CONNECTION = DriverManager.getConnection("jdbc:mysql://" + HOST + ":" + PORT + "/" + NAME, connectionProps);
        logger.info("Database connection established");


        logger.info("Now checking database integrity..");
        checkDatabaseTables();
        Base.open("com.mysql.jdbc.Driver", "jdbc:mysql://" + HOST + ":" + PORT + "/" + NAME, USER, PASS);
        logger.info("Database integrity verified and ActiveJDBC connection initialized");
    }

    /**
     * Checks the integrity of the database in place. Uses the databasestructure.cfg file to compare
     *
     * @throws SQLException When database structure doesn't match the configuration file
     */
    private static void checkDatabaseTables() throws SQLException {

        getRequiredTableData();
        DatabaseMetaData metadata = CONNECTION.getMetaData();
        ResultSet data = metadata.getTables(null, null, "%", null);

        //checking tables
        List<String> tablesInDatabase = new ArrayList<String>();
        while (data.next()) {
            tablesInDatabase.add(data.getString(3));
        }
        for (Map.Entry<String, List<String>> entry : databaseRequirements.entrySet()) {
            if (!tablesInDatabase.contains(entry.getKey())) {
                throw new SQLException("The database doesn't contain the " + entry.getKey() + "table!");
            }
            ResultSet columnData = metadata.getColumns("", "", entry.getKey(), "");
            List<String> columnNames = new ArrayList<String>();

            while (columnData.next()) {
                columnNames.add(columnData.getString(4));
            }

            for (String column : entry.getValue()) {
                if (!columnNames.contains(column)) {
                    throw new SQLException("The " + entry.getKey() + " table doesn't contains the " + column + " column!");
                }
            }
        }
    }

    private static void getRequiredTableData() {
        databaseRequirements = new HashMap<String, List<String>>();

        Properties configFile = new java.util.Properties();
        try {
            configFile.load(new FileInputStream("databasestructure.cfg"));

            String tables = configFile.getProperty("tables");
            String[] table = tables.split(";");

            for (String t : table) {
                String[] colums = configFile.getProperty(t + "_tables").split(";");
                databaseRequirements.put(t, Arrays.asList(colums));
            }

        } catch (IOException e) {
            logger.warn("", e);
        }
    }

    /**
     * Loads the database.cfg file
     *
     * @throws IOException When config file can't be found
     */
    public static void loadSettings() throws IOException {
        // load database settings.config
        Properties configFile = new java.util.Properties();

        configFile.load(new FileInputStream("database.cfg"));
        HOST = configFile.getProperty("DbHost");
        PORT = Integer.parseInt(configFile.getProperty("DbPort"));
        NAME = configFile.getProperty("DbName");
        USER = configFile.getProperty("DbUser");
        PASS = configFile.getProperty("DbPwd");


    }

    public static void saveSettings() {
        Properties configFile = new java.util.Properties();

        configFile.put("DbHost", HOST);
        configFile.put("DbPort", Integer.toString(PORT));
        configFile.put("DbName", NAME);
        configFile.put("DbUser", USER);
        configFile.put("DbPwd", PASS);

        try {
            configFile.store(new FileOutputStream("database.cfg"), "This is the Database settings file for the JobHearted Crawl application \r\n Last saved:");
        } catch (IOException e) {
            logger.warn("Couldn't store configuration!", e);
        }
    }
}
