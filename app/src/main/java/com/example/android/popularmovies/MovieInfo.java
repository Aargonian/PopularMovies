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

    public long id;
    public String title;
    public String overview;
    public Bitmap poster;
    public String releaseDate;
    public String posterPath;
    public int runTime;
    public int vote_count;
    public double rating;
    public double popularity;
    public String[] genres;

    private MovieInfo()
    {
    }

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
            return movie.id >= 0 && !(movie.title == null || movie.title.isEmpty());
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
    }

    @Override
    public int compareTo(@NonNull MovieInfo other)
    {
        if(this.rating < other.rating) {
            return 1;
        }
        else if(this.rating > other.rating) {
            return -1;
        }
        else {
            if(this.vote_count < other.vote_count) {
                return 1;
            } else if (this.vote_count > other.vote_count) {
                return -1;
            } else {
                return 0;
            }
        }
    }

    public void applyPoster(ImageView view)
    {
        view.setImageBitmap(poster);
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

    public static class MalformedMovieException extends RuntimeException {
        public MalformedMovieException(String message) {
            super(message);
        }
    }
}
