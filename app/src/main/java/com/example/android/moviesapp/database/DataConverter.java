package com.example.android.moviesapp.database;

import android.arch.persistence.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Created by Vicuko on 31/1/19.
 */
public class DataConverter {
    @TypeConverter
    public static LinkedHashMap fromStringToLinkedHashMap(String contentString) {
        Type linkedHashMapType = new TypeToken<LinkedHashMap>() {}.getType();
        return new Gson().fromJson(contentString,linkedHashMapType);
    }

    @TypeConverter
    public static String fromLinkedHashMaptoString(LinkedHashMap contentHashMap) {
        Gson gson = new Gson();
        String json = gson.toJson(contentHashMap);
        return json;
    }

    @TypeConverter
    public static ArrayList<String> fromStringtoArray(String value) {
        Type listType = new TypeToken<ArrayList<String>>() {}.getType();
        return new Gson().fromJson(value, listType);
    }

    @TypeConverter
    public static String fromArrayListToString(ArrayList<String> list) {
        Gson gson = new Gson();
        String json = gson.toJson(list);
        return json;
    }
}
