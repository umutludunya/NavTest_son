package com.app.onal.navtest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by USER on 18.03.2017.
 */

public class User {
    private String name;
    private String surname;
    private String image;
    private String agree;
    private String disagree;
    private String ratio;
    private String badge;
    private String city;
    private String phone;

    public User() {
    }

    public User(String name, String surname, String image, String agree, String disagree, String ratio, String badge, String city, String phone) {
        this.name = name;
        this.surname = surname;
        this.image = image;
        this.agree = agree;
        this.disagree = disagree;
        this.ratio = ratio;
        this.badge = badge;
        this.city = city;
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
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

    public void setRatio(String ratio) {
        this.ratio = ratio;
    }

    public String getBadge() {
        return badge;
    }

    public void setBadge(String badge) {
        this.badge = badge;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("surname", surname);
        result.put("image", image);
        result.put("agree", agree);
        result.put("disagree",disagree);
        result.put("ratio", ratio);
        result.put("badge",badge);
        result.put("city",city);
        result.put("phone",phone);

        return result;
    }
}
