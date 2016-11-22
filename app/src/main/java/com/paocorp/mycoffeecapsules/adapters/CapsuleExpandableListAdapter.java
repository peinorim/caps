package com.paocorp.mycoffeecapsules.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.paocorp.mycoffeecapsules.R;
import com.paocorp.mycoffeecapsules.models.Capsule;

import java.util.ArrayList;
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
        return this.listDataChild.get(this.listDataHeader.get(groupPosition))
                .get(childPosititon);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        final String capName = getChild(groupPosition, childPosition).getName();
        final String capImg = getChild(groupPosition, childPosition).getImg();
        final int capQty = getChild(groupPosition, childPosition).getQty();

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_item_capsule, null);
        }

        TextView tvName = (TextView) convertView.findViewById(R.id.capsulename);
        tvName.setText(capName);

        TextView tvQty = (TextView) convertView.findViewById(R.id.capsuleqty);
        tvQty.setText(context.getResources().getString(R.string.capsulesQty, capQty));

        ImageView capsule_img = (ImageView) convertView.findViewById(R.id.capsuleimg);
        int res = context.getResources().getIdentifier(capImg, "drawable", context.getPackageName());
        if(res != 0) {
            Drawable drawable = context.getResources().getDrawable(res);
            capsule_img.setImageDrawable(drawable);
        }

        return convertView;
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
