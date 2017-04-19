package com.app.onal.navtest;

import android.content.Intent;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by USER on 12.03.2017.
 */

public class Comment {
    private String comment;
    private String priceRecommend;
    private String agree;
    private String disagree;
    private String ratio;
    private String cuid;
    private String adid;


    public Comment(String comment, String priceRecommend, String agree, String disagree, String ratio,String adid,String uid) {
        this.comment = comment;
        this.priceRecommend = priceRecommend;
        this.agree = agree;
        this.disagree = disagree;
        this.ratio = ratio;
        this.adid = adid;
        this.cuid = uid;
    }

    public Comment() {
    }

    public String getAdid() {
        return adid;
    }

    public void setAdid(String adid) {
        this.adid = adid;
    }

    public String getUid() {
        return cuid;
    }

    public void setUid(String uid) {
        this.cuid = uid;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getPriceRecommend() {
        return priceRecommend;
    }

    public void setPriceRecommend(String priceRecommend) {
        this.priceRecommend = priceRecommend;
    }

    public String getAgree() {
        return agree;
    }

    public void setAgree(String agree) {
        this.agree = agree;
    }

    public String getDisagree() {
        return disagree;
    }

    public void setDisagree(String disagree) {
        this.disagree = disagree;
    }

    public String getRatio() {
        return ratio;
    }

    public void setRatio() {
        this.ratio = Integer.toString((Integer.parseInt(this.agree)/(Integer.parseInt(this.agree) + Integer.parseInt(this.disagree))) * 100);
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("agree", agree);
        result.put("disagree", disagree);
        result.put("comment", comment);
        result.put("priceRecommend", priceRecommend);
        result.put("ratio", ratio);
        result.put("cuid",cuid);

        return result;
    }

}
