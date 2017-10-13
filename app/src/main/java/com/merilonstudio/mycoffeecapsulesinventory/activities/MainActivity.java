package com.merilonstudio.mycoffeecapsulesinventory.activities;

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.merilonstudio.mycoffeecapsulesinventory.R;
import com.merilonstudio.mycoffeecapsulesinventory.adapters.CapsuleExpandableListAdapter;
import com.merilonstudio.mycoffeecapsulesinventory.db.CapsuleHelper;
import com.merilonstudio.mycoffeecapsulesinventory.db.CapsuleTypeHelper;
import com.merilonstudio.mycoffeecapsulesinventory.models.Capsule;
import com.merilonstudio.mycoffeecapsulesinventory.models.CapsuleType;
import com.merilonstudio.mycoffeecapsulesinventory.models.DBSave;
import com.merilonstudio.mycoffeecapsulesinventory.models.Global;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private HashMap<String, ArrayList<Capsule>> listDataCapsules;
    private CapsuleExpandableListAdapter listAdapter;
    protected InterstitialAd mInterstitialAd = new InterstitialAd(this);
    ArrayList<String> types;
    ExpandableListView expListView;
    ExpandableListView expListViewSearch;
    CapsuleHelper capsuleHelper;
    PackageInfo pInfo;
    ShareDialog shareDialog;
    CallbackManager callbackManager;
    CapsuleTypeHelper capsuleTypeHelper;
    ArrayList<CapsuleType> listCapsuleType;
    NavigationView navigationView;
    static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 31415;

    GoogleApiClient mGoogleApiClient;
    SignInButton signInButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        LayoutInflater.from(this).inflate(R.layout.nav_header_main, navigationView);

        listDataCapsules = (HashMap<String, ArrayList<Capsule>>) getIntent().getSerializableExtra("dataCapsules");
        types = getIntent().getStringArrayListExtra("types");
        capsuleHelper = new CapsuleHelper(getBaseContext());

        if (types == null || listDataCapsules == null || types.size() == 0 || listDataCapsules.size() == 0) {
            types = new ArrayList<String>();
            CapsuleTypeHelper capsuleTypeHelper = new CapsuleTypeHelper(getApplicationContext());
            listCapsuleType = capsuleTypeHelper.getAllCapsuleTypes();
            listDataCapsules = new HashMap<String, ArrayList<Capsule>>();

            for (CapsuleType type : listCapsuleType) {
                ArrayList<Capsule> capsulesByType = capsuleHelper.getAllCapsulesByType(type.getId());
                if (capsulesByType.size() > 0) {
                    types.add(type.getName());
                    listDataCapsules.put(type.getName(), capsulesByType);
                }
            }
        }

        expListView = (ExpandableListView) findViewById(R.id.first_list);
        listAdapter = new CapsuleExpandableListAdapter(this, types, listDataCapsules);
        // setting list adapter
        expListView.setAdapter(listAdapter);

        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            TextView txv = (TextView) findViewById(R.id.appVersion);
            String APPINFO = txv.getText() + " v" + pInfo.versionName;
            txv.setText(APPINFO);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        FacebookSdk.setApplicationId(getResources().getString(R.string.facebook_app_id));
        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(this);

        Global.mDatabase = FirebaseDatabase.getInstance().getReference();
        Global.mAuth = FirebaseAuth.getInstance();
        signInButton = (SignInButton) findViewById(R.id.sign_in_button);

        if (Global.mAuth.getCurrentUser() != null) {

            signInButton.setVisibility(View.GONE);

            Snackbar snackbar = Snackbar.make(navigationView, getResources().getString(R.string.loggedin, Global.mAuth.getCurrentUser().getEmail()), Snackbar.LENGTH_LONG);
            snackbar.show();

        } else {
            signInButton.setSize(SignInButton.SIZE_STANDARD);
            signInButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    signIn();
                }
            });
        }

        if (Global.adDate == null) {
            Global.adDate = new Date();
            launchInterstitial();
        } else {
            Date currentDate = new Date();
            if (isNetworkAvailable() && currentDate.getTime() - Global.adDate.getTime() > 60000 * 5) {
                launchInterstitial();
            }
        }


    }

    private void signIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestId()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                //.enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, 31416);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == 31416) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            firebaseAuthWithGoogle(acct);
        } else {
            // Signed out, show unauthenticated UI.
            signInButton.setVisibility(View.VISIBLE);
        }
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        Global.mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            signInButton.setVisibility(View.GONE);
                            checkDB();
                        } else {
                            Snackbar snackbar = Snackbar.make(navigationView, getResources().getString(R.string.authFailed), Snackbar.LENGTH_SHORT);
                            snackbar.show();
                        }
                    }
                });

    }

    private void checkDB() {
        DatabaseReference ref = Global.mDatabase.child("caps").child(Global.mAuth.getCurrentUser().getUid());
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DBSave dbSave = dataSnapshot.getValue(DBSave.class);

                final String settingVersion = getResources().getString(R.string.versionDBFirebase);
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                int versionDBSaved = settings.getInt(settingVersion, 1);

                if (dbSave != null && !dbSave.getContent().isEmpty() && dbSave.getVersion() > versionDBSaved) {
                    importDBDialog(dbSave);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Snackbar snackbar = Snackbar.make(navigationView, getResources().getString(R.string.importNOK), Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        });
    }

    private void importDBDialog(final DBSave dbSave) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(getBaseContext());
        final View v = inflater.inflate(R.layout.dialog_importdb, null);
        builder.setCancelable(false);

        builder.setView(v)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (capsuleHelper.importDBFromFirebase(dbSave)) {

                            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(v.getContext());
                            SharedPreferences.Editor editor = settings.edit();
                            final String settingVersion = v.getContext().getResources().getString(R.string.versionDBFirebase);
                            editor.putInt(settingVersion, dbSave.getVersion());
                            editor.apply();

                            Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(i);
                            finish();
                        }
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

        AlertDialog alert = builder.create();
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(alert.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        alert.show();
        alert.getWindow().setAttributes(lp);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (Global.adDate == null) {
            Global.adDate = new Date();
            launchInterstitial();
        } else {
            Date currentDate = new Date();
            if (isNetworkAvailable() && currentDate.getTime() - Global.adDate.getTime() > 60000 * 5) {
                Global.adDate = new Date();
                launchInterstitial();
            }
        }
    }

    boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    protected void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder().build();
        mInterstitialAd.loadAd(adRequest);
    }

    protected void showInterstitial() {
        if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public void zero(View v) {
        View convertView = (View) v.getParent().getParent();
        final NumberPicker nb = (NumberPicker) convertView.findViewById(R.id.qty);
        nb.getWrapSelectorWheel();
        nb.setMinValue(0);
        nb.setMaxValue(10000);
        nb.setValue(0);
    }

    public void plus10(View v) {
        View convertView = (View) v.getParent().getParent();
        final NumberPicker nb = (NumberPicker) convertView.findViewById(R.id.qty);
        nb.getWrapSelectorWheel();
        nb.setMinValue(0);
        nb.setMaxValue(10000);
        if (nb.getValue() <= 10000 - 10) {
            nb.setValue(nb.getValue() + 10);
        }
    }

    public void plus50(View v) {
        View convertView = (View) v.getParent().getParent();
        final NumberPicker nb = (NumberPicker) convertView.findViewById(R.id.qty);
        nb.getWrapSelectorWheel();
        nb.setMinValue(0);
        nb.setMaxValue(10000);
        if (nb.getValue() <= 10000 - 50) {
            nb.setValue(nb.getValue() + 50);
        }
    }

    public void plus100(View v) {
        View convertView = (View) v.getParent().getParent();
        final NumberPicker nb = (NumberPicker) convertView.findViewById(R.id.qty);
        nb.getWrapSelectorWheel();
        nb.setMinValue(0);
        nb.setMaxValue(10000);
        if (nb.getValue() <= 10000 - 100) {
            nb.setValue(nb.getValue() + 100);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        SearchView search = (SearchView) menu.findItem(R.id.action_search).getActionView();
        search.setSearchableInfo(manager.getSearchableInfo(getComponentName()));

        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(final String query) {
                if (query.length() >= 3) {
                    findViewById(R.id.first_list).setVisibility(View.GONE);
                    findViewById(R.id.capsules_search_container).setVisibility(View.VISIBLE);

                    HashMap<String, ArrayList<Capsule>> listDataCapsulesSearch = new HashMap<String, ArrayList<Capsule>>();
                    expListViewSearch = (ExpandableListView) findViewById(R.id.search_list);


                    ArrayList<String> types = new ArrayList<String>();
                    capsuleTypeHelper = new CapsuleTypeHelper(getApplicationContext());

                    for (CapsuleType type : capsuleTypeHelper.getAllCapsuleTypes()) {
                        ArrayList<Capsule> listDataSearch = capsuleHelper.getSearchCapsules(type, query);
                        if (listDataSearch.size() > 0) {
                            listDataCapsulesSearch.put(type.getName(), listDataSearch);
                            types.add(type.getName());
                        }
                    }
                    if (listDataCapsulesSearch.size() > 0) {
                        final CapsuleExpandableListAdapter searchListAdapter = new CapsuleExpandableListAdapter(MainActivity.this, types, listDataCapsulesSearch);
                        expListViewSearch.setAdapter(searchListAdapter);
                        findViewById(R.id.noResults).setVisibility(View.GONE);
                        findViewById(R.id.search_list).setVisibility(View.VISIBLE);
                    } else {
                        expListViewSearch.setAdapter((BaseExpandableListAdapter) null);
                        findViewById(R.id.noResults).setVisibility(View.VISIBLE);
                        findViewById(R.id.search_list).setVisibility(View.GONE);
                    }

                } else {
                    capsuleTypeHelper = new CapsuleTypeHelper(getApplicationContext());
                    listCapsuleType = capsuleTypeHelper.getAllCapsuleTypes();
                    listDataCapsules = new HashMap<String, ArrayList<Capsule>>();

                    for (CapsuleType type : listCapsuleType) {
                        types.add(type.getName());
                        ArrayList<Capsule> capsulesByType = capsuleHelper.getAllCapsulesByType(type.getId());
                        listDataCapsules.put(type.getName(), capsulesByType);
                    }
                    listAdapter = new CapsuleExpandableListAdapter(getApplicationContext(), types, listDataCapsules);
                    expListView.setAdapter(listAdapter);

                    findViewById(R.id.first_list).setVisibility(View.VISIBLE);
                    findViewById(R.id.capsules_search_container).setVisibility(View.GONE);
                }
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            return true;
        } else if (id == R.id.action_add) {
            Intent intent = new Intent(this, AddActivity.class);
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            finish();
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Intent intent = new Intent(this, MainActivity.class);

        if (id == R.id.nav_share) {
            if (ShareDialog.canShow(ShareLinkContent.class)) {
                String fbText = getResources().getString(R.string.fb_ContentDesc);
                ShareLinkContent linkContent = new ShareLinkContent.Builder()
                        .setContentUrl(Uri.parse(getResources().getString(R.string.store_url)))
                        .setContentTitle(getResources().getString(R.string.app_name))
                        .setContentDescription(fbText)
                        .setImageUrl(Uri.parse(getResources().getString(R.string.app_icon_url)))
                        .build();

                shareDialog.show(linkContent);
            }
        } else if (id == R.id.nav_rate) {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.store_url)));
        } else if (id == R.id.nav_order) {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.order_url)));
        } else if (id == R.id.nav_add) {
            intent = new Intent(this, AddActivity.class);
        } else if (id == R.id.nav_export) {
            exportData();
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            return true;
        } else if (id == R.id.nav_tw) {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.twitter.com/intent/tweet?url=" + getResources().getString(R.string.store_url) + "&text=" + getResources().getString(R.string.fb_ContentDesc)));
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        finish();
        startActivity(intent);
        return true;
    }

    private void launchInterstitial() {
        mInterstitialAd.setAdUnitId(this.getResources().getString(R.string.interstitial));
        requestNewInterstitial();
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                showInterstitial();
            }
        });
    }

    private void exportData() {
        if (!capsuleHelper.exportDatabase()) {
            if (Build.VERSION.SDK_INT >= 23) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                }
            }
            Snackbar snackbar = Snackbar
                    .make(navigationView, getResources().getString(R.string.permissionDenied), Snackbar.LENGTH_LONG)
                    .setAction(getResources().getString(R.string.check).toUpperCase(), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            final Intent i = new Intent();
                            i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            i.addCategory(Intent.CATEGORY_DEFAULT);
                            i.setData(Uri.parse("package:" + getApplicationContext().getPackageName()));
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                            i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                            getApplicationContext().startActivity(i);
                        }
                    });

            snackbar.setActionTextColor(getApplicationContext().getResources().getColor(R.color.yellow_darken1));
            snackbar.show();
        } else {
            Snackbar snackbar = Snackbar
                    .make(navigationView, getResources().getString(R.string.exportSuccess), Snackbar.LENGTH_LONG)
                    .setAction(getResources().getString(R.string.see).toUpperCase(), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Uri selectedUri = Uri.parse(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath());
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(selectedUri, "resource/folder");
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            getApplicationContext().startActivity(intent);
                        }
                    });

            snackbar.setActionTextColor(getApplicationContext().getResources().getColor(R.color.yellow_darken1));
            snackbar.show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    exportData();
                } else {

                    Snackbar snackbar = Snackbar
                            .make(navigationView, getResources().getString(R.string.permissionDenied), Snackbar.LENGTH_LONG)
                            .setAction(getResources().getString(R.string.check).toUpperCase(), new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    final Intent i = new Intent();
                                    i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    i.addCategory(Intent.CATEGORY_DEFAULT);
                                    i.setData(Uri.parse("package:" + getApplicationContext().getPackageName()));
                                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                    i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                    getApplicationContext().startActivity(i);
                                }
                            });

                    snackbar.setActionTextColor(getApplicationContext().getResources().getColor(R.color.yellow_darken1));
                    snackbar.show();
                }
            }
        }
    }


}
