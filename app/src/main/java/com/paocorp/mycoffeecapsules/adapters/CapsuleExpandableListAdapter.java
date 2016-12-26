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
import android.view.WindowManager;
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
import com.paocorp.mycoffeecapsules.db.DatabaseHelper;
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
    NumberPicker nb;
    AdView adView;

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
    public View getChildView(final int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        Capsule currentCapsule = getChild(groupPosition, childPosition);
        if (currentCapsule != null) {
            final String capName = currentCapsule.getName();
            final String capImg = currentCapsule.getImg();
            final int capQty = currentCapsule.getQty();

            if (convertView == null) {
                LayoutInflater infalInflater = (LayoutInflater) this.context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = infalInflater.inflate(R.layout.list_item_capsule, null);
            }

            TextView tvName = (TextView) convertView.findViewById(R.id.capsulename);
            tvName.setText(capName);
            tvName.setTag(currentCapsule.getId());
            tvName.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    final int capId = (Integer) v.getTag();
                    View parent = (View) v.getParent();
                    createQtyDialog(groupPosition, childPosition, capId, parent);
                }
            });


            TextView tvQty = (TextView) convertView.findViewById(R.id.capsuleqty);
            tvQty.setText(context.getResources().getString(R.string.capsulesQty, capQty));
            tvQty.setTag(currentCapsule.getId());
            tvQty.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    final int capId = (Integer) v.getTag();
                    View parent = (View) v.getParent();
                    createQtyDialog(groupPosition, childPosition, capId, parent);
                }
            });

            ImageView capsule_img = (ImageView) convertView.findViewById(R.id.capsuleimg);
            int res = context.getResources().getIdentifier(capImg, "drawable", context.getPackageName());
            capsule_img.setTag(currentCapsule.getId());
            if (res != 0) {
                Drawable drawable = context.getResources().getDrawable(res);
                capsule_img.setImageDrawable(drawable);
                capsule_img.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        final int capId = (Integer) v.getTag();
                        View parent = (View) v.getParent();
                        createQtyDialog(groupPosition, childPosition, capId, parent);
                    }
                });
            }

            ImageButton btnDelete = (ImageButton) convertView.findViewById(R.id.capsuledelete);
            if (currentCapsule.getType() == DatabaseHelper.CUSTOM_TYPE_ID) {
                btnDelete.setTag(currentCapsule.getId());
                btnDelete.setVisibility(View.VISIBLE);
                btnDelete.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        final int capId = (Integer) v.getTag();
                        deleteDialog(groupPosition, childPosition, capId);
                    }
                });
            } else {
                btnDelete.setVisibility(View.GONE);
            }

            ImageButton btnConso = (ImageButton) convertView.findViewById(R.id.capsuleconso);
            btnConso.setTag(currentCapsule.getId());
            btnConso.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    final int capId = (Integer) v.getTag();
                    View parent = (View) v.getParent();
                    consoDialog(parent, capId);
                }
            });

            majAlertConso(convertView, currentCapsule);

        }

        return convertView;
    }

    private void createQtyDialog(final int groupPosition, final int childPosition, final int id, final View parent) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View v = inflater.inflate(R.layout.qty_dialog, null);
        builder.setCancelable(true);
        final CapsuleHelper capsuleHelper = new CapsuleHelper(context);
        final Capsule currentCapsule = capsuleHelper.getCapsuleById(id);

        if (currentCapsule != null) {

            nb = (NumberPicker) v.findViewById(R.id.qty);
            TextView dialogTitle = (TextView) v.findViewById(R.id.dialogTitle);
            dialogTitle.setText(context.getResources().getString(R.string.capsulesTitle, currentCapsule.getName()));

            builder.setView(v)
                    .setPositiveButton(R.string.action_confirm, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            int capsuleId = currentCapsule.getId();
                            nb = (NumberPicker) v.findViewById(R.id.qty);
                            int qty = nb.getValue();

                            Capsule cap = capsuleHelper.getCapsuleById(capsuleId);
                            if (cap != null) {
                                cap.setQty(qty);
                                capsuleHelper.updateCapsule(cap);
                                Capsule test = listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition);
                                test.setQty(qty);
                                notifyDataSetChanged();
                                majAlertConso(parent, currentCapsule);
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

            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(alert.getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

            alert.show();
            alert.getWindow().setAttributes(lp);
            adView = (AdView) v.findViewById(R.id.banner_bottom);
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
        }
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
                consoPreview.setVisibility(View.VISIBLE);
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
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(alert.getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            alert.show();
            alert.getWindow().setAttributes(lp);
            AdView adView = (AdView) v.findViewById(R.id.banner_bottom);
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
        }
    }

    private void deleteDialog(final int groupPosition, final int childPosition, int id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        final View v = inflater.inflate(R.layout.delete_dialog, null);
        builder.setCancelable(true);
        final CapsuleHelper capsuleHelper = new CapsuleHelper(context);
        final Capsule capsule = capsuleHelper.getCapsuleById(id);

        if (capsule != null) {
            TextView deleteTitle = (TextView) v.findViewById(R.id.deleteTitle);
            deleteTitle.setText(context.getResources().getString(R.string.deleteTitle, capsule.getName()));

            builder.setView(v)
                    .setPositiveButton(R.string.action_confirm, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            capsuleHelper.deleteCapsule(capsule);

                            listDataChild.get(listDataHeader.get(groupPosition)).remove(childPosition);
                            if (listDataChild.get(listDataHeader.get(groupPosition)).size() == 0) {
                                listDataHeader.remove(groupPosition);
                            }
                            notifyDataSetChanged();
                        }
                    })
                    .setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();

        }
    }

    private void majAlertConso(View convertView, Capsule currentcapsule) {

        ImageButton btnConso = (ImageButton) convertView.findViewById(R.id.capsuleconso);
        if (btnConso != null) {
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
