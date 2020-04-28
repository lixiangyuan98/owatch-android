package cn.edu.bupt.owatch;

import android.net.TrafficStats;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
    private static final String FILE_FILENAME = "file.sdp";
    private Gson gson;

    private VLCVideoLayout mVideoLayout = null;
    private VLCVideoLayout mFileLayout = null;
    private TextView speedText;
    private Integer addr;
    private ListView fileList;

    private LibVLC mLibVLC = null;
    private MediaPlayer mMediaPlayer = null;
    private LibVLC mLibVLCFile = null;
    private MediaPlayer mMediaPlayerFile = null;
    private DataHolder dataHolder = null;

    private Handler speedHandler;
    private Runnable speedCountRunnable;
    private long totalRxBytes = TrafficStats.getTotalRxBytes();

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
        mLibVLCFile = new LibVLC(this, args);
        mMediaPlayerFile = new MediaPlayer(mLibVLC);

        mVideoLayout = findViewById(R.id.video_layout);
        mFileLayout = findViewById(R.id.file_video_layout);
        speedText = findViewById(R.id.speedText);

        speedHandler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                if (msg.what == 1) {
                    Log.i("PLAY", "Speed=" + msg.arg1 / 1024 + "KB/s");
                    speedText.setText(msg.arg1 / 1024 + "KB/s");
                }
            }
        };

        speedCountRunnable = new Runnable() {
            @Override
            public void run() {
                long total = TrafficStats.getTotalRxBytes();
                speedHandler.postDelayed(speedCountRunnable, 1000);
                Message msg =  speedHandler.obtainMessage();
                msg.what = 1;
                msg.arg1 = (int)(total - totalRxBytes);
                totalRxBytes = total;
                speedHandler.sendMessage(msg);
            }
        };

        speedHandler.postDelayed(speedCountRunnable, 0);

        fileList = findViewById(R.id.file_list);
        FileAdapter adapter = new FileAdapter(PlayActivity.this, dataHolder.getInfo().getFiles());
        Log.i("PLAY", "files: " + dataHolder.getInfo().getFiles().size());
        fileList.setAdapter(adapter);
        fileList.setOnItemClickListener(this);

        Button realtimeVideoBtn = findViewById(R.id.realtime_video);
        Button stopVideoBtn = findViewById(R.id.stop_video);
        realtimeVideoBtn.setOnClickListener(this);
        stopVideoBtn.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
        addr = 0;
        if (wm != null) {
            addr = wm.getConnectionInfo().getIpAddress();
        }
        mMediaPlayer.attachViews(mVideoLayout, null, ENABLE_SUBTITLES, USE_TEXTURE_VIEW);
        mMediaPlayerFile.attachViews(mFileLayout, null, ENABLE_SUBTITLES, USE_TEXTURE_VIEW);
        sendVideoRequest();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMediaPlayer.detachViews();
        mMediaPlayerFile.detachViews();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMediaPlayer.stop();
        mMediaPlayer.release();
        mLibVLC.release();
        mMediaPlayerFile.stop();
        mMediaPlayerFile.release();
        mLibVLCFile.release();
        speedHandler.removeCallbacks(speedCountRunnable);
    }

    private void sendVideoRequest() {
        WsRequest req = new WsRequest("SEND", dataHolder.getInfo().getHost(), null, itoa(addr), 9000);
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

    private void sendFileRequest(WsRequest req) {
        dataHolder.getWs().send(gson.toJson(req));
        Log.i("PLAY", "send request: " + gson.toJson(req));
        try {
            final Media media = new Media(mLibVLC, getAssets().openFd(FILE_FILENAME));
            mMediaPlayerFile.setMedia(media);
            media.release();
            mMediaPlayerFile.play();
        } catch (IOException e) {
            Log.e("PLAY", "play error: " + e.getMessage());
            Toast.makeText(PlayActivity.this, "播放失败", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onClick(View v) {
        WsRequest req;
        switch (v.getId()) {
            // 播放实时视频
            case R.id.realtime_video:
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.stop();
                }
                sendVideoRequest();
                break;
            // 停止实时视频
            case R.id.stop_video:
                req = new WsRequest("STOP", dataHolder.getInfo().getHost(), null, itoa(addr), 9000);
                dataHolder.getWs().send(gson.toJson(req));
                Log.i("PLAY", "send request: " + gson.toJson(req));
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.stop();
                }
                break;
            // 停止文件
            case R.id.stop_file_video:
                req = new WsRequest("STOP", dataHolder.getInfo().getHost(), null, itoa(addr), 9002);
                dataHolder.getWs().send(gson.toJson(req));
                Log.i("PLAY", "send request: " + gson.toJson(req));
                if (mMediaPlayerFile.isPlaying()) {
                    mMediaPlayerFile.stop();
                }
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.i("FileList", "click file " + position);
        if (mMediaPlayerFile.isPlaying()) {
            mMediaPlayerFile.stop();
        }
        String src = dataHolder.getInfo().getFiles().get(position);
        WsRequest req = new WsRequest("SEND", dataHolder.getInfo().getHost(), src, itoa(addr), 9002);
        sendFileRequest(req);
    }

    private String itoa(int addr) {
        return String.format(Locale.getDefault(), "%d.%d.%d.%d",
                addr & 0xff, addr >> 8 & 0xff, addr >> 16 & 0xff, addr >> 24 & 0xff);
    }
}
