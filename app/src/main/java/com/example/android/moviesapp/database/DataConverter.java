package com.example.android.moviesapp.database;

import android.arch.persistence.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;

/**
 * Created by Vicuko on 31/1/19.
 */
public class DataConverter {
    @TypeConverter
    public static HashMap fromString(String contentString) {
        Type hashMapType = new TypeToken<HashMap>() {}.getType();
        return new Gson().fromJson(contentString,hashMapType);
    }

    @TypeConverter
    public static String fromHashMap(HashMap contentHashMap) {
        Gson gson = new Gson();
        String json = gson.toJson(contentHashMap);
        return json;
    }
}
