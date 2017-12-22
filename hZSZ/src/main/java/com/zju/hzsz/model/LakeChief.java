package com.zju.hzsz.model;

/**
 * Created by ZJTLM4600l on 2017/12/20.
 */

public class LakeChief {
    public long chiefId;
    public String chiefName;
    public String department;//职位
    public String contactWay;
    public int authority;

    public void setChiefId(long chiefId) {
        this.chiefId = chiefId;
    }

    public void setChiefName(String chiefName) {
        this.chiefName = chiefName;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public void setContactWay(String contactWay) {
        this.contactWay = contactWay;
    }

    public void setAuthority(int authority) {
        this.authority = authority;
    }

    public long getChiefId() {
        return chiefId;
    }

    public String getChiefName() {
        return chiefName;
    }

    public String getDepartment() {
        return department;
    }

    public String getContactWay() {
        return contactWay;
    }

    public int getAuthority() {
        return authority;
    }
}
