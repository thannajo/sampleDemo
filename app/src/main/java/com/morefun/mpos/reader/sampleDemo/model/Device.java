package com.morefun.mpos.reader.sampleDemo.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Device {
    @SerializedName("serial")
    @Expose
    public String serial;
    @SerializedName("estatus")
    @Expose
    public String estatus;
    @SerializedName("createAt")
    @Expose
    public String createAt;

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getEstatus() {
        return estatus;
    }

    public void setEstatus(String estatus) {
        this.estatus = estatus;
    }

    public String getCreateAt() {
        return createAt;
    }

    public void setCreateAt(String createAt) {
        this.createAt = createAt;
    }

    public Device() {
    }
}
