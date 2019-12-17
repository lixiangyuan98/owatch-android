package cn.edu.bupt.owatch;

import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.util.VLCVideoLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity  {
    private static final boolean USE_TEXTURE_VIEW = false;
    private static final boolean ENABLE_SUBTITLES = true;
    private static final String ASSET_FILENAME = "play.sdp";

    private VLCVideoLayout mVideoLayout = null;

    private LibVLC mLibVLC = null;
    private MediaPlayer mMediaPlayer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        final ArrayList<String> args = new ArrayList<>();
        args.add("-vvv");
        mLibVLC = new LibVLC(this, args);
        mMediaPlayer = new MediaPlayer(mLibVLC);

        mVideoLayout = findViewById(R.id.video_layout);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMediaPlayer.release();
        mLibVLC.release();
    }

    @Override
    protected void onStart() {
        super.onStart();

        mMediaPlayer.attachViews(mVideoLayout, null, ENABLE_SUBTITLES, USE_TEXTURE_VIEW);

        try {
            final Media media = new Media(mLibVLC, getAssets().openFd(ASSET_FILENAME));
            mMediaPlayer.setMedia(media);
            media.release();
        } catch (IOException e) {
            throw new RuntimeException("Invalid asset folder");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        TextView ipInfo = findViewById(R.id.ip_info);
        TextView wifiState = findViewById(R.id.wifi_state);

        WifiManager wm = (WifiManager)getSystemService(WIFI_SERVICE);
        boolean wifiEnabled = false;
        int addr = 0;
        if (wm != null) {
            wifiEnabled = wm.isWifiEnabled();
            addr = wm.getConnectionInfo().getIpAddress();
        }
        wifiState.setText(wifiEnabled ? getString(R.string.wifi_enabled) : getString(R.string.wifi_disabled));
        ipInfo.setText(getString(R.string.ip_info, itoa(addr)));

        mMediaPlayer.play();
    }

    @Override
    protected void onPause() {
        super.onPause();

        mMediaPlayer.stop();
    }

    @Override
    protected void onStop() {
        super.onStop();

        mMediaPlayer.detachViews();
    }

    private String itoa(int addr) {
        return String.format(Locale.getDefault(), "%d.%d.%d.%d",
                addr & 0xff, addr >> 8 & 0xff, addr >> 16 & 0xff, addr >> 24 & 0xff);
    }
}
