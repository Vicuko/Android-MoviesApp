package com.example.android.moviesapp;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.android.moviesapp.database.AppDatabase;
import com.example.android.moviesapp.database.MovieEntry;

import java.util.List;

/**
 * Created by Vicuko on 3/2/19.
 */
public class MainViewModel extends AndroidViewModel {
    private static final String TAG = MainViewModel.class.getSimpleName();

    private LiveData<List<MovieEntry>> tasks;


    public MainViewModel(@NonNull Application application) {
        super(application);
        AppDatabase database = AppDatabase.getInstance(this.getApplication());
        Log.d(TAG, "Actively retrieving the tasks from the DataBase");
        tasks = database.movieDao().loadAllTasks();
    }

    public LiveData<List<MovieEntry>> getMovies() {
        return tasks;
    }
}
