package com.merilonstudio.mycoffeecapsulesinventory.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseUser;
import com.merilonstudio.mycoffeecapsulesinventory.R;
import com.merilonstudio.mycoffeecapsulesinventory.db.CapsuleHelper;
import com.merilonstudio.mycoffeecapsulesinventory.models.Capsule;
import com.merilonstudio.mycoffeecapsulesinventory.models.DBSave;
import com.merilonstudio.mycoffeecapsulesinventory.models.Global;


public class AddActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setupActionBar();

        Spinner spinner = (Spinner) findViewById(R.id.img_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.caps_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.add, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Intent setIntent = new Intent(this, MainActivity.class);

        if (id == R.id.home) {
            setIntent = new Intent(this, MainActivity.class);
        } else if (id == R.id.action_valid) {
            TextView tvName = (TextView) findViewById(R.id.edit_capsulename);
            Spinner spinner = (Spinner) findViewById(R.id.img_spinner);
            String spinColor = spinner.getSelectedItem().toString().toLowerCase();
            String imgColor = "custom";

            if (spinColor.length() > 0) {
                if (spinColor.equals(getResources().getString(R.string.black).toLowerCase())) {
                    imgColor = "custom_black";
                } else if (spinColor.equals(getResources().getString(R.string.blue).toLowerCase())) {
                    imgColor = "custom_blue";
                } else if (spinColor.equals(getResources().getString(R.string.gold).toLowerCase())) {
                    imgColor = "custom_gold";
                } else if (spinColor.equals(getResources().getString(R.string.green).toLowerCase())) {
                    imgColor = "custom_green";
                } else if (spinColor.equals(getResources().getString(R.string.orange).toLowerCase())) {
                    imgColor = "custom_orange";
                } else if (spinColor.equals(getResources().getString(R.string.violet).toLowerCase())) {
                    imgColor = "custom_violet";
                } else {
                    imgColor = "custom";
                }
            }

            if (tvName != null) {
                String capname = tvName.getText().toString().trim();
                if (!capname.isEmpty()) {
                    tvName.getBackground().mutate().setColorFilter(getResources().getColor(R.color.green_darken1), PorterDuff.Mode.SRC_ATOP);
                    Capsule capsule = new Capsule();
                    capsule.setName(capname.substring(0, 1).toUpperCase() + capname.substring(1));
                    capsule.setImg(imgColor);
                    capsule.setQty(0);
                    capsule.setConso(0);
                    capsule.setNotif(0);
                    CapsuleHelper capsuleHelper = new CapsuleHelper(getApplicationContext());
                    capsuleHelper.insertCustomCapsule(capsule);

                    if (Global.mAuth != null) {
                        saveToFirebase();
                    }

                } else {
                    tvName.getBackground().mutate().setColorFilter(getResources().getColor(R.color.red_darken3), PorterDuff.Mode.SRC_ATOP);
                    tvName.setHint(this.getResources().getString(R.string.nameEmpty));
                    return true;
                }
            }
        }

        setIntent.addCategory(Intent.CATEGORY_HOME);
        setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(setIntent);
        finish();

        return super.onOptionsItemSelected(item);
    }

    private void saveToFirebase() {
        FirebaseUser user = Global.mAuth.getCurrentUser();
        CapsuleHelper capsuleHelper = new CapsuleHelper(this);

        if (user != null && !user.getUid().isEmpty() && capsuleHelper.exportDbToJson() && !Global.backupVal.isEmpty()) {

            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = settings.edit();
            final String settingVersion = getResources().getString(R.string.versionDBFirebase);
            int versionDBSaved = settings.getInt(settingVersion, 1) + 1;

            editor.putInt(settingVersion, versionDBSaved);
            editor.apply();

            DBSave dbSave = new DBSave();
            dbSave.setContent(Global.backupVal);
            dbSave.setVersion(versionDBSaved);

            Global.mDatabase.child("caps").child(user.getUid()).setValue(dbSave);
        }
    }

    @Override
    public void onBackPressed() {
        Intent setIntent = new Intent(this, MainActivity.class);
        setIntent.addCategory(Intent.CATEGORY_HOME);
        setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(setIntent);
        finish();
    }

}
