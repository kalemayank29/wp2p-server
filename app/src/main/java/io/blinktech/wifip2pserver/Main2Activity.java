package io.blinktech.wifip2pserver;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Main2Activity extends AppCompatActivity {
    ArrayList<HashMap<String,String>> drugMap = new ArrayList<HashMap<String, String>>();
    ArrayAdapter<HashMap<String,String>> medicineAdapter;
    ListView lv;
    Button button2;
    MedicineDbHandler dbHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        lv = (ListView) findViewById(R.id.curListView);
        button2 = (Button) findViewById(R.id.button2);
        dbHandler = new MedicineDbHandler(getApplicationContext());
        Intent intent = this.getIntent();
        Bundle bundle = getIntent().getExtras();

        drugMap = (ArrayList<HashMap<String,String>>) bundle.getSerializable("map");

        populateList();
        registerForContextMenu(lv);

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                for (HashMap<String,String> tempMap: drugMap
                     ) {
                   // Log.println(Log.ASSERT,"log",String.valueOf(tempMap.size()));
                    Medicine tempObject = new Medicine(1,tempMap.get("name"), tempMap.get("tab"), tempMap.get("exp_date"),
                            tempMap.get("bott_date"), tempMap.get("no_tab"), tempMap.get("patient_id"));
                    dbHandler.createMedicine(tempObject);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main2, menu);
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

    public void populateList(){
        medicineAdapter = new medicineListAdapter(this.drugMap);
        lv.setAdapter(medicineAdapter);
        medicineAdapter.notifyDataSetChanged();


    }
    private class medicineListAdapter extends ArrayAdapter<HashMap<String,String>>{

        List<HashMap<String, String>> medList;

        public medicineListAdapter(List<HashMap<String,String>> medList){
            super(Main2Activity.this, R.layout.medicine_item, medList);
            this.medList = medList;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.medicine_item, parent, false);
            }
                TextView medName = (TextView) convertView.findViewById(R.id.medName);
                medName.setText(medList.get(position).get("name"));

                TextView medMg = (TextView) convertView.findViewById(R.id.medMg);
                medMg.setText(medList.get(position).get("tab"));

                TextView medNoTab = (TextView) convertView.findViewById(R.id.medNoTab);
                medNoTab.setText(medList.get(position).get("exp_date"));

                TextView medBotExp = (TextView) convertView.findViewById(R.id.medBotExp);
                medBotExp.setText(medList.get(position).get("bott_date"));

                TextView medBotOpen = (TextView) convertView.findViewById(R.id.medBotOpen);
                medBotOpen.setText(medList.get(position).get("no_tab"));

                TextView medPatientId = (TextView) convertView.findViewById(R.id.medPatientId);
                medPatientId.setText(medList.get(position).get("patient_id"));


                return convertView;
            }
        }

}
