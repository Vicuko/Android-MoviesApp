package com.example.android.moviesapp.utilities;

import android.content.res.Resources;
import android.net.Uri;
import android.util.Log;

import com.example.android.moviesapp.R;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by Vicuko on 25/7/18.
 * It will be used to retrieve movies information from the Movies API
 */
public class NetworkUtils {

    private static final String TAG = NetworkUtils.class.getSimpleName();

    private static final String TMDB_URL = "https://api.themoviedb.org/3";
    private static final String MOVIE_SEARCH = "movie";

    final static String API_KEY_PARAM = "api_key";
    final static String LANGUAGE_PARAM = "language";
    final static String PAGE_PARAM = "page";

    private static final String api_key = "";
    private static final String language = "en-US";
    private static final int page = 1;

    public static URL buildUrl(String filter_criteria) {
        String defaultCriteria = Resources.getSystem().getString(R.string.criteria_default);
        filter_criteria = !filter_criteria.isEmpty() ? filter_criteria : defaultCriteria;

        Uri builtUri = Uri.parse(TMDB_URL).buildUpon()
                .appendPath(MOVIE_SEARCH)
                .appendPath(filter_criteria)
                .appendQueryParameter(API_KEY_PARAM, api_key)
                .appendQueryParameter(LANGUAGE_PARAM, language)
                .appendQueryParameter(PAGE_PARAM, Integer.toString(page))
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        Log.v(TAG, "Built URI " + url);

        return url;
    }

    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }

}
