package cn.edu.bupt.owatch;

import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import java.util.ArrayList;

import cn.edu.bupt.owatch.adapter.InfoAdapter;
import cn.edu.bupt.owatch.bean.ServerInfo;
import cn.edu.bupt.owatch.bean.WsResponse;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private WebSocket ws;
    private OkHttpClient client;
    private ArrayList<ServerInfo> serverInfoList = new ArrayList<>();
    private InfoAdapter adapter;
    private ListView infoListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Button loginBtn = findViewById(R.id.login_button);
        loginBtn.setOnClickListener(this);

        client = new OkHttpClient.Builder().build();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.login_button) {
            EditText serverText = findViewById(R.id.server_input);
            EditText portText = findViewById(R.id.port_input);
            EditText usernameText = findViewById(R.id.username_input);
            EditText passwordText = findViewById(R.id.password_input);

            String server = serverText.getText().toString();
            if (server.equals("")) {
                showToast("服务器不能为空");
                return;
            }
            String port = portText.getText().toString();
            if (port.equals("")) {
                showToast("端口不能为空");
                return;
            }
            String username = usernameText.getText().toString();
            if (username.equals("")) {
                showToast("用户名不能为空");
                return;
            }
            String password = passwordText.getText().toString();
            if (password.equals("")) {
                showToast("密码不能为空");
                return;
            }

            Request req = new Request.Builder()
                    .header("username", username)
                    .header("password", password)
                    .url("ws://" + server + ":" + port + "/ws")
                    .build();
            if (ws != null) {
                ws.cancel();
            }
            ws = client.newWebSocket(req, new WebSocketListener() {
                private Gson gson = new Gson();

                @Override
                public void onOpen(WebSocket webSocket, Response response) {
                    super.onOpen(webSocket, response);
                    Log.i("ws", "on open");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setContentView(R.layout.activity_info);
                            infoListView = findViewById(R.id.info_list);
                            adapter = new InfoAdapter(MainActivity.this, serverInfoList);
                            infoListView.setAdapter(adapter);
                        }
                    });
                }

                @Override
                public void onMessage(WebSocket webSocket, String text) {
                    super.onMessage(webSocket, text);
                    Log.i("ws", "on message:" + text);
                    WsResponse resp = gson.fromJson(text, WsResponse.class);
                    serverInfoList = resp.getServerInfoList();
                    adapter.refresh(serverInfoList);
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                    super.onFailure(webSocket, t, response);
                    if (t instanceof java.net.ConnectException) {
                        Log.e("ws", "addr error");
                        showToastInThread("服务器地址错误");
                    } else if (t instanceof java.net.ProtocolException) {
                        Log.e("ws", "password error");
                        showToastInThread("用户名或密码错误");
                    } else if (t instanceof java.io.EOFException) {
                        Log.e("ws", "connection closed");
                        showToastInThread("连接断开");
                    } else if (t instanceof java.net.SocketTimeoutException) {
                        Log.e("ws", "timeout");
                        showToastInThread("连接超时");
                    }
                    Log.i("ws", "on failure:" + t.getLocalizedMessage() + "type: " + t.getClass().getName());
                }
            });
            setContentView(R.layout.activity_info);
        }
    }

    private void showToastInThread(String text) {
        Looper.prepare();
        Toast.makeText(MainActivity.this, text, Toast.LENGTH_LONG).show();
        Looper.loop();
    }

    private void showToast(String text) {
        Toast.makeText(MainActivity.this, text, Toast.LENGTH_LONG).show();
    }
}
