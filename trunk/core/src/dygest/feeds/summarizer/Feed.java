/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dygest.feeds.summarizer;

import dygest.commons.db.simple.IStorable;
import java.util.HashMap;

/**
 *
 * @author anand
 */
public class Feed implements IStorable {

    private String uri;
    private boolean active;
    private String content;
    private String title;

    public Feed(String url, String title, String content, boolean active) {
        this.uri = url;
        this.title = title;
        this.content = content;
        this.active = active;
    }

    public String getID() {
        return this.getUri();
    }

    public HashMap<String, String> toMap() {
        HashMap<String, String> objAsMap = new HashMap<String, String>();

        objAsMap.put("id",getUri());
        objAsMap.put("uri",getUri());
        objAsMap.put("content",getContent());
        objAsMap.put("title",getTitle());
        objAsMap.put("active", String.valueOf(isActive()));

        return objAsMap;
    }

    public String toJSON() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * @return the uri
     */
    public String getUri() {
        return uri;
    }

    /**
     * @param uri the uri to set
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

    /**
     * @return the active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * @param active the active to set
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * @return the content
     */
    public String getContent() {
        return content;
    }

    /**
     * @param content the content to set
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

}
