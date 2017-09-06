package com.merilonstudio.mycoffeecapsulesinventory.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import com.merilonstudio.mycoffeecapsulesinventory.R;
import com.merilonstudio.mycoffeecapsulesinventory.db.CapsuleHelper;
import com.merilonstudio.mycoffeecapsulesinventory.db.CapsuleTypeHelper;
import com.merilonstudio.mycoffeecapsulesinventory.db.DatabaseHelper;
import com.merilonstudio.mycoffeecapsulesinventory.models.Capsule;
import com.merilonstudio.mycoffeecapsulesinventory.models.CapsuleType;

import java.util.ArrayList;
import java.util.HashMap;

public class SplashActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    SharedPreferences sharedPref;
    ArrayList<CapsuleType> listCapsuleType;
    HashMap<String, ArrayList<Capsule>> listDataCapsules;
    ArrayList<String> types;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        sharedPref = this.getPreferences(Context.MODE_PRIVATE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                new PrefetchData().execute();
            }
        }, 3000);

    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        return false;
    }

    /**
     * Async Task to make http call
     */
    private class PrefetchData extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // before making http calls

        }

        @Override
        protected Void doInBackground(Void... arg0) {

            long data_imported = sharedPref.getInt(getString(R.string.data_imported), 0);

            if (data_imported == 0) {
                DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
                dbHelper.startImportation();
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt(getString(R.string.data_imported), 1);
                editor.apply();
            }
            types = new ArrayList<String>();
            CapsuleTypeHelper capsuleTypeHelper = new CapsuleTypeHelper(getApplicationContext());
            CapsuleHelper capsuleHelper = new CapsuleHelper(getApplicationContext());
            listCapsuleType = capsuleTypeHelper.getAllCapsuleTypes();
            listDataCapsules = new HashMap<String, ArrayList<Capsule>>();

            for (CapsuleType type : listCapsuleType) {
                ArrayList<Capsule> capsulesByType = capsuleHelper.getAllCapsulesByType(type.getId());
                if (capsulesByType.size() > 0) {
                    types.add(type.getName());
                    listDataCapsules.put(type.getName(), capsulesByType);
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // After completing http call
            // will close this activity and launch main activity
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            intent.putExtra("types", types);
            intent.putExtra("dataCapsules", listDataCapsules);

            startActivity(intent);
            finish();
        }

    }
}
