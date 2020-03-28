package cn.edu.bupt.owatch;

import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.util.VLCVideoLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class PlayActivity extends AppCompatActivity implements OnClickListener {
    private static final boolean USE_TEXTURE_VIEW = false;
    private static final boolean ENABLE_SUBTITLES = true;
    private static final String ASSET_FILENAME = "play.sdp";

    private VLCVideoLayout mVideoLayout = null;

    private LibVLC mLibVLC = null;
    private MediaPlayer mMediaPlayer = null;
    private Heartbeat mHeartbeat = null;
    private EditText mServerIP = null;
    private EditText mServerPort = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_play);

        final ArrayList<String> args = new ArrayList<>();
        args.add("-vvv");
        mLibVLC = new LibVLC(this, args);
        mMediaPlayer = new MediaPlayer(mLibVLC);

        mVideoLayout = findViewById(R.id.video_layout);

        mServerIP = findViewById(R.id.serverIP);
        mServerPort = findViewById(R.id.serverPort);
        Button confirmBtn = findViewById(R.id.confirmBtn);
        Button resetBtn = findViewById(R.id.resetBtn);
        confirmBtn.setOnClickListener(this);
        resetBtn.setOnClickListener(this);

        mHeartbeat = new Heartbeat();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMediaPlayer.attachViews(mVideoLayout, null, ENABLE_SUBTITLES, USE_TEXTURE_VIEW);
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
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMediaPlayer.detachViews();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMediaPlayer.stop();
        mMediaPlayer.release();
        mLibVLC.release();
        mHeartbeat.stop();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.confirmBtn:
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.stop();
                } else {
                    mHeartbeat.start(mServerIP.getText().toString(), Integer.parseInt(mServerPort.getText().toString()));
                }
                try {
                    final Media media = new Media(mLibVLC, getAssets().openFd(ASSET_FILENAME));
                    mMediaPlayer.setMedia(media);
                    media.release();
                    mMediaPlayer.play();
                } catch (IOException ignored) {}
                break;
            case R.id.resetBtn:
                mHeartbeat.stop();
                mHeartbeat = new Heartbeat();
                mMediaPlayer.stop();
                mServerIP.setText(getString(R.string.default_server_ip));
                mServerPort.setText(getString(R.string.default_server_port));
                break;
        }
    }

    private String itoa(int addr) {
        return String.format(Locale.getDefault(), "%d.%d.%d.%d",
                addr & 0xff, addr >> 8 & 0xff, addr >> 16 & 0xff, addr >> 24 & 0xff);
    }
}
