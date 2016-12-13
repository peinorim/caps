package com.paocorp.mycoffeecapsules.activities;

import android.app.DownloadManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.graphics.drawable.DrawableCompat;
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
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.paocorp.mycoffeecapsules.R;
import com.paocorp.mycoffeecapsules.adapters.CapsuleExpandableListAdapter;
import com.paocorp.mycoffeecapsules.db.CapsuleHelper;
import com.paocorp.mycoffeecapsules.db.CapsuleTypeHelper;
import com.paocorp.mycoffeecapsules.models.Capsule;
import com.paocorp.mycoffeecapsules.models.CapsuleType;
import com.paocorp.mycoffeecapsules.models.ShowAdsApplication;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private HashMap<String, ArrayList<Capsule>> listDataCapsules;
    private CapsuleExpandableListAdapter listAdapter;
    protected InterstitialAd mInterstitialAd = new InterstitialAd(this);
    ArrayList<String> types;
    ExpandableListView expListView;
    ExpandableListView expListViewSearch;
    NumberPicker nb;
    CapsuleHelper capsuleHelper;
    View currentView;
    PackageInfo pInfo;
    ShareDialog shareDialog;
    CallbackManager callbackManager;
    AdView adView;
    CapsuleTypeHelper capsuleTypeHelper;
    ArrayList<CapsuleType> listCapsuleType;
    boolean modif = false;
    NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
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
        onExpandableClickListener();

        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            TextView txv = (TextView) findViewById(R.id.appVersion);
            String APPINFO = txv.getText() + " v" + pInfo.versionName;
            txv.setText(APPINFO);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(this);

        final ShowAdsApplication hideAdObj = ((ShowAdsApplication) getApplicationContext());
        boolean hideAd = hideAdObj.getHideAd();

        if (!hideAd) {
            mInterstitialAd.setAdUnitId(this.getResources().getString(R.string.interstitial));
            requestNewInterstitial();
            mInterstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    showInterstitial();
                    hideAdObj.setHideAd(true);
                }
            });
        }
    }

    public void onExpandableClickListener() {
        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                currentView = v;
                int capId = listAdapter.getChild(groupPosition, childPosition).getId();
                if (capId != 0) {
                    createQtyDialog(capsuleHelper.getCapsuleById(capId));
                }
                return false;
            }
        });
    }

    public void createQtyDialog(final Capsule currentCapsule) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View v = inflater.inflate(R.layout.qty_dialog, null);
        builder.setCancelable(true);

        nb = (NumberPicker) v.findViewById(R.id.qty);
        TextView dialogTitle = (TextView) v.findViewById(R.id.dialogTitle);
        dialogTitle.setText(getResources().getString(R.string.capsulesTitle, currentCapsule.getName()));
        modif = false;

        builder.setView(v)
                .setPositiveButton(R.string.action_confirm, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        int capsuleId = currentCapsule.getId();
                        nb = (NumberPicker) v.findViewById(R.id.qty);
                        int qty = nb.getValue();

                        Capsule cap = capsuleHelper.getCapsuleById(capsuleId);
                        if (cap != null) {
                            modif = true;
                            cap.setQty(qty);
                            capsuleHelper.updateCapsule(cap);
                            TextView majQty = (TextView) currentView.findViewById(R.id.capsuleqty);
                            majQty.setText(getResources().getString(R.string.capsulesQty, qty));
                            majAlertConso(currentView, cap);
                        }
                    }
                })
                .setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

        AlertDialog alert = builder.create();

        nb.getWrapSelectorWheel();
        nb.setMinValue(0);
        nb.setMaxValue(10000);
        nb.setValue(currentCapsule.getQty());

        alert.show();
        adView = (AdView) v.findViewById(R.id.banner_bottom);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
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
                        expListViewSearch.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

                            @Override
                            public boolean onChildClick(ExpandableListView parent, View v,
                                                        int groupPosition, int childPosition, long id) {
                                currentView = v;
                                Capsule currentCapsule = searchListAdapter.getChild(groupPosition, childPosition);
                                createQtyDialog(currentCapsule);
                                return false;
                            }
                        });
                        findViewById(R.id.noResults).setVisibility(View.GONE);
                        findViewById(R.id.search_list).setVisibility(View.VISIBLE);
                    } else {
                        expListViewSearch.setAdapter((BaseExpandableListAdapter) null);
                        findViewById(R.id.noResults).setVisibility(View.VISIBLE);
                        findViewById(R.id.search_list).setVisibility(View.GONE);
                    }

                } else {
                    if (modif) {
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
                        onExpandableClickListener();
                    }

                    findViewById(R.id.first_list).setVisibility(View.VISIBLE);
                    findViewById(R.id.capsules_search_container).setVisibility(View.GONE);
                }
                return true;
            }
        });
        return true;
    }

    private void majAlertConso(View convertView, Capsule currentcapsule) {

        ImageButton btnConso = (ImageButton) convertView.findViewById(R.id.capsuleconso);
        if (Build.VERSION.SDK_INT >= 16 && currentcapsule.getQty() > 0 && currentcapsule.getConso() > 0) {
            long days = Math.round(Math.floor(currentcapsule.getQty() / currentcapsule.getConso()));
            if (days <= 5) {
                changeBtnColor(btnConso, R.color.red_darken3);
            } else {
                changeBtnColor(btnConso, R.color.black);
            }
        } else {
            changeBtnColor(btnConso, R.color.black);
        }
    }

    private void changeBtnColor(View btnConso, int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            btnConso.getBackground().setColorFilter(getApplicationContext().getResources().getColor(color), PorterDuff.Mode.SRC_IN);
        } else {
            Drawable wrapDrawable = DrawableCompat.wrap(btnConso.getBackground());
            DrawableCompat.setTint(wrapDrawable, getApplicationContext().getResources().getColor(color));
            btnConso.setBackgroundDrawable(DrawableCompat.unwrap(wrapDrawable));
        }
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
            if (!capsuleHelper.exportDatabase()) {
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
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            return true;
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        finish();
        startActivity(intent);
        return true;
    }

}