package my.cinemax.app.free.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import my.cinemax.app.free.R;
import my.cinemax.app.free.model.ApiResponse;
import my.cinemax.app.free.model.Category;
import my.cinemax.app.free.network.ApiClient;
import my.cinemax.app.free.network.ApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class CategoryFragment extends Fragment {

    private static final String ARG_CATEGORY_TYPE = "category_type";
    private RecyclerView recyclerView;
    private String categoryType;
    private ContentAdapter adapter;
    private List<Entry> entries;

    public static CategoryFragment newInstance(String categoryType) {
        CategoryFragment fragment = new CategoryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY_TYPE, categoryType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categoryType = getArguments().getString(ARG_CATEGORY_TYPE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_category, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<ApiResponse> call = apiService.getCategories();
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (Category category : response.body().getCategories()) {
                        if (category.getMainCategory().equals(categoryType)) {
                            entries = category.getEntries();
                            adapter = new ContentAdapter(getContext(), entries);
                            recyclerView.setAdapter(adapter);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Timber.e(t);
            }
        });
    }

    public void filter(String query) {
        if (adapter != null) {
            adapter.getFilter().filter(query);
        }
    }
}
