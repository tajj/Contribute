package com.tajj.mapdemo;

import com.parse.ParseClassName;

/**
 * Created by mayajey on 7/21/17.
 */

@ParseClassName("Comment")
public class Comment {

    String body;
    String userName;
    String timeStamp;

    public Comment() {}

    public Comment (String body, String userName, String timeStamp) {
        this.body = body;
        this.userName = userName;
        this.timeStamp = timeStamp;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
}
