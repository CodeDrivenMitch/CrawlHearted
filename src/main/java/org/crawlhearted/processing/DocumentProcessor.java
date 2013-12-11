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

        List<ProcessSetting> databaseList = ProcessSetting.find(ProcessSetting.COL_CRAWLER_ID + " = ?", crawlManager.getInteger("id"));
        for (ProcessSetting processSetting : databaseList) {
            ProcessData key = ProcessData.valueOf(processSetting.getString(ProcessSetting.COL_SETTING_KEY));
            String value = processSetting.getString(ProcessSetting.COL_SETTING_VALUE);

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
                url.setString(Url.COL_URL, u);
                url.setInteger(Url.COL_CRAWLER_ID, crawlManager.getInteger("id"));
                crawlManager.addUrlToList(url);
            }
        }
    }

    private void processContent() {
        if (docHasRequirements(ProcessData.REQUIREMENTFORVACATURE)) {
            Vacature vacature = processVacature();
            vacature.saveSafely();
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

        for(Vacature v : list) {
            v.setInteger(Vacature.COL_ACTIVE, 0);
            v.save();
        }
    }

    private Vacature processVacature() {
        Vacature vacature = new Vacature();

        for(Map.Entry<ProcessData, String> entry : this.settingsMap.entrySet()) {
            if(entry.getKey() != ProcessData.REQUIREMENTFORVACATURE) {
                vacature.putProperty(entry.getKey(), this.documentToProcess.select(entry.getValue()).text());
            }
        }

        vacature.setInteger(Vacature.COL_URL_ID, this.urlOfDocument.getInteger(Url.COL_ID));
        vacature.generateHash();

        return vacature;
    }
}
