package com.morefun.mpos.reader.sampleDemo.model;

import com.google.gson.annotations.SerializedName;

public class RequestUser {
    @SerializedName("username")
    public String username;
    @SerializedName("userpass")
    public String userpass;

    public RequestUser(String username, String userpass) {
        this.username = username;
        this.userpass = userpass;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserpass() {
        return userpass;
    }

    public void setUserpass(String userpass) {
        this.userpass = userpass;
    }

}
