package com.example.android.moviesapp.utilities;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

/**
 * Created by Vicuko on 27/7/18.
 */
public class MoviesJsonUtils {

    public static HashMap[] getMoviesInfoFromJson(Context context, String moviesJsonStr)
            throws JSONException {

        final String TAG = MoviesJsonUtils.class.getSimpleName();
        final String RESPONSE_CODE = "status_code";
        final String RESPONSE_MESSAGE = "status_message";
        final String RESPONSE_RESULTS = "results";

        final String MOVIE_ID = "id";
        final String MOVIE_TITLE = "title";
        final String MOVIE_VOTE = "vote_average";
        final String MOVIE_POSTER_PATH = "poster_path";
        final String MOVIE_POSTER_URL = "poster_url";

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
            HashMap movieHash = new HashMap();

            JSONObject movieObject = moviesArray.getJSONObject(i);

            id = movieObject.getString(MOVIE_ID);
            title = movieObject.getString(MOVIE_TITLE);
            voteAverage = movieObject.getString(MOVIE_VOTE);
            posterPath = movieObject.getString(MOVIE_POSTER_PATH);

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

            String posterUrl = url.toString();

            movieHash.put(MOVIE_ID, id);
            movieHash.put(MOVIE_TITLE, title);
            movieHash.put(MOVIE_VOTE, voteAverage);
            movieHash.put(MOVIE_POSTER_URL, posterUrl);

            parsedMoviesData[i] = movieHash;
        }

        return parsedMoviesData;
    }


}
