package com.example.android.popularmoviesstagetwo.controllers;

import com.example.android.popularmoviesstagetwo.models.Movie;
import com.example.android.popularmoviesstagetwo.models.MovieResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Retrofit Interface for TMDb API
 * The purpose of this interface is to define the endpoints, that include details of
 * request methods (e.g. GET, POST, etc.) and parameters.
 *
 * Created by aditibhattacharya on 23/11/2017.
 */

public interface MovieApiInterface {

    /**
     * Requests details of a specific movie by ID
     * @param id - as found in TMDB Movie List
     * @param apiKey - individual API key
     * @return {@link Movie} object
     */
    @GET("movie/{id}")
    Call<Movie> getMovieDetails(@Path("id") int id, @Query("api_key") String apiKey);

    /**
     * Requests list of popular movies
     * @param apiKey - individual API key
     * @return List of movies sorted by preference (popular or top rated)
     */
    @GET("movie/{preference}")
    Call<MovieResponse> getMovieList(@Path("preference") String preference, @Query("api_key") String apiKey);

}
