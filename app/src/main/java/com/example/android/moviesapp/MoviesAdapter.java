package com.example.android.moviesapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.android.moviesapp.database.MovieEntry;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Vicuko on 27/7/18.
 */
public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.MoviesAdapterViewHolder> {

    private HashMap[] mMoviesData;
    private List<MovieEntry> mMovieEntries;

    private final MoviesAdapterOnClickHandler mClickHandler;

    public interface MoviesAdapterOnClickHandler {
        void onClick(HashMap dataForDetail);
    }

    public MoviesAdapter(MoviesAdapterOnClickHandler clickHandler) {
        mClickHandler = clickHandler;
    }

    public class MoviesAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final ImageView mMovieImageView;

        public MoviesAdapterViewHolder(View itemView) {
            super(itemView);
            mMovieImageView = (ImageView) itemView.findViewById(R.id.movie_poster);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int adapterPosition = getAdapterPosition();
            HashMap dataForDetail = new HashMap();
            if (mMovieEntries != null){
                MovieEntry movieEntry = mMovieEntries.get(adapterPosition);
                dataForDetail.put("id",String.valueOf(movieEntry.id));
                dataForDetail.put("title",movieEntry.title);
            } else{
                dataForDetail = mMoviesData[adapterPosition];
            }
            mClickHandler.onClick(dataForDetail);
        }
    }

    @NonNull
    @Override
    public MoviesAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.movies_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        return new MoviesAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MoviesAdapterViewHolder holder, int position) {
        final String MOVIE_POSTER_URL = "poster_url";
        if (mMovieEntries == null) {
            HashMap movieData = mMoviesData[position];
            String posterUrlForThisMovie = (String) movieData.get(MOVIE_POSTER_URL);
            Picasso.get().load(posterUrlForThisMovie).into(holder.mMovieImageView);
        }else{
            MovieEntry movieEntry = mMovieEntries.get(position);
            Bitmap bmp = BitmapFactory.decodeByteArray(movieEntry.poster, 0, movieEntry.poster.length);
            holder.mMovieImageView.setImageBitmap(bmp);
        }
    }

    @Override
    public int getItemCount() {
        if (mMoviesData == null && mMovieEntries == null) {
            return 0;
        }
        else if(mMovieEntries != null){
            return mMovieEntries.size();
        }
        return mMoviesData.length;
    }

    public void setMoviesData(HashMap[] moviesData) {
        mMoviesData = moviesData;
        mMovieEntries = null;
        notifyDataSetChanged();
    }

    public void setMoviesData(List<MovieEntry> movieEntries) {
        mMovieEntries = movieEntries;
        notifyDataSetChanged();
    }
}
