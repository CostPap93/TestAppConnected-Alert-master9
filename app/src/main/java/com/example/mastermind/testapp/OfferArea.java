package com.example.mastermind.testapp;

/**
 * Created by mastermind on 17/5/2018.
 */

public class OfferArea {


    private int areaid;
    private String title;

    public OfferArea(int catid, String title) {
        this.areaid = catid;
        this.title = title;
    }

    public OfferArea() {

    }

    public int getAreaid() {
        return areaid;
    }

    public void setAreaid(int catid) {
        this.areaid = catid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


}
