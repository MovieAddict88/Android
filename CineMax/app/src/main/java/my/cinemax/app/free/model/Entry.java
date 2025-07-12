package my.cinemax.app.free.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Entry {
    @SerializedName("Title")
    private String title;

    @SerializedName("SubCategory")
    private String subCategory;

    @SerializedName("Country")
    private String country;

    @SerializedName("Description")
    private String description;

    @SerializedName("Poster")
    private String poster;

    @SerializedName("Thumbnail")
    private String thumbnail;

    @SerializedName("Rating")
    private float rating;

    @SerializedName("Duration")
    private String duration;

    @SerializedName("Year")
    private int year;

    @SerializedName("Servers")
    private List<Server> servers;

    @SerializedName("Seasons")
    private List<Season> seasons;

    public String getTitle() {
        return title;
    }

    public String getSubCategory() {
        return subCategory;
    }

    public String getCountry() {
        return country;
    }

    public String getDescription() {
        return description;
    }

    public String getPoster() {
        return poster;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public float getRating() {
        return rating;
    }

    public String getDuration() {
        return duration;
    }

    public int getYear() {
        return year;
    }

    public List<Server> getServers() {
        return servers;
    }

    public List<Season> getSeasons() {
        return seasons;
    }
}
