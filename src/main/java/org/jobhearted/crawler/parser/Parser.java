package org.jobhearted.crawler.parser;

import org.jobhearted.crawler.database.Database;
import org.jobhearted.crawler.processing.Skill;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;

/**
 * Created with IntelliJ IDEA for JobHearted.
 * User: Morlack
 * Date: 12/15/13
 * Time: 1:14 PM
 */
public class Parser implements Runnable {
    private static Logger logger = LoggerFactory.getLogger(Parser.class);
    ProgressWindow window;
    private String filetoParse;
    private boolean parseProfile;
    private boolean parseEducation;
    private boolean parseSkills;

    public Parser(String fileToParse, boolean parseProfile, boolean parseSkills, boolean parseEducation) {
        this.filetoParse = fileToParse;
        this.parseEducation = parseEducation;
        this.parseSkills = parseSkills;
        this.parseProfile = parseProfile;
        try {
            window = ProgressWindow.createProgressWindow(count(filetoParse));
        } catch (IOException e) {
            logger.warn("", e);
        }
    }

    @Override
    public void run() {
        try {
            Database.loadSettings();
        } catch (IOException e) {

        }
        Database.openDatabaseConnection();
        try {
            int progress = 0;
            BufferedReader br = new BufferedReader(new FileReader(filetoParse));
            String line;
            while ((line = br.readLine()) != null) {
                parseJSON(line);
                progress++;
                window.setNewProgressValue(progress);
            }

            br.close();
        } catch (IOException e) {
            logger.warn("", e);
        }


    }

    private void parseJSON(String json) {
        JSONObject object = new JSONObject(json);
        try {
            JSONArray skills = object.getJSONArray("skills");


            for (int i = 0; i < skills.length(); i++) {
                List<Skill> list = Skill.find("skill = ?", skills.getString(i));
                if (list.isEmpty()) {
                    logger.info("new Skill " + skills.getString(i));
                    Skill skill = new Skill();
                    skill.setString("skill", skills.getString(i));
                    skill.saveIt();
                }
            }


        } catch (JSONException e) {
            logger.info("", e);
        }

    }

    /**
     * Gets the number of lines in a file. Taken from http://stackoverflow.com/questions/453018/number-of-lines-in-a-file-in-java
     *
     * @param filename
     * @return
     * @throws IOException
     */

    public int count(String filename) throws IOException {
        InputStream is = new BufferedInputStream(new FileInputStream(filename));
        try {
            byte[] c = new byte[1024];
            int count = 0;
            int readChars = 0;
            boolean empty = true;
            while ((readChars = is.read(c)) != -1) {
                empty = false;
                for (int i = 0; i < readChars; ++i) {
                    if (c[i] == '\n') {
                        ++count;
                    }
                }
            }
            return (count == 0 && !empty) ? 1 : count;
        } finally {
            is.close();
        }
    }


}
