package my.cinemax.app.free.repository;

import android.content.Context;
import android.content.res.AssetManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;

import my.cinemax.app.free.entity.Data;

public class LocalDataRepository {

    private static final String TAG = "LocalDataRepository";

    public Data loadData(Context context) {
        AssetManager assetManager = context.getAssets();
        try (InputStream inputStream = assetManager.open("data.json");
             InputStreamReader reader = new InputStreamReader(inputStream)) {

            Gson gson = new Gson();
            Type dataType = new TypeToken<Data>() {}.getType();
            Data data = gson.fromJson(reader, dataType);
            if (data != null) {
                // Optionally, you can map the JSON structure to the slightly different Data.java structure here if needed
                // For example, if data.json has "movies" and "series" lists, but Data.java expects them in "posters"
                // For now, assuming Data.java will be adapted or the JSON matches its expected direct fields (e.g. it has channels, posters, slides, genres)

                // Log success or basic data stats if needed for debugging
                // android.util.Log.d(TAG, "Successfully loaded data.json. Slides: " + (data.getSlides() != null ? data.getSlides().size() : 0));
            }
            return data;
        } catch (IOException e) {
            android.util.Log.e(TAG, "Error reading data.json from assets", e);
            // You could return null or an empty Data object, or throw a custom exception
            return null;
        } catch (com.google.gson.JsonSyntaxException e) {
            android.util.Log.e(TAG, "Error parsing data.json", e);
            return null;
        }
    }

    // We might need to create a new root object that matches data.json structure exactly first,
    // then map it to the existing Data.java object or adapt Data.java.
    // For now, this assumes Data.java can deserialize the root of data.json.
    // If Data.java itself doesn't match the new JSON (e.g. it expects 'posters'
    // but JSON has 'movies' and 'series'), we'll need an intermediate parsing step or
    // new POJO classes that match data.json structure.

    // Let's assume for now that Data.java will be compatible enough,
    // or we will adjust it in the next step if direct parsing fails.
    // The `data.json` was designed to have `slides`, `channels`, `genres` at the root
    // which `Data.java` has (or had).
    // The `posters` field in `Data.java` would need to be populated from `movies` and `series`
    // from the JSON. This part needs careful handling.

    // For a cleaner approach, let's define a new RootJsonData class that exactly matches data.json
    // and then map from RootJsonData to the existing Data Pojo.
}
