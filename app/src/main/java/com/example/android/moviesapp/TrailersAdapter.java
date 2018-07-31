package com.example.android.moviesapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.youtube.player.YouTubePlayer;

import java.util.ArrayList;

/**
 * Created by Vicuko on 30/7/18.
 */
public class TrailersAdapter extends RecyclerView.Adapter<TrailersAdapter.TrailersAdapterViewHolder> {

    private ArrayList<String> mVideoList;
    private Context mContext;
    private YouTubePlayer mYouTubePlayer;


    public TrailersAdapter(Context context, YouTubePlayer youTubePlayer) {
        mContext = context;
        mYouTubePlayer = youTubePlayer;
    }

    public class TrailersAdapterViewHolder extends RecyclerView.ViewHolder {
        public Button mTrailerButton;

        public TrailersAdapterViewHolder(final View itemView) {
            super(itemView);
            mTrailerButton = (Button) itemView.findViewById(R.id.trailer_button);
            mTrailerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int adapterPosition = getAdapterPosition();
                    mYouTubePlayer.cueVideo(mVideoList.get(adapterPosition));
                }
            });
        }
    }

    @NonNull
    @Override
    public TrailersAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.trailer_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        return new TrailersAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrailersAdapterViewHolder holder, int position) {
        String trailer_button = mContext.getResources().getString(R.string.trailer_button);
        holder.mTrailerButton.setText(trailer_button+(position+1));
    }

    @Override
    public int getItemCount() {
        if (null == mVideoList) return 0;
        return mVideoList.size();
    }

    public void setMoviesData(ArrayList<String> videoList) {
        mVideoList = videoList;
        notifyDataSetChanged();
    }

}
