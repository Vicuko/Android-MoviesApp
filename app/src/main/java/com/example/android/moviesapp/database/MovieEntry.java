package com.example.android.moviesapp.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Created by Vicuko on 31/1/19.
 */
@Entity(tableName = "movies")
public class MovieEntry {
    @PrimaryKey
    public int id;

    public ArrayList<String> videoArray;
    public String title;
    public String tagline;
    public byte[] poster;
    public String parsedReleaseDate;
    public String genres;
    public String budget;
    public String productionCompanies;
    public String homepage;
    public String voteAverage;
    public Float voteAverageRounded;
    public String overview;
    public LinkedHashMap reviewHash;

    public MovieEntry (int id,
                       ArrayList<String> videoArray,
                       String title,
                       String tagline,
                       byte[] poster,
                       String parsedReleaseDate,
                       String genres,
                       String budget,
                       String productionCompanies,
                       String homepage,
                       String voteAverage,
                       Float voteAverageRounded,
                       String overview,
                       LinkedHashMap reviewHash) {

        this.id = id;
        this.videoArray = videoArray;
        this.title = title;
        this.tagline = tagline;
        this.poster = poster;
        this.parsedReleaseDate = parsedReleaseDate;
        this.genres = genres;
        this.budget = budget;
        this.productionCompanies = productionCompanies;
        this.homepage = homepage;
        this.voteAverage = voteAverage;
        this.voteAverageRounded = voteAverageRounded;
        this.overview = overview;
        this.reviewHash = reviewHash;
    }

}
