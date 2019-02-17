package com.example.android.moviesapp.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

/**
 * Created by Vicuko on 31/1/19.
 */
@Dao
public interface MovieDao {

    @Query("SELECT * FROM movies")
    LiveData<List<MovieEntry>> loadAllMovies();

    @Query("SELECT * FROM movies WHERE id = :id")
    LiveData<MovieEntry> loadMovieById(int id);

    @Insert
    void insertMovie(MovieEntry movieEntry);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateMovie(MovieEntry movieEntry);

    @Delete
    void deleteMovie(MovieEntry movieEntry);


}
