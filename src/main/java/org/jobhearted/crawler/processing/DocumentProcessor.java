package org.jobhearted.crawler.processing;

import org.jobhearted.crawler.management.CrawlManager;
import org.jobhearted.crawler.processing.objects.*;
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
 * Class used by the CrawlManager to process information in documents. Makes use of the Blacklist class to process URLs
 * and makes use of the ProcessSetting class to retrieve information in a document and store it in the Vacature Model.
 *
 * @see CrawlManager
 * @see Vacature
 * @see Blacklist
 * @see Url
 * @see ProcessSetting
 */


public class DocumentProcessor {
    private static Logger logger = LoggerFactory.getLogger(DocumentProcessor.class);
    public static final String REGEX_WHITESPACE_BEFORE = ".*\\s";
    public static final String REGEX_WHITESPACE_AFTER = "\\s.*";
    // Illegal character array for use in regex
    private static final String[] ILLEGAL_CHARACTERS = {
            "(", ")", ";", ".", ",", ":", "{", "}", "[", "]", "*", "?",
            "&", "^", "%", "$", "@", "!", "?", "\"", "\\", "/", "\'"
    };
    private static final String NULL_VALUE = "NULL";
    // Static lists of data
    private static List<Skill> allSkills;
    private static List<Education> allEducations;
    // instance fields
    private Map<ProcessData, String> settingsMap;
    private CrawlManager crawlManager;
    private Blacklist blacklist;
    private Document documentToProcess;
    private Url urlOfDocument;

    /**
     * Constructor for the class
     *
     * @param crawlManager crawlManager to create the processor for
     */
    private DocumentProcessor(CrawlManager crawlManager) {
        this.crawlManager = crawlManager;
        this.blacklist = crawlManager.getBlacklist();

        initializeProcessSettings();
        initializeDataMaps();
    }

    /**
     * Factory Method for the Processor. May be replaced with a normal constructor in the feature as right now, it adds
     * no functionality.
     *
     * @param crawlManager Crawlmanager to create a processor for
     * @return The processor created.
     */
    public static DocumentProcessor createProcessor(CrawlManager crawlManager) {
        return new DocumentProcessor(crawlManager);
    }

    /**
     * Initializes the Datamaps if they are null, only needed for the first processor constructed.
     */
    private void initializeDataMaps() {
        if (allSkills == null) {
            allSkills = Skill.findAll();
            logger.info("Loaded {} Skill entries!", allSkills.size());
        }

        if (allEducations == null) {
            allEducations = Education.findAll();
            logger.info("Loaded {} Education entries!", allEducations.size());
        }


    }

    /**
     * Initializes the process Settings for the processor. Loaded for every processor individually.
     */
    private void initializeProcessSettings() {
        settingsMap = new HashMap<ProcessData, String>();

        List<ProcessSetting> databaseList = ProcessSetting.find(ProcessSetting.COL_CRAWLER_ID + " = ?", crawlManager.getInteger("id"));
        for (ProcessSetting processSetting : databaseList) {
            ProcessData key = ProcessData.valueOf(processSetting.getString(ProcessSetting.COL_SETTING_KEY));
            String value = processSetting.getString(ProcessSetting.COL_SETTING_VALUE);

            settingsMap.put(key, value);
        }

        logger.info("Loaded {} Setting entries!", settingsMap.size());
    }

    /**
     * Processes the given document.
     *
     * @param url      Url the document belongs to
     * @param document the document to be processed
     */
    public void processDocument(Url url, Document document) {
        this.documentToProcess = document;
        this.urlOfDocument = url;
        processLinks();
        processContent();
    }

    /**
     * Processes all links of the document in the documentToProcess instance variable. Uses the Blacklist to check if
     * and url is allowed. If so, it sends it to the crawlmanager to add it to the list.
     */
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

    /**
     * Calls the right functions to process the current document, and if no vacature is found removes all vacatures that
     * Url has.
     */
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

    /**
     * Checks whether a document has the needed requirements stored in the settingsMap, indicated by the ProcessData
     *
     * @param data Key of the datamap
     * @return Whether the document has the requirements or not.
     */
    private boolean docHasRequirements(ProcessData data) {
        String requirements = settingsMap.get(data);
        if (requirements != null && !requirements.isEmpty()) {
            String[] req = requirements.split(";");

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

    /**
     * Function called when there are no vacatures on a page. Sets all the vacatures registered with that page to
     * inactive, to make sure they they don't show up in the matcher.
     */
    private void removeAnyVacaturesFromUrl() {
        List<Vacature> list = Vacature.where(Vacature.COL_URL_ID + " = ?", urlOfDocument.getInteger(Url.COL_ID));

        for (Vacature v : list) {
            v.setActive(false);
            v.save();
        }
    }

    /**
     * Processes the Document into a Vacature. Contains al information we can find, so we can refine it later.
     * Uses the settingsMap for DOM selectors to retrieve the info.
     *
     * @return The Processed Vacancy
     */
    private Vacature processVacature() {
        Vacature vacature = new Vacature();

        for (Map.Entry<ProcessData, String> entry : this.settingsMap.entrySet()) {
            if (entry.getKey() != ProcessData.REQUIREMENTFORVACATURE) {
                if (!NULL_VALUE.equals(entry.getValue())) {
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

    /**
     * Checks the description field of the vacature against all known skills in the database. If a skill is found,
     * it is added to the vacature using the Many2Many ActiveJDBC relationship with the Skill model.
     *
     * @param vacature Vacature to process skills of
     */
    private void processSkills(Vacature vacature) {

        String omschrijving = vacature.getOmschrijving().toLowerCase();
        for (String i : ILLEGAL_CHARACTERS) {
            omschrijving = omschrijving.replace(i, " ");
        }

        List<Skill> foundSkills = new LinkedList<Skill>();
        String regex;
        for (Skill skill : allSkills) {
            regex = REGEX_WHITESPACE_BEFORE + skill.getSkill().toLowerCase() + REGEX_WHITESPACE_AFTER;
            if (omschrijving.matches(regex) && !foundSkills.contains(skill)) {
                vacature.addSkill(skill);
                foundSkills.add(skill);
            }
        }
    }

    /**
     * Processes the educations in the omschrijving field of the vacature. When it finds one, it adds it to the
     * Many2Many relationship.
     *
     * @param vacature Vacature to process
     */
    private void processEducation(Vacature vacature) {
        // Get the omschrijving and remove illegal characters from it
        String omschrijving = vacature.getOmschrijving().toLowerCase();
        for (String i : ILLEGAL_CHARACTERS) {
            omschrijving = omschrijving.replace(i, " ");
        }

        List<Education> foundEducations = new LinkedList<Education>();
        // Loop over all known skills, execute the regex it needs to fulfill. If it matches, it is added to the model
        for (Education education : allEducations) {
            String regex = REGEX_WHITESPACE_BEFORE + education.getString("education").toLowerCase() + REGEX_WHITESPACE_AFTER;

            if (omschrijving.matches(regex) && !foundEducations.contains(education)) {
                vacature.addEducation(education);
                foundEducations.add(education);
            }
        }
    }

    /**
     * Processes the Location field of the vacature. If the Location is a new one, get the coordinates of that location
     * and store it in the database by using the Google API. Its is then added to the vacature as a Many2Many relationship.
     * <p/>
     * Why Many2Many and not OneToMany? Some locations are like "Apeldoorn, Amersfoort,..". We want to store them all.
     *
     * @param vacature Vacature to process Location of.
     */
    private void processLocation(Vacature vacature) {
        if (vacature.getPlaats() != null && !vacature.getPlaats().isEmpty()) {
            String[] locs = vacature.getPlaats().split(",|;|/");
            for (String loc : locs) {
                LocationParser.parseLocation(loc, vacature);
            }
        }
    }
}
