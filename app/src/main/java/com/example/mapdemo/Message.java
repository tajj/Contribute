package com.tajj.mapdemo;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by amade002 on 7/24/17.
 */

@ParseClassName("Message")
public class Message extends ParseObject {
    public static final String USER_ID_KEY = "userId";
    public static final String BODY_KEY = "body";
    public static final String UserName_KEY = "username";
    public static final String GroupPointer_KEY = "GRPOINTER";


    public String getUserId() {
        return getString(USER_ID_KEY);
    }

    public String getBody() {
        return getString(BODY_KEY);
    }

    public String getUserName() {
        return getString(UserName_KEY);
    }


    public String getGroupPointer() {
        return getString(GroupPointer_KEY);
    }


    public void setUserName(String username) {
        put(UserName_KEY, username);
    }

    public void setGroupPointer(String GroupPointer) {
        put(GroupPointer_KEY, GroupPointer);
    }

    public void setUserId(String userId) {
        put(USER_ID_KEY, userId);
    }

    public void setBody(String body) {
        put(BODY_KEY, body);
    }


}