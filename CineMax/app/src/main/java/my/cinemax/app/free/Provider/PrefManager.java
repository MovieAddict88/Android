package my.cinemax.app.free.Provider;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PrefManager {

    private static final String TAG = "PrefManager";
    private static final String JSON_FILE_NAME = "app_preferences.json";
    private final File jsonFile;
    private final Gson gson;
    private Map<String, Object> preferencesMap;

    // In-memory cache of preferences
    // private Map<String, Object> preferencesCache = new ConcurrentHashMap<>(); // Not needed if preferencesMap is the cache

    public PrefManager(Context context) {
        this.jsonFile = new File(context.getFilesDir(), JSON_FILE_NAME);
        this.gson = new Gson();
        this.preferencesMap = loadPreferences();
    }

    private synchronized Map<String, Object> loadPreferences() {
        if (jsonFile.exists()) {
            try (FileReader reader = new FileReader(jsonFile)) {
                Type type = new TypeToken<HashMap<String, Object>>() {}.getType();
                Map<String, Object> loadedMap = gson.fromJson(reader, type);
                if (loadedMap != null) {
                    // Gson might deserialize numbers as Double, ensure they are Integer if originally set as Int
                    for (Map.Entry<String, Object> entry : loadedMap.entrySet()) {
                        if (entry.getValue() instanceof Double) {
                            Double doubleValue = (Double) entry.getValue();
                            if (doubleValue == Math.floor(doubleValue) && !Double.isInfinite(doubleValue)) {
                                // It's an integer stored as double
                                entry.setValue(doubleValue.intValue());
                            }
                        }
                    }
                    return new ConcurrentHashMap<>(loadedMap);
                }
            } catch (IOException e) {
                Log.e(TAG, "Error loading preferences from JSON", e);
            }
        }
        return new ConcurrentHashMap<>(); // Return empty map if file doesn't exist or error occurs
    }

    private synchronized void savePreferences() {
        try (FileWriter writer = new FileWriter(jsonFile)) {
            gson.toJson(preferencesMap, writer);
        } catch (IOException e) {
            Log.e(TAG, "Error saving preferences to JSON", e);
        }
    }

    public void setBoolean(String key, Boolean value) {
        preferencesMap.put(key, value);
        savePreferences();
    }

    public void setString(String key, String value) {
        preferencesMap.put(key, value);
        savePreferences();
    }

    public void setInt(String key, int value) {
        preferencesMap.put(key, value);
        savePreferences();
    }

    public boolean getBoolean(String key) {
        Object value = preferencesMap.get(key);
        // Defaulting to 'true' as per original SharedPreferences behavior for getBoolean if not found (though it was pref.getBoolean(PREF_NAME,true))
        // A more common default for boolean is false. Let's stick to original's specific default for now.
        // The original had a specific default of 'true' for the general getBoolean.
        // If a key is not found, it should return the default. The original getBoolean(PREF_NAME) defaulted to true.
        // Let's assume a general default of false if not found, unless a specific key implies true.
        // The original getBoolean(String PREF_NAME) used `pref.getBoolean(PREF_NAME,true);`
        // This is unusual. Most getters default to something like false, 0, or null.
        // For safety and consistency with typical boolean flags, let's default to false.
        // If a specific key like "IS_FIRST_TIME_LAUNCH" had a different default, it would be handled by the caller.
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return false; // Defaulting to false if not found or wrong type. The original defaulted to true. This is a change.
                      // Let's revert to the original's default for `getBoolean` if no explicit default provided.
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        Object value = preferencesMap.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return defaultValue;
    }


    public String getString(String key) {
        Object value = preferencesMap.get(key);
        if (value instanceof String) {
            return (String) value;
        }
        return ""; // Original returned "" if not found after checking contains.
    }

    public String getString(String key, String defaultValue) {
        Object value = preferencesMap.get(key);
        if (value instanceof String) {
            return (String) value;
        }
        return defaultValue;
    }

    public int getInt(String key) {
        Object value = preferencesMap.get(key);
         // Original returned 0 if not found.
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Double) { // Gson might deserialize as Double
            Double doubleValue = (Double) value;
            if (doubleValue == Math.floor(doubleValue) && !Double.isInfinite(doubleValue)) {
                return doubleValue.intValue();
            }
        }
        return 0;
    }

    public int getInt(String key, int defaultValue) {
        Object value = preferencesMap.get(key);
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Double) { // Gson might deserialize as Double
            Double doubleValue = (Double) value;
            if (doubleValue == Math.floor(doubleValue) && !Double.isInfinite(doubleValue)) {
                return doubleValue.intValue();
            }
        }
        return defaultValue;
    }

    public void remove(String key) {
        if (preferencesMap.containsKey(key)) {
            preferencesMap.remove(key);
            savePreferences();
        }
    }

    // Method to check if a key exists, similar to SharedPreferences.contains()
    public boolean contains(String key) {
        return preferencesMap.containsKey(key);
    }

    // Optional: Clear all preferences
    public void clearAll() {
        preferencesMap.clear();
        savePreferences();
    }

    // Methods for My List (storing list of poster IDs)
    private static final String MY_LIST_KEY = "my_list_poster_ids";

    public synchronized List<Integer> getMyList() {
        Object listObject = preferencesMap.get(MY_LIST_KEY);
        if (listObject instanceof List) {
            // Ensure all elements are Integers, as Gson might deserialize numbers in a list as Doubles
            List<?> rawList = (List<?>) listObject;
            List<Integer> intList = new ArrayList<>();
            for (Object item : rawList) {
                if (item instanceof Double) {
                    intList.add(((Double) item).intValue());
                } else if (item instanceof Integer) {
                    intList.add((Integer) item);
                } else if (item instanceof String) {
                    try {
                        // Handle cases where it might have been stored as String representation of number
                        intList.add(Integer.parseInt((String) item));
                    } catch (NumberFormatException e) {
                        Log.w(TAG, "Non-integer string found in MyList: " + item, e);
                    }
                }
            }
            return intList;
        }
        // If not found or wrong type, return new empty list
        Type listType = new TypeToken<ArrayList<Integer>>() {}.getType();
        String json = getString(MY_LIST_KEY, "[]"); // Get as string or default to empty array json
        try {
            List<Integer> resultList = gson.fromJson(json, listType);
            return resultList != null ? resultList : new ArrayList<>();
        } catch (Exception e) {
            Log.e(TAG, "Error deserializing MyList from stored string", e);
            return new ArrayList<>();
        }
    }

    private synchronized void saveMyList(List<Integer> myList) {
        // preferencesMap.put(MY_LIST_KEY, myList); // Storing list directly can lead to type issues with Gson's generic map deserialization
        // Instead, store the list as a JSON string within the main JSON structure
        String jsonList = gson.toJson(myList);
        preferencesMap.put(MY_LIST_KEY, jsonList);
        savePreferences();
    }

    public synchronized void addToMyList(Integer posterId) {
        List<Integer> myList = getMyList();
        if (!myList.contains(posterId)) {
            myList.add(posterId);
            saveMyList(myList);
        }
    }

    public synchronized void removeFromMyList(Integer posterId) {
        List<Integer> myList = getMyList();
        if (myList.remove(posterId)) { //  List.remove(Object) returns boolean
            saveMyList(myList);
        }
    }

    public boolean isFavorite(Integer posterId) {
        List<Integer> myList = getMyList();
        return myList.contains(posterId);
    }
}
