package my.cinemax.app.free.ui.activities;

import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.PlayerView;
import my.cinemax.app.free.R;

public class SimplePlayerActivity extends AppCompatActivity {

    private PlayerView playerView;
    private ExoPlayer player;
    public static final String VIDEO_URL_EXTRA = "video_url";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_player);

        playerView = findViewById(R.id.player_view);

        String videoUrl = getIntent().getStringExtra(VIDEO_URL_EXTRA);

        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);

        MediaItem mediaItem = MediaItem.fromUri(Uri.parse(videoUrl));
        player.setMediaItem(mediaItem);
        player.prepare();
        player.play();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        player.release();
    }
}
