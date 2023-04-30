package com.morefun.mpos.reader.sampleDemo.model;

import com.google.gson.annotations.SerializedName;

public class Response {
    @SerializedName("code")
    public String code;
    @SerializedName("message")
    public String message;
    @SerializedName("data")
    public Data data;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public Response() {

    }

    public Response(String code, String message, Data data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
}
