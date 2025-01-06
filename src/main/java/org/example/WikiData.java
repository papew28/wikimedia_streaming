package org.example;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WikiData {
    private Meta meta;
    private long id;
    private String type;
    private long namespace;
    private String title;
    private String title_url;
    private String comment;
    private String user;
    private boolean bot;


    @JsonProperty("length")
    private Map<String, Long> length;

    @JsonProperty("revision")
    private Map<String, Long> revision;

    private boolean minor;

    public boolean isPatrolled() {
        return patrolled;
    }

    public void setPatrolled(boolean patrolled) {
        this.patrolled = patrolled;
    }

    public boolean isMinor() {
        return minor;
    }

    public void setMinor(boolean minor) {
        this.minor = minor;
    }

    public String getWike_type() {
        return wike_type;
    }

    public void setWike_type(String wike_type) {
        this.wike_type = wike_type;
    }

    private boolean patrolled;
    private String wike_type;

    // Getters et setters

    public Meta getMeta() {
        return meta;
    }

    public void setMeta(Meta meta) {
        this.meta = meta;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getNamespace() {
        return namespace;
    }

    public void setNamespace(long namespace) {
        this.namespace = namespace;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public boolean isBot() {
        return bot;
    }

    public void setBot(boolean bot) {
        this.bot = bot;
    }

    public Map<String, Long> getLength() { // Remplacé Integer par Long
        return length;
    }

    public void setLength(Map<String, Long> length) { // Remplacé Integer par Long
        this.length = length;
    }

    public Map<String, Long> getRevision() { // Remplacé Integer par Long
        return revision;
    }

    public void setRevision(Map<String, Long> revision) { // Remplacé Integer par Long
        this.revision = revision;
    }

    public String getTitle_url() {
        return title_url;
    }

    public void setTitle_url(String title_url) {
        this.title_url = title_url;
    }
}
