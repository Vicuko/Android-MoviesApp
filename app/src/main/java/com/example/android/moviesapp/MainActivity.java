package com.example.android.moviesapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.moviesapp.utilities.MoviesJsonUtils;
import com.example.android.moviesapp.utilities.NetworkUtils;

import java.net.URL;
import java.util.HashMap;

import static android.widget.GridLayout.VERTICAL;

public class MainActivity extends AppCompatActivity implements MoviesAdapter.MoviesAdapterOnClickHandler {

    private RecyclerView mRecyclerView;
    private MoviesAdapter mMoviesAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private TextView mErrorMessageDisplay;
    private ProgressBar mLoadingIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mErrorMessageDisplay = (TextView) findViewById(R.id.error_message_display);
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);
        mRecyclerView = (RecyclerView) findViewById(R.id.movies_recycler_view);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new GridLayoutManager(this,2, VERTICAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mMoviesAdapter = new MoviesAdapter(this);
        mRecyclerView.setAdapter(mMoviesAdapter);

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String moviesFilter = sharedPrefs.getString(
                getString(R.string.criteria_key),
                getString(R.string.criteria_default));
        String activityTitle = getSelectionTitle(moviesFilter);
        setTitle(activityTitle);
        loadMovies(moviesFilter);
    }

    private String getSelectionTitle(String preferenceValue){
        String[] entries = getResources().getStringArray(R.array.criteria_entries);
        String[] values = getResources().getStringArray(R.array.criteria_values);
        for (int i = 0; i<values.length; i++){
            if (values[i].equals(preferenceValue)){
                return entries[i];
            }
        }
        return (String) this.getApplicationInfo().loadLabel(this.getPackageManager());
    }

    private void loadMovies(String filter_criteria){
        showPosters();
        new FetchMoviesTask().execute(filter_criteria);
    };

    private void showPosters() {
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showErrorMessage() {
        mRecyclerView.setVisibility(View.INVISIBLE);
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_preferences){
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(HashMap dataForDetail, int adapterPosition) {
        Context context = this;
        Class destinationClass = DetailActivity.class;
        Intent intentToStartDetailActivity = new Intent(context, destinationClass);
        intentToStartDetailActivity.putExtra(Intent.EXTRA_TEXT, dataForDetail);
        startActivity(intentToStartDetailActivity);
    }

    public class FetchMoviesTask extends AsyncTask<String, Void, HashMap[]>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingIndicator.setVisibility(View.VISIBLE);
        }

        @Override
        protected HashMap[] doInBackground(String... params) {
            if (params.length == 0) {
                return null;
            }
            String filter_criteria = params[0];
            URL moviesRequestUrl = NetworkUtils.buildUrl(MainActivity.this, filter_criteria);

            try {
                String jsonMoviesResponse = NetworkUtils
                        .getResponseFromHttpUrl(moviesRequestUrl);

                HashMap[] simpleJsonMoviesData = MoviesJsonUtils
                        .getMoviesInfoFromJson(jsonMoviesResponse);

                return simpleJsonMoviesData;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(HashMap[] moviesData) {
            mLoadingIndicator.setVisibility(View.INVISIBLE);
            if (moviesData!= null) {
                showPosters();
                mMoviesAdapter.setMoviesData(moviesData);
            } else {
                showErrorMessage();
            }
        }
    }
}
