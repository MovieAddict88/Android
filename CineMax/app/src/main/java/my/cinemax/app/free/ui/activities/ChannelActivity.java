package my.cinemax.app.free.ui.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatRatingBar;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.congle7997.google_iap.BillingSubs;
import com.congle7997.google_iap.CallBackBilling;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;

import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaLoadRequestData;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaTrack;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.Session;
import com.google.android.gms.cast.framework.SessionManager;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.gms.common.images.WebImage;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jackandphantom.blurimage.BlurImage;
import my.cinemax.app.free.Provider.PrefManager;
import my.cinemax.app.free.R;
import my.cinemax.app.free.api.apiClient;
import my.cinemax.app.free.api.apiRest;
import my.cinemax.app.free.config.Global;
import my.cinemax.app.free.entity.ApiResponse;
import my.cinemax.app.free.entity.Channel;
import my.cinemax.app.free.entity.Comment;
import my.cinemax.app.free.entity.Source;
import my.cinemax.app.free.event.CastSessionEndedEvent;
import my.cinemax.app.free.event.CastSessionStartedEvent;
import my.cinemax.app.free.ui.Adapters.CategoryAdapter;
import my.cinemax.app.free.ui.Adapters.ChannelAdapter;
import my.cinemax.app.free.ui.Adapters.CommentAdapter;
import my.cinemax.app.free.ui.Adapters.CountryAdapter;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class ChannelActivity extends AppCompatActivity {
    private  static String TAG= "ChannelActivity";

    private CastContext mCastContext;
    private SessionManager mSessionManager;
    private CastSession mCastSession;
    private final SessionManagerListener mSessionManagerListener =
            new SessionManagerListenerImpl();

    private String payment_methode_id = "null";


    private ImageView image_view_activity_channel_background;
    private ImageView image_view_activity_channel_cover;
    private TextView text_view_activity_channel_title;
    private TextView text_view_activity_channel_sub_title;
    private TextView text_view_activity_channel_description;
    private TextView text_view_activity_channel_classification;
    private RatingBar rating_bar_activity_channel_rating;
    private RecyclerView recycle_view_activity_channel_categories;
    private FloatingActionButton floating_action_button_activity_channel_play;
    private FloatingActionButton floating_action_button_activity_channel_comment;
    private LinearLayout linear_layout_activity_channel_cast;
    private RecyclerView recycle_view_activity_activity_channel_cast;
    private LinearLayoutManager linearLayoutManagerCast;
    private LinearLayout linear_layout_channel_activity_rate;


    private RelativeLayout relative_layout_subtitles_loading;
    private RecyclerView recycle_view_activity_activity_channel_more_channels;
    private LinearLayout linear_layout_activity_channel_more_channels;
    private LinearLayout linear_layout_activity_channel_my_list;
    private ImageView image_view_activity_channel_my_list;
    private Dialog play_source_dialog;
    private Dialog download_source_dialog;
    private LinearLayout linear_layout_channel_activity_download;
    private LinearLayout linear_layout_channel_activity_share;
    private LinearLayout linear_layout_channel_activity_website_clicked;
    private LinearLayout linear_layout_channel_activity_website;
    private RecyclerView recycle_view_activity_channel_countires;

    // onjects
    private Channel channel;
    private String from;

    // adapters
    private CommentAdapter commentAdapter;

    // list
    private ArrayList<Comment> commentList= new ArrayList<>();

    // layout manager
    private LinearLayoutManager linearLayoutManagerComments;
    private LinearLayoutManager linearLayoutManagerSources;
    private LinearLayoutManager linearLayoutManagerCategories;
    private LinearLayoutManager linearLayoutManagerCountires;
    private CategoryAdapter categoryAdapter;
    private CountryAdapter countryAdapter;
    private LinearLayoutManager linearLayoutManagerMoreChannel;
    private ImageView image_view_activity_channel_cover_bg;

    private RewardedAd mRewardedVideoAd;

    private ArrayList<Source> playSources = new ArrayList<>();

    private  int current_position_play=-1 ;

    private  Boolean DialogOpened = false;
    private  Boolean fromLoad = false;
    private  int operationAfterAds = 0;


    private Dialog dialog;
    private boolean autoDisplay = false;
    private ProgressBar progress_bar_activity_channel_my_list;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mSessionManager = CastContext.getSharedInstance(this).getSessionManager();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel);
        mCastContext = CastContext.getSharedInstance(this);
        initView();
        getChannel();
        setChannel();
        initAction();
        checkFavorite();
        getRandomChannels();
        showAdsBanner();
        loadRewardedVideoAd();
        setPlayableList();

        initBuy();
    }

    BillingSubs billingSubs;
    public void initBuy(){
        List<String> listSkuStoreSubs = new ArrayList<>();
        listSkuStoreSubs.add(Global.SUBSCRIPTION_ID);
        billingSubs = new BillingSubs(this, listSkuStoreSubs, new CallBackBilling() {
            @Override
            public void onPurchase() {
                PrefManager prefManager= new PrefManager(getApplicationContext());
                prefManager.setString("SUBSCRIBED","TRUE");
                Toasty.success(ChannelActivity.this, "you have successfully subscribed ", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNotPurchase() {
                Toasty.warning(ChannelActivity.this, "Operation has been cancelled  ", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNotLogin() {
            }
        });
    }

    public void subscribe(){
        billingSubs.purchase(Global.SUBSCRIPTION_ID);
    }
    public void loadRewardedVideoAd() {
        PrefManager   prefManager= new PrefManager(getApplicationContext());

        mRewardedVideoAd.load(getApplicationContext(), prefManager.getString("ADMIN_REWARDED_ADMOB_ID"),
                new AdRequest.Builder().build(), new RewardedAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                        super.onAdLoaded(rewardedAd);
                        if (autoDisplay){
                            dialog.dismiss();
                            mRewardedVideoAd = rewardedAd;

                            autoDisplay = false;
                            mRewardedVideoAd.show(ChannelActivity.this, new OnUserEarnedRewardListener() {
                                @Override
                                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                                    dialog.dismiss();
                                    Toasty.success(getApplicationContext(),getString(R.string.use_content_for_free)).show();
                                    Log.d("Rewarded","onRewarded ");
                                    switch (operationAfterAds){
                                        case  200 :
                                            channel.setPlayas("1");
                                        case 300 :
                                            if (current_position_play != -1 ){
                                                playSources.get(current_position_play).setPremium("1");
                                                showSourcesPlayDialog();
                                            }
                                            break;
                                    }
                                }
                            });
                        }
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        super.onAdFailedToLoad(loadAdError);
                    }
                });
    }



    private void initView() {
        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        this.linear_layout_channel_activity_share =  (LinearLayout) findViewById(R.id.linear_layout_channel_activity_share);
        this.floating_action_button_activity_channel_comment =  (FloatingActionButton) findViewById(R.id.floating_action_button_activity_channel_comment);
        this.relative_layout_subtitles_loading =  (RelativeLayout) findViewById(R.id.relative_layout_subtitles_loading);
        this.floating_action_button_activity_channel_play =  (FloatingActionButton) findViewById(R.id.floating_action_button_activity_channel_play);
        this.image_view_activity_channel_background =  (ImageView) findViewById(R.id.image_view_activity_channel_background);
        this.image_view_activity_channel_cover =  (ImageView) findViewById(R.id.image_view_activity_channel_cover);
        this.image_view_activity_channel_cover_bg =  (ImageView) findViewById(R.id.image_view_activity_channel_cover_bg);
        this.text_view_activity_channel_title =  (TextView) findViewById(R.id.text_view_activity_channel_title);
        this.text_view_activity_channel_sub_title =  (TextView) findViewById(R.id.text_view_activity_channel_sub_title);
        this.text_view_activity_channel_description =  (TextView) findViewById(R.id.text_view_activity_channel_description);
        this.text_view_activity_channel_classification =  (TextView) findViewById(R.id.text_view_activity_channel_classification);
        this.rating_bar_activity_channel_rating =  (RatingBar) findViewById(R.id.rating_bar_activity_channel_rating);
        this.recycle_view_activity_channel_countires =  (RecyclerView) findViewById(R.id.recycle_view_activity_channel_countires);
        this.recycle_view_activity_channel_categories =  (RecyclerView) findViewById(R.id.recycle_view_activity_channel_categories);
        this.recycle_view_activity_activity_channel_more_channels =  (RecyclerView) findViewById(R.id.recycle_view_activity_activity_channel_more_channels);
        this.linear_layout_channel_activity_rate =  (LinearLayout) findViewById(R.id.linear_layout_channel_activity_rate);
        this.linear_layout_activity_channel_more_channels =  (LinearLayout) findViewById(R.id.linear_layout_activity_channel_more_channels);
        this.linear_layout_activity_channel_my_list =  (LinearLayout) findViewById(R.id.linear_layout_activity_channel_my_list);
        this.image_view_activity_channel_my_list =  (ImageView) findViewById(R.id.image_view_activity_channel_my_list);
        this.linear_layout_channel_activity_website =  (LinearLayout) findViewById(R.id.linear_layout_channel_activity_website);
        this.linear_layout_channel_activity_website_clicked =  (LinearLayout) findViewById(R.id.linear_layout_channel_activity_website_clicked);
        this.progress_bar_activity_channel_my_list =  (ProgressBar) findViewById(R.id.progress_bar_activity_channel_my_list);


    }
    private void setPlayableList() {
        for (int i = 0; i < channel.getSources().size(); i++) {
            if (channel.getSources().get(i).getKind().equals("both") || channel.getSources().get(i).getKind().equals("play")){
                playSources.add(channel.getSources().get(i));
            }
        }

    }
    private void getChannel() {
        channel = getIntent().getParcelableExtra("channel");
        from = getIntent().getStringExtra("from");
    }
    private void setChannel() {
        Picasso.with(this).load(channel.getImage()).into(image_view_activity_channel_cover);
        final com.squareup.picasso.Target target = new com.squareup.picasso.Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                BlurImage.with(getApplicationContext()).load(bitmap).intensity(25).Async(true).into(image_view_activity_channel_background);
            }
            @Override
            public void onBitmapFailed(Drawable errorDrawable) { }
            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) { }
        };
        Picasso.with(getApplicationContext()).load(channel.getImage()).into(target);
        image_view_activity_channel_background.setTag(target);

        final com.squareup.picasso.Target target1 = new com.squareup.picasso.Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                BlurImage.with(getApplicationContext()).load(bitmap).intensity(25).Async(true).into(image_view_activity_channel_cover_bg);
            }
            @Override
            public void onBitmapFailed(Drawable errorDrawable) { }
            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) { }
        };
        Picasso.with(getApplicationContext()).load(channel.getImage()).into(target1);
        image_view_activity_channel_cover_bg.setTag(target1);

        ViewCompat.setTransitionName(image_view_activity_channel_cover, "imageMain");
        if (channel.getWebsite()!=null){
            if (!channel.getWebsite().isEmpty())
                linear_layout_channel_activity_website.setVisibility(View.VISIBLE);
        }
        text_view_activity_channel_title.setText(channel.getTitle());
        text_view_activity_channel_sub_title.setText(channel.getTitle());
        text_view_activity_channel_description.setText(channel.getDescription());
        text_view_activity_channel_classification.setText(channel.getClassification());
        rating_bar_activity_channel_rating.setRating(channel.getRating());
        rating_bar_activity_channel_rating.setVisibility(channel.getRating()==0 ? View.GONE:View.VISIBLE);

        if (channel.getCategories()!=null){
            if (channel.getCategories().size()>0){
                this.linearLayoutManagerCategories=  new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
                this.categoryAdapter =new CategoryAdapter(channel.getCategories(),this);
                recycle_view_activity_channel_categories.setHasFixedSize(true);
                recycle_view_activity_channel_categories.setAdapter(categoryAdapter);
                recycle_view_activity_channel_categories.setLayoutManager(linearLayoutManagerCategories);
                recycle_view_activity_channel_categories.setVisibility(View.VISIBLE);
            }
        }

        if (channel.getCountries()!=null){
            if (channel.getCountries().size()>0){
                this.linearLayoutManagerCountires=  new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
                this.countryAdapter =new CountryAdapter(channel.getCountries(),this);
                recycle_view_activity_channel_countires.setHasFixedSize(true);
                recycle_view_activity_channel_countires.setAdapter(countryAdapter);
                recycle_view_activity_channel_countires.setLayoutManager(linearLayoutManagerCountires);
                recycle_view_activity_channel_countires.setVisibility(View.VISIBLE);
            }
        }

    }
    private void initAction() {
        linear_layout_channel_activity_website_clicked.setOnClickListener(v->{
            String url = channel.getWebsite();
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        });
        linear_layout_channel_activity_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                share();
            }
        });
        linear_layout_activity_channel_my_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addMyList();
            }
        });

        floating_action_button_activity_channel_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkSUBSCRIBED()){
                    showSourcesPlayDialog();
                }else{
                    if (channel.getPlayas().equals("2")){
                        showDialog(false);
                    }else if(channel.getPlayas().equals("3") ){
                        operationAfterAds = 200;
                        showDialog(true);
                    }else{
                        showSourcesPlayDialog();
                    }
                }
            }
        });
        linear_layout_channel_activity_rate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rateDialog();
            }
        });

        floating_action_button_activity_channel_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCommentsDialog();
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
    public void rateDialog(){
        Dialog rateDialog = new Dialog(this,
                R.style.Theme_Dialog);
        rateDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        rateDialog.setCancelable(true);
        rateDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Window window = rateDialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
        wlp.gravity = Gravity.BOTTOM;
        window.setAttributes(wlp);

        rateDialog.setContentView(R.layout.dialog_rate);
        final AppCompatRatingBar AppCompatRatingBar_dialog_rating_app=(AppCompatRatingBar) rateDialog.findViewById(R.id.AppCompatRatingBar_dialog_rating_app);
        final Button buttun_send=(Button) rateDialog.findViewById(R.id.buttun_send);
        final Button button_cancel=(Button) rateDialog.findViewById(R.id.button_cancel);
        final TextView text_view_rate_title=(TextView) rateDialog.findViewById(R.id.text_view_rate_title);
        text_view_rate_title.setText(getResources().getString(R.string.rate_this_channel));
        button_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rateDialog.dismiss();
            }
        });
        buttun_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PrefManager prf = new PrefManager(getApplicationContext());
                if (prf.getString("LOGGED").toString().equals("TRUE")) {
                    Integer id_user=  Integer.parseInt(prf.getString("ID_USER"));
                    String   key_user=  prf.getString("TOKEN_USER");
                    Retrofit retrofit = apiClient.getClient();
                    apiRest service = retrofit.create(apiRest.class);
                    Call<ApiResponse> call = service.addChannelRate(id_user+"",key_user, channel.getId(), AppCompatRatingBar_dialog_rating_app.getRating());
                    call.enqueue(new retrofit2.Callback<ApiResponse>() {
                        @Override
                        public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                            if (response.isSuccessful()) {
                                if (response.body().getCode() == 200) {
                                    Toasty.success(ChannelActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                                    if (response.body().getValues().size()>0){
                                        if (response.body().getValues().get(0).getName().equals("rate") ){
                                            rating_bar_activity_channel_rating.setVisibility(View.VISIBLE);
                                            rating_bar_activity_channel_rating.setRating(Float.parseFloat(response.body().getValues().get(0).getValue()));
                                        }
                                    }
                                } else {
                                    Toasty.error(ChannelActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                                }

                            }
                            rateDialog.dismiss();
                        }

                        @Override
                        public void onFailure(Call<ApiResponse> call, Throwable t) {
                            rateDialog.dismiss();
                        }
                    });
                } else {
                    rateDialog.dismiss();
                    Intent intent = new Intent(ChannelActivity.this,LoginActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_up, R.anim.slide_down);

                }
            }
        });
        rateDialog.setOnKeyListener(new Dialog.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface arg0, int keyCode,
                                 KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    rateDialog.dismiss();
                }
                return true;
            }
        });
        rateDialog.show();

    }

    public void showCommentsDialog(){

        Dialog dialog= new Dialog(this,
                R.style.Theme_Dialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
        wlp.gravity = Gravity.BOTTOM;
        window.setAttributes(wlp);
        dialog.setContentView(R.layout.dialog_comment);
        TextView text_view_comment_dialog_count=dialog.findViewById(R.id.text_view_comment_dialog_count);
        ImageView image_view_comment_dialog_close=dialog.findViewById(R.id.image_view_comment_dialog_close);
        ImageView image_view_comment_dialog_empty=dialog.findViewById(R.id.image_view_comment_dialog_empty);
        ImageView image_view_comment_dialog_add_comment=dialog.findViewById(R.id.image_view_comment_dialog_add_comment);
        ProgressBar progress_bar_comment_dialog_comments=dialog.findViewById(R.id.progress_bar_comment_dialog_comments);
        ProgressBar progress_bar_comment_dialog_add_comment=dialog.findViewById(R.id.progress_bar_comment_dialog_add_comment);
        EditText edit_text_comment_dialog_add_comment=dialog.findViewById(R.id.edit_text_comment_dialog_add_comment);
        RecyclerView recycler_view_comment_dialog_comments=dialog.findViewById(R.id.recycler_view_comment_dialog_comments);

        image_view_comment_dialog_empty.setVisibility(View.GONE);
        recycler_view_comment_dialog_comments.setVisibility(View.GONE);
        progress_bar_comment_dialog_comments.setVisibility(View.VISIBLE);
        commentAdapter = new CommentAdapter(commentList, ChannelActivity.this);
        linearLayoutManagerComments = new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL, false);
        recycler_view_comment_dialog_comments.setHasFixedSize(true);
        recycler_view_comment_dialog_comments.setAdapter(commentAdapter);
        recycler_view_comment_dialog_comments.setLayoutManager(linearLayoutManagerComments);

        Retrofit retrofit = apiClient.getClient();
        apiRest service = retrofit.create(apiRest.class);
        Call<List<Comment>> call = service.getCommentsByChannel(channel.getId());
        call.enqueue(new Callback<List<Comment>>() {
            @Override
            public void onResponse(Call<List<Comment>> call, Response<List<Comment>> response) {
                if (response.isSuccessful()){
                    if (response.body().size()>0) {
                        commentList.clear();
                        for (int i = 0; i < response.body().size(); i++)
                            commentList.add(response.body().get(i));

                        commentAdapter.notifyDataSetChanged();

                        text_view_comment_dialog_count.setText(commentList.size()+" Comments");
                        image_view_comment_dialog_empty.setVisibility(View.GONE);
                        recycler_view_comment_dialog_comments.setVisibility(View.VISIBLE);
                        progress_bar_comment_dialog_comments.setVisibility(View.GONE);
                        recycler_view_comment_dialog_comments.scrollToPosition(recycler_view_comment_dialog_comments.getAdapter().getItemCount()-1);
                        recycler_view_comment_dialog_comments.scrollToPosition(recycler_view_comment_dialog_comments.getAdapter().getItemCount()-1);
                    }else{
                        image_view_comment_dialog_empty.setVisibility(View.VISIBLE);
                        recycler_view_comment_dialog_comments.setVisibility(View.GONE);
                        progress_bar_comment_dialog_comments.setVisibility(View.GONE);
                    }
                }else{
                    image_view_comment_dialog_empty.setVisibility(View.VISIBLE);
                    recycler_view_comment_dialog_comments.setVisibility(View.GONE);
                    progress_bar_comment_dialog_comments.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<List<Comment>> call, Throwable t) {
                image_view_comment_dialog_empty.setVisibility(View.VISIBLE);
                recycler_view_comment_dialog_comments.setVisibility(View.GONE);
                progress_bar_comment_dialog_comments.setVisibility(View.GONE);
            }
        });

        image_view_comment_dialog_add_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edit_text_comment_dialog_add_comment.getText().length()>0){
                    PrefManager prf= new PrefManager(ChannelActivity.this.getApplicationContext());
                    if (prf.getString("LOGGED").toString().equals("TRUE")){
                        Integer id_user=  Integer.parseInt(prf.getString("ID_USER"));
                        String   key_user=  prf.getString("TOKEN_USER");
                        byte[] data = new byte[0];
                        String comment_final ="";
                        try {
                            data = edit_text_comment_dialog_add_comment.getText().toString().getBytes("UTF-8");
                            comment_final = Base64.encodeToString(data, Base64.DEFAULT);
                        } catch (UnsupportedEncodingException e) {
                            comment_final = edit_text_comment_dialog_add_comment.getText().toString();
                            e.printStackTrace();
                        }
                        progress_bar_comment_dialog_add_comment.setVisibility(View.VISIBLE);
                        image_view_comment_dialog_add_comment.setVisibility(View.GONE);
                        Retrofit retrofit = apiClient.getClient();
                        apiRest service = retrofit.create(apiRest.class);
                        Call<ApiResponse> call = service.addChannelComment(id_user+"",key_user,channel.getId(),comment_final);
                        call.enqueue(new Callback<ApiResponse>() {
                            @Override
                            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                                if (response.isSuccessful()){
                                    if (response.body().getCode()==200){
                                        recycler_view_comment_dialog_comments.setVisibility(View.VISIBLE);
                                        image_view_comment_dialog_empty.setVisibility(View.GONE);
                                        Toasty.success(ChannelActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                                        edit_text_comment_dialog_add_comment.setText("");
                                        String id="";
                                        String content="";
                                        String user="";
                                        String image="";

                                        for (int i=0;i<response.body().getValues().size();i++){
                                            if (response.body().getValues().get(i).getName().equals("id")){
                                                id=response.body().getValues().get(i).getValue();
                                            }
                                            if (response.body().getValues().get(i).getName().equals("content")){
                                                content=response.body().getValues().get(i).getValue();
                                            }
                                            if (response.body().getValues().get(i).getName().equals("user")){
                                                user=response.body().getValues().get(i).getValue();
                                            }
                                            if (response.body().getValues().get(i).getName().equals("image")){
                                                image=response.body().getValues().get(i).getValue();
                                            }
                                        }
                                        Comment comment= new Comment();
                                        comment.setId(Integer.parseInt(id));
                                        comment.setUser(user);
                                        comment.setContent(content);
                                        comment.setImage(image);
                                        comment.setEnabled(true);
                                        comment.setCreated(getResources().getString(R.string.now_time));
                                        commentList.add(comment);
                                        commentAdapter.notifyDataSetChanged();
                                        text_view_comment_dialog_count.setText(commentList.size()+" Comments");

                                    }else{
                                        Toasty.error(ChannelActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                                recycler_view_comment_dialog_comments.scrollToPosition(recycler_view_comment_dialog_comments.getAdapter().getItemCount()-1);
                                recycler_view_comment_dialog_comments.scrollToPosition(recycler_view_comment_dialog_comments.getAdapter().getItemCount()-1);
                                commentAdapter.notifyDataSetChanged();
                                progress_bar_comment_dialog_add_comment.setVisibility(View.GONE);
                                image_view_comment_dialog_add_comment.setVisibility(View.VISIBLE);
                            }
                            @Override
                            public void onFailure(Call<ApiResponse> call, Throwable t) {
                                progress_bar_comment_dialog_add_comment.setVisibility(View.GONE);
                                image_view_comment_dialog_add_comment.setVisibility(View.VISIBLE);
                            }
                        });
                    }else{
                        Intent intent = new Intent(ChannelActivity.this,LoginActivity.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
                    }
                }
            }
        });
        image_view_comment_dialog_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
    private void checkFavorite() {

        final PrefManager prefManager = new PrefManager(this);
        if (prefManager.getString("LOGGED").toString().equals("TRUE")){
            Integer id_user=  Integer.parseInt(prefManager.getString("ID_USER"));
            String   key_user=  prefManager.getString("TOKEN_USER");
            Retrofit retrofit = apiClient.getClient();
            apiRest service = retrofit.create(apiRest.class);
            progress_bar_activity_channel_my_list.setVisibility(View.VISIBLE);
            linear_layout_activity_channel_my_list.setClickable(false);

            image_view_activity_channel_my_list.setVisibility(View.GONE);
            Call<Integer> call = service.CheckMyList(channel.getId(),id_user,key_user,"channel");
            call.enqueue(new Callback<Integer>() {
                @Override
                public void onResponse(Call<Integer> call, retrofit2.Response<Integer> response) {
                    if (response.isSuccessful()){
                        if (response.body() == 200){
                            image_view_activity_channel_my_list.setImageDrawable(getResources().getDrawable(R.drawable.ic_close));
                        }else{
                            image_view_activity_channel_my_list.setImageDrawable(getResources().getDrawable(R.drawable.ic_check));

                        }
                    }
                    progress_bar_activity_channel_my_list.setVisibility(View.GONE);
                    image_view_activity_channel_my_list.setVisibility(View.VISIBLE);
                    linear_layout_activity_channel_my_list.setClickable(true);

                }
                @Override
                public void onFailure(Call<Integer> call, Throwable t) {
                    progress_bar_activity_channel_my_list.setVisibility(View.GONE);
                    image_view_activity_channel_my_list.setVisibility(View.VISIBLE);
                    linear_layout_activity_channel_my_list.setClickable(true);


                }
            });
        }
    }
    public void addMyList(){
        final PrefManager prefManager = new PrefManager(this);
        if (prefManager.getString("LOGGED").toString().equals("TRUE")){
            Integer id_user=  Integer.parseInt(prefManager.getString("ID_USER"));
            String   key_user=  prefManager.getString("TOKEN_USER");
            Retrofit retrofit = apiClient.getClient();
            apiRest service = retrofit.create(apiRest.class);
            progress_bar_activity_channel_my_list.setVisibility(View.VISIBLE);
            image_view_activity_channel_my_list.setVisibility(View.GONE);
            linear_layout_activity_channel_my_list.setClickable(false);
            Call<Integer> call = service.AddMyList(channel.getId(),id_user,key_user,"channel");
            call.enqueue(new Callback<Integer>() {
                @Override
                public void onResponse(Call<Integer> call, retrofit2.Response<Integer> response) {
                    if (response.isSuccessful()){
                        if (response.body() == 200){
                            image_view_activity_channel_my_list.setImageDrawable(getResources().getDrawable(R.drawable.ic_close));
                            Toasty.info(ChannelActivity.this, "This movie has been added to your list", Toast.LENGTH_SHORT).show();
                        }else{
                            image_view_activity_channel_my_list.setImageDrawable(getResources().getDrawable(R.drawable.ic_check));
                            Toasty.warning(ChannelActivity.this, "This movie has been removed from your list", Toast.LENGTH_SHORT).show();
                        }
                    }
                    progress_bar_activity_channel_my_list.setVisibility(View.GONE);
                    image_view_activity_channel_my_list.setVisibility(View.VISIBLE);
                    linear_layout_activity_channel_my_list.setClickable(true);

                }
                @Override
                public void onFailure(Call<Integer> call, Throwable t) {
                    progress_bar_activity_channel_my_list.setVisibility(View.GONE);
                    image_view_activity_channel_my_list.setVisibility(View.VISIBLE);
                    linear_layout_activity_channel_my_list.setClickable(true);

                }
            });
        }else{
            Intent intent = new Intent(ChannelActivity.this,LoginActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
        }
    }
    public void share(){
        String shareBody = channel.getTitle()+"\n\n"+getResources().getString(R.string.get_this_channel_here)+"\n"+ Global.API_URL.replace("api","c/share")+ channel.getId()+".html";
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT,  getString(R.string.app_name));
        startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.app_name)));
        addShare();
    }
    public void addView(){


        Retrofit retrofit = apiClient.getClient();
        apiRest service = retrofit.create(apiRest.class);
        Call<Integer> call = service.addChannelView(channel.getId());
        call.enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, retrofit2.Response<Integer> response) {

            }
            @Override
            public void onFailure(Call<Integer> call, Throwable t) {

            }
        });

    }
    public void addShare(){
        final PrefManager prefManager = new PrefManager(this);

        if (!prefManager.getString(channel.getId()+"_channel_share").equals("true")) {
            prefManager.setString(channel.getId()+"_channel_share", "true");
            Retrofit retrofit = apiClient.getClient();
            apiRest service = retrofit.create(apiRest.class);
            Call<Integer> call = service.addChannelShare(channel.getId());
            call.enqueue(new Callback<Integer>() {
                @Override
                public void onResponse(Call<Integer> call, retrofit2.Response<Integer> response) {

                }
                @Override
                public void onFailure(Call<Integer> call, Throwable t) {

                }
            });
        }
    }
    public void showSourcesPlayDialog(){
        if (playSources.size()==0){
            Toasty.warning(getApplicationContext(),getResources().getString(R.string.no_source_available),Toast.LENGTH_LONG).show();
            return;
        }
        if (playSources.size()==1){
            if (checkSUBSCRIBED()) {
                if (playSources.get(0).getExternal()) {
                    openLink(0);
                } else {
                    playSource(0);
                }
            }else {
                if (playSources.get(0).getPremium().equals("2")) {
                    showDialog(false);
                } else if (playSources.get(0).getPremium().equals("3")) {
                    operationAfterAds = 300;
                    current_position_play = 0;
                    showDialog(true);
                } else {
                    if (playSources.get(0).getExternal()) {
                        openLink(0);
                    } else {
                        playSource(0);
                    }
                }
            }
            return;
        }

        play_source_dialog= new Dialog(this,
                R.style.Theme_Dialog);
        play_source_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        play_source_dialog.setCancelable(true);
        play_source_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Window window = play_source_dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
        wlp.gravity = Gravity.BOTTOM;
        window.setAttributes(wlp);
        play_source_dialog.setContentView(R.layout.dialog_sources);

        RelativeLayout relative_layout_dialog_source_close =  play_source_dialog.findViewById(R.id.relative_layout_dialog_source_close);
        RecyclerView recycle_view_activity_dialog_sources =  play_source_dialog.findViewById(R.id.recycle_view_activity_dialog_sources);
        this.linearLayoutManagerSources =  new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        SourceAdapter sourceAdapter =new SourceAdapter();
        recycle_view_activity_dialog_sources.setHasFixedSize(true);
        recycle_view_activity_dialog_sources.setAdapter(sourceAdapter);
        recycle_view_activity_dialog_sources.setLayoutManager(linearLayoutManagerSources);

        relative_layout_dialog_source_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play_source_dialog.dismiss();
            }
        });
        play_source_dialog.setOnKeyListener(new Dialog.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface arg0, int keyCode,
                                 KeyEvent event) {
                // TODO Auto-generated method stub
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    play_source_dialog.dismiss();
                }
                return true;
            }
        });
        play_source_dialog.show();
    }

    public class SourceAdapter extends  RecyclerView.Adapter<SourceAdapter.SourceHolder>{


        @Override
        public SourceAdapter.SourceHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_source_play,parent, false);
            SourceAdapter.SourceHolder mh = new SourceAdapter.SourceHolder(v);
            return mh;
        }
        @Override
        public void onBindViewHolder(SourceAdapter.SourceHolder holder, final int position) {

            if (playSources.get(position).getTitle() == null){
                holder.text_view_item_source_type.setText(playSources.get(position).getType());
            }else{
                holder.text_view_item_source_type.setText(playSources.get(position).getTitle());
            }
            holder.image_view_item_source_type_link.setVisibility(View.GONE);
            holder.image_view_item_source_type_play.setVisibility(View.VISIBLE);
            if (playSources.get(position).getExternal() != null) {
                if (playSources.get(position).getExternal()){
                    holder.image_view_item_source_type_link.setVisibility(View.VISIBLE);
                    holder.image_view_item_source_type_play.setVisibility(View.GONE);
                }
            }
            holder.image_view_item_source_premium.setVisibility(View.GONE);
            if (playSources.get(position).getPremium() != null) {
                if (!playSources.get(position).getPremium().equals("1")){
                    holder.image_view_item_source_premium.setVisibility(View.VISIBLE);
                }
            }

            holder.text_view_item_source_size.setVisibility(View.GONE);
            if (playSources.get(position).getSize() != null) {
                if (playSources.get(position).getSize().length()>0){
                    holder.text_view_item_source_size.setVisibility(View.VISIBLE);
                    holder.text_view_item_source_size.setText(playSources.get(position).getSize());
                }
            }

            if (playSources.get(position).getQuality() != null) {
                if (playSources.get(position).getQuality().length() >0 ){
                    holder.text_view_item_source_quality.setVisibility(View.VISIBLE);
                    holder.text_view_item_source_quality.setText(playSources.get(position).getQuality());

                }else{
                    holder.text_view_item_source_quality.setVisibility(View.GONE);
                }
            }else{
                holder.text_view_item_source_quality.setVisibility(View.GONE);
            }

            switch (playSources.get(position).getType()){
                case "mp4":
                    holder.image_view_item_source_type_image.setImageDrawable(getResources().getDrawable(R.drawable.ic_mp4_file));
                    break;
                case "webm":
                    holder.image_view_item_source_type_image.setImageDrawable(getResources().getDrawable(R.drawable.ic_webm_file));
                    break;
                case "mkv":
                    holder.image_view_item_source_type_image.setImageDrawable(getResources().getDrawable(R.drawable.ic_mkv_file));
                    break;
                case "m3u8":
                    holder.image_view_item_source_type_image.setImageDrawable(getResources().getDrawable(R.drawable.ic_m3u_file));
                    break;
                case "youtube":
                    holder.image_view_item_source_type_image.setImageDrawable(getResources().getDrawable(R.drawable.ic_youtube));
                    break;
                case "embed":
                    holder.image_view_item_source_type_image.setImageDrawable(getResources().getDrawable(R.drawable.ic_embed_file));
                    break;
            }



            holder.image_view_item_source_type_play.setOnClickListener(v-> {
                if (checkSUBSCRIBED()) {
                    playSource(position);
                }else{
                    if (playSources.get(position).getPremium().equals("2")){
                        showDialog(false);
                    }else if(playSources.get(position).getPremium().equals("3") ){
                        operationAfterAds = 300;
                        current_position_play=  position;
                        showDialog(true);
                    }else{
                        playSource(position);
                    }
                }

                play_source_dialog.dismiss();
            });
            holder.image_view_item_source_type_link.setOnClickListener( v -> {
                if (checkSUBSCRIBED()) {
                    openLink(position);
                }else{
                    if (playSources.get(position).getPremium().equals("2")){
                        showDialog(false);
                    }else if(playSources.get(position).getPremium().equals("3") ){
                        operationAfterAds = 300;
                        current_position_play=  position;
                        showDialog(true);
                    }else{
                        openLink(position);
                    }
                }
                play_source_dialog.dismiss();

            });
        }
        @Override
        public int getItemCount() {
            return playSources.size();
        }
        public class SourceHolder extends RecyclerView.ViewHolder {
            private final ImageView image_view_item_source_type_play;
            private final ImageView image_view_item_source_type_image;
            private final TextView text_view_item_source_size;
            private final TextView text_view_item_source_type;
            private final TextView text_view_item_source_quality;
            private final ImageView image_view_item_source_premium;
            private final ImageView image_view_item_source_type_link;

            public SourceHolder(View itemView) {
                super(itemView);
                this.text_view_item_source_quality =  (TextView) itemView.findViewById(R.id.text_view_item_source_quality);
                this.text_view_item_source_type =  (TextView) itemView.findViewById(R.id.text_view_item_source_type);
                this.text_view_item_source_size =  (TextView) itemView.findViewById(R.id.text_view_item_source_size);
                this.image_view_item_source_type_image =  (ImageView) itemView.findViewById(R.id.image_view_item_source_type_image);
                this.image_view_item_source_type_play =  (ImageView) itemView.findViewById(R.id.image_view_item_source_type_play);
                this.image_view_item_source_premium =  (ImageView) itemView.findViewById(R.id.image_view_item_source_premium);
                this.image_view_item_source_type_link =  (ImageView) itemView.findViewById(R.id.image_view_item_source_type_link);
            }
        }
    }
    public void playSource(int position){
        addView();
        if (playSources.get(position).getType().equals("youtube")){
            Intent intent = new Intent(ChannelActivity.this,YoutubeActivity.class);
            intent.putExtra("url",playSources.get(position).getUrl());
            startActivity(intent);
            return;
        }
        if (playSources.get(position).getType().equals("embed")){
            Intent intent = new Intent(ChannelActivity.this,EmbedActivity.class);
            intent.putExtra("url",playSources.get(position).getUrl());
            startActivity(intent);
            return;
        }
        if (mCastSession == null) {
            mCastSession = mSessionManager.getCurrentCastSession();
        }
        if (mCastSession != null) {
            loadRemoteMediaSource(position, true);

        } else {
            Intent intent = new Intent(ChannelActivity.this,PlayerActivity.class);
            intent.putExtra("id",channel.getId());
            intent.putExtra("url",playSources.get(position).getUrl());
            intent.putExtra("type",playSources.get(position).getType());
            intent.putExtra("image",channel.getImage());
            intent.putExtra("kind","channel");
            intent.putExtra("isLive",true);
            intent.putExtra("title",channel.getTitle());
            intent.putExtra("subtitle",channel.getTitle());
            startActivity(intent);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        mCastSession = mSessionManager.getCurrentCastSession();
        mSessionManager.addSessionManagerListener(mSessionManagerListener);
    }

    @Override
    protected void onPause() {
        mSessionManager.removeSessionManagerListener(mSessionManagerListener);
        mCastSession = null;
        super.onPause();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_cast, menu);
        CastButtonFactory.setUpMediaRouteButton(getApplicationContext(),
                menu,
                R.id.media_route_menu_item);
        return true;
    }

    private class SessionManagerListenerImpl implements SessionManagerListener {
        @Override
        public void onSessionStarting(Session session) {
            Log.d(TAG,"onSessionStarting");
        }

        @Override
        public void onSessionStarted(Session session, String s) {
            Log.d(TAG,"onSessionStarted");
            invalidateOptionsMenu();
            EventBus.getDefault().post(new CastSessionStartedEvent());
        }

        @Override
        public void onSessionStartFailed(Session session, int i) {
            Log.d(TAG,"onSessionStartFailed");
        }

        @Override
        public void onSessionEnding(Session session) {
            Log.d(TAG,"onSessionEnding");
            EventBus.getDefault().post(new CastSessionEndedEvent(session.getSessionRemainingTimeMs()));
        }

        @Override
        public void onSessionEnded(Session session, int i) {
            Log.d(TAG,"onSessionEnded");
        }

        @Override
        public void onSessionResuming(Session session, String s) {
            Log.d(TAG,"onSessionResuming");
        }

        @Override
        public void onSessionResumed(Session session, boolean b) {
            Log.d(TAG,"onSessionResumed");
            invalidateOptionsMenu();
        }

        @Override
        public void onSessionResumeFailed(Session session, int i) {
            Log.d(TAG,"onSessionResumeFailed");
        }

        @Override
        public void onSessionSuspended(Session session, int i) {
            Log.d(TAG,"onSessionSuspended");
        }
    }
    private void loadRemoteMediaSource(int position, boolean autoPlay) {
        final RemoteMediaClient remoteMediaClient = mCastSession.getRemoteMediaClient();
        if (remoteMediaClient == null) {
            mCastSession = mSessionManager.getCurrentCastSession();
            mSessionManager.addSessionManagerListener(mSessionManagerListener);
            if (mCastSession == null) {
                mCastSession = mSessionManager.getCurrentCastSession();
            }

            playSource(position);

            return;
        }

        remoteMediaClient.registerCallback(new RemoteMediaClient.Callback() {
            @Override
            public void onStatusUpdated() {
                Log.d(TAG,"onStatusUpdated");
                if (remoteMediaClient.getMediaStatus() != null) {

                }
            }
        });
        remoteMediaClient.load(new MediaLoadRequestData.Builder()
                .setMediaInfo(getSourceMediaInfos(position))
                .setAutoplay(autoPlay).build());
    }

    private MediaInfo getSourceMediaInfos(int position) {
        MediaMetadata movieMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
        movieMetadata.putString(MediaMetadata.KEY_TITLE, channel.getTitle());
        movieMetadata.putString(MediaMetadata.KEY_SUBTITLE, channel.getTitle());

        movieMetadata.addImage(new WebImage(Uri.parse(channel.getImage())));
        movieMetadata.addImage(new WebImage(Uri.parse(channel.getImage())));
        List<MediaTrack> tracks =  new ArrayList<>();



        MediaInfo mediaInfo = new MediaInfo.Builder(playSources.get(position).getUrl())
                .setStreamType(MediaInfo.STREAM_TYPE_LIVE)
                .setMetadata(movieMetadata)
                .setMediaTracks(tracks)
                .build();
        return mediaInfo;
    }
    private void getRandomChannels() {
        String categories_list = "";
        for (int i = 0; i < channel.getCategories().size(); i++) {
            if (channel.getCategories().size()-1 == i){
                categories_list+=channel.getCategories().get(i).getId();
            }else{
                categories_list+=channel.getCategories().get(i).getId()+",";
            }
        }
        Retrofit retrofit = apiClient.getClient();
        apiRest service = retrofit.create(apiRest.class);

        Call<List<Channel>> call = service.getRandomChannel(categories_list);
        call.enqueue(new Callback<List<Channel>>() {
            @Override
            public void onResponse(Call<List<Channel>> call, Response<List<Channel>> response) {
                if (response.isSuccessful()){
                    if (response.body().size()>0) {
                        linearLayoutManagerMoreChannel = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
                        ChannelAdapter channelAdapter  = new ChannelAdapter(response.body(), ChannelActivity.this);
                        recycle_view_activity_activity_channel_more_channels.setHasFixedSize(true);
                        recycle_view_activity_activity_channel_more_channels.setAdapter(channelAdapter);
                        recycle_view_activity_activity_channel_more_channels.setLayoutManager(linearLayoutManagerMoreChannel);
                        linear_layout_activity_channel_more_channels.setVisibility(View.VISIBLE);
                    }
                }
            }
            @Override
            public void onFailure(Call<List<Channel>> call, Throwable t) {
            }
        });
    }
    @Override
    public void onBackPressed(){
        if (from!=null){
            Intent intent =  new Intent(getApplicationContext(),HomeActivity.class);
            startActivity(intent);
            finish();
        }else{
            super.onBackPressed();
        }
        return;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (from!=null){
            Intent intent =  new Intent(getApplicationContext(),HomeActivity.class);
            startActivity(intent);
            finish();
        }else{
            super.onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
    public void showDialog(Boolean withAds){
        this.dialog = new Dialog(this,
                R.style.Theme_Dialog);



        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
        wlp.gravity = Gravity.BOTTOM;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(wlp);
        final   PrefManager prf= new PrefManager(getApplicationContext());
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_subscribe);

        RelativeLayout relative_layout_watch_ads=(RelativeLayout) dialog.findViewById(R.id.relative_layout_watch_ads);
        TextView text_view_watch_ads=(TextView) dialog.findViewById(R.id.text_view_watch_ads);
        TextView text_view_policy_2=(TextView) dialog.findViewById(R.id.text_view_policy_2);
        TextView text_view_policy=(TextView) dialog.findViewById(R.id.text_view_policy);
        SpannableString content = new SpannableString(getResources().getString(R.string.subscription_policy));
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        text_view_policy.setText(content);
        text_view_policy_2.setText(content);


        text_view_policy.setOnClickListener(view -> {
            startActivity(new Intent(ChannelActivity.this,RefundActivity.class));
        });
        text_view_policy_2.setOnClickListener(view -> {
            startActivity(new Intent(ChannelActivity.this,RefundActivity.class));
        });
        CardView card_view_gpay=(CardView) dialog.findViewById(R.id.card_view_gpay);
        CardView card_view_paypal=(CardView) dialog.findViewById(R.id.card_view_paypal);
        CardView card_view_cash=(CardView) dialog.findViewById(R.id.card_view_cash);
        CardView card_view_credit_card=(CardView) dialog.findViewById(R.id.card_view_credit_card);
        LinearLayout payment_methode=(LinearLayout) dialog.findViewById(R.id.payment_methode);
        LinearLayout dialog_content=(LinearLayout) dialog.findViewById(R.id.dialog_content);
        RelativeLayout relative_layout_subscibe_back=(RelativeLayout) dialog.findViewById(R.id.relative_layout_subscibe_back);

        RelativeLayout relative_layout_select_method=(RelativeLayout) dialog.findViewById(R.id.relative_layout_select_method);

        if (prf.getString("APP_STRIPE_ENABLED").toString().equals("FALSE")){
            card_view_credit_card.setVisibility(View.GONE);
        }
        if (prf.getString("APP_PAYPAL_ENABLED").toString().equals("FALSE")){
            card_view_paypal.setVisibility(View.GONE);
        }
        if (prf.getString("APP_CASH_ENABLED").toString().equals("FALSE")){
            card_view_cash.setVisibility(View.GONE);
        }
        if (prf.getString("APP_GPLAY_ENABLED").toString().equals("FALSE")){
            card_view_gpay.setVisibility(View.GONE);
        }
        relative_layout_select_method.setOnClickListener(v->{
            if(payment_methode_id.equals("null")) {
                Toasty.error(getApplicationContext(), getResources().getString(R.string.select_payment_method), Toast.LENGTH_LONG).show();
                return;
            }
            switch (payment_methode_id){
                case "gp" :
                    subscribe();
                    dialog.dismiss();
                    break;
                default:
                    PrefManager prf1= new PrefManager(getApplicationContext());
                    if (prf1.getString("LOGGED").toString().equals("TRUE")){
                        Intent intent  =  new Intent(getApplicationContext(), PlansActivity.class);
                        intent.putExtra("method",payment_methode_id);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
                        dialog.dismiss();

                    }else{
                        Intent intent= new Intent(ChannelActivity.this, LoginActivity.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
                    }
                    dialog.dismiss();
                    break;
            }
        });

        if (withAds){
            relative_layout_watch_ads.setVisibility(View.VISIBLE);
        }else{
            relative_layout_watch_ads.setVisibility(View.GONE);
        }
        relative_layout_watch_ads.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mRewardedVideoAd != null) {
                    mRewardedVideoAd.show(ChannelActivity.this, rewardItem -> {
                        dialog.dismiss();
                        Toasty.success(getApplicationContext(),getString(R.string.use_content_for_free)).show();
                        Log.d("Rewarded","onRewarded ");
                        switch (operationAfterAds){
                            case  200 :
                                channel.setPlayas("1");
                            case 300 :
                                if (current_position_play != -1 ){
                                    playSources.get(current_position_play).setPremium("1");
                                    showSourcesPlayDialog();
                                }
                                break;
                        }
                    });
                }else{
                    autoDisplay =  true;
                    loadRewardedVideoAd();
                    text_view_watch_ads.setText("SHOW LOADING.");
                }
            }
        });
        TextView text_view_go_pro=(TextView) dialog.findViewById(R.id.text_view_go_pro);
        text_view_go_pro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                payment_methode.setVisibility(View.VISIBLE);
                dialog_content.setVisibility(View.GONE);
                relative_layout_subscibe_back.setVisibility(View.VISIBLE);
            }
        });
        relative_layout_subscibe_back.setOnClickListener(v->{
            payment_methode.setVisibility(View.GONE);
            dialog_content.setVisibility(View.VISIBLE);
            relative_layout_subscibe_back.setVisibility(View.GONE);
        });
        card_view_gpay.setOnClickListener(v->{
            payment_methode_id="gp";
            card_view_gpay.setCardBackgroundColor(getResources().getColor(R.color.colorAccent));
            card_view_paypal.setCardBackgroundColor(getResources().getColor(R.color.dark_gray));
            card_view_cash.setCardBackgroundColor(getResources().getColor(R.color.dark_gray));
            card_view_credit_card.setCardBackgroundColor(getResources().getColor(R.color.dark_gray));
        });
        card_view_paypal.setOnClickListener(v->{
            payment_methode_id="pp";
            card_view_gpay.setCardBackgroundColor(getResources().getColor(R.color.dark_gray));
            card_view_paypal.setCardBackgroundColor(getResources().getColor(R.color.colorAccent));
            card_view_cash.setCardBackgroundColor(getResources().getColor(R.color.dark_gray));
            card_view_credit_card.setCardBackgroundColor(getResources().getColor(R.color.dark_gray));
        });
        card_view_credit_card.setOnClickListener(v->{
            payment_methode_id="cc";
            card_view_gpay.setCardBackgroundColor(getResources().getColor(R.color.dark_gray));
            card_view_paypal.setCardBackgroundColor(getResources().getColor(R.color.dark_gray));
            card_view_cash.setCardBackgroundColor(getResources().getColor(R.color.dark_gray));
            card_view_credit_card.setCardBackgroundColor(getResources().getColor(R.color.colorAccent));
        });
        card_view_cash.setOnClickListener(v->{
            payment_methode_id="cash";
            card_view_gpay.setCardBackgroundColor(getResources().getColor(R.color.dark_gray));
            card_view_paypal.setCardBackgroundColor(getResources().getColor(R.color.dark_gray));
            card_view_cash.setCardBackgroundColor(getResources().getColor(R.color.colorAccent));
            card_view_credit_card.setCardBackgroundColor(getResources().getColor(R.color.dark_gray));
        });
        dialog.setOnKeyListener(new Dialog.OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface arg0, int keyCode,
                                 KeyEvent event) {
                // TODO Auto-generated method stub
                if (keyCode == KeyEvent.KEYCODE_BACK) {

                    dialog.dismiss();
                }
                return true;
            }
        });
        dialog.show();
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


    public void openLink(int position){
        String url = playSources.get(position).getUrl();
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }
}
