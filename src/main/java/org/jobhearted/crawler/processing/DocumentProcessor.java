package org.jobhearted.crawler.processing;

import org.javalite.activejdbc.Model;
import org.jobhearted.crawler.management.CrawlManager;
import org.jobhearted.crawler.objects.Blacklist;
import org.jobhearted.crawler.objects.Flag;
import org.jobhearted.crawler.objects.Url;
import org.jobhearted.crawler.objects.Vacature;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA for JobHearted.
 * User: Morlack
 * Date: 12/9/13
 * Time: 12:28 PM
 */


public class DocumentProcessor {

    private static Logger logger = LoggerFactory.getLogger(DocumentProcessor.class);
    // Static lists
    private static List<Skill> allSkills;
    private static List<Education> allEducations;
    private static List<Location> allLocations;
    Map<ProcessData, String> settingsMap;
    private CrawlManager crawlManager;
    private Blacklist blacklist;
    private Document documentToProcess;
    private Url urlOfDocument;

    // Illegal character array for use in regex
    private static final String[] illegalCharacters = {
            "(", ")", ";", ".", ",", ":", "{", "}", "[", "]", "*",
            "&", "^", "%", "$", "@", "!", "?", "\"", "\\", "/", "\'"
    };

    private DocumentProcessor(CrawlManager crawlManager) {
        this.crawlManager = crawlManager;
        this.blacklist = crawlManager.getBlacklist();

        settingsMap = new HashMap<ProcessData, String>();

        List<ProcessSetting> databaseList = ProcessSetting.find(ProcessSetting.COL_CRAWLER_ID + " = ?", crawlManager.getInteger("id"));
        for (ProcessSetting processSetting : databaseList) {
            ProcessData key = ProcessData.valueOf(processSetting.getString(ProcessSetting.COL_SETTING_KEY));
            String value = processSetting.getString(ProcessSetting.COL_SETTING_VALUE);

            settingsMap.put(key, value);
        }

        logger.info("Loaded {} Setting entries!", settingsMap.size());

        if (allSkills == null) {
            allSkills = Skill.findAll();
            logger.info("Loaded {} Skill entries!", allSkills.size());
        }

        if (allEducations == null) {
            allEducations = Education.findAll();
            logger.info("Loaded {} Education entries!", allEducations.size());
        }

        if(allLocations == null) {
            allLocations = new LinkedList<Location>();
            for(Model l : Location.findAll().load())
            {
                allLocations.add((Location) l);
            }
            logger.info("Loaded {} Location entries!", allLocations.size());
        }

    }

    public static DocumentProcessor createProcessor(CrawlManager crawlManager) {
        return new DocumentProcessor(crawlManager);
    }

    public void processDocument(Url url, Document document) {
        this.documentToProcess = document;
        this.urlOfDocument = url;
        processLinks();
        processContent();
    }

    private void processLinks() {

        Elements elements = documentToProcess.getElementsByTag("a");

        for (Element e : elements) {
            String u = e.attr("abs:href");
            if (blacklist.urlAllowed(u)) {
                Url url = new Url();
                url.setString(Url.COL_URL, u);
                if (!crawlManager.getUrlList().contains(url)) {
                    url.setParentCrawlmanager(crawlManager);
                    url.setFlag(Flag.FOUND);
                    crawlManager.addUrlToList(url);
                }
            }
        }
    }

    private void processContent() {
        if (docHasRequirements(ProcessData.REQUIREMENTFORVACATURE)) {
            Vacature vacature = processVacature();
            if (vacature.saveSafely()) {
                processLocation(vacature);
                processSkills(vacature);
                processEducation(vacature);
            }
        } else {
            removeAnyVacaturesFromUrl();
        }
    }

    private boolean docHasRequirements(ProcessData data) {
        String requirements = settingsMap.get(data);
        if (requirements != null && !requirements.isEmpty()) {
            String req[] = requirements.split(";");

            for (String r : req) {
                if (documentToProcess.select(r).text().isEmpty()) {
                    return false;
                }
            }
        } else {
            logger.warn("Could not determine {} for url {}", data.toString(), this.urlOfDocument.getString(Url.COL_URL));
            return false;
        }

        return true;
    }

    private void removeAnyVacaturesFromUrl() {
        List<Vacature> list = Vacature.where(Vacature.COL_URL_ID + " = ?", urlOfDocument.getInteger(Url.COL_ID));

        for (Vacature v : list) {
            v.setActive(false);
            v.removeAllSkills();
            v.save();
        }
    }

    private Vacature processVacature() {
        Vacature vacature = new Vacature();

        for (Map.Entry<ProcessData, String> entry : this.settingsMap.entrySet()) {
            if (entry.getKey() != ProcessData.REQUIREMENTFORVACATURE) {
                if (!entry.getValue().equals("NULL")) {
                    vacature.putProperty(entry.getKey(), this.documentToProcess.select(entry.getValue()).text());
                } else {
                    vacature.putProperty(entry.getKey(), "");
                }
            }
        }

        vacature.setUrlId(urlOfDocument.getID());
        vacature.generateHash();

        return vacature;
    }

    private void processSkills(Vacature vacature) {

        String omschrijving = vacature.getOmschrijving().toLowerCase();
        for (String i : illegalCharacters) {
            omschrijving = omschrijving.replace(i, " ");
        }

        List<Skill> foundSkills = new LinkedList<Skill>();
        String regex;
        for (Skill skill : allSkills) {
            regex = ".*\\s" + skill.getSkill().toLowerCase() + "\\s.*";
            if (omschrijving.matches(regex) && !foundSkills.contains(skill)) {
                vacature.addSkill(skill);
                foundSkills.add(skill);
            }
        }
    }

    /**
     * Processes the educations in the omschrijving field of the vacature. When it finds one, it adds it to the
     * Many2Many relationship.
     * @param vacature Vacature to process
     */
    private void processEducation(Vacature vacature) {
        // Get the omschrijving and remove illegal characters from it
        String omschrijving = vacature.getOmschrijving().toLowerCase();
        for (String i : illegalCharacters) {
            omschrijving = omschrijving.replace(i, " ");
        }

        List<Education> foundEducations = new LinkedList<Education>();
        // Loop over all known skills, execute the regex it needs to fulfill. If it matches, it is added to the model
        for (Education education : allEducations) {
            String regex = ".*\\s" + education.getString("education").toLowerCase() + "\\s.*";

            if (omschrijving.matches(regex) && !foundEducations.contains(education)) {
                vacature.addEducation(education);
                foundEducations.add(education);
            }
        }
    }

    private void processLocation(Vacature vacature) {
        if(vacature.getPlaats() != null && !vacature.getPlaats().isEmpty()) {
            Location location = new Location();
            location.setName(vacature.getPlaats());

            int index = allLocations.indexOf(location);

            if(index == -1) {
                try {
                    location.getCoords();
                    location.saveIt();
                    location.add(vacature);
                    allLocations.add(location);
                } catch (Exception e) {
                    logger.info("Could not get location!", e);
                }
            } else {
                location = allLocations.get(index);
                location.add(vacature);
            }
        }
    }
}
