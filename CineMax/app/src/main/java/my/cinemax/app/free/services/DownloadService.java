package my.cinemax.app.free.services;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
//import com.orhanobut.hawk.Hawk; // Removed Hawk
import my.cinemax.app.free.R;
import my.cinemax.app.free.api.apiClient;
import my.cinemax.app.free.api.apiRest;
import my.cinemax.app.free.crypto.PlaylistDownloader;
import my.cinemax.app.free.entity.DownloadItem;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

import androidx.core.app.NotificationCompat;
import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

public class DownloadService extends IntentService implements PlaylistDownloader.DownloadListener {

    private static final String TAG = "DownloadService";
    private static final String DOWNLOAD_LIST_JSON_FILE_NAME = "download_list.json";
    private final Gson gson = new Gson();
    private File downloadListJsonFile;
    private final ReentrantLock fileLock = new ReentrantLock();


    private String title = "";
    private String playlistUrl = "";
    private Integer id;
    private String path;
    private Integer element;
    private String image;
    private String type;
    private boolean downloaded = false;
    private String duration;

    public DownloadService() {
        super("DownloadService"); // Changed service name for clarity if needed
    }

    @Override
    public void onCreate() {
        super.onCreate();
        downloadListJsonFile = new File(getApplicationContext().getFilesDir(), DOWNLOAD_LIST_JSON_FILE_NAME);
    }

    private NotificationCompat.Builder notificationBuilder;
    private NotificationManager notificationManager;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return START_NOT_STICKY;
        }
        playlistUrl = intent.getStringExtra("url");
        title = intent.getStringExtra("title");
        image = intent.getStringExtra("image");
        type = intent.getStringExtra("type");
        id = intent.getIntExtra("id", 0); // Keep original ID for notification
        element = intent.getIntExtra("element", 0);
        duration = intent.getStringExtra("duration");

        Log.d("MY SERVICE DATA", "url =" + playlistUrl + ";id = " + id);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel("download_service_channel", "Downloads", NotificationManager.IMPORTANCE_LOW);
            notificationChannel.setDescription("Channel for active downloads");
            notificationChannel.setSound(null, null);
            notificationChannel.enableLights(false);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(false);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        notificationBuilder = new NotificationCompat.Builder(this, "download_service_channel")
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setContentTitle(title)
                .setContentText("Downloading")
                .setDefaults(0)
                .setOngoing(true)
                .setAutoCancel(false);
        notificationManager.notify(id, notificationBuilder.build()); // Use original id for notification

        try {
            PlaylistDownloader downloader = new PlaylistDownloader(playlistUrl, this);
            final int min = 100;
            final int max = 999;
            final int random = new Random().nextInt((max - min) + 1) + min;
            // Ensure external storage is available and handle permissions if necessary (not covered here)
            File downloadDir = new File(Environment.getExternalStorageDirectory(), getResources().getString(R.string.download_foler));
            if (!downloadDir.exists()) {
                downloadDir.mkdirs();
            }
            this.path = new File(downloadDir, title.replace(" ", "_").replaceAll("[^\\.A-Za-z0-9_]", "") + "_" + id + "_" + random + ".mp4").getAbsolutePath();
            downloader.download(this.path);

        } catch (java.io.IOException e) {
            Log.e(TAG, "Error starting download", e);
            Toast.makeText(this, "Url/path not correct or storage error", Toast.LENGTH_SHORT).show();
            stopSelf(); // Stop service if download cannot start
        }

        return START_NOT_STICKY;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // IntentService handles onStartCommand on a worker thread.
        // The actual download is initiated in onStartCommand via PlaylistDownloader which seems to be asynchronous.
        // This method can be kept empty if PlaylistDownloader handles its own threading for the download process.
    }

    private List<DownloadItem> loadDownloadList() {
        fileLock.lock();
        try {
            if (downloadListJsonFile.exists()) {
                try (FileReader reader = new FileReader(downloadListJsonFile)) {
                    Type type = new TypeToken<ArrayList<DownloadItem>>() {}.getType();
                    List<DownloadItem> list = gson.fromJson(reader, type);
                    return list != null ? list : new ArrayList<>();
                } catch (IOException e) {
                    Log.e(TAG, "Error loading download list from JSON", e);
                }
            }
        } finally {
            fileLock.unlock();
        }
        return new ArrayList<>();
    }

    private void saveDownloadList(List<DownloadItem> list) {
        fileLock.lock();
        try {
            try (FileWriter writer = new FileWriter(downloadListJsonFile)) {
                gson.toJson(list, writer);
            } catch (IOException e) {
                Log.e(TAG, "Error saving download list to JSON", e);
            }
        } finally {
            fileLock.unlock();
        }
    }

    private void updateNotification(int currentProgress) {
        notificationBuilder.setProgress(100, currentProgress, false);
        notificationBuilder.setContentText(currentProgress + "%");
        notificationManager.notify(id, notificationBuilder.build());
    }

    private void sendProgressUpdate(boolean downloadComplete) {
        // Original implementation was commented out. If needed, re-implement with LocalBroadcastManager.
    }

    public void addMovieDownload(Integer movieId) { // Changed param name for clarity
        Retrofit retrofit = apiClient.getClient();
        apiRest service = retrofit.create(apiRest.class);
        Call<Integer> call = service.addMovieDownload(movieId);
        call.enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, retrofit2.Response<Integer> response) {
                Log.d(TAG, "addMovieDownload response: " + response.code());
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                Log.e(TAG, "addMovieDownload failure", t);
            }
        });
    }

    public void addEpisodeDownload(Integer episodeId) { // Changed param name for clarity
        Retrofit retrofit = apiClient.getClient();
        apiRest service = retrofit.create(apiRest.class);
        Call<Integer> call = service.addEpisodeDownload(episodeId);
        call.enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, retrofit2.Response<Integer> response) {
                Log.d(TAG, "addEpisodeDownload response: " + response.code());
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                Log.e(TAG, "addEpisodeDownload failure", t);
            }
        });
    }

    private void onDownloadComplete(boolean downloadComplete) {
        sendProgressUpdate(downloadComplete);
        notificationManager.cancel(id); // Cancel the progress notification

        // Show a completion notification
        NotificationCompat.Builder completeNotificationBuilder = new NotificationCompat.Builder(this, "download_service_channel")
                .setSmallIcon(R.drawable.ic_file_download)
                .setContentTitle(title)
                .setContentText("File has been downloaded successfully")
                .setAutoCancel(true); // Allow user to dismiss it
        notificationManager.notify(id + 1000, completeNotificationBuilder.build()); // Use a different ID for completion to avoid conflicts


        DownloadItem downloadItem = new DownloadItem();
        downloadItem.setId(this.id); // Use the 'id' from the intent extras for the item's ID
        downloadItem.setElement(this.element);
        downloadItem.setImage(this.image);
        downloadItem.setPath(this.path);
        downloadItem.setType(this.type);
        downloadItem.setTitle(this.title);

        Log.v("MYDOWNLOADLIST_TOAST", downloadItem.getPath());
        File file = new File(downloadItem.getPath());
        String size = "";
        if (file.exists()) {
            size = getStringSizeLengthFile(file.length());
        }
        downloadItem.setDuration(this.duration);
        downloadItem.setSize(size);

        List<DownloadItem> my_downloads_list = loadDownloadList();

        // Remove existing item with the same ID if any, and delete its old file
        // Using a simple loop; for large lists, a map or more efficient search might be better.
        for (int i = my_downloads_list.size() - 1; i >= 0; i--) {
            if (my_downloads_list.get(i).getId().equals(downloadItem.getId())) { // Compare Integer objects with .equals()
                String oldPath = my_downloads_list.get(i).getPath();
                if (oldPath != null && !oldPath.equals(downloadItem.getPath())) { // Don't delete the new file
                    File oldFile = new File(oldPath);
                    if (oldFile.exists()) {
                        oldFile.delete();
                         Log.d(TAG, "Deleted old file: " + oldPath);
                    }
                }
                my_downloads_list.remove(i);
            }
        }

        my_downloads_list.add(downloadItem);
        saveDownloadList(my_downloads_list);

        if ("episode".equals(this.type)) {
            addEpisodeDownload(this.element);
        }
        if ("movie".equals(this.type)) {
            addMovieDownload(this.element);
        }
        this.stopSelf();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        notificationManager.cancel(id);
    }

    @Override
    public void onProgressUpdate(int progress) {
        updateNotification(progress);
    }

    @Override
    public void onStartDownload(String url) {
        // Can be used for logging or initial setup if needed
    }

    @Override
    public void OnDownloadCompleted() {
        this.downloaded = true;
        onDownloadComplete(true);
    }

    public static String getStringSizeLengthFile(long size) {
        if (size <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public static String formatDuration(final long millis) {
        long seconds = (millis / 1000) % 60;
        long minutes = (millis / (1000 * 60)) % 60;
        long hours = millis / (1000 * 60 * 60);

        StringBuilder b = new StringBuilder();
        if (hours > 0) {
            b.append(hours < 10 ? "0" + hours : hours).append("h ");
        }
        b.append(minutes < 10 ? "0" + minutes : minutes).append("min ");
        b.append(seconds < 10 ? "0" + seconds : seconds).append("s");

        return b.toString().trim();
    }

    @Override
    public void onDestroy() {
        if (downloaded) {
            Toasty.success(this, getResources().getString(R.string.file_has_been_downloaded), Toast.LENGTH_SHORT).show();
        }
        super.onDestroy();
    }
}
