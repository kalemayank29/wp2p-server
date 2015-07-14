package io.blinktech.wifip2pserver;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import javax.sql.ConnectionEventListener;

public class MainActivity extends AppCompatActivity implements WifiP2pManager.PeerListListener, WifiP2pManager.ConnectionInfoListener, WifiP2pManager.GroupInfoListener {

    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    MyBroadcastReceiver mReceiver;
    public  static final String TAG = "log";
    IntentFilter mIntentFilter;

    public List peers = new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        manager.setWifiEnabled(false);
        Log.println(Log.ASSERT, TAG, "Resetting WIFI");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        manager.setWifiEnabled(true);

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this,getMainLooper(),null);
        mReceiver = new MyBroadcastReceiver(mManager,mChannel,this);


        deletePersistentGroups();

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);


        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.println(Log.ASSERT,TAG,"Discovery process success");
            }

            @Override
            public void onFailure(int i) {
                Log.println(Log.ASSERT,TAG,"Discovery process failed");
            }
        });

        //mManager.connect(mChannel,);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override   //Register broadcast receiver with intent values
    protected  void onResume(){
        super.onResume();
        registerReceiver(mReceiver,mIntentFilter);
    }

    @Override   //Register broadcast receiver with intent values
    protected  void onPause(){
        super.onPause();
        unregisterReceiver(mReceiver);
    }


    @Override
    public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {

        peers.clear();
        peers.addAll(wifiP2pDeviceList.getDeviceList());

        if(peers.size()==0){
            Log.println(Log.ASSERT,TAG,"0 Devices found");
            return;
        }
        else {
            Log.println(Log.ASSERT,TAG,String.valueOf(peers.size())+" Devices found");
        }

        mManager.requestGroupInfo(mChannel, new WifiP2pManager.GroupInfoListener() {
            @Override
            public void onGroupInfoAvailable(WifiP2pGroup wifiP2pGroup) {
                if(wifiP2pGroup != null)
                {
                    mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {

                            deletePersistentGroups();

                            mManager.createGroup(mChannel, new WifiP2pManager.ActionListener() {
                                @Override
                                public void onSuccess() {
                                    Log.println(Log.ASSERT, TAG, "Group created");
                                }
                                @Override
                                public void onFailure(int i) {
                                    Log.println(Log.ASSERT, TAG, "Group creation failed");
                                }
                            });
                        }
                        @Override
                        public void onFailure(int i) {

                        }
                    });
                }

                else{
                    mManager.createGroup(mChannel, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            Log.println(Log.ASSERT, TAG, "Group created");
                        }

                        @Override
                        public void onFailure(int i) {
                            Log.println(Log.ASSERT, TAG, "Group creation failed");
                        }
                    });
                    //connect();
                }

            }
        });
       /*
        mManager.createGroup(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.println(Log.ASSERT, TAG, "Group created");
            }

            @Override
            public void onFailure(int i) {
                Log.println(Log.ASSERT, TAG, "Group creation failed");
            }
        });
*/
    }

    public void connect(){
        // Picking the first device found on the network.
        WifiP2pDevice device = (WifiP2pDevice) peers.get(0);
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;
        config.groupOwnerIntent = 15;

        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
            }

            @Override
            public void onFailure(int reason) {
                // Toast.makeText(WiFiDirectActivity.this, "Connect failed. Retry.",
                //  Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onConnectionInfoAvailable(final WifiP2pInfo info){
        InetAddress groupOwnerAddress = info.groupOwnerAddress;
        Log.println(Log.ASSERT,TAG,"Connection info available");
        if(info.groupFormed && info.isGroupOwner) {
            Log.println(Log.ASSERT, TAG, "This is group owner");
            Log.println(Log.ASSERT, TAG, groupOwnerAddress.getHostAddress());
            deletePersistentGroups();
            try {
                Thread.sleep(9000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
            else if(!info.isGroupOwner){
            Log.println(Log.ASSERT, TAG, "Is not the group owner");
        }
    }

    @Override
    public void onGroupInfoAvailable(WifiP2pGroup group){
        connect();
    }

    private void deletePersistentGroups(){
        try {
            Method[] methods = WifiP2pManager.class.getMethods();

            for (int i = 0; i < methods.length; i++) {
                if (methods[i].getName().equals("deletePersistentGroup")) {
                    // Delete any persistent group
                    for (int netid = 0; netid < 32; netid++) {
                        methods[i].invoke(mManager, mChannel, netid, null);
                    }
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
