package com.example.android.popularmoviesstagetwo.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.example.android.popularmoviesstagetwo.R;
import com.example.android.popularmoviesstagetwo.models.Movie;
import com.example.android.popularmoviesstagetwo.models.MovieResponse;
import com.example.android.popularmoviesstagetwo.utils.BuildConfig;
import com.example.android.popularmoviesstagetwo.utils.Utils;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * {@link MovieListAdapter} creates a list of weather forecasts to a
 * {@link android.support.v7.widget.RecyclerView}
 *
 * Created by aditibhattacharya on 26/11/2017.
 */

public class MovieListAdapter extends RecyclerView.Adapter<MovieListAdapter.MovieListAdapterViewHolder> {

    private MovieResponse mMovieResponse;
    private List<Movie> mMovieList;
    private MovieListAdapterOnClickHandler mClickHandler;
    private Context mContext;

    /**
     * Interface to receive onClick messages
     */
    public interface MovieListAdapterOnClickHandler {
        void onClick(Movie movie);
    }

    /**
     * OnClick handler for the adapter that handles situation when a single item is clicked
     * @param clickHandler
     */
    public MovieListAdapter(MovieListAdapterOnClickHandler clickHandler) {
        mClickHandler = clickHandler;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class MovieListAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.image_movie_poster) ImageView mImageViewPoster;

        private MovieListAdapterViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            view.setOnClickListener(this);
        }

        /**
         * This method gets called when child view is clicked
         * @param view
         */
        @Override
        public void onClick(View view) {
            int adapterPosition = getAdapterPosition();
            Movie movie = mMovieResponse.getMovieList().get(adapterPosition);
            mClickHandler.onClick(movie);
        }
    }


    /**
     * Method called when a new ViewHolder gets created in the event of RecyclerView being laid out.
     * This creates enough ViewHolders to fill up the screen and allow scrolling
     * @param viewGroup
     * @param viewType
     * @return A new MovieListAdapterViewHolder that holds the View for each list item
     */
    @Override
    public MovieListAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        mContext = viewGroup.getContext();
        int listItemLayoutId = R.layout.movie_list_item;
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        boolean shouldAttachToParentImmediately = false;

        View view = layoutInflater.inflate(listItemLayoutId, viewGroup, shouldAttachToParentImmediately);
        return new MovieListAdapterViewHolder(view);
    }

    /**
     * Method used by RecyclerView to display the movie poster image
     * @param movieListAdapterViewHolder
     * @param position
     */
    @Override
    public void onBindViewHolder(MovieListAdapterViewHolder movieListAdapterViewHolder, int position) {
        String moviePoster = "";
        String moviePosterUrl = "";

        if (position < getItemCount()) {
            Movie movie = mMovieResponse.getMovieList().get(position);
            moviePoster = movie.getPosterPath();
            if (!Utils.isEmptyString(moviePoster)) {
                moviePosterUrl = BuildConfig.MOVIE_POSTER_BASE_URL + moviePoster;
                Picasso.with(mContext)
                    .load(moviePosterUrl)
                    .placeholder(R.drawable.image_placeholder)
                    .error(R.drawable.no_image_placeholder)
                    .into(movieListAdapterViewHolder.mImageViewPoster);
            }
        }
    }

    /**
     * Returns number of items in the fetched movie list
     * @return number of movie items
     */
    @Override
    public int getItemCount() {
        return (mMovieResponse == null) ? 0 : mMovieResponse.getMovieList().size();
    }

    /**
     * Method used to refresh the movie list once the MovieListAdapter is
     * already created, to avoid creating a new MovieListAdapter
     * @param movieResponse - the new movie set to be displayed
     */
    public void setMovieData(MovieResponse movieResponse) {
        mMovieResponse = movieResponse;
        mMovieList = mMovieResponse.getMovieList();
        notifyDataSetChanged();
    }
}
