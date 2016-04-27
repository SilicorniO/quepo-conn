package com.silicornio.quepoconnexample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.silicornio.quepoconn.QPConnConf;
import com.silicornio.quepoconn.QPConnConfig;
import com.silicornio.quepoconn.QPConnManager;
import com.silicornio.quepoconn.QPConnResponse;
import com.silicornio.quepoconn.QPResponseListener;
import com.silicornio.quepoconn.general.L;
import com.silicornio.quepoconn.general.QPL;
import com.silicornio.quepoconn.general.QPUtils;

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

        //get the configuration of connections
        QPConnConf conf = QPUtils.readConfObjectFromAssets(this, "connections.conf", QPConnConf.class);

        QPConnConfig config = conf.getService("test");

        QPConnManager manager = new QPConnManager();
        manager.addConn(config, null, new QPResponseListener() {
            @Override
            public void responseOnMainThread(QPConnResponse response, QPConnConfig config) {
                String text = new String(response.getData());
                L.d("DATA: " + text);
            }
        });

    }
}
