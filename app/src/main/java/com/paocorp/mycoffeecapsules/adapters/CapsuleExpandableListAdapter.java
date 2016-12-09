package com.paocorp.mycoffeecapsules.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.paocorp.mycoffeecapsules.R;
import com.paocorp.mycoffeecapsules.db.CapsuleHelper;
import com.paocorp.mycoffeecapsules.models.Capsule;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class CapsuleExpandableListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<String> listDataHeader; // header titles
    // child data in format of header title, child title
    private HashMap<String, ArrayList<Capsule>> listDataChild;
    private HashMap<Integer, Integer> listMinus = new HashMap<>();
    private HashMap<Integer, Integer> listPlus = new HashMap<>();

    public CapsuleExpandableListAdapter(Context context, List<String> listDataHeader,
                                        HashMap<String, ArrayList<Capsule>> listChildData) {
        this.context = context;
        this.listDataHeader = listDataHeader;
        this.listDataChild = listChildData;
    }

    @Override
    public Capsule getChild(int groupPosition, int childPosititon) {
        if (this.listDataHeader.get(groupPosition) != null && this.listDataChild.get(this.listDataHeader.get(groupPosition)) != null) {
            return this.listDataChild.get(this.listDataHeader.get(groupPosition)).get(childPosititon);
        } else {
            return null;
        }
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        Capsule currentcapsule = getChild(groupPosition, childPosition);
        if (currentcapsule != null) {
            final String capName = currentcapsule.getName();
            final String capImg = currentcapsule.getImg();
            final int capQty = currentcapsule.getQty();

            if (convertView == null) {
                LayoutInflater infalInflater = (LayoutInflater) this.context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = infalInflater.inflate(R.layout.list_item_capsule, null);
            }

            final View ultiView = convertView;

            TextView tvName = (TextView) convertView.findViewById(R.id.capsulename);
            tvName.setText(capName);

            TextView tvQty = (TextView) convertView.findViewById(R.id.capsuleqty);
            tvQty.setText(context.getResources().getString(R.string.capsulesQty, capQty));

            ImageView capsule_img = (ImageView) convertView.findViewById(R.id.capsuleimg);
            int res = context.getResources().getIdentifier(capImg, "drawable", context.getPackageName());
            if (res != 0) {
                Drawable drawable = context.getResources().getDrawable(res);
                capsule_img.setImageDrawable(drawable);
            }

            ImageButton btnConso = (ImageButton) convertView.findViewById(R.id.capsuleconso);
            btnConso.setTag(currentcapsule.getId());
            btnConso.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    final int capId = (Integer) v.getTag();
                    consoDialog(ultiView, capId);
                }
            });

            majAlertConso(convertView, currentcapsule);

        }

        return convertView;
    }

    private void consoDialog(final View convertView, int id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        final View v = inflater.inflate(R.layout.conso_dialog, null);
        builder.setCancelable(true);
        final CapsuleHelper capsuleHelper = new CapsuleHelper(context);
        final Capsule capsule = capsuleHelper.getCapsuleById(id);

        if (capsule != null) {
            TextView dialogTitle = (TextView) v.findViewById(R.id.consoTitle);
            dialogTitle.setText(context.getResources().getString(R.string.daily_conso, capsule.getName()));
            final NumberPicker nb = (NumberPicker) v.findViewById(R.id.consoQty);
            nb.getWrapSelectorWheel();
            nb.setMinValue(0);
            nb.setMaxValue(10000);
            nb.setValue(capsule.getConso());

            if (capsule.getQty() > 0 && capsule.getConso() > 0) {
                TextView consoPreview = (TextView) v.findViewById(R.id.consoPreview);
                long days = Math.round(Math.floor(capsule.getQty() / capsule.getConso()));

                if (days > 0) {
                    Calendar c = Calendar.getInstance();
                    c.add(Calendar.DATE, (int) days);
                    String consoDatePreview = DateFormat.getDateInstance(DateFormat.SHORT).format(c.getTime());
                    consoPreview.setText(context.getResources().getString(R.string.consoPreview, capsule.getName(), consoDatePreview));
                } else {
                    consoPreview.setText(context.getResources().getString(R.string.consoOut, capsule.getName()));
                }
            }

            builder.setView(v)
                    .setPositiveButton(R.string.action_confirm, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            int conso = nb.getValue();
                            capsule.setConso(conso);
                            capsuleHelper.updateCapsule(capsule);
                            majAlertConso(convertView, capsule);
                        }
                    })
                    .setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });

            AlertDialog alert = builder.create();
            alert.show();
            AdView adView = (AdView) v.findViewById(R.id.banner_bottom);
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
        }
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
            btnConso.getBackground().setColorFilter(context.getResources().getColor(color), PorterDuff.Mode.SRC_IN);
        } else {
            Drawable wrapDrawable = DrawableCompat.wrap(btnConso.getBackground());
            DrawableCompat.setTint(wrapDrawable, context.getResources().getColor(color));
            btnConso.setBackgroundDrawable(DrawableCompat.unwrap(wrapDrawable));
        }
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this.listDataChild.get(this.listDataHeader.get(groupPosition))
                .size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.listDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this.listDataHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_group, null);
        }

        ExpandableListView mExpandableListView = (ExpandableListView) parent;
        mExpandableListView.expandGroup(groupPosition);

        TextView lblListHeader = (TextView) convertView
                .findViewById(R.id.lblListHeader);
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(headerTitle);

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
