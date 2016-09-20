package com.teralogics.parcelermaps;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.teralogics.parcelermaps.model.MapMarker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Stream;

/**
 * TODO: Add a class header comment!
 */
public class MapMarkerAdapter extends ArrayAdapter<MapMarker> {

    private static class ViewHolder {
        private TextView title;
        private TextView subtitle;
    }

    private ArrayList<MapMarker> markerCache = new ArrayList<>();

    public MapMarkerAdapter(Context context) {
        super(context, R.layout.view_marker_item);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(this.getContext()).inflate(R.layout.view_marker_item, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.title = (TextView) convertView.findViewById(R.id.title);
            viewHolder.subtitle = (TextView) convertView.findViewById(R.id.subtitle);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        MapMarker item = getItem(position);

        if (item != null) {
            viewHolder.title.setText(item.getTitle());
            viewHolder.subtitle.setText(String.format("%f, %f", item.getLatitude(), item.getLongitude()));
        }

        return convertView;
    }

    public void add(MapMarker object) {
        markerCache.add(object);
        super.add(object);
    }

    public void addAll(Collection<? extends MapMarker> collection) {
        markerCache.addAll(collection);
        super.addAll(collection);
    }

    public void addAll(MapMarker... items) {
        Collections.addAll(markerCache, items);
        super.addAll(items);
    }

    public void insert(MapMarker object, int position) {
        markerCache.add(object);
        super.insert(object, position);
    }

    public void remove(MapMarker object) {
        markerCache.remove(object);
        super.remove(object);
    }

    public void clear() {
        markerCache.clear();
        super.clear();
    }

    public  ArrayList<MapMarker> getItems() {
        return markerCache;
    }
}
