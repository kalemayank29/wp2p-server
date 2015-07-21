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
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.apache.commons.io.IOUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.ConnectionEventListener;

public class MainActivity extends AppCompatActivity implements WifiP2pManager.PeerListListener, WifiP2pManager.ConnectionInfoListener, WifiP2pManager.GroupInfoListener {

    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    MyBroadcastReceiver mReceiver;
    public  static final String TAG = "log";
    IntentFilter mIntentFilter;
    Button button;
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

/*
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.println(Log.ASSERT,TAG,"Discovery process success");
            }

            @Override
            public void onFailure(int i) {
                Log.println(Log.ASSERT,TAG,"Discovery process failed");
            }
        });*/

        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deletePersistentGroups();
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
        ///connect()
        //;

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

       /* mManager.requestGroupInfo(mChannel, new WifiP2pManager.GroupInfoListener() {
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
        });*/
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


           /* mManager.stopPeerDiscovery(mChannel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Log.e("Peer discovery stopped", "Here");
                }

                @Override
                public void onFailure(int i) {
                    Log.e("Still discovering",String.valueOf(i));

                }
            });*/

            //deletePersistentGroups();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //deletePersistentGroups();

            new FileServerAsyncTask(this.getApplicationContext(),mManager,mChannel,this).execute();
        }
            else if(!info.isGroupOwner){
            Log.println(Log.ASSERT, TAG, "Is not the group owner");
        }
    }

    @Override
    public void onGroupInfoAvailable(WifiP2pGroup group){

       /* try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/

        //
        // connect();
                Log.println(Log.ASSERT,TAG,group.toString());
    }

    public void deletePersistentGroups(){
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

    public static class FileServerAsyncTask extends AsyncTask<Object, Void, String> {


        private Context context;
        private WifiP2pManager mManager;
        private WifiP2pManager.Channel mChannel;
        private MainActivity mActivity;
        private ArrayList<HashMap<String,String>> drugMap;

        public FileServerAsyncTask(Context context, WifiP2pManager mManager, WifiP2pManager.Channel mChannel, MainActivity mActivity) {
            this.context = context;
            this.mManager = mManager;
            this.mChannel = mChannel;
            this.mActivity = mActivity;
        }

        @Override
        protected String doInBackground(Object[] objects) {
            Log.println(Log.ASSERT,"log","Its here");
            ServerSocket serverSocket = null;
            try {

                serverSocket = new ServerSocket(8888);
                Socket client = serverSocket.accept();

                Log.e("Inside","try");


                InputStream inputstream = client.getInputStream();
                byte[] buffer = IOUtils.toByteArray(inputstream);
                ByteArrayInputStream bInStream = new ByteArrayInputStream(buffer);
                ObjectInput in = null;

                try {
                    in = new ObjectInputStream(bInStream);
                    drugMap = (ArrayList<HashMap<String,String>>) in.readObject();
                            //new ArrayList<HashMap<String, String>>();
                  // ArrayList<HashMap<String,String>>  temp =
                   // element.addAll();
                   // Log.println(Log.ASSERT,TAG,String.valueOf(element.get(0).get("Kyle")));
                    ArrayList<NameValuePair> parameters = new ArrayList<NameValuePair>();

                    Log.println(Log.ASSERT,TAG,String.valueOf(drugMap.size()));

                   /* for (Map.Entry<String, String> entry:element.entrySet()
                         ) {
                        parameters.add(new BasicNameValuePair(entry.getKey(),entry.getValue()));

                    }*/
                    //for(int i=0;i<parameters.size();i++){
                        //Log.println(Log.ASSERT,parameters.get(i).getName(),parameters.get(i).getValue());
                        //Log.println(Log.ASSERT, "log", element.get(0).get("Kyle"));
                   // Log.println(Log.ASSERT,"log",element.get(1).get("Mayank"));

                  //  }

//                    Log.println(Log.ASSERT,parameters.get(1).getName(),parameters.get(1).getValue());


                    //Log.println(Log.ASSERT,"Element: ", String.valueOf(element.get("Mayank")));
                    //List<String> list = new ArrayList<String>(element.keySet());
                    //Log.println(Log.ASSERT,TAG,list.get(1));

                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

                //String data = new String(buffer, "UTF-8");
                //Log.println(Log.ASSERT, "DATA: ", data);

                //Toast.makeText(context,data, Toast.LENGTH_LONG).show();
                serverSocket.close();
                return "Data Stream closed";
            }
            catch(IOException e){
                e.printStackTrace();
                return "In Catch";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Log.e("In", "Post Execute");
            Log.e("RESULT",result);
           // MyApplication.count++;
            //super.onPostExecute(result);
            //FileServerAsyncTask myTast = new FileServerAsyncTask(context);
            //Toast.makeText(context, "Data Transfer successful" + result, Toast.LENGTH_LONG).show();

            mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Log.e("Group","Removed");
                }

                @Override
                public void onFailure(int i) {
                    Log.e("Group not removed","no");

                }
            });
            mManager.stopPeerDiscovery(mChannel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Log.e("Peer discovery stopped", "Here");
                }

                @Override
                public void onFailure(int i) {
                    Log.e("Still discovering",String.valueOf(i));

                }
            });
            mActivity.deletePersistentGroups();

            Bundle bundle = new Bundle();
            bundle.putSerializable("map",drugMap);
            Intent intent = new Intent(mActivity, Main2Activity.class);
            intent.putExtras(bundle);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(android.content.Intent.ACTION_VIEW);
            this.context.startActivity(intent);
           // this.context.startActivity(intent);


        }
    }
}
