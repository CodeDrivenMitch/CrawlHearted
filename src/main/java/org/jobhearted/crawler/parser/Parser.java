package org.jobhearted.crawler.parser;

import org.javalite.activejdbc.Model;
import org.jobhearted.crawler.database.Database;
import org.jobhearted.crawler.objects.Profile;
import org.jobhearted.crawler.processing.Education;
import org.jobhearted.crawler.processing.Skill;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.LinkedList;
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
    private List<Education> educationList;
    private List<Skill> skillList;

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
        Database.openDatabaseConnection();
        educationList = new LinkedList<Education>();
        for (Model education : Education.findAll()) {
            educationList.add((Education) education);
        }

        skillList = new LinkedList<Skill>();
        for (Model skill : Skill.findAll()) {
            skillList.add((Skill) skill);
        }

    }

    @Override
    public void run() {
        Database.openDatabaseConnection();
        try {
            int progress = 0;
            BufferedReader br = new BufferedReader(new FileReader(filetoParse));
            String line;
            while ((line = br.readLine()) != null) {
                parseJSON(line);
                progress++;
                window.setNewProgressValue(progress);
                logger.info("Done processing Line " + progress);
            }

            br.close();
        } catch (IOException e) {
            logger.warn("", e);
        }


    }

    private void parseJSON(String json) {
        JSONObject object = new JSONObject(json);
        Profile person = null;
        if (parseProfile) {
            person = createProfileFromJSO(object);
        }
        if (parseSkills) {
            processSkills(person, object);
        }
        if (parseEducation) {
            processEducations(person, object);
        }

    }

    private void processSkills(Profile profile, JSONObject json) {
        try {
            JSONArray skills = json.getJSONArray("skills");
            String currentSkill;
            for (int i = 0; i < skills.length(); i++) {
                currentSkill = skills.getString(i);
                if (!currentSkill.isEmpty()) {

                    Skill skill = new Skill();
                    skill.setString("skill", skills.getString(i));
                    if (!skillList.contains(skill)) {
                        logger.info("new Skill " + skills.getString(i));

                        skill.saveIt();
                        skillList.add(skill);
                    }
                    if (parseProfile) {
                        profile.addSkill(skill);
                    }

                }
            }
        } catch (JSONException e) {
            logger.trace("Could not read Skills from JSON", e);
        }
    }

    private void processEducations(Profile profile, JSONObject json) {
        try {
            String jsonString = json.toString();


            for (Education education : educationList) {
                if (jsonString.contains(education.getString(Education.COL_EDUCATION))) {
                    // add education to person
                    logger.info("found education " + education.getString(Education.COL_EDUCATION));
                    if (parseProfile) {
                        profile.addEducation(education);
                    }
                }
            }

        } catch (JSONException e) {
            logger.trace("Could not read Education of JSON" , e);
        }
    }

    private Profile createProfileFromJSO(JSONObject json) {
        Profile profile = new Profile();

        List<Profile> list = Profile.find("url = ?", json.getString("url"));
        if (list.isEmpty()) {
            JSONObject nameArray = json.getJSONObject("name");
            profile.setName(nameArray.getString("firstname") + " " + nameArray.getString("lastname"));
            profile.setLocation(json.getString("location"));
            profile.setUrl(json.getString("url"));

            profile.saveIt();
            return profile;
        } else {
            return list.get(0);
        }
    }

    /**
     * Gets the number of lines in a file. Taken from http://stackoverflow.com/questions/453018/number-of-lines-in-a-file-in-java
     * Used for the progress bar.
     *
     * @param filename Filename to count lines of
     * @return Count of lines
     * @throws IOException In case it cannot find the file specified
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
