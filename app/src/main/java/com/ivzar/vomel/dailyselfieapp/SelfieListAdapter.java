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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.ivzar.vomel.dailyselfieapp.MainActivity.TAG;
import static com.ivzar.vomel.dailyselfieapp.MainActivity.isValidDir;
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

    private void removeFile(int pos) {
        File storageDir = activity.getStorageDir();
        if (isValidDir(storageDir)) {
            File image = new File(storageDir, mItems.get(pos).description);
            if (image.exists()) image.delete();
        }
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Object getItem(int position) {
        return getSelfie(position);
    }

    public Selfie getSelfie(int position) {
        if (mItems.size() <= position)
            Log.e(TAG, "!!!getSelfie: position: " + position + ", in list: " + mItems.size());
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        Selfie item = getSelfie(position);
        final ViewHolder viewHolder;
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
                Log.i(TAG, "onLongClick: ");
                new AlertDialog.Builder(activity)
                        .setTitle("Selfie Remover")
                        .setMessage("Do you really want to delete an selfie??")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                removeFile(position);
                                remove(position);
                                Toast.makeText(activity, "Deleted", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null).show();
                return true;
            }
        });
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick: ");
                ImageView fullView = (ImageView) activity.findViewById(R.id.full_view);
                fullView.setImageBitmap(MainActivity.getScaledBitmap(getSelfie(position).description, activity.screenWidth, activity.screenHeight));
                fullView.setVisibility(View.VISIBLE);
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
    Selfie(Bitmap preview, String description) {
        this.preview = preview;
        this.description = description;
    }

    Bitmap preview;
    String description;

    @Override
    public String toString() {
        return description + ITEM_SEP + preview;
    }
}
