package my.cinemax.app.free.ui.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import my.cinemax.app.free.Provider.PrefManager;
import my.cinemax.app.free.R;
import my.cinemax.app.free.api.apiClient;
import my.cinemax.app.free.api.apiRest;
import my.cinemax.app.free.entity.Channel;
import my.cinemax.app.free.entity.Data;
import my.cinemax.app.free.entity.Poster;
import my.cinemax.app.free.ui.Adapters.PosterAdapter;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MyListActivity extends AppCompatActivity {

    private SwipeRefreshLayout swipe_refresh_layout_list_my_list_search;
    private Button button_try_again;
    private LinearLayout linear_layout_layout_error;
    private RecyclerView recycler_view_activity_my_list;
    private ImageView image_view_empty_list;
    private GridLayoutManager gridLayoutManager;
    private PosterAdapter adapter;

    private int pastVisiblesItems, visibleItemCount, totalItemCount;
    private boolean loading = true;

    private Integer page = 0;
    private Integer position = 0;
    private Integer item = 0 ;
    ArrayList<Poster> posterArrayList = new ArrayList<>();
    ArrayList<Channel> channelArrayList = new ArrayList<>();

    private RelativeLayout relative_layout_load_more;
    private LinearLayout linear_layout_load_my_list_activity;

    private Integer lines_beetween_ads = 2 ;
    private boolean tabletSize;
    private Boolean native_ads_enabled = false ;
    private int type_ads = 0;
    private PrefManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_list);
        prefManager= new PrefManager(getApplicationContext());

        initView();
        initAction();
        loadPosters();
        showAdsBanner();
    }




    private void loadPosters() {
        // No longer require login. Load list from local PrefManager.
        // PrefManager prf= new PrefManager(MyListActivity.this.getApplicationContext()); // already have prefManager instance
        // if (prf.getString("LOGGED").toString().equals("TRUE")){ // LOGGED check removed

        swipe_refresh_layout_list_my_list_search.setRefreshing(true);
        linear_layout_load_my_list_activity.setVisibility(View.VISIBLE);

        List<Integer> favoriteIds = prefManager.getMyList();
        posterArrayList.clear(); // Clear previous items
        channelArrayList.clear(); // Assuming channels are not part of local "My List" for now. If they are, this needs adjustment.

        if (favoriteIds.isEmpty()) {
            linear_layout_layout_error.setVisibility(View.GONE);
            recycler_view_activity_my_list.setVisibility(View.GONE);
            image_view_empty_list.setVisibility(View.VISIBLE);
            swipe_refresh_layout_list_my_list_search.setRefreshing(false);
            linear_layout_load_my_list_activity.setVisibility(View.GONE);
            adapter.notifyDataSetChanged(); // Notify adapter of empty list
            return;
        }

        // We need to fetch Poster details for each ID.
        // This can be slow if done one by one. A batch API would be better.
        // For this example, let's assume we fetch them one by one.
        // In a real app, consider a more efficient way or storing Poster objects directly if small enough.

        Retrofit retrofit = apiClient.getClient();
        apiRest service = retrofit.create(apiRest.class);
        final int[] pendingCalls = {favoriteIds.size()}; // Counter for async calls

        for (Integer posterId : favoriteIds) {
            Call<Poster> call = service.getPosterById(posterId); // Assuming this endpoint exists
            call.enqueue(new Callback<Poster>() {
                @Override
                public void onResponse(Call<Poster> call, Response<Poster> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        posterArrayList.add(response.body().setTypeView(1));
                        // Handle ads if necessary, though it might be simpler to add them after all posters are loaded
                    }
                    // else { Log.e("MyListActivity", "Failed to fetch poster details for ID: " + posterId); }

                    pendingCalls[0]--;
                    if (pendingCalls[0] == 0) {
                        // All calls finished
                        onAllPostersFetched();
                    }
                }

                @Override
                public void onFailure(Call<Poster> call, Throwable t) {
                    Log.e("MyListActivity", "API call failed for poster ID: " + posterId, t);
                    pendingCalls[0]--;
                    if (pendingCalls[0] == 0) {
                        // All calls finished (even if some failed)
                        onAllPostersFetched();
                    }
                }
            });
        }
    }

    private void onAllPostersFetched() {
        // This method is called after all individual poster fetch attempts are complete.
        // Add native ads logic here if needed, after posterArrayList is populated.
        item = 0; // Reset ad counter
        List<Poster> tempListWithAds = new ArrayList<>();
        for (Poster p : posterArrayList) {
            tempListWithAds.add(p);
            if (native_ads_enabled) {
                item++;
                if (item == lines_beetween_ads) {
                    item = 0;
                    if (prefManager.getString("ADMIN_NATIVE_TYPE").equals("FACEBOOK")) {
                        tempListWithAds.add(new Poster().setTypeView(4));
                    } else if (prefManager.getString("ADMIN_NATIVE_TYPE").equals("ADMOB")) {
                        tempListWithAds.add(new Poster().setTypeView(5));
                    } else if (prefManager.getString("ADMIN_NATIVE_TYPE").equals("BOTH")) {
                        if (type_ads == 0) {
                            tempListWithAds.add(new Poster().setTypeView(4));
                            type_ads = 1;
                        } else if (type_ads == 1) {
                            tempListWithAds.add(new Poster().setTypeView(5));
                            type_ads = 0;
                        }
                    }
                }
            }
        }
        posterArrayList.clear();
        posterArrayList.addAll(tempListWithAds);


        if (posterArrayList.isEmpty() && channelArrayList.isEmpty()) { // channelArrayList is likely empty now
            linear_layout_layout_error.setVisibility(View.GONE);
            recycler_view_activity_my_list.setVisibility(View.GONE);
            image_view_empty_list.setVisibility(View.VISIBLE);
        } else {
            linear_layout_layout_error.setVisibility(View.GONE);
            recycler_view_activity_my_list.setVisibility(View.VISIBLE);
            image_view_empty_list.setVisibility(View.GONE);
        }

        // Setup GridLayoutManager (moved from original response block as it depends on channelArrayList.size())
        if (channelArrayList.size()>0){ // This condition might always be false now
            posterArrayList.add(0,new Poster().setTypeView(3)); // Add placeholder for channels if any
            // ... (rest of gridLayoutManager setup as before) ...
        } else {
            if (native_ads_enabled){
                if (tabletSize) {
                    gridLayoutManager=  new GridLayoutManager(getApplicationContext(),6,RecyclerView.VERTICAL,false);
                    gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                        @Override
                        public int getSpanSize(int position) {
                            return ((position  + 1) % (lines_beetween_ads  + 1  ) == 0 && position!=0) ? 6 : 1;
                        }
                    });
                } else {
                    gridLayoutManager=  new GridLayoutManager(getApplicationContext(),3,RecyclerView.VERTICAL,false);
                    gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                        @Override
                        public int getSpanSize(int position) {
                            return ((position  + 1) % (lines_beetween_ads + 1 ) == 0  && position!=0)  ? 3 : 1;
                        }
                    });
                }
            }else {
                if (tabletSize) {
                    gridLayoutManager=  new GridLayoutManager(getApplicationContext(),6,RecyclerView.VERTICAL,false);
                } else {
                    gridLayoutManager=  new GridLayoutManager(getApplicationContext(),3,RecyclerView.VERTICAL,false);
                }
            }
        }
        recycler_view_activity_my_list.setLayoutManager(gridLayoutManager);
        adapter.notifyDataSetChanged();

        swipe_refresh_layout_list_my_list_search.setRefreshing(false);
        linear_layout_load_my_list_activity.setVisibility(View.GONE);
    }
    // }else{ // Removed else block for !LOGGED
    //     Intent intent = new Intent(MyListActivity.this,LoginActivity.class);
    //     startActivity(intent);
    //     overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
    //     finish();
    // }
// }


    private void initAction() {



        swipe_refresh_layout_list_my_list_search.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                item = 0;
                page = 0;
                loading = true;
                channelArrayList.clear();
                posterArrayList.clear();
                adapter.notifyDataSetChanged();
                loadPosters();
            }
        });
        button_try_again.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                item = 0;
                page = 0;
                loading = true;
                channelArrayList.clear();
                posterArrayList.clear();
                adapter.notifyDataSetChanged();
                loadPosters();
            }
        });
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

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle("My list");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        this.linear_layout_load_my_list_activity=findViewById(R.id.linear_layout_load_my_list_activity);
        this.relative_layout_load_more=findViewById(R.id.relative_layout_load_more);
        this.swipe_refresh_layout_list_my_list_search=findViewById(R.id.swipe_refresh_layout_list_my_list_search);
        button_try_again            = findViewById(R.id.button_try_again);
        image_view_empty_list       = findViewById(R.id.image_view_empty_list);
        linear_layout_layout_error  = findViewById(R.id.linear_layout_layout_error);
        recycler_view_activity_my_list          = findViewById(R.id.recycler_view_activity_my_list);
        adapter = new PosterAdapter(posterArrayList,channelArrayList, this,true);

        if (native_ads_enabled){
            Log.v("MYADS","ENABLED");
            if (tabletSize) {
                this.gridLayoutManager=  new GridLayoutManager(getApplicationContext(),6,RecyclerView.VERTICAL,false);
                Log.v("MYADS","tabletSize");
                gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        return ((position  + 1) % (lines_beetween_ads  + 1  ) == 0 && position!=0) ? 6 : 1;
                    }
                });
            } else {
                this.gridLayoutManager=  new GridLayoutManager(getApplicationContext(),3,RecyclerView.VERTICAL,false);
                gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        return ((position  + 1) % (lines_beetween_ads + 1 ) == 0  && position!=0)  ? 3 : 1;
                    }
                });
            }
        }else {
            if (tabletSize) {
                this.gridLayoutManager=  new GridLayoutManager(getApplicationContext(),6,RecyclerView.VERTICAL,false);
            } else {
                this.gridLayoutManager=  new GridLayoutManager(getApplicationContext(),3,RecyclerView.VERTICAL,false);
            }
        }
        recycler_view_activity_my_list.setHasFixedSize(true);
        recycler_view_activity_my_list.setAdapter(adapter);
        recycler_view_activity_my_list.setLayoutManager(gridLayoutManager);

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem itemMenu) {
        switch (itemMenu.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                super.onBackPressed();
                return true;
            }
            return true;
    }

    public boolean checkSUBSCRIBED(){
        PrefManager prefManager= new PrefManager(getApplicationContext());
        if (!prefManager.getString("SUBSCRIBED").equals("TRUE") && !prefManager.getString("NEW_SUBSCRIBE_ENABLED").equals("TRUE")) {
            return false;
        }
        return true;
    }
    public void showAdsBanner() {
        if (!checkSUBSCRIBED()) {
            PrefManager prefManager= new PrefManager(getApplicationContext());
            if (!prefManager.getString("ADMIN_BANNER_TYPE").equals("FALSE")){
                showAdmobBanner();
            }
        }
    }
    public void showAdmobBanner(){
        PrefManager prefManager= new PrefManager(getApplicationContext());
        LinearLayout linear_layout_ads =  (LinearLayout) findViewById(R.id.linear_layout_ads);
        final AdView mAdView = new AdView(this);
        mAdView.setAdSize(AdSize.SMART_BANNER);
        mAdView.setAdUnitId(prefManager.getString("ADMIN_BANNER_ADMOB_ID"));
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        mAdView.loadAd(adRequest);
        linear_layout_ads.addView(mAdView);

        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                mAdView.setVisibility(View.VISIBLE);
            }
        });
    }

}
