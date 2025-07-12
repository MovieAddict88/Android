package my.cinemax.app.free.network;

import my.cinemax.app.free.model.ApiResponse;
import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {
    @GET("cini/pagsure.json")
    Call<ApiResponse> getCategories();
}
