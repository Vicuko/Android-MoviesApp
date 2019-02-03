package com.example.android.moviesapp;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.moviesapp.database.MovieEntry;
import com.example.android.moviesapp.utilities.MoviesJsonUtils;
import com.example.android.moviesapp.utilities.NetworkUtils;

import java.net.URL;
import java.util.HashMap;
import java.util.List;

import static android.widget.GridLayout.VERTICAL;

public class MainActivity extends AppCompatActivity implements MoviesAdapter.MoviesAdapterOnClickHandler {

    private static final String TAG = MainActivity.class.getSimpleName();
    private RecyclerView mRecyclerView;

    private MoviesAdapter mMoviesAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private TextView mErrorMessageDisplay;
    private ProgressBar mLoadingIndicator;

    private String mFilterCriteria;
    private boolean mShowingFavorite;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        mShowingFavorite = false;
        updateFilterCriteria();
        setRefreshLayout();
        setTitle(getSelectionTitle(mFilterCriteria));
        loadMovies();
    }

    private void initViews() {
        mErrorMessageDisplay = (TextView) findViewById(R.id.error_message_display);
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);
        mRecyclerView = (RecyclerView) findViewById(R.id.movies_recycler_view);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new GridLayoutManager(this, 2, VERTICAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mMoviesAdapter = new MoviesAdapter(this);
        mRecyclerView.setAdapter(mMoviesAdapter);
    }

    private void setRefreshLayout() {
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateFilterCriteria();
                loadMovies();
            }
        });
    }

    private void loadMovies() {
        MainViewModel viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        viewModel.getMovies().observe(this, new Observer<List<MovieEntry>>() {
            @Override
            public void onChanged(@Nullable List<MovieEntry> movieEntries) {
                Log.d(TAG, "Updating list of movies from LiveData in ViewModel");
                showPosters();
                if (movieEntries != null && mShowingFavorite) {
                    if (movieEntries.isEmpty()) {
                        showErrorMessage(getString(R.string.no_favorites_message));
                    } else {
                        mMoviesAdapter.setMoviesData(movieEntries);
                    }
                } else if (movieEntries == null && mShowingFavorite) {
                    showErrorMessage(getString(R.string.general_error_message));
                } else {
                    new FetchMoviesTask().execute(mFilterCriteria);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateFilterCriteria();
        loadMovies();
    }

    private void updateFilterCriteria() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        mFilterCriteria = sharedPrefs.getString(
                getString(R.string.criteria_key),
                getString(R.string.criteria_default));
        String favorite_option = getResources().getStringArray(R.array.criteria_values)[4];
        if (mFilterCriteria.equals(favorite_option)) {
            mShowingFavorite = true;
        }
    }

    private String getSelectionTitle(String preferenceValue) {
        String[] entries = getResources().getStringArray(R.array.criteria_entries);
        String[] values = getResources().getStringArray(R.array.criteria_values);
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(preferenceValue)) {
                return entries[i];
            }
        }
        return (String) this.getApplicationInfo().loadLabel(this.getPackageManager());
    }

    private void showPosters() {
        mErrorMessageDisplay.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showErrorMessage(String errorText) {
        mRecyclerView.setVisibility(View.GONE);
        mErrorMessageDisplay.setText(errorText);
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
        if (id == R.id.action_preferences) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(HashMap dataForDetail) {
        Context context = this;
        Class destinationClass = DetailActivity.class;
        Intent intentToStartDetailActivity = new Intent(context, destinationClass);
        intentToStartDetailActivity.putExtra(Intent.EXTRA_TEXT, dataForDetail);
        startActivity(intentToStartDetailActivity);
    }

    public class FetchMoviesTask extends AsyncTask<String, Void, HashMap[]> {

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
            mLoadingIndicator.setVisibility(View.GONE);
            if (moviesData != null) {
                showPosters();
                mMoviesAdapter.setMoviesData(moviesData);
            } else {
                showErrorMessage(getString(R.string.non_loading_message));
            }
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }
}
