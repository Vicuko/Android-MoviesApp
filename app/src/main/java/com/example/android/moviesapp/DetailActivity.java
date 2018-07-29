package com.example.android.moviesapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.HashMap;

public class DetailActivity extends AppCompatActivity {

    private HashMap mMovieDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intentThatStartedThisActivity = getIntent();

        if (intentThatStartedThisActivity != null) {
            if (intentThatStartedThisActivity.hasExtra(Intent.EXTRA_TEXT)) {
                mMovieDetail = (HashMap) intentThatStartedThisActivity.getSerializableExtra(Intent.EXTRA_TEXT);
                setTitle((String) mMovieDetail.get("title"));
            }
        }
    }
}
