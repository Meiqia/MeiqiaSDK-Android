package com.meiqia.meiqiasdk.model;

import android.text.TextUtils;

public class Agent {

    private String id;
    private String avatar;
    private String cellphone;
    private String email;
    private int enterprise_id;
    private String token;
    private String nickname;
    private String public_cellphone;
    private String public_email;
    private String qq;
    private String realname;
    private String signature;
    private String status;
    private String telephone;
    private String weixin;
    private boolean isOnline;
    private String privilege;

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getAvatar() {
        return this.avatar;
    }

    public void setCellphone(String cellphone) {
        this.cellphone = cellphone;
    }

    public String getCellphone() {
        return this.cellphone;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEnterprise_id(int enterprise_id) {
        this.enterprise_id = enterprise_id;
    }

    public int getEnterprise_id() {
        return this.enterprise_id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getNickname() {
        return this.nickname;
    }

    public void setPublic_cellphone(String public_cellphone) {
        this.public_cellphone = public_cellphone;
    }

    public String getPublic_cellphone() {
        return this.public_cellphone;
    }

    public void setPublic_email(String public_email) {
        this.public_email = public_email;
    }

    public String getPublic_email() {
        return this.public_email;
    }

    public void setQq(String qq) {
        this.qq = qq;
    }

    public String getQq() {
        return this.qq;
    }

    public void setRealname(String realname) {
        this.realname = realname;
    }

    public String getRealname() {
        return this.realname;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getSignature() {
        return this.signature;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return this.status;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getTelephone() {
        return this.telephone;
    }

    public void setWeixin(String weixin) {
        this.weixin = weixin;
    }

    public String getWeixin() {
        return this.weixin;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isOffDuty() {
        return "off_duty".equals(status);
    }

    public void setIsOnline(boolean isOnline) {
        this.isOnline = isOnline;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public String getPrivilege() {
        return privilege;
    }

    public void setPrivilege(String privilege) {
        this.privilege = privilege;
    }

    public boolean isRobot() {
        return TextUtils.equals("bot", privilege);
    }
}
