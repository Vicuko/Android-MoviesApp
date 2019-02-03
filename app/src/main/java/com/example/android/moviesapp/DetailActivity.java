package com.example.android.moviesapp;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.android.moviesapp.database.AppDatabase;
import com.example.android.moviesapp.database.MovieEntry;
import com.example.android.moviesapp.utilities.MoviesJsonUtils;
import com.example.android.moviesapp.utilities.NetworkUtils;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.OnInitializedListener;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;

public class DetailActivity extends AppCompatActivity {

    private static final String TAG = DetailActivity.class.getSimpleName();
    private HashMap mMovieInfo;
    private HashMap mMovieDetails;
    private MovieEntry mCurrentMovieEntry;

    private boolean mFavorite;
    private MenuItem mMenuFavorite;

    private AppDatabase mDatabase;

    private ProgressBar mLoadingIndicator;
    private TextView mErrorMessageDisplay;

    private RelativeLayout mTrailerBlock;
    private RelativeLayout mContentBlock;
    private RelativeLayout mReviewsBlock;
    private ImageView mPosterView;
    private TextView mOverviewView;
    private TextView mVoteAverageView;
    private TextView mReleaseDateView;
    private RatingBar mVoteAverageBar;
    private TextView mBudgetView;
    private TextView mHomepageView;
    private TextView mTaglineView;
    private TextView mGenresView;
    private TextView mProductionCompaniesView;


    private YouTubePlayerFragment mYouTubePlayerFragment;
    private YouTubePlayer mYouTubePlayer;
    private RecyclerView mTrailersRecyclerView;
    private TrailersAdapter mTrailersAdapter;
    private RecyclerView.LayoutManager mTrailersLayoutManager;

    private RecyclerView mReviewsRecyclerView;
    private ReviewsAdapter mReviewsAdapter;
    private RecyclerView.LayoutManager mReviewsLayoutManager;

    private boolean mConfigurationHasChanged;
    private SwipeRefreshLayout mSwipeRefreshLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        mDatabase = AppDatabase.getInstance(getApplicationContext());
        initViews();
        setUpRecyclerView();

        mConfigurationHasChanged = false;
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_detail_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                processIntent();
            }
        });
    }

    private void initViews() {
        mErrorMessageDisplay = (TextView) findViewById(R.id.error_detail_message_display);
        mLoadingIndicator = (ProgressBar) findViewById(R.id.details_loader);
        mTrailerBlock = (RelativeLayout) findViewById(R.id.trailer_block);
        mContentBlock = (RelativeLayout) findViewById(R.id.content_block);
        mReviewsBlock = (RelativeLayout) findViewById(R.id.reviews_block);
        mPosterView = (ImageView) findViewById(R.id.poster_imageview);
        mOverviewView = (TextView) findViewById(R.id.overview_textview);
        mVoteAverageView = (TextView) findViewById(R.id.vote_average_textview);
        mReleaseDateView = (TextView) findViewById(R.id.release_date_textview);
        mVoteAverageBar = (RatingBar) findViewById(R.id.vote_average_ratingbar);
        mBudgetView = (TextView) findViewById(R.id.budget_textview);
        mHomepageView = (TextView) findViewById(R.id.homepage_textview);
        mTaglineView = (TextView) findViewById(R.id.tagline_textview);
        mGenresView = (TextView) findViewById(R.id.genres_textview);
        mProductionCompaniesView = (TextView) findViewById(R.id.production_companies_textview);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail_menu, menu);
        mMenuFavorite = menu.findItem(R.id.action_favorite);
        processIntent();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_favorite) {
            int resource;
            if (!mFavorite) {
                resource = R.drawable.ic_baseline_star;
            } else {
                resource = R.drawable.ic_baseline_star_border;
            }
            item.setIcon(resource);
            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    if (!mFavorite) {
                        mDatabase.movieDao().insertMovie(mCurrentMovieEntry);
                        mFavorite = true;
                    } else {
                        mDatabase.movieDao().deleteMovie(mCurrentMovieEntry);
                        mFavorite = false;
                    }
                }
            });

        }
        return super.onOptionsItemSelected(item);
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
                int movieId = Integer.parseInt((String) mMovieInfo.get("id"));

                AddMovieViewModelFactory factory = new AddMovieViewModelFactory(mDatabase, movieId);
                final AddMovieViewModel viewModel
                        = ViewModelProviders.of(this, factory).get(AddMovieViewModel.class);
                viewModel.getMovie().observe(this, new Observer<MovieEntry>() {
                    @Override
                    public void onChanged(@Nullable MovieEntry movieEntry) {
                        if (movieEntry == null) {
                            mFavorite = false;
                            if (!(mMenuFavorite == null)) {
                                mMenuFavorite.setIcon(R.drawable.ic_baseline_star_border);
                                mMenuFavorite.setVisible(true);
                            }
                        } else {
                            mFavorite = true;
                            if (!(mMenuFavorite == null)) {
                                mMenuFavorite.setIcon(R.drawable.ic_baseline_star);
                                mMenuFavorite.setVisible(true);
                            }
                        }
                        viewModel.getMovie().removeObserver(this);
                        loadUI(movieEntry);
                    }
                });
            } else {
                showErrorMessage();
                Log.e(TAG, "Missing information in intent. Can't load content");
            }
        }
    }

    private void loadUI(MovieEntry movieEntry) {
        if (movieEntry == null) {
            mFavorite = false;
            new FetchDetailsTask().execute((String) mMovieInfo.get("id"));
        } else {
            mFavorite = true;
            mCurrentMovieEntry = movieEntry;
            populateUI(mCurrentMovieEntry);
        }
    }

    private void initializeYouTubePlayer(ArrayList<String> videoArray) {
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
                }
                showTrailerBlock();
                if (!videoArray.isEmpty() && mYouTubePlayer != null) {
                    mTrailersAdapter.setMoviesData(videoArray);
                    mYouTubePlayer.cueVideo(videoArray.get(0));

                    if (videoArray.size() < 2) {
                        mTrailersRecyclerView.setVisibility(View.GONE);
                    }
                } else if (mConfigurationHasChanged) {
                    mTrailersAdapter.setMoviesData(videoArray);
                }
            }


            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
                Log.e(TAG, "YouTube Player View initialization failed");
            }
        });
    }

    private void setUpRecyclerView() {
        mTrailersRecyclerView = findViewById(R.id.trailers_recycler_view);
        mTrailersRecyclerView.setHasFixedSize(true);

        mTrailersLayoutManager = new GridLayoutManager(this, 3, LinearLayoutManager.VERTICAL, false);
        mTrailersRecyclerView.setLayoutManager(mTrailersLayoutManager);

        mTrailersAdapter = new TrailersAdapter(this, mYouTubePlayer);
        mTrailersRecyclerView.setAdapter(mTrailersAdapter);
    }

    private void showTrailerBlock() {
        mTrailerBlock.setVisibility(View.VISIBLE);
        mErrorMessageDisplay.setVisibility(View.GONE);
    }

    private void hideTrailerBlock() {
        mTrailerBlock.setVisibility(View.GONE);
    }

    private void showContentBlock() {
        mContentBlock.setVisibility(View.VISIBLE);
        mErrorMessageDisplay.setVisibility(View.GONE);
    }

    private void showErrorMessage() {
        mTrailerBlock.setVisibility(View.GONE);
        mContentBlock.setVisibility(View.GONE);
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
            mLoadingIndicator.setVisibility(View.GONE);
            if (movieDetails != null) {
                mMovieDetails = movieDetails;
                MovieEntry movieEntry = getMovieDetails();
                if (movieEntry!=null) populateUI(movieEntry);
            } else {
                showErrorMessage();
            }
//          TODO => Make the refresh icon disappear when refreshing using the info from the db + Toast when there is no internet connection
            mSwipeRefreshLayout.setRefreshing(false);
        }

        private MovieEntry getMovieDetails() {
            if (mMovieDetails != null && mMovieInfo != null) {
                int id = Integer.parseInt((String) mMovieInfo.get("id"));
                String title = (String) mMovieInfo.get("title");
                String posterUrl = (String) mMovieInfo.get("poster_url");
                String overview = (String) mMovieInfo.get("overview");
                String voteAverage = (String) mMovieInfo.get("vote_average");
                Float voteAverageRounded = Float.parseFloat(voteAverage);
                String releaseDate = (String) mMovieInfo.get("release_date");
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
                String productionCompanies = (String) mMovieDetails.get("production_companies");
                ArrayList<String> videoArray = (ArrayList<String>) mMovieDetails.get("videos");
                LinkedHashMap reviewsHash = (LinkedHashMap) mMovieDetails.get("reviews");

                Picasso.get().load(posterUrl).into(mPosterView);
                Bitmap bitmap = ((BitmapDrawable) mPosterView.getDrawable()).getBitmap();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] posterInByte = baos.toByteArray();

                MovieEntry movieEntry = new MovieEntry(id, videoArray, title,
                        tagline, posterInByte, parsedReleaseDate, genres, budget, productionCompanies,
                        homepage, voteAverage, voteAverageRounded, overview, reviewsHash);

                mCurrentMovieEntry = movieEntry;
                return movieEntry;
            } else {
                return null;
            }
        }
    }

    private void populateUI(MovieEntry movieEntry) {
        if (mFavorite) {
            Bitmap bmp = BitmapFactory.decodeByteArray(movieEntry.poster, 0, movieEntry.poster.length);
            mPosterView.setImageBitmap(bmp);
        }
        initializeYouTubePlayer(movieEntry.videoArray);
        reviewsLoad(movieEntry.reviewHash);
        setElementToView(R.string.description_descriptor, movieEntry.overview, mOverviewView);
        mVoteAverageBar.setRating(movieEntry.voteAverageRounded / 2);
        setElementToView(R.string.vote_average_descriptor, movieEntry.voteAverage, mVoteAverageView);
        setElementToView(R.string.release_date_descriptor, movieEntry.parsedReleaseDate, mReleaseDateView);
        setElementToView(R.string.budget_descriptor, movieEntry.budget, mBudgetView);
        setElementToView(movieEntry.homepage, mHomepageView);
        setElementToView(movieEntry.tagline, mTaglineView);
        setElementToView(R.string.genres_descriptor, movieEntry.genres, mGenresView);
        setElementToView(R.string.producers_descriptor, movieEntry.productionCompanies, mProductionCompaniesView);
        showContentBlock();
    }

    private void reviewsLoad(LinkedHashMap reviewsHash) {
        if (!reviewsHash.isEmpty()) {
            mReviewsRecyclerView = (RecyclerView) findViewById(R.id.reviews_recycler_view);
            mReviewsRecyclerView.setHasFixedSize(true);

            mReviewsLayoutManager = new LinearLayoutManager(getParent());
            mReviewsRecyclerView.setLayoutManager(mReviewsLayoutManager);

            mReviewsAdapter = new ReviewsAdapter(reviewsHash);
            mReviewsRecyclerView.setAdapter(mReviewsAdapter);

            mReviewsBlock.setVisibility(View.VISIBLE);
        }
    }

    private void setElementToView(String text, TextView textview) {
        if (!text.isEmpty() && !text.equals("null") && !text.equals("0")) {
            textview.setText(text);
        } else {
            textview.setVisibility(View.GONE);
        }
    }

    private void setElementToView(int descriptor_id, String text, TextView textview) {
        if (!text.isEmpty() && !text.equals("null") && !text.equals("$0.00")) {
            String descriptor = getResources().getString(descriptor_id);
            textview.setText(Html.fromHtml("<b>" + descriptor + "</b>" + "&nbsp;" + text));
        } else {
            textview.setVisibility(View.GONE);
        }
    }

}
