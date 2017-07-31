package com.example.mapdemo;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by amade002 on 7/24/17.
 */

@ParseClassName("Message")
public class Message extends ParseObject {
    String body;
    String userName;
    String timeStamp;

    public Message() {}

    public Message (String body, String userName, String timeStamp) {
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