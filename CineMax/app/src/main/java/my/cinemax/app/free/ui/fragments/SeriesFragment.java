package my.cinemax.app.free.ui.fragments;


import android.os.Bundle;

import androidx.appcompat.widget.AppCompatSpinner;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import my.cinemax.app.free.Provider.PrefManager;
import my.cinemax.app.free.R;
// import my.cinemax.app.free.api.apiClient; // Replaced
// import my.cinemax.app.free.api.apiRest; // Replaced
import my.cinemax.app.free.repository.LocalJsonRepository; // Added
import my.cinemax.app.free.entity.Genre;
import my.cinemax.app.free.entity.Poster;
import my.cinemax.app.free.ui.Adapters.PosterAdapter;

import java.util.ArrayList;
import java.util.List;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * A simple {@link Fragment} subclass.
 */
public class SeriesFragment extends Fragment {



    private View view;
    private RelativeLayout relative_layout_series_fragement_filtres_button;
    private CardView card_view_series_fragement_filtres_layout;
    private ImageView image_view_series_fragement_close_filtres;
    private AppCompatSpinner spinner_fragement_series_orders_list;
    private List<Genre> genreList =  new ArrayList<>();
    private AppCompatSpinner spinner_fragement_series_genre_list;
    private RelativeLayout relative_layout_frament_series_genres;
    private RecyclerView recycler_view_series_fragment;
    private LinearLayout linear_layout_page_error_series_fragment;
    private LinearLayout linear_layout_load_series_fragment;
    private SwipeRefreshLayout swipe_refresh_layout_series_fragment;
    private RelativeLayout relative_layout_load_more_series_fragment;
    private ImageView image_view_empty_list;


    private GridLayoutManager gridLayoutManager;
    private PosterAdapter adapter;
    private List<Poster> movieList =  new ArrayList<>();

    private int pastVisiblesItems, visibleItemCount, totalItemCount;
    private boolean loading = true;

    private Integer page = 0;
    private Integer position = 0;
    private Integer item = 0 ;
    private Button button_try_again;
    private int genreSelected = 0;
    private String orderSelected = "created";

    private boolean firstLoadGenre = true;
    private boolean firstLoadOrder = true;
    private boolean loaded = false;


    private Integer lines_beetween_ads = 2 ;
    private boolean tabletSize;
    private Boolean native_ads_enabled = false ; // To be removed
    private int type_ads = 0; // To be removed
    private PrefManager prefManager;

    private LocalJsonRepository localJsonRepository; // Added


    public SeriesFragment() {
        // Required empty public constructor
    }
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser){
            if (!loaded) {
                if (localJsonRepository == null && getContext() != null) {
                    localJsonRepository = new LocalJsonRepository(requireContext());
                }
                loaded=true;
                page = 0;
                loading = true;
                getGenreList();
                loadSeries();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view =  inflater.inflate(R.layout.fragment_series, container, false);
        // movieList.add(new Poster().setTypeView(2)); // Dummy item for ads header removed
        prefManager= new PrefManager(getApplicationContext());
        localJsonRepository = new LocalJsonRepository(requireContext()); // Added

        initView();
        initActon();

        if (getUserVisibleHint() && !loaded) { // Handle case where fragment is visible on creation
             loaded=true;
             page = 0;
             loading = true;
             getGenreList();
             loadSeries();
         }

        return view;
    }

    private void getGenreList() {
        if (getContext() == null) return;

        List<Genre> fetchedGenres = localJsonRepository.getGenreList();
        if (fetchedGenres != null && !fetchedGenres.isEmpty()) {
            genreList.clear();
            final String[] genreNames = new String[fetchedGenres.size() + 1];
            genreNames[0] = "All genres";
            genreList.add(new Genre()); // Placeholder for "All genres"

            for (int i = 0; i < fetchedGenres.size(); i++) {
                genreNames[i + 1] = fetchedGenres.get(i).getTitle();
                genreList.add(fetchedGenres.get(i));
            }
            ArrayAdapter<String> filtresAdapter = new ArrayAdapter<>(requireActivity(),
                    R.layout.spinner_layout, R.id.textView, genreNames);
            filtresAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
            spinner_fragement_series_genre_list.setAdapter(filtresAdapter);
            relative_layout_frament_series_genres.setVisibility(View.VISIBLE);
        } else {
            relative_layout_frament_series_genres.setVisibility(View.GONE);
        }
    }

    private void initActon() {
        this.relative_layout_series_fragement_filtres_button.setOnClickListener(v->{
            card_view_series_fragement_filtres_layout.setVisibility(View.VISIBLE);
            relative_layout_series_fragement_filtres_button.setVisibility(View.INVISIBLE);
        });
        this.image_view_series_fragement_close_filtres.setOnClickListener(v->{
            card_view_series_fragement_filtres_layout.setVisibility(View.INVISIBLE);
            relative_layout_series_fragement_filtres_button.setVisibility(View.VISIBLE);
        });
        spinner_fragement_series_genre_list.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!firstLoadGenre) {
                    if (id==0){
                        genreSelected  =0;

                    }else{
                        genreSelected  = genreList.get((int) id).getId();
                    }
                    item = 0;
                    page = 0;
                    loading = true;
                    movieList.clear();
                    // movieList.add(new Poster().setTypeView(2)); // Dummy item for ads header removed
                    adapter.notifyDataSetChanged();
                    loadSeries();
                }else{
                    firstLoadGenre = false;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinner_fragement_series_orders_list.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!firstLoadOrder) {

                    switch ((int) id) {
                        case 0:
                            orderSelected = "created";
                            break;
                        case 1:
                            orderSelected = "rating";
                            break;
                        case 2:
                            orderSelected = "imdb";
                            break;
                        case 3:
                            orderSelected = "title";
                            break;
                        case 4:
                            orderSelected = "year";
                            break;
                        case 5:
                            orderSelected = "views";
                            break;
                    }
                    item = 0;
                    page = 0;
                    loading = true;
                    movieList.clear();
                    movieList.add(new Poster().setTypeView(2));
                    adapter.notifyDataSetChanged();
                    loadSeries();
                }else{
                    firstLoadOrder = false;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        swipe_refresh_layout_series_fragment.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                item = 0;
                page = 0;
                loading = true;
                movieList.clear();
                // movieList.add(new Poster().setTypeView(2)); // Dummy item for ads header removed
                adapter.notifyDataSetChanged();
                loadSeries();
            }
        });
        button_try_again.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                item = 0;
                page = 0;
                loading = true;
                movieList.clear();
                // movieList.add(new Poster().setTypeView(2)); // Dummy item for ads header removed
                adapter.notifyDataSetChanged();
                loadSeries();
            }
        });
        recycler_view_series_fragment.addOnScrollListener(new RecyclerView.OnScrollListener()
        {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy)
            {
                if(dy > 0) //check for scroll down
                {

                    visibleItemCount    = gridLayoutManager.getChildCount();
                    totalItemCount      = gridLayoutManager.getItemCount();
                    pastVisiblesItems   = gridLayoutManager.findFirstVisibleItemPosition();

                    if (loading)
                    {
                        if ( (visibleItemCount + pastVisiblesItems) >= totalItemCount)
                        {
                            loading = false;
                            loadSeries();
                        }
                    }
                }else{

                }
            }
        });
    }
    public boolean checkSUBSCRIBED(){
        if (!prefManager.getString("SUBSCRIBED").equals("TRUE") && !prefManager.getString("NEW_SUBSCRIBE_ENABLED").equals("TRUE")) {
            return false;
        }
        return true;
    }
    private void initView() {

        boolean tabletSize = getResources().getBoolean(R.bool.isTablet);
        if (!prefManager.getString("ADMIN_NATIVE_TYPE").equals("FALSE")){
            native_ads_enabled=true;
            if (tabletSize) {
                lines_beetween_ads=6*Integer.parseInt(prefManager.getString("ADMIN_NATIVE_LINES"));
            }else{
                lines_beetween_ads=3*Integer.parseInt(prefManager.getString("ADMIN_NATIVE_LINES"));
            }
        }
        if (checkSUBSCRIBED()) {
            native_ads_enabled=false;
        }
        // prod

        this.button_try_again = (Button) view.findViewById(R.id.button_try_again);
        this.image_view_empty_list = (ImageView) view.findViewById(R.id.image_view_empty_list);
        this.relative_layout_load_more_series_fragment = (RelativeLayout) view.findViewById(R.id.relative_layout_load_more_series_fragment);
        this.swipe_refresh_layout_series_fragment = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout_series_fragment);
        this.linear_layout_load_series_fragment = (LinearLayout) view.findViewById(R.id.linear_layout_load_series_fragment);
        this.linear_layout_page_error_series_fragment = (LinearLayout) view.findViewById(R.id.linear_layout_page_error_series_fragment);
        this.recycler_view_series_fragment = (RecyclerView) view.findViewById(R.id.recycler_view_series_fragment);
        this.relative_layout_series_fragement_filtres_button = (RelativeLayout) view.findViewById(R.id.relative_layout_series_fragement_filtres_button);
        this.card_view_series_fragement_filtres_layout = (CardView) view.findViewById(R.id.card_view_series_fragement_filtres_layout);
        this.image_view_series_fragement_close_filtres = (ImageView) view.findViewById(R.id.image_view_series_fragement_close_filtres);
        this.spinner_fragement_series_orders_list = (AppCompatSpinner) view.findViewById(R.id.spinner_fragement_series_orders_list);
        this.spinner_fragement_series_genre_list = (AppCompatSpinner) view.findViewById(R.id.spinner_fragement_series_genre_list);
        this.relative_layout_frament_series_genres = (RelativeLayout) view.findViewById(R.id.relative_layout_frament_series_genres);


        adapter = new PosterAdapter(movieList,getActivity());
        if (native_ads_enabled){
            Log.v("MYADS","ENABLED");
            if (tabletSize) {
                this.gridLayoutManager=  new GridLayoutManager(requireActivity().getApplicationContext(),6,RecyclerView.VERTICAL,false);
            } else {
                this.gridLayoutManager=  new GridLayoutManager(requireActivity().getApplicationContext(),3,RecyclerView.VERTICAL,false);
            }
        } //End of native ads enabled block

        // Reset SpanSizeLookup if ads are removed
        if (!native_ads_enabled) {
             gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    return 1; // Each item takes 1 span
                }
            });
        }

        recycler_view_series_fragment.setHasFixedSize(true);
        recycler_view_series_fragment.setAdapter(adapter);
        recycler_view_series_fragment.setLayoutManager(gridLayoutManager);
        // test


        final String[] countryCodes = getResources().getStringArray(R.array.orders_list);

        ArrayAdapter<String> ordersAdapter = new ArrayAdapter<String>(getActivity(),
                R.layout.spinner_layout,R.id.textView,countryCodes);
        ordersAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        spinner_fragement_series_orders_list.setAdapter(ordersAdapter);
    }
    private void loadSeries() {
        if (page==0){
            linear_layout_load_series_fragment.setVisibility(View.VISIBLE);
        }else{
            relative_layout_load_more_series_fragment.setVisibility(View.VISIBLE);
        }
        swipe_refresh_layout_series_fragment.setRefreshing(false);

        List<Poster> fetchedSeries = localJsonRepository.getSeriesByFilters(genreSelected, orderSelected, page);

        if (fetchedSeries != null) {
            if (!fetchedSeries.isEmpty()) {
                // if (movieList.size() > 0 && movieList.get(0).getTypeView() == 2) { // Removed dummy ad item check
                //     movieList.remove(0);
                // }
                for (Poster serie : fetchedSeries) {
                    movieList.add(serie);
                    // Ad logic removed
                }
                linear_layout_page_error_series_fragment.setVisibility(View.GONE);
                recycler_view_series_fragment.setVisibility(View.VISIBLE);
                image_view_empty_list.setVisibility(View.GONE);

                adapter.notifyDataSetChanged();
                page++;
                loading = true;
            } else {
                if (page == 0 && movieList.isEmpty()) {
                    linear_layout_page_error_series_fragment.setVisibility(View.GONE);
                    recycler_view_series_fragment.setVisibility(View.GONE);
                    image_view_empty_list.setVisibility(View.VISIBLE);
                }
                loading = false;
            }
        } else {
             if (page == 0) {
                linear_layout_page_error_series_fragment.setVisibility(View.VISIBLE);
                recycler_view_series_fragment.setVisibility(View.GONE);
                image_view_empty_list.setVisibility(View.GONE);
            }
        }

        relative_layout_load_more_series_fragment.setVisibility(View.GONE);
        swipe_refresh_layout_series_fragment.setRefreshing(false);
        linear_layout_load_series_fragment.setVisibility(View.GONE);
    }
}
