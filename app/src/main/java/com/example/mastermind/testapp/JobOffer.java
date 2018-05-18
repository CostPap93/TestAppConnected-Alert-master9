package com.example.mastermind.testapp;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by mastermind on 19/4/2018.
 */

public class JobOffer implements Serializable {

    private int id;
    private int catid;
    private String cattitle;
    private int areaid;
    private String areatitle;
    private String title;
    private String link;
    private String desc;
    private Date date;
    private String downloaded;


    public JobOffer(){

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCatid() {
        return catid;
    }

    public void setCatid(int catid) {
        this.catid = catid;
    }

    public String getCattitle() {
        return cattitle;
    }

    public void setCattitle(String cattitle) {
        this.cattitle = cattitle;
    }

    public int getAreaid() {
        return areaid;
    }

    public void setAreaid(int areaid) {
        this.areaid = areaid;
    }

    public String getAreatitle() {
        return areatitle;
    }

    public void setAreatitle(String areatitle) {
        this.areatitle = areatitle;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getDownloaded() {
        return downloaded;
    }

    public void setDownloaded(String downloaded) {
        this.downloaded = downloaded;
    }
}
