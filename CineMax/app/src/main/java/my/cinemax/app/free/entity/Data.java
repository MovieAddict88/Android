package my.cinemax.app.free.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Data {


    @SerializedName("slides")
    @Expose
    private List<Slide> slides = null;

    @SerializedName("channels")
    @Expose
    private List<Channel> channels = null;

    // actors list removed

    @SerializedName("movies") // Changed from posters
    @Expose
    private List<Poster> movies = null;

    @SerializedName("series") // Added for series
    @Expose
    private List<Poster> series = null;

    @SerializedName("genres")
    @Expose
    private List<Genre> genres = null;

    @SerializedName("genre")
    @Expose
    private Genre genre;

    private int viewType = 1;

    public List<Slide> getSlides() {
        return slides;
    }

    public void setSlides(List<Slide> slides) {
        this.slides = slides;
    }


    public void setChannels(List<Channel> channels) {
        this.channels = channels;
    }

    // setActors removed

    public List<Channel> getChannels() {
        return channels;
    }

    // getActors removed

    public void setGenre(Genre genre) {
        this.genre = genre;
    }

    public Genre getGenre() {
        return genre;
    }

    public List<Genre> getGenres() {
        return genres;
    }

    public void setGenres(List<Genre> genres) {
        this.genres = genres;
    }

    public Data setViewType(int viewType) {
        this.viewType = viewType;
        return this;
    }
    public int getViewType() {
        return viewType;
    }

    public List<Poster> getMovies() {
        return movies;
    }

    public void setMovies(List<Poster> movies) {
        this.movies = movies;
    }

    public List<Poster> getSeries() {
        return series;
    }

    public void setSeries(List<Poster> series) {
        this.series = series;
    }
}
