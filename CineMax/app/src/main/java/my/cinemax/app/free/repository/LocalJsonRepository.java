package my.cinemax.app.free.repository;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import my.cinemax.app.free.entity.Channel;
// import my.cinemax.app.free.entity.Movie; // Assuming a Movie specific class might be useful, or use Poster - REMOVED
import my.cinemax.app.free.entity.Poster; // General purpose for movies and series items
// import my.cinemax.app.free.entity.Serie;  // Assuming a Serie specific class might be useful, or use Poster - REMOVED
import my.cinemax.app.free.entity.Season;
import my.cinemax.app.free.entity.Episode;
import my.cinemax.app.free.entity.Genre;
import my.cinemax.app.free.entity.Category;
import my.cinemax.app.free.entity.Country;


// Helper class to match the structure of data.json
class DataWrapper {
    List<Poster> movies;
    List<Poster> series; // Using Poster for series list items as well, detailed view might differ
    List<Channel> channels;
    // Add seasons if they are globally accessible, or handle them nested under series.
    // For now, assuming seasons are part of the series object.
}

public class LocalJsonRepository {

    private List<Poster> allMovies = new ArrayList<>();
    private List<Poster> allSeries = new ArrayList<>();
    private List<Channel> allChannels = new ArrayList<>();
    private Map<Integer, Poster> moviesMap = new HashMap<>();
    private Map<Integer, Poster> seriesMap = new HashMap<>();
    private Map<Integer, Channel> channelsMap = new HashMap<>();
    // It might be beneficial to map seasons directly if frequently accessed by ID,
    // or keep them nested within their respective series.
    // For now, seasons will be retrieved from their parent series.

    public LocalJsonRepository(Context context) {
        loadJsonData(context);
    }

    private void loadJsonData(Context context) {
        Gson gson = new Gson();
        try {
            InputStream is = context.getAssets().open("data.json");
            InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
            DataWrapper dataWrapper = gson.fromJson(reader, DataWrapper.class);
            reader.close();

            if (dataWrapper != null) {
                if (dataWrapper.movies != null) {
                    allMovies = dataWrapper.movies;
                    for (Poster movie : allMovies) {
                        moviesMap.put(movie.getId(), movie);
                    }
                }
                if (dataWrapper.series != null) {
                    allSeries = dataWrapper.series;
                    for (Poster serie : allSeries) {
                        seriesMap.put(serie.getId(), serie);
                        // If seasons are directly within the series Poster object (as defined in data.json structure)
                        // they are loaded implicitly. No separate mapping here unless needed for performance.
                    }
                }
                if (dataWrapper.channels != null) {
                    allChannels = dataWrapper.channels;
                    for (Channel channel : allChannels) {
                        channelsMap.put(channel.getId(), channel);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            // Handle error, perhaps log or throw a runtime exception
            // For now, lists will remain empty if loading fails.
        }
    }

    public List<Poster> getMovies() {
        return allMovies;
    }

    public List<Poster> getSeries() {
        return allSeries;
    }

    public List<Channel> getChannels() {
        return allChannels;
    }

    public Poster getMovieById(int id) {
        return moviesMap.get(id);
    }

    public Poster getSerieById(int id) {
        // This will return the Poster object representing the series.
        // The Poster object itself should contain its seasons and episodes as per data.json
        return seriesMap.get(id);
    }

    public Channel getChannelById(int id) {
        return channelsMap.get(id);
    }

    public List<Season> getSeasonsBySerieId(int serieId) {
        Poster serie = seriesMap.get(serieId);
        if (serie != null && serie.getSeasons() != null) { // Assuming Poster has getSeasons()
            return serie.getSeasons();
        }
        return new ArrayList<>(); // Return empty list if not found or no seasons
    }

    // We might need methods to get Episodes, but they are nested.
    // Access them via getSerieById(id).getSeasons().get(seasonIndex).getEpisodes()
    // Or provide a more direct method if commonly needed:
    public List<Episode> getEpisodesBySeasonId(int serieId, int seasonId) {
        Poster serie = seriesMap.get(serieId);
        if (serie != null && serie.getSeasons() != null) {
            for (Season season : serie.getSeasons()) {
                if (season.getId() == seasonId) {
                    return season.getEpisodes();
                }
            }
        }
        return new ArrayList<>();
    }

    // Mocking some other methods that might be called by UI, returning empty lists or defaults
    // These would need more complex filtering logic if they are to be fully implemented.

    public List<Poster> getRandomMovies(String genresCsv) {
        // Simple random: take first 5 if available, ignoring genres for now
        if (allMovies.size() > 5) {
            return new ArrayList<>(allMovies.subList(0, 5));
        }
        return allMovies;
    }

    public List<Channel> getRandomChannels(String categoriesCsv) {
        // Simple random: take first 5 if available, ignoring categories for now
        if (allChannels.size() > 5) {
            return new ArrayList<>(allChannels.subList(0, 5));
        }
        return allChannels;
    }

    public List<Poster> getMoviesByFilters(Integer genreId, String order, Integer page) {
        // Basic implementation: return all movies, ignoring filters and pagination for now
        // A real implementation would filter by genreId, sort by order, and implement pagination
        return allMovies;
    }

    public List<Poster> getSeriesByFilters(Integer genreId, String order, Integer page) {
        // Basic implementation: return all series, ignoring filters and pagination
        return allSeries;
    }

    public List<Channel> getChannelsByFilters(Integer categoryId, Integer countryId, Integer page) {
        // Basic implementation: return all channels, ignoring filters and pagination
        return allChannels;
    }

    // Dummy data for genres, categories, countries if needed, or they can be extracted from content
    public List<Genre> getGenreList() {
        // This could be dynamically generated from all movie/series genres
        // Or have a predefined list if data.json doesn't guarantee all are present
        Map<Integer, Genre> genres = new HashMap<>();
        for (Poster movie : allMovies) {
            if (movie.getGenres() != null) {
                for (Genre genre : movie.getGenres()) {
                    genres.put(genre.getId(), genre);
                }
            }
        }
        for (Poster serie : allSeries) {
            if (serie.getGenres() != null) {
                for (Genre genre : serie.getGenres()) {
                    genres.put(genre.getId(), genre);
                }
            }
        }
        return new ArrayList<>(genres.values());
    }

    public List<Category> getCategoriesList() {
        Map<Integer, Category> categories = new HashMap<>();
        for (Channel channel : allChannels) {
            if (channel.getCategories() != null) {
                for (Category category : channel.getCategories()) {
                    categories.put(category.getId(), category);
                }
            }
        }
        return new ArrayList<>(categories.values());
    }

    public List<Country> getCountriesList() {
         Map<Integer, Country> countries = new HashMap<>();
        for (Channel channel : allChannels) {
            if (channel.getCountries() != null) {
                for (Country country : channel.getCountries()) {
                    countries.put(country.getId(), country);
                }
            }
        }
        return new ArrayList<>(countries.values());
    }

    // Home data can be a composite of some movies, series, and channels
    // For simplicity, returning all for now. A real app might have specific logic.
    public DataWrapper getHomeData() {
        DataWrapper homeData = new DataWrapper();
        homeData.movies = getMovies().size() > 5 ? new ArrayList<>(getMovies().subList(0, Math.min(getMovies().size(), 5))) : getMovies();
        homeData.series = getSeries().size() > 5 ? new ArrayList<>(getSeries().subList(0, Math.min(getSeries().size(), 5))) : getSeries();
        homeData.channels = getChannels().size() > 5 ? new ArrayList<>(getChannels().subList(0, Math.min(getChannels().size(), 5))) : getChannels();
        return homeData;
    }

    // Search: a simple title-based search for now
    public DataWrapper searchData(String query) {
        DataWrapper searchResult = new DataWrapper();
        List<Poster> foundMovies = new ArrayList<>();
        List<Poster> foundSeries = new ArrayList<>();
        List<Channel> foundChannels = new ArrayList<>();
        String lowerQuery = query.toLowerCase();

        for (Poster movie : allMovies) {
            if (movie.getTitle().toLowerCase().contains(lowerQuery)) {
                foundMovies.add(movie);
            }
        }
        for (Poster serie : allSeries) {
            if (serie.getTitle().toLowerCase().contains(lowerQuery)) {
                foundSeries.add(serie);
            }
        }
        for (Channel channel : allChannels) {
            if (channel.getTitle().toLowerCase().contains(lowerQuery)) {
                foundChannels.add(channel);
            }
        }
        searchResult.movies = foundMovies;
        searchResult.series = foundSeries;
        searchResult.channels = foundChannels;
        return searchResult;
    }
}
