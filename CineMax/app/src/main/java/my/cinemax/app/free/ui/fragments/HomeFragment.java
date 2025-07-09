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
import my.cinemax.app.free.api.apiClient;
import my.cinemax.app.free.api.apiRest;
import my.cinemax.app.free.entity.Data;
import my.cinemax.app.free.entity.Genre;
import my.cinemax.app.free.entity.Poster; // Added for explicit Poster list handling
import my.cinemax.app.free.repository.LocalDataRepository; // Added import
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


    private Integer lines_beetween_ads = 2 ;
    private boolean tabletSize;
    private Boolean native_ads_enabled = false ;
    private int type_ads = 0;
    private PrefManager prefManager;
    private Integer item = 0 ;
    private LocalDataRepository localDataRepository; // Added field

    public HomeFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        this.view=  inflater.inflate(R.layout.fragment_home, container, false);
        prefManager= new PrefManager(getApplicationContext());
        this.localDataRepository = new LocalDataRepository(); // Initialize repository

        initViews();
        initActions();
        loadData();
        return view;
    }

    private void loadData() {
        showLoadingView();

        // New local data loading logic
        Data localData = localDataRepository.loadData(getContext());

        if (localData != null) {
            // apiClient.FormatData(getActivity(),response); // This was for remote config, may need to be handled differently or removed if not applicable

            dataList.clear();
            dataList.add(new Data().setViewType(0)); // Assuming this is for a header or some UI element

            if (localData.getSlides() != null && localData.getSlides().size() > 0) {
                Data slideData = new Data();
                slideData.setSlides(localData.getSlides());
                dataList.add(slideData);
            }

            if (localData.getChannels() != null && localData.getChannels().size() > 0) {
                Data channelData = new Data();
                channelData.setChannels(localData.getChannels());
                dataList.add(channelData);
            }

            // Movies Section
            if (localData.getMovies() != null && localData.getMovies().size() > 0) {
                Genre moviesGenre = new Genre();
                moviesGenre.setTitle("Movies");
                // moviesGenre.setId(-100); // Optional: give it a unique ID if adapter needs it

                // Filter movies that don't have basic info (e.g. no sources) - if necessary
                List<Poster> validMovies = new ArrayList<>();
                for(Poster movie : localData.getMovies()){
                    if(movie.getSources() != null && !movie.getSources().isEmpty()){
                        validMovies.add(movie);
                    }
                }
                if(!validMovies.isEmpty()){
                    moviesGenre.setPosters(validMovies);
                    Data moviesDataSection = new Data();
                    moviesDataSection.setGenre(moviesGenre);
                    dataList.add(moviesDataSection);

                    if (native_ads_enabled){
                        item++;
                        if (item == lines_beetween_ads ){
                            item= 0;
                            if (prefManager.getString("ADMIN_NATIVE_TYPE").equals("FACEBOOK")) {
                                dataList.add(new Data().setViewType(5));
                            } else if (prefManager.getString("ADMIN_NATIVE_TYPE").equals("ADMOB")){
                                dataList.add(new Data().setViewType(6));
                            } else if (prefManager.getString("ADMIN_NATIVE_TYPE").equals("BOTH")){
                                if (type_ads == 0) {
                                    dataList.add(new Data().setViewType(5));
                                    type_ads = 1;
                                } else if (type_ads == 1){
                                    dataList.add(new Data().setViewType(6));
                                    type_ads = 0;
                                }
                            }
                        }
                    }
                }
            }

            // Series Section
            if (localData.getSeries() != null && localData.getSeries().size() > 0) {
                Genre seriesGenre = new Genre();
                seriesGenre.setTitle("TV Series");
                // seriesGenre.setId(-101); // Optional: give it a unique ID

                List<Poster> validSeries = new ArrayList<>();
                for(Poster seriesItem : localData.getSeries()){
                    // Add validation if needed, e.g. check for seasons/episodes if structure allows
                    validSeries.add(seriesItem);
                }

                if(!validSeries.isEmpty()){
                    seriesGenre.setPosters(validSeries);
                    Data seriesDataSection = new Data();
                    seriesDataSection.setGenre(seriesGenre);
                    dataList.add(seriesDataSection);

                    if (native_ads_enabled){
                        item++;
                         if (item == lines_beetween_ads ){
                            item= 0;
                            if (prefManager.getString("ADMIN_NATIVE_TYPE").equals("FACEBOOK")) {
                                dataList.add(new Data().setViewType(5));
                            }else if (prefManager.getString("ADMIN_NATIVE_TYPE").equals("ADMOB")){
                                dataList.add(new Data().setViewType(6));
                            } else if (prefManager.getString("ADMIN_NATIVE_TYPE").equals("BOTH")){
                                if (type_ads == 0) {
                                    dataList.add(new Data().setViewType(5));
                                    type_ads = 1;
                                }else if (type_ads == 1){
                                    dataList.add(new Data().setViewType(6));
                                    type_ads = 0;
                                }
                            }
                        }
                    }
                }
            }
            showListView();
            if (homeAdapter!=null)
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
            native_ads_enabled=true;
            if (tabletSize) {
                lines_beetween_ads=Integer.parseInt(prefManager.getString("ADMIN_NATIVE_LINES"));
            }else{
                lines_beetween_ads=Integer.parseInt(prefManager.getString("ADMIN_NATIVE_LINES"));
            }
        }
        if (checkSUBSCRIBED()) {
            native_ads_enabled=false;
        }
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
