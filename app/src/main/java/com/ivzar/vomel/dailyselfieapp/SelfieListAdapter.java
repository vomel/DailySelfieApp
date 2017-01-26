package com.ivzar.vomel.dailyselfieapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static com.ivzar.vomel.dailyselfieapp.MainActivity.TAG;
import static com.ivzar.vomel.dailyselfieapp.SelfieListAdapter.ITEM_SEP;

/**
 * Created by vomel on 26/01/2017.
 */

public class SelfieListAdapter extends BaseAdapter {
    public static final String ITEM_SEP = System.getProperty("line.separator");

    private final List<Selfie> mItems = new ArrayList<>();
    private final Context mContext;
    private MainActivity activity;

    public SelfieListAdapter(Context mContext) {
        this(mContext, null);
    }

    public SelfieListAdapter(Context mContext, MainActivity activity) {
        this.mContext = mContext;
        this.activity = activity;
    }

    public void add(Selfie item) {
        Log.i(TAG, "add: " + item);
        mItems.add(item);
        notifyDataSetChanged();
    }

    public void remove(int pos) {
        Log.i(TAG, "remove: " + pos);
        mItems.remove(pos);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Log.i(TAG, "getView: " + position + ", " + convertView + ", " + parent);
        Selfie item = (Selfie) getItem(position);
        ViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(mContext);
            convertView = layoutInflater.inflate(R.layout.selfie, parent, false);
            viewHolder = new ViewHolder();
            convertView.setTag(viewHolder);
            viewHolder.preview = (ImageView) convertView.findViewById(R.id.previewView);
            viewHolder.description = (TextView) convertView.findViewById(R.id.previewDescription);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.position = position;
        viewHolder.preview.setImageBitmap(item.preview);
        viewHolder.description.setText(item.description);
        convertView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View v) {
                new AlertDialog.Builder(activity)
                        .setTitle("Selfie Remover")
                        .setMessage("Do you really want to delete an selfie??")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                remove(position);
                                Toast.makeText(activity, "Deleted", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null).show();
                return true;
            }
        });
        return convertView;
    }

    private static class ViewHolder {
        int position;
        ImageView preview;
        TextView description;
    }
}

class Selfie {
    Selfie(Bitmap preview, CharSequence description) {
        this.preview = preview;
        this.description = description;
    }

    Bitmap preview;
    CharSequence description;

    @Override
    public String toString() {
        return description + ITEM_SEP + preview;
    }
}
