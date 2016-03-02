package com.example.android.popularmovies;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.widget.ImageView;

/**
 * Created by Aaron Helton on 1/30/2016
 */
public class MovieInfo implements Comparable<MovieInfo>
{
    @SuppressWarnings("unused")
    private static final String LOG_TAG = MovieInfo.class.getSimpleName();

    private long id;
    private String title;
    private String overview;
    private Bitmap poster;
    private String releaseDate;
    private String posterPath;
    private int runTime;
    private int vote_count;
    private double rating;
    private double popularity;
    private String[] genres;
    private String[] trailers;
    private String[] reviews;

    private MovieInfo(){}

    public static MovieBuilder buildMovie() {
        return new MovieBuilder();
    }

    public static final class MovieBuilder {
        MovieInfo movie;
        public MovieBuilder() {
            movie = new MovieInfo();
        }

        public MovieInfo build() {
            if(isValid())
                return movie;
            else {
                throw new MalformedMovieException("MovieInfo must have an ID and a Title!");
            }
        }

        public boolean isValid() {
            return movie.getId() >= 0 && !(movie.getTitle() == null || movie.getTitle().isEmpty());
        }

        public MovieBuilder withId(Long id) {
            movie.id = id;
            return this;
        }

        public MovieBuilder withTitle(String title) {
            movie.title = title;
            return this;
        }

        public MovieBuilder withRuntime(int runTime) {
            movie.runTime = runTime;
            return this;
        }

        public MovieBuilder withOverview(String overview) {
            movie.overview = overview;
            return this;
        }

        public MovieBuilder withPoster(Bitmap poster) {
            movie.poster = poster;
            return this;
        }

        public MovieBuilder withReleaseDate(String date)
        {
            movie.releaseDate = date;
            return this;
        }

        public MovieBuilder withVoteCount(int votes)
        {
            movie.vote_count = votes;
            return this;
        }

        public MovieBuilder withRating(double rating)
        {
            movie.rating = rating;
            return this;
        }

        public MovieBuilder withGenres(String[] genres)
        {
            movie.genres = genres;
            return this;
        }

        public MovieBuilder withPopularity(double popularity)
        {
            movie.popularity = popularity;
            return this;
        }

        public MovieBuilder withPosterPath(String path)
        {
            movie.posterPath = path;
            return this;
        }

        public MovieBuilder withTrailers(String[] trailerList)
        {
            movie.trailers = trailerList;
            return this;
        }

        public MovieBuilder withReviews(String[] reviews)
        {
            movie.reviews = reviews;
            return this;
        }
    }

    @Override
    public int compareTo(@NonNull MovieInfo other)
    {
        if(this.getRating() < other.getRating()) {
            return 1;
        }
        else if(this.getRating() > other.getRating()) {
            return -1;
        }
        else {
            if(this.getVoteCount() < other.getVoteCount()) {
                return 1;
            } else if (this.getVoteCount() > other.getVoteCount()) {
                return -1;
            } else {
                return 0;
            }
        }
    }

    public void applyPoster(ImageView view)
    {
        view.setImageBitmap(getPoster());
    }

    public Long getId() { return id; }

    public String getTitle() {
        return title;
    }

    public String getOverview() {
        return overview;
    }

    public Bitmap getPoster() {
        return poster;
    }

    public String getPosterPath() { return posterPath; }

    public String getReleaseDate() {
        return releaseDate;
    }

    public Integer getRunTime() {
        return runTime;
    }

    public Double getPopularity() { return popularity; }

    public Double getRating() {
        return rating;
    }

    public Integer getVoteCount() { return vote_count; }

    public String[] getGenres() {
        return genres;
    }

    public String[] getReviews() { return reviews; }

    public String[] getTrailers() { return trailers; }

    public static class MalformedMovieException extends RuntimeException {
        public MalformedMovieException(String message) {
            super(message);
        }
    }
}
