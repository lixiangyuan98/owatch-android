package cn.edu.bupt.owatch;

import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.util.VLCVideoLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import cn.edu.bupt.owatch.adapter.FileAdapter;
import cn.edu.bupt.owatch.bean.DataHolder;
import cn.edu.bupt.owatch.bean.WsRequest;

public class PlayActivity extends AppCompatActivity implements OnClickListener, AdapterView.OnItemClickListener {
    private static final boolean USE_TEXTURE_VIEW = false;
    private static final boolean ENABLE_SUBTITLES = true;
    private static final String ASSET_FILENAME = "play.sdp";
    private Gson gson;

    private VLCVideoLayout mVideoLayout = null;
    private Integer addr;
    private ListView fileList;

    private LibVLC mLibVLC = null;
    private MediaPlayer mMediaPlayer = null;
    private DataHolder dataHolder = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_play);
        dataHolder = (DataHolder) getApplication();
        gson = new Gson();

        final ArrayList<String> args = new ArrayList<>();
        args.add("-vvv");
        mLibVLC = new LibVLC(this, args);
        mMediaPlayer = new MediaPlayer(mLibVLC);

        mVideoLayout = findViewById(R.id.video_layout);

//        mServerIP = findViewById(R.id.serverIP);
//        mServerPort = findViewById(R.id.serverPort);
//        Button confirmBtn = findViewById(R.id.confirmBtn);
//        Button resetBtn = findViewById(R.id.resetBtn);
//        confirmBtn.setOnClickListener(this);
//        resetBtn.setOnClickListener(this);
        fileList = findViewById(R.id.file_list);
        FileAdapter adapter = new FileAdapter(PlayActivity.this, dataHolder.getInfo().getFiles());
        Log.i("PLAY", "files: " + dataHolder.getInfo().getFiles().size());
        fileList.setAdapter(adapter);
        fileList.setOnItemClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMediaPlayer.attachViews(mVideoLayout, null, ENABLE_SUBTITLES, USE_TEXTURE_VIEW);
    }

    @Override
    protected void onResume() {
        super.onResume();
        WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
        addr = 0;
        if (wm != null) {
            addr = wm.getConnectionInfo().getIpAddress();
        }
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
    }

    @Override
    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.confirmBtn:
//                if (mMediaPlayer.isPlaying()) {
//                    mMediaPlayer.stop();
//                } else {
//                    mHeartbeat.start(mServerIP.getText().toString(), Integer.parseInt(mServerPort.getText().toString()));
//                }
//                try {
//                    final Media media = new Media(mLibVLC, getAssets().openFd(ASSET_FILENAME));
//                    mMediaPlayer.setMedia(media);
//                    media.release();
//                    mMediaPlayer.play();
//                } catch (IOException ignored) {}
//                break;
//            case R.id.resetBtn:
//                mHeartbeat.stop();
//                mHeartbeat = new Heartbeat();
//                mMediaPlayer.stop();
//                mServerIP.setText(getString(R.string.default_server_ip));
//                mServerPort.setText(getString(R.string.default_server_port));
//                break;
//        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.i("FileList", "click file " + position);
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
        }
        String src = null;
        if (position != 0) {
            src = dataHolder.getInfo().getFiles().get(position - 1);
        }
        WsRequest req = new WsRequest("SEND", dataHolder.getInfo().getHost(), src, itoa(addr), 9000);
        dataHolder.getWs().send(gson.toJson(req));
        Log.i("PLAY", "send request: " + gson.toJson(req));
        try {
            final Media media = new Media(mLibVLC, getAssets().openFd(ASSET_FILENAME));
            mMediaPlayer.setMedia(media);
            media.release();
            mMediaPlayer.play();
        } catch (IOException e) {
            Log.e("PLAY", "play error: " + e.getMessage());
            Toast.makeText(PlayActivity.this, "播放失败", Toast.LENGTH_LONG).show();
        }
    }

    private String itoa(int addr) {
        return String.format(Locale.getDefault(), "%d.%d.%d.%d",
                addr & 0xff, addr >> 8 & 0xff, addr >> 16 & 0xff, addr >> 24 & 0xff);
    }
}
