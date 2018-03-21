package com.edtech.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by gautamkarnik on 2015-05-05.
 * Based on:  https://guides.codepath.com/android/Converting-JSON-to-Models
 */
public class Unit implements Serializable {

    private final String CLASS_NAME = getClass().getSimpleName();

    public static final String TAG_UNIT = "unit";
    public static final String TAG_TITLE = "title";
    public static final String TAG_MODULES = "modules";

    private String unit;
    private String title;
    private ArrayList<Module> modules;

    public Unit () {

    }

    public String getUnit() {
        return this.unit;
    }

    public String getTitle() {
        return this.title;
    }

    public ArrayList<Module> getModules() {
        return this.modules;
    }

    // Decodes module json results into data model objects
    public static Unit fromJson (JSONObject jsonObject) {
        Unit u = new Unit();

        try {
            u.unit = jsonObject.getString(TAG_UNIT);
            u.title = jsonObject.getString(TAG_TITLE);
            u.modules = new ArrayList<>();
            JSONArray jsonArray = (JSONArray) jsonObject.get(TAG_MODULES);
            for (int i=0; i < jsonArray.length(); i++) {
                JSONObject moduleObj = jsonArray.getJSONObject(i);
                Module module = Module.fromJson(moduleObj);
                u.modules.add(module);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return u;
    }

}
