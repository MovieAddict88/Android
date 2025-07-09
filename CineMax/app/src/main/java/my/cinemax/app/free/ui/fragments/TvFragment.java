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
import my.cinemax.app.free.entity.Category;
import my.cinemax.app.free.entity.Channel;
import my.cinemax.app.free.entity.Country;
import my.cinemax.app.free.ui.Adapters.ChannelAdapter;

import java.util.ArrayList;
import java.util.List;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * A simple {@link Fragment} subclass.
 */
public class TvFragment extends Fragment {


    private View view;
    private RelativeLayout relative_layout_channel_fragement_filtres_button;
    private CardView card_view_channel_fragement_filtres_layout;
    private ImageView image_view_channel_fragement_close_filtres;
    private AppCompatSpinner spinner_fragement_channel_categories_list;
    private AppCompatSpinner spinner_fragement_channel_countries_list;
    private RecyclerView recycler_view_channel_fragment;
    private LinearLayout linear_layout_page_error_channel_fragment;
    private LinearLayout linear_layout_load_channel_fragment;
    private SwipeRefreshLayout swipe_refresh_layout_channel_fragment;
    private RelativeLayout relative_layout_load_more_channel_fragment;
    private ImageView image_view_empty_list;
    private RelativeLayout relative_layout_frament_channel_categories;
    private RelativeLayout relative_layout_frament_channel_countries;

    private GridLayoutManager gridLayoutManager;
    private ChannelAdapter adapter;
    private List<Channel> channelList =  new ArrayList<>();
    private List<Country> countriesList =  new ArrayList<>();
    private List<Category> categoryList =  new ArrayList<>();

    private int pastVisiblesItems, visibleItemCount, totalItemCount;
    private boolean loading = true;

    private Integer page = 0;
    private Integer position = 0;
    private Integer item = 0 ;
    private Button button_try_again;
    private int countrySelected = 0;
    private int categorySelected = 0;

    private boolean firstLoadCountries = true;
    private boolean firstLoadCategories = true;
    private boolean loaded = false;

    private Integer lines_beetween_ads = 2 ;
    private boolean tabletSize;
    private Boolean native_ads_enabled = false ; // To be removed
    private int type_ads = 0; // To be removed
    private PrefManager prefManager;

    private LocalJsonRepository localJsonRepository; // Added


    public TvFragment() {
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
                getCountiesList();
                getCategoriesList();
                loadChannels();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view =  inflater.inflate(R.layout.fragment_tv, container, false);
        // channelList.add(new Channel().setTypeView(2)); // Dummy item for ads header removed
        prefManager= new PrefManager(getApplicationContext());
        localJsonRepository = new LocalJsonRepository(requireContext()); // Added

        initView();
        initActon();
         if (getUserVisibleHint() && !loaded) { // Handle case where fragment is visible on creation
             loaded=true;
             page = 0;
             loading = true;
             getCountiesList();
             getCategoriesList();
             loadChannels();
         }
        return view;
    }
    private void getCountiesList() {
        if (getContext() == null) return;

        List<Country> fetchedCountries = localJsonRepository.getCountriesList();
        if (fetchedCountries != null && !fetchedCountries.isEmpty()) {
            countriesList.clear();
            final String[] countryNames = new String[fetchedCountries.size() + 1];
            countryNames[0] = "All countries";
            countriesList.add(new Country()); // Placeholder

            for (int i = 0; i < fetchedCountries.size(); i++) {
                countryNames[i + 1] = fetchedCountries.get(i).getTitle();
                countriesList.add(fetchedCountries.get(i));
            }
            ArrayAdapter<String> filtresAdapter = new ArrayAdapter<>(requireActivity(),
                    R.layout.spinner_layout, R.id.textView, countryNames);
            filtresAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
            spinner_fragement_channel_countries_list.setAdapter(filtresAdapter);
            relative_layout_frament_channel_countries.setVisibility(View.VISIBLE);
        } else {
            relative_layout_frament_channel_countries.setVisibility(View.GONE);
        }
    }
    private void getCategoriesList() {
        if (getContext() == null) return;

        List<Category> fetchedCategories = localJsonRepository.getCategoriesList();
        if (fetchedCategories != null && !fetchedCategories.isEmpty()) {
            categoryList.clear();
            final String[] categoryNames = new String[fetchedCategories.size() + 1];
            categoryNames[0] = "All categories";
            categoryList.add(new Category()); // Placeholder

            for (int i = 0; i < fetchedCategories.size(); i++) {
                categoryNames[i + 1] = fetchedCategories.get(i).getTitle();
                categoryList.add(fetchedCategories.get(i));
            }
            ArrayAdapter<String> filtresAdapter = new ArrayAdapter<>(requireActivity(),
                    R.layout.spinner_layout, R.id.textView, categoryNames);
            filtresAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
            spinner_fragement_channel_categories_list.setAdapter(filtresAdapter);
            relative_layout_frament_channel_categories.setVisibility(View.VISIBLE);
        } else {
            relative_layout_frament_channel_categories.setVisibility(View.GONE);
        }
    }

    private void initActon() {
        this.relative_layout_channel_fragement_filtres_button.setOnClickListener(v->{
            card_view_channel_fragement_filtres_layout.setVisibility(View.VISIBLE);
            relative_layout_channel_fragement_filtres_button.setVisibility(View.INVISIBLE);
        });
        this.image_view_channel_fragement_close_filtres.setOnClickListener(v->{
            card_view_channel_fragement_filtres_layout.setVisibility(View.INVISIBLE);
            relative_layout_channel_fragement_filtres_button.setVisibility(View.VISIBLE);
        });
        spinner_fragement_channel_countries_list.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
               if (!firstLoadCountries) {
                   if (id == 0) {
                       countrySelected = 0;
                   } else {
                       countrySelected = countriesList.get((int) id).getId();
                   }
                   item = 0;
                   page = 0;
                   loading = true;
                   channelList.clear();
                    // channelList.add(new Channel().setTypeView(2)); // Dummy item for ads header removed
                   adapter.notifyDataSetChanged();
                   loadChannels();
               }else{
                   firstLoadCountries = false;
               }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinner_fragement_channel_categories_list.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!firstLoadCategories) {
                    if (id==0){
                    categorySelected  =0;
                    }else{
                        categorySelected  = categoryList.get((int) id).getId();
                    }
                    item = 0;
                    page = 0;
                    loading = true;
                    channelList.clear();
                    // channelList.add(new Channel().setTypeView(2)); // Dummy item for ads header removed
                    adapter.notifyDataSetChanged();

                    loadChannels();
                }else{
                    firstLoadCategories = false;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        swipe_refresh_layout_channel_fragment.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                item = 0;
                page = 0;
                loading = true;
                channelList.clear();
                // channelList.add(new Channel().setTypeView(2)); // Dummy item for ads header removed
                adapter.notifyDataSetChanged();
                loadChannels();
            }
        });
        button_try_again.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                item = 0;
                page = 0;
                loading = true;
                channelList.clear();
                // channelList.add(new Channel().setTypeView(2)); // Dummy item for ads header removed
                adapter.notifyDataSetChanged();
                loadChannels();
            }
        });
        recycler_view_channel_fragment.addOnScrollListener(new RecyclerView.OnScrollListener()
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
                            loadChannels();
                        }
                    }
                }else{

                }
            }
        });
    }
    public boolean checkSUBSCRIBED(){
        PrefManager prefManager= new PrefManager(getApplicationContext());
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
                lines_beetween_ads=8*Integer.parseInt(prefManager.getString("ADMIN_NATIVE_LINES"));
            }else{
                lines_beetween_ads=4*Integer.parseInt(prefManager.getString("ADMIN_NATIVE_LINES"));
            }
        }
        if (checkSUBSCRIBED()) {
            native_ads_enabled=false;
        }
        // prod

        this.button_try_again = (Button) view.findViewById(R.id.button_try_again);
        this.image_view_empty_list = (ImageView) view.findViewById(R.id.image_view_empty_list);
        this.relative_layout_load_more_channel_fragment = (RelativeLayout) view.findViewById(R.id.relative_layout_load_more_channel_fragment);
        this.swipe_refresh_layout_channel_fragment = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout_channel_fragment);
        this.linear_layout_load_channel_fragment = (LinearLayout) view.findViewById(R.id.linear_layout_load_channel_fragment);
        this.linear_layout_page_error_channel_fragment = (LinearLayout) view.findViewById(R.id.linear_layout_page_error_channel_fragment);
        this.recycler_view_channel_fragment = (RecyclerView) view.findViewById(R.id.recycler_view_channel_fragment);
        this.relative_layout_channel_fragement_filtres_button = (RelativeLayout) view.findViewById(R.id.relative_layout_channel_fragement_filtres_button);
        this.card_view_channel_fragement_filtres_layout = (CardView) view.findViewById(R.id.card_view_channel_fragement_filtres_layout);
        this.image_view_channel_fragement_close_filtres = (ImageView) view.findViewById(R.id.image_view_channel_fragement_close_filtres);
        this.spinner_fragement_channel_categories_list = (AppCompatSpinner) view.findViewById(R.id.spinner_fragement_channel_categories_list);
        this.spinner_fragement_channel_countries_list = (AppCompatSpinner) view.findViewById(R.id.spinner_fragement_channel_countries_list);
        this.relative_layout_frament_channel_countries = (RelativeLayout) view.findViewById(R.id.relative_layout_frament_channel_countries);
        this.relative_layout_frament_channel_categories = (RelativeLayout) view.findViewById(R.id.relative_layout_frament_channel_categories);

        this.gridLayoutManager=  new GridLayoutManager(getActivity().getApplicationContext(),2,RecyclerView.VERTICAL,false);

        adapter = new ChannelAdapter(channelList,getActivity());
        if (native_ads_enabled){
            Log.v("MYADS","ENABLED");
            if (tabletSize) {
                this.gridLayoutManager=  new GridLayoutManager(requireActivity().getApplicationContext(),4,RecyclerView.VERTICAL,false);
            } else {
                this.gridLayoutManager=  new GridLayoutManager(requireActivity().getApplicationContext(),2,RecyclerView.VERTICAL,false);
            }
        } // End of native_ads_enabled

        // Reset SpanSizeLookup if ads are removed
        if (!native_ads_enabled) {
             gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    return 1; // Each item takes 1 span
                }
            });
        }

        recycler_view_channel_fragment.setHasFixedSize(true);
        recycler_view_channel_fragment.setAdapter(adapter);
        recycler_view_channel_fragment.setLayoutManager(gridLayoutManager);
        // test


    }
    private void loadChannels() {
        if (page==0){
            linear_layout_load_channel_fragment.setVisibility(View.VISIBLE);
        }else{
            relative_layout_load_more_channel_fragment.setVisibility(View.VISIBLE);
        }
        swipe_refresh_layout_channel_fragment.setRefreshing(false);

        List<Channel> fetchedChannels = localJsonRepository.getChannelsByFilters(categorySelected, countrySelected, page);

        if (fetchedChannels != null) {
            if (!fetchedChannels.isEmpty()) {
                // if (channelList.size() > 0 && channelList.get(0).getTypeView() == 2) { // Removed dummy ad item
                //     channelList.remove(0);
                // }
                for (Channel channel : fetchedChannels) {
                    channelList.add(channel);
                    // Ad logic removed
                }
                linear_layout_page_error_channel_fragment.setVisibility(View.GONE);
                recycler_view_channel_fragment.setVisibility(View.VISIBLE);
                image_view_empty_list.setVisibility(View.GONE);

                adapter.notifyDataSetChanged();
                page++;
                loading = true;
            } else {
                if (page == 0 && channelList.isEmpty()) {
                    linear_layout_page_error_channel_fragment.setVisibility(View.GONE);
                    recycler_view_channel_fragment.setVisibility(View.GONE);
                    image_view_empty_list.setVisibility(View.VISIBLE);
                }
                loading = false;
            }
        } else {
            if (page == 0) {
                linear_layout_page_error_channel_fragment.setVisibility(View.VISIBLE);
                recycler_view_channel_fragment.setVisibility(View.GONE);
                image_view_empty_list.setVisibility(View.GONE);
            }
        }

        relative_layout_load_more_channel_fragment.setVisibility(View.GONE);
        swipe_refresh_layout_channel_fragment.setRefreshing(false);
        linear_layout_load_channel_fragment.setVisibility(View.GONE);
    }
}
