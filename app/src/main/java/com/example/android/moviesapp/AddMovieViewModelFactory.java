package com.example.android.moviesapp;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import com.example.android.moviesapp.database.AppDatabase;

/**
 * Created by Vicuko on 31/1/19.
 */
class AddMovieViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final AppDatabase mAppDatabase;
    private final int mTaskId;

    public AddMovieViewModelFactory(AppDatabase database, int taskId) {
        mAppDatabase = database;
        mTaskId = taskId;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        //noinspection unchecked
        return (T) new AddMovieViewModel(mAppDatabaseg, mTaskId);
    }

}
