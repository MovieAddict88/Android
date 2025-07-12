package my.cinemax.app.free.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Season {
    @SerializedName("Season")
    private int season;

    @SerializedName("SeasonPoster")
    private String seasonPoster;

    @SerializedName("Episodes")
    private List<Episode> episodes;

    public int getSeason() {
        return season;
    }

    public String getSeasonPoster() {
        return seasonPoster;
    }

    public List<Episode> getEpisodes() {
        return episodes;
    }
}
