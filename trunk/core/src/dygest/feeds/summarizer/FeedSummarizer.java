/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dygest.feeds.summarizer;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.fetcher.FeedFetcher;
import com.sun.syndication.fetcher.impl.FeedFetcherCache;
import com.sun.syndication.fetcher.impl.HashMapFeedInfoCache;
import com.sun.syndication.fetcher.impl.HttpURLFeedFetcher;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.SyndFeedOutput;
import dygest.commons.db.simple.DocumentDB;
import dygest.commons.store.s3.S3Accessor;
import dygest.text.ScoredSentence;
import dygest.text.summerizer.SynmanticSummerizer;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author anand
 */
public class FeedSummarizer {

    private static SynmanticSummerizer summarizer = null;
    private static DocumentDB db = new DocumentDB();
    private static FeedFetcherCache feedInfoCache = HashMapFeedInfoCache.getInstance();
    private static FeedFetcher feedFetcher = new HttpURLFeedFetcher(feedInfoCache);
    private static S3Accessor s3 = null;

    static {
        try {
            summarizer = new SynmanticSummerizer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private SyndFeed getFeed(String url) throws Exception {
        URL feedUrl = new URL(url);
        SyndFeedInput input = new SyndFeedInput(false);
        SyndFeed feed = feedFetcher.retrieveFeed(feedUrl);

        return feed;
    }

    public void summarize(String feedURL) {
        if (summarizer != null) {
            try {
                SyndFeedOutput output = new SyndFeedOutput();
                SyndFeed feed = getFeed(feedURL);
                List<SyndEntryImpl> entries = feed.getEntries();
                String title = feed.getTitle();

                for (SyndEntryImpl entry : entries) {
                    String uri = entry.getLink();

                    Iterator contentIter = entry.getContents().iterator();
                    while (contentIter.hasNext()) {
                        // Target the description node
                        SyndContent content =
                                (SyndContent) contentIter.next();

                        StringBuffer summary = new StringBuffer();
                        List<ScoredSentence> sentences = summarizer.summarizeText(content.getValue());
                        int len = (int) Math.ceil(0.3 * sentences.size());
                        for (ScoredSentence s : sentences) {
                            summary.append(s.getText());
                            if (len-- == 0) {
                                break;
                            }
                        }

                        // add footer
                        summary.append("<br><p><i>summarized by <a href='http://dyge.st'>dyge.st</a></p>");

                        // Create and set a footer-appended description
                        content.setValue(summary.toString());
                    }

                    SyndContent desc = entry.getDescription();
                    if (desc != null) {
                        StringBuffer summary = new StringBuffer();
                        List<ScoredSentence> sentences = summarizer.summarizeText(desc.getValue());
                        int len = (int) Math.ceil(0.3 * sentences.size());
                        for (ScoredSentence s : sentences) {
                            summary.append(s.getText());
                            if (len-- == 0) {
                                break;
                            }
                        }
                        // add footer
                        summary.append("<br><p><i>summarized by <a href='http://dyge.st'>dyge.st</a></p>");
                        // Create and set a footer-appended description
                        desc.setValue(summary.toString());
                    }
                }

                // store to db
                Feed f = new Feed(feedURL, title, output.outputString(feed), false);
                FeedUploader uploader = new FeedUploader(f);
                uploader.upload();
            } catch (Exception e) {
                e.printStackTrace();
                // return the original feed
            }
        }
    }

    public static void main(String args[]) {
        FeedSummarizer fs = new FeedSummarizer();

        List<HashMap<String, String>> feeds = db.select("select * from feedindex");

        for (HashMap<String, String> feedAsMap : feeds) {
            if (feedAsMap.containsKey("url")) {
                String url = feedAsMap.get("url");
                System.out.print("summarizing " + url + "\t");
                fs.summarize(url);
                System.out.println("[ DONE ]");
            }
        }
    }
}
