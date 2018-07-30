package com.example.android.moviesapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.example.android.moviesapp.utilities.MoviesJsonUtils;
import com.example.android.moviesapp.utilities.NetworkUtils;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.OnInitializedListener;
import com.google.android.youtube.player.YouTubePlayerFragment;

import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DetailActivity extends AppCompatActivity {

    private static final String TAG = DetailActivity.class.getSimpleName();
    private HashMap mMovieInfo;
    private ProgressBar mLoadingIndicator;
    private YouTubePlayerFragment mYouTubePlayerFragment;
    private YouTubePlayer mYouTubePlayer;
    private Button mTrailerButton1;
    private Button mTrailerButton2;
    private Button mTrailerButton3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        mLoadingIndicator = (ProgressBar) findViewById(R.id.details_loader);
        mTrailerButton1 = (Button) findViewById(R.id.trailer_button1);
        mTrailerButton2 = (Button) findViewById(R.id.trailer_button1);
        mTrailerButton3 = (Button) findViewById(R.id.trailer_button1);

        Intent intentThatStartedThisActivity = getIntent();

        if (intentThatStartedThisActivity != null) {
            if (intentThatStartedThisActivity.hasExtra(Intent.EXTRA_TEXT)) {
                mMovieInfo = (HashMap) intentThatStartedThisActivity.getSerializableExtra(Intent.EXTRA_TEXT);
                setTitle((String) mMovieInfo.get("title"));
                new FetchDetailsTask().execute((String) mMovieInfo.get("id"));
            } else {
//                showError();
                Log.e(TAG, "Missing information in intent. Can't load content");
            }
        }
    }

    private void initializeYouTubePlayer(final String video_key) {
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
                    mYouTubePlayer.cueVideo(video_key);
                }
            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
                Log.e(TAG, "YouTube Player View initialization failed");
            }
        });
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

            try {
                String jsonMovieDetailsResponse = NetworkUtils
                        .getResponseFromHttpUrl(movieDetailsUrl);

                String jsonMovieVideosResponse = NetworkUtils
                        .getResponseFromHttpUrl(movieVideosUrl);

                HashMap detailsJsonMovieData = MoviesJsonUtils
                        .getMovieDetailsFromJson(jsonMovieDetailsResponse);

                HashMap videosJsonMovieData = MoviesJsonUtils
                        .getMovieVideosFromJson(jsonMovieVideosResponse);

                detailsJsonMovieData.putAll(videosJsonMovieData);

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
                loadMovieDetails(movieDetails);
            } else {

            }
        }

        private void loadMovieDetails(HashMap movieDetails) {
            String budget = (String) movieDetails.get("budget");
            String genres = (String) movieDetails.get("genres");

            String[] videos = (String[]) movieDetails.get("videos");

            loadTrailers(videos);
        }

        private void loadTrailers(String[] videos){
            initializeYouTubePlayer("wb49-oV0F78");

            HashMap videosHashMap = new HashMap();
            videosHashMap.put(mTrailerButton1, videos[0]);
            videosHashMap.put(mTrailerButton2, videos[1]);
            videosHashMap.put(mTrailerButton3, videos[2]);

            Iterator it = videosHashMap.entrySet().iterator();
            while (it.hasNext()) {
                HashMap.Entry element = (HashMap.Entry) it.next();
                setClickActionOnButtons((Button) element.getKey(), (String) element.getValue());
                showButton((Button) element.getKey());
            }
        }

        private void setClickActionOnButtons (Button button, final String key) {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mYouTubePlayer.cueVideo(key);
                }
            });
        }

        private void showButton(Button button){
            button.setVisibility(View.VISIBLE);
        }

        private void hideButton(Button button){
            button.setVisibility(View.INVISIBLE);
        }
    }
}
