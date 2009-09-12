/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dygest.feeds.summarizer;

import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.SyndFeedOutput;
import com.sun.syndication.io.XmlReader;
import dygest.commons.db.simple.DocumentDB;
import dygest.commons.store.s3.S3Accessor;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.URL;

/**
 *  I and only I am the your God, you Feed
 * @author anand
 */
public class FeedUploader {

    private static DocumentDB db = new DocumentDB();
    private S3Accessor s3 = null;
    private Feed f = null;
    private String url = null;
    private String hash = null;

    public FeedUploader(Feed f) {
        this.f = f;
        this.url = f.getUri();
        init();
    }

    public FeedUploader(String url) {
        this.url = url;
        init();
    }

    private void init() {
        try {
            s3 = new S3Accessor("dygest-feeds");
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private SyndFeed getFeed(String url) throws Exception {
        URL feedUrl = new URL(url);
        SyndFeedInput input = new SyndFeedInput(false);
        SyndFeed feed = input.build(new XmlReader(feedUrl));

        return feed;
    }

    public void upload() {
        Feed f = null;
        
        try {
            if(this.f == null) {
                SyndFeed feed = getFeed(this.url);

                try {
                    SyndFeedOutput output = new SyndFeedOutput();
                    String title = feed.getTitle();

                    // store to db
                    f = new Feed(url, title, output.outputString(feed), false);
                    
                } catch(Exception e) {
                    e.printStackTrace();
                    // return the original feed
                }
            } else {
                f = this.f;
            }

            FeedIndexRecord record = new FeedIndexRecord(url);

            // save into s3
            s3.put(record.getHash(), f.getContent(), true);

            // save into simple db
            db.put("feedindex", record);
            
            this.hash = record.getHash();
        } catch(Exception e) {
            e.printStackTrace();
        }
        
    }

    /**
     * @return the hash
     */
    public String getHash() {
        return hash;
    }

    public static void main(String[] args) {
        FeedUploader uploader = new FeedUploader("http://googleblog.blogspot.com/atom.xml");
        uploader.upload();
    }
}
