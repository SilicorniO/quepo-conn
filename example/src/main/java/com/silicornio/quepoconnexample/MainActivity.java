package com.silicornio.quepoconnexample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.silicornio.quepoconn.QPConnConfig;
import com.silicornio.quepoconn.QPConnManager;
import com.silicornio.quepoconn.QPResponse;
import com.silicornio.quepoconn.QPResponseListener;
import com.silicornio.quepoconn.general.L;
import com.silicornio.quepoconn.general.QPL;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init(){

        L.showLogs = true;
        QPL.showLogs = true;

        QPConnConfig config = new QPConnConfig();
        config.setUrl("http://192.168.1.220/temp/quepotest/test.html");

        QPConnManager manager = new QPConnManager();
        manager.addConn(config, null, new QPResponseListener() {
            @Override
            public void responseOnMainThread(QPResponse response, QPConnConfig config) {
                String text = new String(response.getData());
                L.d("DATA: " + text);
            }
        });

    }
}
