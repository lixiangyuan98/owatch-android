package cn.edu.bupt.owatch;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import java.util.ArrayList;

import cn.edu.bupt.owatch.adapter.InfoAdapter;
import cn.edu.bupt.owatch.bean.DataHolder;
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
    private Handler uiHandler = new Handler(new UIHandler());
    private DataHolder dataHolder;

    class UIHandler implements Handler.Callback, AdapterView.OnItemClickListener {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case 0:
                    Toast.makeText(MainActivity.this, "服务器地址错误", Toast.LENGTH_LONG).show();
                    break;
                case 1:
                    Toast.makeText(MainActivity.this, "用户名或密码错误", Toast.LENGTH_LONG).show();
                    break;
                case 2:
                    Toast.makeText(MainActivity.this, "连接断开", Toast.LENGTH_LONG).show();
                    break;
                case 3:
                    Toast.makeText(MainActivity.this, "连接超时", Toast.LENGTH_LONG).show();
                    break;
                case 4:
                    setContentView(R.layout.activity_info);
                    ListView infoListView = findViewById(R.id.info_list);
                    adapter = new InfoAdapter(MainActivity.this, serverInfoList);
                    infoListView.setAdapter(adapter);
                    infoListView.setOnItemClickListener(this);
                    break;
                case 5:
                    adapter.refresh(serverInfoList);
                    adapter.notifyDataSetChanged();
                    break;
                case 6:
                    Toast.makeText(MainActivity.this, "请求错误", Toast.LENGTH_LONG).show();
                    break;
            }
            return false;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Log.i("onClick", "position=" + position);
            ServerInfo info = serverInfoList.get(position);
            Intent intent = new Intent(getApplicationContext(), PlayActivity.class);
            dataHolder.setInfo(info);
            startActivity(intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Button loginBtn = findViewById(R.id.login_button);
        loginBtn.setOnClickListener(this);

        client = new OkHttpClient.Builder().build();
        dataHolder = (DataHolder)getApplication();
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
                    uiHandler.sendEmptyMessage(4);
                }

                @Override
                public void onMessage(WebSocket webSocket, String text) {
                    super.onMessage(webSocket, text);
                    Log.i("ws", "on message:" + text);
                    WsResponse resp = gson.fromJson(text, WsResponse.class);
                    Log.i("ws", "message=" + resp.getMessage());
                    if (resp.getMessage().equals("info")) {
                        serverInfoList = resp.getServerInfoList();
                        uiHandler.sendEmptyMessage(5);
                    } else if (resp.getCode() != 0) {
                        uiHandler.sendEmptyMessage(6);
                    }
                }

                @Override
                public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                    super.onFailure(webSocket, t, response);
                    if (t instanceof java.net.ConnectException) {
                        Log.e("ws", "addr error");
                        uiHandler.sendEmptyMessage(0);
                    } else if (t instanceof java.net.ProtocolException) {
                        Log.e("ws", "password error");
                        uiHandler.sendEmptyMessage(1);
                    } else if (t instanceof java.io.EOFException) {
                        Log.e("ws", "connection closed");
                        uiHandler.sendEmptyMessage(2);
                    } else if (t instanceof java.net.SocketTimeoutException) {
                        Log.e("ws", "timeout");
                        uiHandler.sendEmptyMessage(3);
                    }
                    Log.i("ws", "on failure:" + t.getLocalizedMessage() + "type: " + t.getClass().getName());
                }
            });
            dataHolder.setWs(ws);
        }
    }

    private void showToast(String text) {
        Toast.makeText(MainActivity.this, text, Toast.LENGTH_LONG).show();
    }
}
