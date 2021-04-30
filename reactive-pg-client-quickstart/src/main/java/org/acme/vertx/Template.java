package org.acme.vertx;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.vertx.sqlclient.Row;

public class Template {

    @JsonIgnore
    private UUID id;
    private String name;
    @JsonIgnore
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Instant lastChange;
    private String subject;
    private String plain;
    private String html;

    public void setId(UUID id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLastChange(Instant lastChange) {
        this.lastChange = lastChange;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setPlain(String plain) {
        this.plain = plain;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Instant getLastChange() {
        return lastChange;
    }

    public String getSubject() {
        return subject;
    }

    public String getPlain() {
        return plain;
    }

    public String getHtml() {
        return html;
    }

    public static Template toTemplate(Row row) {
        Template t = new Template();
        t.setId(row.getUUID("id"));
        t.setHtml(row.getString("html"));
        t.setName(row.getString("name"));
        t.setLastChange(toInstant(row.getLocalDateTime("last_change")));
        t.setPlain(row.getString("plain"));
        t.setSubject(row.getString("subject"));
        return t;
    }

    private static Instant toInstant(LocalDateTime tijdstip) {
        return tijdstip == null ? null : tijdstip.toInstant(ZoneOffset.UTC);
    }

    @Override
    public String toString() {
        return "Template{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", lastChange=" + lastChange +
                ", subject='" + subject + '\'' +
                ", plain='" + plain + '\'' +
                ", html='" + html + '\'' +
                '}';
    }
}
