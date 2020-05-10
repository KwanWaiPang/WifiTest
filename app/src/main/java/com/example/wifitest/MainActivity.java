package com.example.wifitest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION = 1;//设置权限之后回调函数中用于区别不同权限回调的自定义常量值
    String wifi_name;
    int wifi_rssi;

    //自定义一个数据类型，后面用于存储与排序
    public class WifiStream
    {
        String WifiStream_name;
        int WifiStream_rssi;

        public WifiStream(String ssid, int level) {
            this.WifiStream_name = ssid;
            this.WifiStream_rssi = level;
        }
    }

    ArrayAdapter<String> adapter;
    List<String> WifiList = new ArrayList<>();
    WifiManager wifiManager;//想要获得wifi信息就必须要一个WifiManager对象


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //定义按键实例
        Button button1=(Button) findViewById(R.id.wifi_rssi);
        //定义按钮点击事件
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //想要获得wifi信息就必须要一个WifiManager对象
                wifiManager= (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);//获取wifi服务
                assert wifiManager != null;

                //创建WifiInfo对象
                WifiInfo wifiInfo=wifiManager.getConnectionInfo();
                wifi_name=wifiInfo.getSSID();

                //华为手机要通过下面代码才可以获得wifi名称
                int networkID=wifiInfo.getNetworkId();
                List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
                for (WifiConfiguration wifiConfiguration:configuredNetworks){
                    if (wifiConfiguration.networkId==networkID){
                        wifi_name=wifiConfiguration.SSID;
                        break;
                    }
                }

                wifi_rssi=wifiInfo.getRssi();
                //通过Toast输出
                Toast.makeText(MainActivity.this, "current WIFI:"+"rssi:"+wifi_rssi+"---wifiId:"+wifi_name,Toast.LENGTH_SHORT).show();

            }
        });


        //////////////**************下面才是扫描wifi列表*********************/////////////////
        //调用扫描wifi列表。调用的函数会返回wifi列表
        //需要权限才可以获取wifi列表
//        registerPermission();//里面修改了WifiList
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] { Manifest.permission.ACCESS_COARSE_LOCATION }, PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION);

        } else {
            getWifiList();//调用上面函数获取wifi列表
        }

        //获取listview控件实例
        ListView listwifiView=(ListView) findViewById(R.id.wifi_list_view);

        //设置适配器对应listview
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, WifiList);
        listwifiView.setAdapter(adapter);

        adapter.notifyDataSetChanged();//并通知刷新一下ListView

    }


    //扫描wifi列表
    //通过wifiManager获取wifi列表
    public void getWifiList() {
        //定义一个WifiManager对象
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);

        List<ScanResult> scanWifiList = wifiManager.getScanResults();
        List<ScanResult> wifiList = new ArrayList<>();//最终返回的列表


        List <WifiStream> list = new ArrayList<WifiStream>();

        if (scanWifiList != null && scanWifiList.size() > 0) {

            for (int i = 0; i < scanWifiList.size(); i++) {
                ScanResult scanResult = scanWifiList.get(i);
                if (!scanResult.SSID.isEmpty()) {//wifi名称不是空的话
                    String key = scanResult.SSID + " " + scanResult.level;//显示wifi的名称以及其RSSI
                    list.add(new WifiStream(scanResult.SSID, scanResult.level));

                }
            }
        }
        //通过Collections.sort(List , Comparator c)方法来进行排序
        //按照rssi来排序
        Collections.sort(list,comparator);

        //然后将list转为string放入WifiList中
        for (int i=0;i<list.size();i++){
            String key=list.get(i).WifiStream_name+""+list.get(i).WifiStream_rssi;
            WifiList.add(key);//输出key（wifi名称+RSSI）
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION) {
            getWifiList();
        }
    }

    //按照rssi来排序(小到大排序。)
    static Comparator<WifiStream> comparator = new Comparator<WifiStream>() {
        @Override
        public int compare(WifiStream p1 , WifiStream p2 ) {
            if( p1.WifiStream_rssi > p2.WifiStream_rssi ){
                return 1 ;  //正数
            }else if ( p1.WifiStream_rssi < p2.WifiStream_rssi) {
                return -1 ;  //负数
            }else {
                return 0;  //相等为0
            }
        }
    };
//    private class SortRssi implements Comparator<WifiStream> {
//        @Override
//        public int compare(MainActivity.WifiStream o1, MainActivity.WifiStream o2) {
//            return o1.WifiStream_rssi-o2.WifiStream_rssi;
//        }
//    }
}

