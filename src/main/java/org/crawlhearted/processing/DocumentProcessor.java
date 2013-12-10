package org.crawlhearted.processing;

import org.crawlhearted.management.CrawlManager;
import org.crawlhearted.objects.Blacklist;
import org.crawlhearted.objects.Flag;
import org.crawlhearted.objects.Url;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Created with IntelliJ IDEA for JobHearted.
 * User: Morlack
 * Date: 12/9/13
 * Time: 12:28 PM
 */
public class DocumentProcessor {
    private CrawlManager crawlManager;
    private Blacklist blacklist;

    private DocumentProcessor(CrawlManager crawlManager) {
        this.crawlManager = crawlManager;
        this.blacklist = new Blacklist(crawlManager);
    }

    public void processDocument(Document document) {
        processLinks(document);
    }

    private void processLinks(Document document) {

        Elements elements = document.getElementsByTag("a");

        for(Element e : elements) {
            String u = e.attr("abs:href");
            if(blacklist.urlAllowed(u)) {
                Url url = new Url();
                url.setFlag(Flag.FOUND);
                url.setString("url", u);
                url.setInteger("crawler_id", crawlManager.getInteger("id"));
                crawlManager.addUrlToList(url);
            }
        }
    }

    public static DocumentProcessor createProcessor(CrawlManager crawlManager) {
        return new DocumentProcessor(crawlManager);
    }
}
