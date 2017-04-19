package com.app.onal.navtest;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by USER on 06.03.2017.
 */

public class Ad {
    String description;
    String category;
    String title;
    String uid;
    HashMap<String,String> images;
    HashMap<String,String> userComments;

    public Ad(){

    }


    public Ad(String uid,String title,String description, String category, HashMap<String,String> images) {
        this.uid = uid;
        this.description = description;
        this.category = category;
        this.images = images;
        this.title = title;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getHeadImage(){
        return images.get("img1");
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public HashMap<String,String> getImages() {
        return images;
    }

    public void setImages(HashMap<String,String> images) {
        this.images = images;
    }

    // [START post_to_map]
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();

        result.put("title", title);
        result.put("description",description);
        result.put("uid",uid);
        result.put("category",category);
        result.put("images",images);

        return result;
    }
    // [END post_to_map]
}
