package com.example.android.moviesapp;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.android.moviesapp.utilities.MoviesJsonUtils;
import com.example.android.moviesapp.utilities.NetworkUtils;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.OnInitializedListener;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.squareup.picasso.Picasso;

import java.net.URL;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class DetailActivity extends AppCompatActivity {

    private static final String TAG = DetailActivity.class.getSimpleName();
    private HashMap mMovieInfo;
    private HashMap mMovieDetails;

    private ProgressBar mLoadingIndicator;
    private TextView mErrorMessageDisplay;

    private RelativeLayout mTrailerBlock;
    private RelativeLayout mContentBlock;
    private YouTubePlayerFragment mYouTubePlayerFragment;
    private YouTubePlayer mYouTubePlayer;
    private RecyclerView mRecyclerView;
    private TrailersAdapter mTrailersAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private boolean mConfigurationHasChanged;
    private SwipeRefreshLayout mSwipeRefreshLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        mErrorMessageDisplay = (TextView) findViewById(R.id.error_detail_message_display);
        mLoadingIndicator = (ProgressBar) findViewById(R.id.details_loader);
        mTrailerBlock = (RelativeLayout) findViewById(R.id.trailer_block);
        mContentBlock = (RelativeLayout) findViewById(R.id.content_block);

        mConfigurationHasChanged = false;
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_detail_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                processIntent();
            }
        });
        processIntent();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mConfigurationHasChanged = true;
    }

    private void processIntent() {
        Intent intentThatStartedThisActivity = getIntent();
        if (intentThatStartedThisActivity != null) {
            if (intentThatStartedThisActivity.hasExtra(Intent.EXTRA_TEXT)) {
                mMovieInfo = (HashMap) intentThatStartedThisActivity.getSerializableExtra(Intent.EXTRA_TEXT);
                setTitle((String) mMovieInfo.get("title"));
                initializeYouTubePlayer();
                new FetchDetailsTask().execute((String) mMovieInfo.get("id"));
            } else {
                showErrorMessage();
                Log.e(TAG, "Missing information in intent. Can't load content");
            }
        }
    }

    private void initializeYouTubePlayer() {
        String api_key = getApplicationContext().getResources().getString(R.string.youtube_api_key);
        mYouTubePlayerFragment = (YouTubePlayerFragment) getFragmentManager().findFragmentById(R.id.trailer_youtube_view);

        if (mYouTubePlayerFragment == null)
            return;

        mYouTubePlayerFragment.initialize(api_key, new OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player, boolean wasRestored) {
                if (!wasRestored) {
                    mYouTubePlayer = player;
                    mYouTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT);
                    hideTrailerBlock();
                    setUpRecyclerView();
                }
            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
                Log.e(TAG, "YouTube Player View initialization failed");
            }
        });
    }

    private void setUpRecyclerView() {
        mRecyclerView = findViewById(R.id.trailers_recycler_view);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new GridLayoutManager(this, 3, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mTrailersAdapter = new TrailersAdapter(this, mYouTubePlayer);
        mRecyclerView.setAdapter(mTrailersAdapter);
    }

    private void showTrailerBlock() {
        mTrailerBlock.setVisibility(View.VISIBLE);
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
    }

    private void hideTrailerBlock() {
        mTrailerBlock.setVisibility(View.INVISIBLE);
    }

    private void showContentBlock() {
        mContentBlock.setVisibility(View.VISIBLE);
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);

    }

    private void showErrorMessage() {
        mTrailerBlock.setVisibility(View.INVISIBLE);
        mContentBlock.setVisibility(View.INVISIBLE);
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

    public class FetchDetailsTask extends AsyncTask<String, Void, HashMap> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingIndicator.setVisibility(View.VISIBLE);
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected HashMap doInBackground(String... params) {
            if (params.length == 0) {
                return null;
            }
            String movieId = params[0];
            URL movieDetailsUrl = NetworkUtils.buildDetailsUrl(DetailActivity.this, movieId);
            URL movieVideosUrl = NetworkUtils.buildVideosUrl(DetailActivity.this, movieId);
            URL reviewsUrl = NetworkUtils.buildReviewsUrl(DetailActivity.this, movieId);

            try {
                String jsonMovieDetailsResponse = NetworkUtils
                        .getResponseFromHttpUrl(movieDetailsUrl);

                String jsonMovieVideosResponse = NetworkUtils
                        .getResponseFromHttpUrl(movieVideosUrl);

                String jsonReviewsResponse = NetworkUtils
                        .getResponseFromHttpUrl(reviewsUrl);

                HashMap detailsJsonMovieData = MoviesJsonUtils
                        .getMovieDetailsFromJson(jsonMovieDetailsResponse);

                HashMap videosJsonMovieData = MoviesJsonUtils
                        .getMovieVideosFromJson(jsonMovieVideosResponse);

                HashMap movieReviewsData = MoviesJsonUtils
                        .getMovieReviewsFromJson(jsonReviewsResponse);

                detailsJsonMovieData.putAll(videosJsonMovieData);

                detailsJsonMovieData.putAll(movieReviewsData);

                return detailsJsonMovieData;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(HashMap movieDetails) {
            mLoadingIndicator.setVisibility(View.INVISIBLE);
            if (movieDetails != null) {
                mMovieDetails = movieDetails;
                loadMovieDetails();
            } else {
                showErrorMessage();
            }
            mSwipeRefreshLayout.setRefreshing(false);
        }

        private void loadMovieDetails() {
            if (mMovieDetails != null && mMovieInfo != null) {
                String posterUrl = (String) mMovieInfo.get("poster_url");
                String overview = (String) mMovieInfo.get("overview");
                String voteAverage = (String) mMovieInfo.get("vote_average");
                Float voteAverageRounded = Float.parseFloat(voteAverage);
                String releaseDate = (String) mMovieInfo.get("release_date");
//                releaseDate = releaseDate.replace("-","/");
                DateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                DateFormat targetFormat = new SimpleDateFormat("EEE, MMM d, yyyy");
                Date originalReleaseDate = new Date();
                String parsedReleaseDate = "";
                try {
                    originalReleaseDate = originalFormat.parse(releaseDate);
                    parsedReleaseDate = targetFormat.format(originalReleaseDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                String budget = NumberFormat.getCurrencyInstance().format(Integer.parseInt((String) mMovieDetails.get("budget")));
                String homepage = (String) mMovieDetails.get("homepage");
                String tagline = (String) mMovieDetails.get("tagline");
                String genres = (String) mMovieDetails.get("genres");
                String production_companies = (String) mMovieDetails.get("production_companies");
                ArrayList<String> videoArray = (ArrayList<String>) mMovieDetails.get("videos");

                ImageView posterView = (ImageView) findViewById(R.id.poster_imageview);
                TextView overviewView = (TextView) findViewById(R.id.overview_textview);
                TextView voteAverageView = (TextView) findViewById(R.id.vote_average_textview);
                TextView releaseDateView = (TextView) findViewById(R.id.release_date_textview);
                RatingBar voteAverageBar = (RatingBar) findViewById(R.id.vote_average_ratingbar);
                TextView budgetView = (TextView) findViewById(R.id.budget_textview);
                TextView homepageView = (TextView) findViewById(R.id.homepage_textview);
                TextView taglineView = (TextView) findViewById(R.id.tagline_textview);
                TextView genresView = (TextView) findViewById(R.id.genres_textview);
                TextView productionCompaniesView = (TextView) findViewById(R.id.production_companies_textview);

                if (!videoArray.isEmpty() && mYouTubePlayer != null) {
                    mYouTubePlayer.cueVideo(videoArray.get(0));
                    mTrailersAdapter.setMoviesData(videoArray);
                    showTrailerBlock();
                    if (videoArray.size()<2){
                        mRecyclerView.setVisibility(View.GONE);
                    }
                } else if (mConfigurationHasChanged) {
                    mTrailersAdapter.setMoviesData(videoArray);
                    showTrailerBlock();
                }
                Picasso.get().load(posterUrl).into(posterView);
                setElementToView(R.string.description_descriptor, overview, overviewView);
                voteAverageBar.setRating(voteAverageRounded/2);
                setElementToView(R.string.vote_average_descriptor,voteAverage, voteAverageView);
                setElementToView(R.string.release_date_descriptor, parsedReleaseDate, releaseDateView);
                setElementToView(R.string.budget_descriptor,budget, budgetView);
                setElementToView(homepage, homepageView);
                setElementToView(tagline, taglineView);
                setElementToView(R.string.genres_descriptor, genres, genresView);
                setElementToView(R.string.producers_descriptor,production_companies, productionCompaniesView);
                showContentBlock();
            }
        }

        private void setElementToView(String text, TextView textview){
            if (!text.isEmpty() && !text.equals("null") && !text.equals("0")){
                textview.setText(text);
            }
            else {
                textview.setVisibility(View.GONE);
            }
        }

        private void setElementToView(int descriptor_id, String text, TextView textview){
            if (!text.isEmpty() && !text.equals("null") && !text.equals("$0.00")){
                String descriptor = getResources().getString(descriptor_id);
                textview.setText(Html.fromHtml("<b>" + descriptor + "</b>" + "&nbsp;" + text));
            }
            else {
                textview.setVisibility(View.GONE);
            }
        }

    }

}
