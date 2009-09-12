/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dygest.feeds.summarizer;

import dygest.commons.db.simple.IStorable;
import java.security.MessageDigest;
import java.util.HashMap;

/**
 *
 * @author anand
 */
public class FeedIndexRecord implements IStorable {

    private String url;
    private String hash;

    private static final char[] hex = {
	'0', '1', '2', '3', '4', '5', '6', '7',
	'8', '9', 'a', 'b', 'c', 'd', 'e', 'f',
    };

    public FeedIndexRecord(String url) throws Exception {
        this.url = url;
        MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
        digest.update(url.getBytes());
        byte[] hash = digest.digest();
        this.hash = toHex(hash);
    }

    private static final String toHex(byte hash[]) {
	StringBuffer buf = new StringBuffer(hash.length * 2);

	for (int idx=0; idx<hash.length; idx++)
	    buf.append(hex[(hash[idx] >> 4) & 0x0f]).append(hex[hash[idx] & 0x0f]);

	return buf.toString();
    }

    public String getID() {
        return url;
    }

    public HashMap<String, String> toMap() {
        HashMap<String, String> objAsMap = new HashMap<String, String>();

        objAsMap.put("id", url);
        objAsMap.put("url", url);
        objAsMap.put("hash", hash);

        return objAsMap;
    }

    public String toJSON() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @return the hash
     */
    public String getHash() {
        return hash;
    }

    

}
