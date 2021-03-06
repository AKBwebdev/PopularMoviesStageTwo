package com.example.android.popularmoviesstagetwo.fragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import androidx.core.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.core.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.example.android.popularmoviesstagetwo.DetailActivity;
import com.example.android.popularmoviesstagetwo.R;
import com.example.android.popularmoviesstagetwo.adapters.ReviewAdapter;
import com.example.android.popularmoviesstagetwo.controllers.MovieApiController;
import com.example.android.popularmoviesstagetwo.controllers.MovieApiInterface;
import com.example.android.popularmoviesstagetwo.exceptions.NoConnectivityException;
import com.example.android.popularmoviesstagetwo.models.Movie;
import com.example.android.popularmoviesstagetwo.models.MovieReview;
import com.example.android.popularmoviesstagetwo.models.MovieReviewResponse;
import com.example.android.popularmoviesstagetwo.utils.BuildConfig;
import com.example.android.popularmoviesstagetwo.utils.UtilDialog;
import com.example.android.popularmoviesstagetwo.utils.Utils;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MovieReviewFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private static final String LOG_TAG = MovieReviewFragment.class.getSimpleName();

    private DetailActivity mParentActivity;
    private View mViewFragment;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private ReviewAdapter mAdapter;
    private ProgressBar mLoadingIndicator;
    private TextView mTextViewEmptyList;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private final Movie mMovie = DetailActivity.sCurrentMovie;
    private MovieReviewResponse mMovieReviewResponse;
    private List<MovieReview> mMovieReviewList;
    private boolean mIsDialogVisible = false;
    private String mErrorMessage;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mParentActivity = (DetailActivity) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        // Inflate view object
        mViewFragment = inflater.inflate(R.layout.fragment_review, container, false);
        mLoadingIndicator = mViewFragment.findViewById(R.id.progress_indicator);
        mTextViewEmptyList = mViewFragment.findViewById(R.id.text_empty_list);

        // Enable layout for SwipeRefresh
        mSwipeRefreshLayout = mViewFragment.findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(mParentActivity, R.color.colorAccent));

        // Set RecyclerView Layout
        initRecyclerViewLayout();

        // Couple RecyclerView with Adapter
        mAdapter = new ReviewAdapter(mMovieReviewResponse, mMovie);
        mRecyclerView.setAdapter(mAdapter);

        // Load list data
        loadReviewData();

        return mViewFragment;
    }

    /**
     * Method to initialize RecyclerView Layout  in LinearLayout mode, to be used to display the list items
     */
    public void initRecyclerViewLayout() {
        mLayoutManager = new LinearLayoutManager(mParentActivity);
        mRecyclerView = mViewFragment.findViewById(R.id.list_reviews);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mAdapter);
    }

    /**
     * Method to load movie review data into the adapter and display in the RecyclerView layout
     * Displays alert messages if
     * (1) API key is found missing
     * (2) There is no connectivity
     * (3) A failure happened while fetching/loading movie data from the API
     */
    public void loadReviewData() {

        if (Utils.isEmptyString(BuildConfig.API_KEY)) {
            mSwipeRefreshLayout.setRefreshing(false);
            mIsDialogVisible = UtilDialog.showDialog(getString(R.string.alert_api_key_missing), mParentActivity);
            mErrorMessage = new StringBuilder()
                    .append(getString(R.string.alert_api_key_missing))
                    .append(getString(R.string.error_review_fetch_failed))
                    .toString();

            showHideEmptyListMessage(true);
            return;
        }

        try {
            mSwipeRefreshLayout.setRefreshing(false);
            mLoadingIndicator.setVisibility(View.VISIBLE);

            MovieApiInterface apiInterface = MovieApiController.
                    getClient(mParentActivity).
                    create(MovieApiInterface.class);

            final Call<MovieReviewResponse> responseCall =
                    apiInterface.getMovieReviewList(mMovie.getId(), BuildConfig.API_KEY);

            responseCall.enqueue(new Callback<MovieReviewResponse>() {
                @Override
                public void onResponse(Call<MovieReviewResponse> call, Response<MovieReviewResponse> response) {
                    int statusCode = response.code();
                    if (response.isSuccessful()) {
                        mLoadingIndicator.setVisibility(View.INVISIBLE);
                        mMovieReviewResponse = response.body();
                        mAdapter.setReviewData(mMovieReviewResponse);
                        if (mAdapter.getItemCount() > 1) {
                            mAdapter.notifyDataSetChanged();
                            mMovieReviewList = mMovieReviewResponse.getMovieReviewList();
                            mErrorMessage = "";
                            showHideEmptyListMessage(false);
                        } else {
                            mErrorMessage = getString(R.string.alert_no_reviews);
                            showHideEmptyListMessage(true);
                        }
                    } else {
                        mLoadingIndicator.setVisibility(View.INVISIBLE);
                        mIsDialogVisible = UtilDialog
                                .showDialog(getString(R.string.error_review_load_failed) + statusCode, mParentActivity);
                        mErrorMessage = getString(R.string.error_review_fetch_failed);
                        showHideEmptyListMessage(true);
                    }
                }

                @Override
                public void onFailure(Call<MovieReviewResponse> call, Throwable t) {
                    mLoadingIndicator.setVisibility(View.INVISIBLE);
                    mIsDialogVisible = UtilDialog.showDialog(getString(R.string.error_review_fetch_failed), mParentActivity);
                    mErrorMessage = getString(R.string.error_review_fetch_failed);
                    showHideEmptyListMessage(true);
                }
            });
        } catch (NoConnectivityException nce) {
            mLoadingIndicator.setVisibility(View.INVISIBLE);
            mIsDialogVisible = UtilDialog.showDialog(getString(R.string.error_no_connection), mParentActivity);
            mErrorMessage = getString(R.string.error_review_fetch_failed);
            showHideEmptyListMessage(true);
        }
    }

    /**
     * Method to show and hide empty list message
     * @param isEmptyList
     */
    public void showHideEmptyListMessage(boolean isEmptyList) {
        // When no list items are displayed
        if (isEmptyList) {
            mTextViewEmptyList.setText(mErrorMessage);
            mTextViewEmptyList.setVisibility(View.VISIBLE);
        } else { // When list items are displayed
            mTextViewEmptyList.setText("");
            mTextViewEmptyList.setVisibility(View.GONE);
        }
    }

    /**
     * Refreshes movie list when screen is swiped
     */
    @Override
    public void onRefresh() {
        loadReviewData();
    }

    /**
     * Method invoked when the activity is destroyed, e.g. in an event when screen is rotated
     * Checks if the dialog is visible; if yes it dismisses the dialog before re-creating it
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mIsDialogVisible) {
            UtilDialog.dismissDialog();
            mIsDialogVisible = false;
        }
    }

}
