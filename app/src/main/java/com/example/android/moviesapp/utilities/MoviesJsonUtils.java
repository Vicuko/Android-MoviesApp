package com.example.android.moviesapp.utilities;

import android.annotation.TargetApi;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Vicuko on 27/7/18.
 */
public class MoviesJsonUtils {

    public static HashMap[] getMoviesInfoFromJson(String moviesJsonStr)
            throws JSONException {

        final String TAG = MoviesJsonUtils.class.getSimpleName();
        final String RESPONSE_CODE = "status_code";
        final String RESPONSE_MESSAGE = "status_message";
        final String RESPONSE_RESULTS = "results";

        final String MOVIE_ID = "id";
        final String MOVIE_TITLE = "title";
        final String MOVIE_VOTE = "vote_average";
        final String MOVIE_POSTER_PATH = "poster_path";
        final String MOVIE_DESCRIPTION = "overview";
        final String MOVIE_POSTER_URL = "poster_url";
        final String MOVIE_RELEASE_DATE = "release_date";

        final String TMDB_POSTERS_URL = "http://image.tmdb.org/t/p/";
        final String POSTER_SIZE = "w300";

        HashMap[] parsedMoviesData = null;

        JSONObject moviesJson = new JSONObject(moviesJsonStr);

        if (moviesJson.has(RESPONSE_CODE)) {
            int errorCode = moviesJson.getInt(RESPONSE_CODE);
            String errorMessage = moviesJson.getString(RESPONSE_MESSAGE);
            Log.e(TAG, RESPONSE_CODE + ":" + errorCode + ", " + RESPONSE_MESSAGE + ":" + errorMessage);
            return null;
        }

        JSONArray moviesArray = moviesJson.getJSONArray(RESPONSE_RESULTS);

        parsedMoviesData = new HashMap[moviesArray.length()];

        for (int i = 0; i < moviesArray.length(); i++) {
            String id;
            String title;
            String voteAverage;
            String posterPath;
            String description;
            String releaseDate;
            HashMap movieHash = new HashMap();

            JSONObject movieObject = moviesArray.getJSONObject(i);

            id = movieObject.getString(MOVIE_ID);
            title = movieObject.getString(MOVIE_TITLE);
            voteAverage = movieObject.getString(MOVIE_VOTE);
            posterPath = movieObject.getString(MOVIE_POSTER_PATH);
            description = movieObject.getString(MOVIE_DESCRIPTION);
            releaseDate = movieObject.getString(MOVIE_RELEASE_DATE);


            Uri builtUri = Uri.parse(TMDB_POSTERS_URL).buildUpon()
                    .appendPath(POSTER_SIZE)
                    .appendEncodedPath(posterPath)
                    .build();

            URL url = null;
            try {
                url = new URL(builtUri.toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            assert url != null;
            String posterUrl = url.toString();

            movieHash.put(MOVIE_ID, id);
            movieHash.put(MOVIE_TITLE, title);
            movieHash.put(MOVIE_VOTE, voteAverage);
            movieHash.put(MOVIE_POSTER_URL, posterUrl);
            movieHash.put(MOVIE_DESCRIPTION, description);
            movieHash.put(MOVIE_RELEASE_DATE, releaseDate);

            parsedMoviesData[i] = movieHash;
        }

        return parsedMoviesData;
    }

    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static HashMap getMovieDetailsFromJson(String movieDetailsJsonStr)
            throws JSONException {

        final String TAG = MoviesJsonUtils.class.getSimpleName();
        final String RESPONSE_CODE = "status_code";
        final String RESPONSE_MESSAGE = "status_message";

        final String MOVIE_BUDGET = "budget";
        final String MOVIE_GENRES = "genres";
        final String MOVIE_GENRES_NAME = "name";
        final String MOVIE_HOMEPAGE = "homepage";
        final String MOVIE_PRODUCTION_COMPANIES = "production_companies";
        final String MOVIE_PRODUCTION_COMPANIES_NAME = "name";
        final String MOVIE_PRODUCTION_COMPANIES_LOGO = "logo_path";
        final String MOVIE_TAGLINE = "tagline";

        JSONObject movieDetailsJson = new JSONObject(movieDetailsJsonStr);

        if (movieDetailsJson.has(RESPONSE_CODE)) {
            int errorCode = movieDetailsJson.getInt(RESPONSE_CODE);
            String errorMessage = movieDetailsJson.getString(RESPONSE_MESSAGE);
            Log.e(TAG, RESPONSE_CODE + ":" + errorCode + ", " + RESPONSE_MESSAGE + ":" + errorMessage);
            return null;
        }

        HashMap parsedMovieDetailsHash = new HashMap();

        String budget = movieDetailsJson.getString(MOVIE_BUDGET);
        String homepage = movieDetailsJson.getString(MOVIE_HOMEPAGE);
        String tagline = movieDetailsJson.getString(MOVIE_TAGLINE);

        JSONArray genresArray = movieDetailsJson.getJSONArray(MOVIE_GENRES);
        String[] genres = new String[genresArray.length()];

        for (int i = 0; i < genresArray.length(); i++) {
            JSONObject genreObject = genresArray.getJSONObject(i);
            genres[i] = genreObject.getString(MOVIE_GENRES_NAME);
        }
        String genresResult = String.join(", ", genres);

        JSONArray productionArray = movieDetailsJson.getJSONArray(MOVIE_PRODUCTION_COMPANIES);
        String[] productionCompanies = new String[productionArray.length()];
        String[] productionCompaniesLogos = new String[productionArray.length()];

        for (int i = 0; i < productionArray.length(); i++) {
            JSONObject productionObject = productionArray.getJSONObject(i);
            productionCompanies[i] = productionObject.getString(MOVIE_PRODUCTION_COMPANIES_NAME);
            productionCompaniesLogos[i] = productionObject.getString(MOVIE_PRODUCTION_COMPANIES_LOGO);
        }
        String companiesResult = String.join(", ", productionCompanies);

        parsedMovieDetailsHash.put(MOVIE_BUDGET, budget);
        parsedMovieDetailsHash.put(MOVIE_HOMEPAGE, homepage);
        parsedMovieDetailsHash.put(MOVIE_TAGLINE, tagline);

        parsedMovieDetailsHash.put(MOVIE_GENRES, genresResult);
        parsedMovieDetailsHash.put(MOVIE_PRODUCTION_COMPANIES, companiesResult);
        parsedMovieDetailsHash.put(MOVIE_PRODUCTION_COMPANIES_LOGO, productionCompaniesLogos);

        return parsedMovieDetailsHash;
    }

    public static HashMap getMovieVideosFromJson(String movieVideosJsonStr)
            throws JSONException {

        final String TAG = MoviesJsonUtils.class.getSimpleName();
        final String RESPONSE_CODE = "status_code";
        final String RESPONSE_MESSAGE = "status_message";

        final String MOVIE_VIDEOS_KEY = "videos";

        final String MOVIE_VIDEO_RESULTS = "results";
        final String MOVIE_VIDEO_SITE = "site";
        final String MOVIE_VIDEO_TYPE = "type";
        final String MOVIE_VIDEO_KEY = "key";

        final String MOVIE_VIDEO_YOUTUBE_VALIDATION = "YouTube";
        final String MOVIE_VIDEO_TRAILER_VALIDATION = "Trailer";

        JSONObject movieVideosJson = new JSONObject(movieVideosJsonStr);

        if (movieVideosJson.has(RESPONSE_CODE)) {
            int errorCode = movieVideosJson.getInt(RESPONSE_CODE);
            String errorMessage = movieVideosJson.getString(RESPONSE_MESSAGE);
            Log.e(TAG, RESPONSE_CODE + ":" + errorCode + ", " + RESPONSE_MESSAGE + ":" + errorMessage);
            return null;
        }

        JSONArray movieList = movieVideosJson.getJSONArray(MOVIE_VIDEO_RESULTS);
        ArrayList<String> parsedMovieVideos = new ArrayList<String>();
        HashMap parsedMovieVideosHash = new HashMap();

        for (int i = 0; i < movieList.length(); i++) {
            JSONObject currentMovieInList = movieList.getJSONObject(i);
            String site = currentMovieInList.getString(MOVIE_VIDEO_SITE);
            String type = currentMovieInList.getString(MOVIE_VIDEO_TYPE);
            if (site.equals(MOVIE_VIDEO_YOUTUBE_VALIDATION) && type.equals(MOVIE_VIDEO_TRAILER_VALIDATION)) {
                String key = currentMovieInList.getString(MOVIE_VIDEO_KEY);
                parsedMovieVideos.add(key);
            }
        }

        parsedMovieVideosHash.put(MOVIE_VIDEOS_KEY, parsedMovieVideos);

        return parsedMovieVideosHash;
    }

}
