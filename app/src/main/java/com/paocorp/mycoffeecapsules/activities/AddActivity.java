package com.paocorp.mycoffeecapsules.activities;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.paocorp.mycoffeecapsules.R;
import com.paocorp.mycoffeecapsules.db.CapsuleHelper;
import com.paocorp.mycoffeecapsules.models.Capsule;


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
                switch (spinColor) {
                    case "red":
                        imgColor = "custom_red";
                        break;
                    case "black":
                        imgColor = "custom_black";
                        break;
                    default:
                        imgColor = "custom";
                        break;
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

    @Override
    public void onBackPressed() {
        Intent setIntent = new Intent(this, MainActivity.class);
        setIntent.addCategory(Intent.CATEGORY_HOME);
        setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(setIntent);
        finish();
    }

}
