

package my.cinemax.app.free.crypto;

import android.os.AsyncTask;

import my.cinemax.app.free.Utils.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class PlaylistDownloader {
    private URL url;
    private List<String> playlist;
    private Crypto crypto;
    private DownloadListener downloadListener;

    private static String EXT_X_KEY = "#EXT-X-KEY";
    private static final String BANDWIDTH = "BANDWIDTH";

    public PlaylistDownloader(String playlistUrl, DownloadListener downloadListener) throws MalformedURLException {
        this.url = new URL(playlistUrl);
        this.playlist = new ArrayList<String>();
        this.downloadListener = downloadListener;
    }

    public void download(String outfile) throws IOException {
        this.download(outfile, null);
    }

    public void download(final String outfile, final String key) throws IOException {
        new FetchPlaylist().execute(outfile, key);

    }

    private void downloadAfterCrypto(String outfile, String key) throws IOException {
        Log.log("downloadAfterCrypto url:"+url);
        downloadListener.onStartDownload(url.toString());
        this.crypto = new Crypto(getBaseUrl(this.url), key);
        int Mypos = 0;
        int MYsize = 0;
        for (int j = 0; j< playlist.size(); j++) {
            String line = playlist.get(j);
            line = line.trim();
            if (line.length() > 0 && !line.startsWith("#")) {
                MYsize++;
            }
        }
        for (int i = 0; i< playlist.size(); i++) {

            String line = playlist.get(i);
            line = line.trim();

            if (line.startsWith(EXT_X_KEY)) {
                crypto.updateKeyString(line);

                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {

                    }
                }, 0, 10);
                Log.log("\rCurrent Key: "+crypto.getCurrentKey());
                Log.log("Current IV:  "+ crypto.getCurrentIV());
            } else if (line.length() > 0 && !line.startsWith("#")) {
                URL segmentUrl;

                if (!line.startsWith("http")) {
                    String baseUrl = getBaseUrl(this.url);
                    segmentUrl = new URL(baseUrl + line);
                } else {
                    segmentUrl = new URL(line);
                }
                Mypos++;
                Log.log("MY POSITION I:"+i+" POS : "+Mypos+" MYSIZE : "+MYsize);
                downloadInternal(segmentUrl, outfile, Mypos,MYsize);
            }
        }

    }

    private void downloadInternal(final URL segmentUrl, final String outFile, final int currProgress, final int size) {
        final byte[] buffer = new byte[512];

        new AsyncTask<Void, Integer, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                InputStream is = null;
                try {
                    is = crypto.hasKey()
                            ? crypto.wrapInputStream(segmentUrl.openStream())
                            : segmentUrl.openStream();


                FileOutputStream out;

                if (outFile != null) {
                    File file = new File(outFile);
                    out = new FileOutputStream(outFile, file.exists());
                } else {
                    String path = segmentUrl.getPath();
                    int pos = path.lastIndexOf('/');
                    out = new FileOutputStream(path.substring(++pos), false);
                }

                Log.log("Downloading segment: "+ segmentUrl);

                int read;

                while ((read = is.read(buffer)) >= 0) {
                    out.write(buffer, 0, read);
                }
                if (currProgress == size){
                    downloadListener.OnDownloadCompleted();
                }else{
                publishProgress(currProgress*100/size);
                }
                is.close();
                out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
                Log.log("Progress:"+values[0]);
                downloadListener.onProgressUpdate(values[0]);
            }
        }.execute();
    }

    private String getBaseUrl(URL url) {
        String urlString = url.toString();
        int index = urlString.lastIndexOf('/');
        return urlString.substring(0, ++index);
    }

    private class FetchPlaylist extends AsyncTask<String, Void, Boolean> {
        boolean isMaster = false;
        long maxRate = 0L;
        int maxRateIndex = 0;
        private String outfile;
        private String key;

        @Override
            protected Boolean doInBackground(String... params) {
                BufferedReader reader = null;
            outfile = params[0];
            key = params[1];
                try {
                    reader = new BufferedReader(new InputStreamReader(url.openStream()));


                String line;
                int index = 0;

                while ((line = reader.readLine()) != null) {
                    playlist.add(line);

                    if (line.contains(BANDWIDTH))
                        isMaster = true;

                    if (isMaster && line.contains(BANDWIDTH)) {
                        try {
                            int pos = line.lastIndexOf(BANDWIDTH+"=") + 10;
                            int end = line.indexOf(",",pos);
                            if (end < 0 || end < pos) end = line.length()-1;
                            long bandwidth = Long.parseLong(line.substring(pos, end));

                            maxRate = Math.max(bandwidth, maxRate);

                            if (bandwidth == maxRate)
                                maxRateIndex = index + 1;
                        } catch (NumberFormatException ignore) {
                            Log.log("NumberFormatException"+ignore.getMessage());
                        }
                    }

                    index++;
                }

                reader.close();

                } catch (IOException e) {
                    Log.log("Exception");
                    e.printStackTrace();
                }
                return isMaster;
            }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (isMaster) {
                Log.log("Found master playlist, fetching highest stream at Kb/s: "+ maxRate / 1024);
                URL tempUrl = updateUrlForSubPlaylist(playlist.get(maxRateIndex));
                if (null != tempUrl) {
                    url = tempUrl;
                    playlist.clear();
                    new FetchPlaylist().execute(outfile, key);
                }else {
                    try {
                        downloadAfterCrypto(outfile, key);
                    } catch (IOException e) {
                        Log.log("Exception");
                        e.printStackTrace();
                    }
                }
            } else {
                try {
                    downloadAfterCrypto(outfile, key);
                } catch (IOException e) {
                    Log.log("Exception");
                    e.printStackTrace();
                }
            }
            Log.log("MY DATA FINISHED");
            downloadListener.onStartDownload("ok");
        }
    }


    private URL updateUrlForSubPlaylist(String sub) {
        String newUrl;
        URL aUrl = null;

        if (!sub.startsWith("http")) {
            newUrl = getBaseUrl(this.url) + sub;
        } else {
            newUrl = sub;
        }

        try {
            aUrl = new URL(newUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return aUrl;
    }

    public interface DownloadListener{
        void onProgressUpdate(int progress);
        void onStartDownload(String url);
        void OnDownloadCompleted();
    }
}
