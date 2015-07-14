package io.blinktech.wifip2pserver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mayank on 7/13/15.
 */
public class MyBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private MainActivity mActivity;
    static final String TAG = "log";



    public MyBroadcastReceiver(WifiP2pManager mManager, WifiP2pManager.Channel mChannel, MainActivity mActivity){
        super();
        this.mManager = mManager;
        this.mChannel = mChannel;
        this.mActivity = mActivity;
    }

    @Override
    public void onReceive(Context context, Intent intent){
        String action = intent.getAction();

        if(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)){
            // Wifi Enabled or not
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE,-1);
            if(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED)
            {
                Log.println(Log.ASSERT,TAG,"Wifi state enabled");

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

            }
            else
                Log.println(Log.ASSERT,TAG,"Wifi state disabled");
        }

        else if(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)){
            //Call Manager to get list of current peers

            Log.println(Log.ASSERT,TAG,"P2P Peers Changed");
            if(mManager != null){
                mManager.requestPeers(mChannel,mActivity);
            }

        }

        else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)){
            //Respond to new connections or disconnections.
            if(mManager == null) return;
            NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if(networkInfo.isConnected()){
                mManager.requestConnectionInfo(mChannel,mActivity);
            }
        }

        else if(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)){
            // Respond to this devices WiFi state change
        }
    }



   /* @Override
    public void connect(){
        WifiP2pDevice device = (WifiP2pDevice) peers.get(0);

        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;
        config.groupOwnerIntent = 15;

        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int i) {
                // Toast.makeText(MainActivity.this, "Connect Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
*/

}
