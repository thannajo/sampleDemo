package com.morefun.mpos.reader.sampleDemo.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Afiliado {
    @SerializedName("rif")
    @Expose
    public String rif;
    @SerializedName("userName")
    @Expose
    public String userName;
    @SerializedName("userPass")
    @Expose
    public String userPass;
    @SerializedName("commerceName")
    @Expose
    public String commerceName;
    @SerializedName("commerceAddress")
    @Expose
    public String commerceAddress;
    @SerializedName("terminal")
    @Expose
    public String terminal;
    @SerializedName("merchant")
    @Expose
    public String merchant;
    @SerializedName("status")
    @Expose
    public String status;
    @SerializedName("createAt")
    @Expose
    public String createAt;
    @SerializedName("device")
    @Expose
    public Device device;

    public String getRif() {
        return rif;
    }

    public void setRif(String rif) {
        this.rif = rif;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPass() {
        return userPass;
    }

    public void setUserPass(String userPass) {
        this.userPass = userPass;
    }

    public String getCommerceName() {
        return commerceName;
    }

    public void setCommerceName(String commerceName) {
        this.commerceName = commerceName;
    }

    public String getCommerceAddress() {
        return commerceAddress;
    }

    public void setCommerceAddress(String commerceAddress) {
        this.commerceAddress = commerceAddress;
    }

    public String getTerminal() {
        return terminal;
    }

    public void setTerminal(String terminal) {
        this.terminal = terminal;
    }

    public String getMerchant() {
        return merchant;
    }

    public void setMerchant(String merchant) {
        this.merchant = merchant;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreateAt() {
        return createAt;
    }

    public void setCreateAt(String createAt) {
        this.createAt = createAt;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public Afiliado() {
    }


}
