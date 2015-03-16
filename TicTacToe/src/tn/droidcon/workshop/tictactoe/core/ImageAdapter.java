package tn.droidcon.workshop.tictactoe.core;

import java.util.ArrayList;

import tn.droidcon.workshop.tictactoe.app.R;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class ImageAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<Integer> gridContentArray;

    public ImageAdapter(Context c, int gridSize) {
        mContext = c;
        gridContentArray = new ArrayList<Integer>();

        for (int i = 0; i < gridSize; i++) {
            gridContentArray.add(R.drawable.blank);
        }
    }

    @Override
    public int getCount() {
        return gridContentArray.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    public void setItem(int position, int ressource) {
        gridContentArray.set(position, new Integer(ressource));
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) { // if it's not recycled, initialize some
            // attributes
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (ImageView) convertView;
        }

        imageView.setImageResource(gridContentArray.get(position));
        return imageView;
    }
}
