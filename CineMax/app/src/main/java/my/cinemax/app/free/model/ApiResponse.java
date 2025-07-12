package my.cinemax.app.free.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ApiResponse {
    @SerializedName("Categories")
    private List<Category> categories;

    public List<Category> getCategories() {
        return categories;
    }
}
