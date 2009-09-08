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
import dygest.text.ScoredSentence;
import dygest.text.summerizer.SynmanticSummerizer;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author anand
 */
public class FeedSummarizer {

    private static SynmanticSummerizer summarizer = null;
    private static FeedFetcherCache feedInfoCache = HashMapFeedInfoCache.getInstance();
    private static FeedFetcher feedFetcher = new HttpURLFeedFetcher(feedInfoCache);


    static {
        try {
            summarizer = new SynmanticSummerizer();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private SyndFeed getFeed(String url) throws Exception {
        URL feedUrl = new URL(url);
        SyndFeedInput input = new SyndFeedInput(false);
        SyndFeed feed = feedFetcher.retrieveFeed(feedUrl);

        return feed;
    }

    public String summarize(String feedURL) {
        if(summarizer != null) {
            try {
                SyndFeedOutput output = new SyndFeedOutput();
                SyndFeed feed = getFeed(feedURL);
                List<SyndEntryImpl> entries = feed.getEntries();

                for(SyndEntryImpl entry : entries) {
                    String uri = entry.getLink();
                    StringBuffer summary = new StringBuffer();
                    List<ScoredSentence> sentences = summarizer.summerize(uri);
                    int len = 0;
                    for(ScoredSentence s : sentences) {
                        summary.append(s.getText());
                        len++;
                        if(len == 4) {
                            break;
                        }
                    }

                    Iterator contentIter = entry.getContents().iterator();
                    while(contentIter.hasNext()) {
                        // Target the description node
                        SyndContent content =
                                (SyndContent) contentIter.next();

                        // Create and set a footer-appended description
                        content.setValue(summary.toString());
                    }
                }

                return output.outputString(feed);
            } catch(Exception e) {
                e.printStackTrace();
                // return the original feed
            }
        }

        return null;
    }

    public static void main(String args[]) {
        FeedSummarizer fs = new FeedSummarizer();
        String feed = fs.summarize("http://googleblog.blogspot.com/atom.xml");

        System.out.println(feed);
    }

}
