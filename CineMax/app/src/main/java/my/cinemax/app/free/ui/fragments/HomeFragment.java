package my.cinemax.app.free.ui.fragments;


import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import my.cinemax.app.free.Provider.PrefManager;
import my.cinemax.app.free.R;
// import my.cinemax.app.free.api.apiClient; // Replaced
// import my.cinemax.app.free.api.apiRest; // Replaced
import my.cinemax.app.free.repository.LocalJsonRepository; // Added
import my.cinemax.app.free.repository.DataWrapper; // Added
import my.cinemax.app.free.entity.Data;
import my.cinemax.app.free.entity.Genre;
import my.cinemax.app.free.entity.Poster; // Added
import my.cinemax.app.free.entity.Channel; // Added
import my.cinemax.app.free.ui.Adapters.HomeAdapter;

import java.util.ArrayList;
import java.util.List;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {


    private View view;
    private SwipeRefreshLayout swipe_refresh_layout_home_fragment;
    private LinearLayout linear_layout_load_home_fragment;
    private LinearLayout linear_layout_page_error_home_fragment;
    private RecyclerView recycler_view_home_fragment;
    private RelativeLayout relative_layout_load_more_home_fragment;
    private HomeAdapter homeAdapter;



    private Genre my_genre_list;
    private List<Data> dataList=new ArrayList<>();
    private GridLayoutManager gridLayoutManager;
    private Button button_try_again;


    private Integer lines_beetween_ads = 2 ; // Will be removed later
    private boolean tabletSize;
    private Boolean native_ads_enabled = false ; // Will be removed later
    private int type_ads = 0; // Will be removed later
    private PrefManager prefManager;
    private Integer item = 0 ; // Will be removed later

    private LocalJsonRepository localJsonRepository; // Added

    public HomeFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        this.view=  inflater.inflate(R.layout.fragment_home, container, false);
        prefManager= new PrefManager(getApplicationContext());
        localJsonRepository = new LocalJsonRepository(requireContext()); // Added

        initViews();
        initActions();
        loadData();
        return view;
    }

    private void loadData() {
        showLoadingView();
        dataList.clear();

        // Add a dummy item for search view or any other static top element if needed
        dataList.add(new Data().setViewType(0)); // This was for search bar in original

        DataWrapper homeData = localJsonRepository.getHomeData();

        if (homeData != null) {
            // Slides: Use first few movies/series as slides if desired
            // For simplicity, let's take up to 3 movies as slides for now.
            // The original HomeAdapter handles a List<Slide> in Data.setSlides().
            // We'll adapt Poster objects to look like Slides or modify HomeAdapter later.
            // For now, we create a Data item that could hold Poster items for a slider.
            List<Poster> slidePosters = new ArrayList<>();
            if (homeData.movies != null && !homeData.movies.isEmpty()) {
                for (int i = 0; i < Math.min(homeData.movies.size(), 3); i++) {
                    slidePosters.add(homeData.movies.get(i));
                }
            }
            if (!slidePosters.isEmpty()) {
                Data slideData = new Data();
                // We need to ensure HomeAdapter can handle List<Poster> for slides,
                // or convert Posters to Slide objects if Slide entity is very different.
                // Assuming Slide and Poster are compatible enough for a horizontal list:
                // slideData.setSlides(convertToSlideList(slidePosters)); // Requires a conversion method
                // For now, let's assume HomeAdapter's slide section can take List<Poster>
                // This might require adjustment in HomeAdapter or Data entity.
                // Or, if Slide is a very specific structure, we might skip this for now.
                // Let's create a new Data type for "Featured Movies" instead of "Slides"
                // to avoid conflict with the existing Slide structure if it's too different.
                // Or, more simply, add a "featured_movies" section to HomeAdapter.
                // Given the structure, let's assume we can make a Data item of Posters for slides.
                // Data class has `private List<Slide> slides = null;`
                // We will need to adapt Poster to Slide or create a new view type in HomeAdapter.
                // For now, let's add movies directly as a genre-like section.
            }

            // Channels
            if (homeData.channels != null && !homeData.channels.isEmpty()) {
                Data channelData = new Data();
                // The original Data object has setChannels(List<Channel> channels)
                // Our DataWrapper has List<Channel> channels. So this is compatible.
                channelData.setChannels(new ArrayList<>(homeData.channels)); // Create a new list to avoid modification issues
                dataList.add(channelData);
            }

            // Actors section will be removed.

            // Genres: Movies and Series grouped by Genre
            // The original code iterated through response.body().getGenres()
            // Each Genre object in that list itself contained a List<Poster>
            // We will fetch all genres and then for each genre, fetch its posters.
            List<Genre> allGenres = localJsonRepository.getGenreList();
            if (allGenres != null && !allGenres.isEmpty()) {
                for (Genre genre : allGenres) {
                    List<Poster> postersInGenre = new ArrayList<>();
                    // Filter movies by this genre
                    if (homeData.movies != null) {
                        for (Poster movie : homeData.movies) {
                            if (movie.getGenres() != null) {
                                for (Genre movieGenre : movie.getGenres()) {
                                    if (movieGenre.getId().equals(genre.getId())) {
                                        postersInGenre.add(movie);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    // Filter series by this genre
                     if (homeData.series != null) {
                        for (Poster serie : homeData.series) {
                            if (serie.getGenres() != null) {
                                for (Genre serieGenre : serie.getGenres()) {
                                    if (serieGenre.getId().equals(genre.getId())) {
                                        postersInGenre.add(serie);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    if (!postersInGenre.isEmpty()) {
                        Genre genreWithPosters = new Genre();
                        genreWithPosters.setId(genre.getId());
                        genreWithPosters.setTitle(genre.getTitle());
                        genreWithPosters.setPosters(postersInGenre); // Assuming Genre has setPosters

                        Data genreData = new Data();
                        genreData.setGenre(genreWithPosters);
                        dataList.add(genreData);
                    }
                }
            }

            // Add Latest Movies (if not covered by genres above or as a separate section)
            if (homeData.movies != null && !homeData.movies.isEmpty()) {
                 Data latestMoviesData = new Data();
                 Genre latestMoviesGenre = new Genre();
                 latestMoviesGenre.setTitle("Latest Movies"); // Use a special title
                 latestMoviesGenre.setPosters(new ArrayList<>(homeData.movies)); // Or a sublist
                 latestMoviesData.setGenre(latestMoviesGenre); // Re-using Genre section for adapter
                 dataList.add(latestMoviesData);
            }
            // Add Latest Series
            if (homeData.series != null && !homeData.series.isEmpty()) {
                 Data latestSeriesData = new Data();
                 Genre latestSeriesGenre = new Genre();
                 latestSeriesGenre.setTitle("Latest Series");
                 latestSeriesGenre.setPosters(new ArrayList<>(homeData.series));
                 latestSeriesData.setGenre(latestSeriesGenre);
                 dataList.add(latestSeriesData);
            }


            showListView();
            homeAdapter.notifyDataSetChanged();
        } else {
            showErrorView();
        }
    }

    private void showLoadingView(){
       linear_layout_load_home_fragment.setVisibility(View.VISIBLE);
       linear_layout_page_error_home_fragment.setVisibility(View.GONE);
       recycler_view_home_fragment.setVisibility(View.GONE);
   }
    private void showListView(){
        linear_layout_load_home_fragment.setVisibility(View.GONE);
        linear_layout_page_error_home_fragment.setVisibility(View.GONE);
        recycler_view_home_fragment.setVisibility(View.VISIBLE);
    }
    private void showErrorView(){
        linear_layout_load_home_fragment.setVisibility(View.GONE);
        linear_layout_page_error_home_fragment.setVisibility(View.VISIBLE);
        recycler_view_home_fragment.setVisibility(View.GONE);
    }
    private void initActions() {
        swipe_refresh_layout_home_fragment.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
                swipe_refresh_layout_home_fragment.setRefreshing(false);
            }
        });
        button_try_again.setOnClickListener(v->{
            loadData();
        });
    }
    public boolean checkSUBSCRIBED(){
        if (!prefManager.getString("SUBSCRIBED").equals("TRUE") && !prefManager.getString("NEW_SUBSCRIBE_ENABLED").equals("TRUE")) {
            return false;
        }
        return true;
    }
    private void initViews() {

        boolean tabletSize = getResources().getBoolean(R.bool.isTablet);
        if (!prefManager.getString("ADMIN_NATIVE_TYPE").equals("FALSE")){
            // native_ads_enabled=true; // Ads logic removed for now
            // if (tabletSize) {
            //     lines_beetween_ads=Integer.parseInt(prefManager.getString("ADMIN_NATIVE_LINES"));
            // }else{
            //     lines_beetween_ads=Integer.parseInt(prefManager.getString("ADMIN_NATIVE_LINES"));
            // }
        }
        // if (checkSUBSCRIBED()) { // Subscription check will be removed
        //     native_ads_enabled=false;
        // }
        native_ads_enabled=false; // Force disable ads

        this.swipe_refresh_layout_home_fragment = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout_home_fragment);
        this.linear_layout_load_home_fragment = (LinearLayout) view.findViewById(R.id.linear_layout_load_home_fragment);
        this.linear_layout_page_error_home_fragment = (LinearLayout) view.findViewById(R.id.linear_layout_page_error_home_fragment);
        this.recycler_view_home_fragment = (RecyclerView) view.findViewById(R.id.recycler_view_home_fragment);
        this.relative_layout_load_more_home_fragment = (RelativeLayout) view.findViewById(R.id.relative_layout_load_more_home_fragment);
        this.button_try_again = (Button) view.findViewById(R.id.button_try_again);

        this.gridLayoutManager=  new GridLayoutManager(getActivity().getApplicationContext(),1,RecyclerView.VERTICAL,false);


        this.homeAdapter =new HomeAdapter(dataList,getActivity());
        recycler_view_home_fragment.setHasFixedSize(true);
        recycler_view_home_fragment.setAdapter(homeAdapter);
        recycler_view_home_fragment.setLayoutManager(gridLayoutManager);
    }

}
