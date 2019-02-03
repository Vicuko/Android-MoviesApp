package com.example.android.moviesapp;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.example.android.moviesapp.database.AppDatabase;
import com.example.android.moviesapp.database.MovieEntry;

/**
 * Created by Vicuko on 31/1/19.
 */
class DetailMovieViewModel extends ViewModel {

    private LiveData<MovieEntry> movie;

    public DetailMovieViewModel(AppDatabase database, int movieId) {
        movie = database.movieDao().loadMovieById(movieId);
    }

    public LiveData<MovieEntry> getMovie() {
        return movie;
    }
}

