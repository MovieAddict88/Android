package my.cinemax.app.free.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Episode {
    @SerializedName("Episode")
    private int episode;

    @SerializedName("Title")
    private String title;

    @SerializedName("Duration")
    private String duration;

    @SerializedName("Description")
    private String description;

    @SerializedName("Thumbnail")
    private String thumbnail;

    @SerializedName("Servers")
    private List<Server> servers;

    public int getEpisode() {
        return episode;
    }

    public String getTitle() {
        return title;
    }

    public String getDuration() {
        return duration;
    }

    public String getDescription() {
        return description;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public List<Server> getServers() {
        return servers;
    }
}
