package org.jobhearted.crawler.processing;

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
    private static List<Skill> allSkills;
    private static List<Education> allEducations;
    Map<ProcessData, String> settingsMap;
    String[] illegalCharacter = {
            "(", ")", ";", ".", ",", ":", "{", "}", "[", "]", "*", "&", "^", "%", "$", "@", "!", "?"
    };
    private CrawlManager crawlManager;
    private Blacklist blacklist;
    private Document documentToProcess;
    private Url urlOfDocument;

    private DocumentProcessor(CrawlManager crawlManager) {
        this.crawlManager = crawlManager;
        this.blacklist = new Blacklist(crawlManager);

        settingsMap = new HashMap<ProcessData, String>();

        List<ProcessSetting> databaseList = ProcessSetting.find(ProcessSetting.COL_CRAWLER_ID + " = ?", crawlManager.getInteger("id"));
        for (ProcessSetting processSetting : databaseList) {
            ProcessData key = ProcessData.valueOf(processSetting.getString(ProcessSetting.COL_SETTING_KEY));
            String value = processSetting.getString(ProcessSetting.COL_SETTING_VALUE);

            settingsMap.put(key, value);
        }

        logger.info("Loaded " + settingsMap.size() + " Setting entries!");

        if (allSkills == null) {
            allSkills = Skill.findAll();
            logger.info("Loaded " + allSkills.size() + " Skill entries!");
        }

        if (allEducations == null) {
            allEducations = Education.findAll();
            logger.info("Loaded " + allEducations.size() + " Education entries!");
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
                if(!crawlManager.getUrlList().contains(url)) {
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
            logger.info("validvacature");
            if (vacature.saveSafely()) {
                processSkillsAndEducation(vacature);
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
            logger.warn("Could not determine " + data.toString() + " for url " + this.urlOfDocument.getString(Url.COL_URL));
            return false;
        }

        return true;
    }

    private void removeAnyVacaturesFromUrl() {
        List<Vacature> list = Vacature.where(Vacature.COL_URL_ID + " = ?", urlOfDocument.getInteger(Url.COL_ID));

        for (Vacature v : list) {
            v.setInteger(Vacature.COL_ACTIVE, 0);
            v.removeAllSkills();
            v.save();
        }
    }

    private Vacature processVacature() {
        Vacature vacature = new Vacature();

        for (Map.Entry<ProcessData, String> entry : this.settingsMap.entrySet()) {
            if (entry.getKey() != ProcessData.REQUIREMENTFORVACATURE) {
                if(!entry.getValue().equals("NULL")) {
                    vacature.putProperty(entry.getKey(), this.documentToProcess.select(entry.getValue()).text());
                } else {
                    vacature.putProperty(entry.getKey(), "");
                }
            }
        }

        vacature.setInteger(Vacature.COL_URL_ID, this.urlOfDocument.getInteger(Url.COL_ID));
        vacature.generateHash();

        return vacature;
    }

    private void processSkillsAndEducation(Vacature vacature) {


        String omschrijving = vacature.getString(Vacature.COL_OMSCHRIJVING);

        for (Skill skill : allSkills) {
            if(omschrijving.contains(skill.getString("skill"))) {
                logger.info("found skill " + skill.getString("skill"));
                vacature.add(skill);
            }
        }

        for(Education education : allEducations) {
            if(omschrijving.contains(education.getString(Education.COL_EDUCATION))) {
                logger.info("found education " + education.getString(Education.COL_EDUCATION));
                vacature.add(education);
            }
        }
    }
}
