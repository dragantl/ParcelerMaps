package com.teralogics.parcelermaps;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sdoward.rxgooglemap.MapObservableProvider;
import com.teralogics.parcelermaps.model.MapMarker;
import com.trello.navi.Event;
import com.trello.navi.component.support.NaviAppCompatActivity;
import com.trello.navi.rx.RxNavi;

import org.parceler.Parcels;

import java.util.List;
import java.util.Random;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public class MainActivity extends NaviAppCompatActivity {

    @SuppressWarnings("unused")
    private final static String TAG = "MainActivity";
    private final static String STATE_MARKERS = "MainActivity.state.Markers";
    private final static String MAP_MARKER_TITLE_CHARACTERS = "abcdefghijklmnopqrstuvwxyz";
    private final static int MAP_MARKER_TITLE_LENGTH = 16;

    @Bind(R.id.map)
    MapView map;
    @Bind(R.id.marker_list)
    ListView markerListView;

    private final Random randomProvider = new Random();

    private MapMarkerAdapter markerAdapter = null;
    private MapObservableProvider mapProvider = null;

    private List<MapMarker> mapMarkers = null;

    private PublishSubject<MapMarker> markerSubject = null;

    private static String generateRandomTitle(final Random random, String characters, int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Random string length has to be greater than zero.");
        }

        char[] text = new char[length];

        for (int i = 0; i < length; i++) {
            text[i] = characters.charAt(random.nextInt(characters.length()));
        }

        return new String(text);
    }

    private static float getRandomLatitude(final Random random) {
        return (random.nextFloat() * 180) - 90;
    }

    private static float getRandomLongitude(final Random random) {
        return (random.nextFloat() * 360) - 180;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        markerSubject = PublishSubject.create();

        markerAdapter = new MapMarkerAdapter(this);
        markerListView.setAdapter(markerAdapter);

        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_MARKERS)) {
            mapMarkers = Parcels.unwrap(savedInstanceState.getParcelable(STATE_MARKERS));
        }

        map.onCreate(savedInstanceState);
        mapProvider = new MapObservableProvider(map);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        map.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putParcelable(STATE_MARKERS, Parcels.wrap(markerAdapter.getItems()));
    }

    @Override
    public void onStart() {
        super.onStart();

        mapProvider
                .getMapReadyObservable()
                .subscribeOn(Schedulers.computation())
                .takeUntil(RxNavi.observe(this, Event.STOP))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::initializeMap, error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Unable to load the map: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void initializeMap(GoogleMap mapApi) {
        final Observable<LatLngBounds.Builder> cameraBoundsObservable;

        if (mapMarkers != null && mapMarkers.size() > 0) {
            final LatLngBounds.Builder initialBuilder = new LatLngBounds.Builder();

            for (MapMarker marker : mapMarkers) {
                initialBuilder.include(marker.getPosition());
                mapApi.addMarker(generateMarker(marker));
            }

            markerAdapter.addAll(mapMarkers);
            markerAdapter.notifyDataSetChanged();

            cameraBoundsObservable = markerSubject
                    .subscribeOn(Schedulers.computation())
                    .scan(initialBuilder, (builder, mapMarker) -> builder.include(mapMarker.getPosition()));
        } else {
            cameraBoundsObservable = markerSubject
                    .subscribeOn(Schedulers.computation())
                    .scan(null, new Func2<LatLngBounds.Builder, MapMarker, LatLngBounds.Builder>() {
                        @Override
                        public LatLngBounds.Builder call(LatLngBounds.Builder builder, MapMarker mapMarker) {
                            if (builder == null) {
                                builder = new LatLngBounds.Builder();
                            }

                            builder.include(mapMarker.getPosition());

                            return builder;
                        }
                    });
        }

        markerSubject
                .subscribeOn(Schedulers.computation())
                .takeUntil(RxNavi.observe(this, Event.STOP))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(marker -> {
                    mapApi.addMarker(generateMarker(marker));
                    markerAdapter.insert(marker, 0);
                });

        cameraBoundsObservable
                .filter(builder -> builder != null)
                .map(LatLngBounds.Builder::build)
                .takeUntil(RxNavi.observe(this, Event.STOP))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bbox -> {
                    final CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bbox, 100);
                    mapApi.animateCamera(cameraUpdate);
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_marker:
                addMarker();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void addMarker() {
        markerSubject.onNext(
                new MapMarker(
                        generateRandomTitle(randomProvider, MAP_MARKER_TITLE_CHARACTERS, MAP_MARKER_TITLE_LENGTH),
                        getRandomLatitude(randomProvider),
                        getRandomLongitude(randomProvider)));
    }

    private static MarkerOptions generateMarker(MapMarker mapMarker) {
        return new MarkerOptions()
                .title(mapMarker.getTitle())
                .position(mapMarker.getPosition());
    }

    @Override
    public void onResume() {
        map.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        map.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        map.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        map.onLowMemory();
    }
}
