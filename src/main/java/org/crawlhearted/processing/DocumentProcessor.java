package org.crawlhearted.processing;

import org.crawlhearted.management.CrawlManager;
import org.crawlhearted.objects.Blacklist;
import org.crawlhearted.objects.Flag;
import org.crawlhearted.objects.Url;
import org.crawlhearted.objects.Vacature;
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
    Map<ProcessData, String> settingsMap;
    private CrawlManager crawlManager;
    private Blacklist blacklist;
    private Document documentToProcess;
    private Url urlOfDocument;

    private DocumentProcessor(CrawlManager crawlManager) {
        this.crawlManager = crawlManager;
        this.blacklist = new Blacklist(crawlManager);

        settingsMap = new HashMap<ProcessData, String>();

        List<ProcessSetting> databaseList = ProcessSetting.find("crawler_id = ?", crawlManager.getInteger("id"));
        for (ProcessSetting processSetting : databaseList) {
            ProcessData key = ProcessData.valueOf(processSetting.getString("setting_name"));
            String value = processSetting.getString("setting_value");

            settingsMap.put(key, value);
        }

        logger.info("Loaded " + settingsMap.size() + " Setting entries!");
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
                url.setFlag(Flag.FOUND);
                url.setString("url", u);
                url.setInteger("crawler_id", crawlManager.getInteger("id"));
                crawlManager.addUrlToList(url);
            }
        }
    }

    private void processContent() {
        if (docHasRequirements(ProcessData.REQUIREMENTFORVACATURE)) {
            processVacature().saveSafely();
        } else {
            removeAnyVacaturesFromUrl();
        }
    }

    private boolean docHasRequirements(ProcessData data) {
        boolean hasIt = true;
        String requirements = settingsMap.get(data);
        if (requirements != null && !requirements.isEmpty()) {
            String req[] = requirements.split(";");

            for (String r : req) {
                if (documentToProcess.select(r).text().isEmpty()) {
                    hasIt = false;
                }
            }
        } else {
            logger.warn("Could not determine " + data.toString() + " for url " + this.urlOfDocument.getString("url"));
            hasIt = false;
        }

        return hasIt;
    }

    private void removeAnyVacaturesFromUrl() {
        List<Vacature> list = Vacature.where("url_id = ?", urlOfDocument.getInteger("id"));

        for(Vacature v : list) {
            v.setInteger("active", 0);
            v.save();
        }
    }

    private Vacature processVacature() {
        // Save the url to get an id
        urlOfDocument.saveIt();

        Vacature vacature = new Vacature();

        for(Map.Entry<ProcessData, String> entry : this.settingsMap.entrySet()) {
            if(entry.getKey() != ProcessData.REQUIREMENTFORVACATURE) {
                vacature.putProperty(entry.getKey(), this.documentToProcess.select(entry.getValue()).text());
            }
        }

        vacature.setInteger("url_id", this.urlOfDocument.getInteger("id"));
        vacature.generateHash();

        return vacature;
    }
}
