package com.example.android.moviesapp;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import com.example.android.moviesapp.database.AppDatabase;

/**
 * Created by Vicuko on 31/1/19.
 */
class DetailMovieViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final AppDatabase mAppDatabase;
    private final int mTaskId;

    public DetailMovieViewModelFactory(AppDatabase database, int taskId) {
        mAppDatabase = database;
        mTaskId = taskId;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        //noinspection unchecked
        return (T) new DetailMovieViewModel(mAppDatabase, mTaskId);
    }

}
