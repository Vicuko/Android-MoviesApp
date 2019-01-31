package com.example.android.moviesapp.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by Vicuko on 31/1/19.
 */
@Entity(tableName = "movies")
public class MovieEntry {
    @PrimaryKey
    private int id;

    private ArrayList<String> videoArray;
    private String title;
    private String tagline;
    private Picasso poster;
    private String parsedReleaseDate;
    private String genres;
    private String budget;
    private String productionCompanies;
    private String homepage;
    private Float voteAverageRounded;
    private String overview;
    private LinkedHashMap reviewHash;

    public MovieEntry (HashMap movieDetails, HashMap movieInfo) {
        this.id = (int) movieInfo.get("id");
//        this.videoArray = ;
//        this.title = ;
//        this.tagline = ;
//        this.poster = ;
//        this.parsedReleaseDate = ;
//        this.genres = ;
//        this.budget = ;
//        this.productionCompanies = ;
//        this.homepage = ;
//        this.voteAverageRounded = ;
//        this.overview = ;
//        this.reviewHash = ;
    }

}
