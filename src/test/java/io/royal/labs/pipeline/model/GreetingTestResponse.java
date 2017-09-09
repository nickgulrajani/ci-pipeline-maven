package io.royal.labs.pipeline.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GreetingTestResponse {

    private long id;
    private String content;

    public GreetingTestResponse() {

    }

    public long getId() {

        return id;
    }

    public void setId(long id) {

        this.id = id;
    }

    public String getContent() {

        return content;
    }

    public void setContent(String content) {

        this.content = content;
    }
}
