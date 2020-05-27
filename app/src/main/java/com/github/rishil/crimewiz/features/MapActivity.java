package com.github.rishil.crimewiz.features;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.github.rishil.crimewiz.R;
import com.github.rishil.crimewiz.base.BaseActivity;
import com.github.rishil.crimewiz.core.objects.Crime;
import com.github.rishil.crimewiz.core.services.CloudService;
import com.github.rishil.crimewiz.core.util.JsonParser;
import com.github.rishil.crimewiz.core.util.clustering.CrimeClusterIcon;

import static com.github.rishil.crimewiz.core.services.CloudService.HTTP_KEY_RESPONSE;
import static com.github.rishil.crimewiz.core.services.CloudService.HTTP_REQUEST_CODE;

public class MapActivity extends BaseActivity implements OnMapReadyCallback {

    private GoogleMap map;
    private HeatmapTileProvider heatmapTileProvider;
    private TileOverlay tileOverlay;
    private ArrayList<LatLng> heatmapLocations;

    private CloudService httpService;
    protected Handler httpRequestHandler = null;
    private JsonParser jsonParser;

    private CardView progressBar;

    private Button circleToggle; //markerToggle;

    private static String addCircle = "Add Circle";
    private static String removeCircle = "Re move Circle";

    private Circle circle;

    private LatLng[] markerPos;

    private static Float HUE_BLUE = BitmapDescriptorFactory.HUE_BLUE;
    private static Float HUE_ORANGE = BitmapDescriptorFactory.HUE_ORANGE;
    private static Float HUE_RED = BitmapDescriptorFactory.HUE_RED;
    private static Float HUE_YELLOW = BitmapDescriptorFactory.HUE_YELLOW;
    private static Float HUE_MAGENTA = BitmapDescriptorFactory.HUE_MAGENTA;


    // clustering
    // Declare a variable for the cluster manager.
    private ClusterManager<CrimeClusterIcon> clusterManager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        loadUi();

        progressBar = findViewById(R.id.progressBarCardView);

        final FloatingActionButton clearButton = findViewById(R.id.clearActionButton);
        final FloatingActionButton searchButton = findViewById(R.id.searchActionButton);
        final FloatingActionButton heatMapButton = findViewById(R.id.heatMapActionButton);


        FloatingActionButton moreButton = findViewById(R.id.moreActionButton);
        moreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                giveHapticFeedback();
                if (clearButton.getVisibility() == View.GONE
                        && searchButton.getVisibility() == View.GONE){
                    clearButton.setVisibility(View.VISIBLE);
                    searchButton.setVisibility(View.VISIBLE);
                    heatMapButton.setVisibility(View.VISIBLE);
                } else {
                    clearButton.setVisibility(View.GONE);
                    searchButton.setVisibility(View.GONE);
                    heatMapButton.setVisibility(View.GONE);
                }

            }
        });

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                giveHapticFeedback();
                clearButton.setVisibility(View.GONE);
                searchButton.setVisibility(View.GONE);
                heatMapButton.setVisibility(View.GONE);
                clusterManager.clearItems();
                map.clear();
                if (tileOverlay !=null){
                    tileOverlay.clearTileCache();
                }
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                giveHapticFeedback();
                clearButton.setVisibility(View.GONE);
                searchButton.setVisibility(View.GONE);
                heatMapButton.setVisibility(View.GONE);
                Intent searchActivity = new Intent(getApplicationContext(), SearchActivity.class);
                startActivity(searchActivity);
            }
        });

        heatMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                giveHapticFeedback();
                clearButton.setVisibility(View.GONE);
                searchButton.setVisibility(View.GONE);
                heatMapButton.setVisibility(View.GONE);
                loadHeatmap(heatmapLocations);
            }
        });


        jsonParser = new JsonParser(this);

        // Setup a handler to retrieve the response from the API
        if (httpRequestHandler == null) {
             httpRequestHandler = new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    if (msg.what == HTTP_REQUEST_CODE) {
                        Bundle bundle = msg.getData();
                        if (bundle != null) {
                            String responseText = bundle.getString(HTTP_KEY_RESPONSE);
                            if (responseText != null) {
                                if (!responseText.equals("[]")){
                                    jsonParser.parse(responseText);
                                    List<Crime> crimeList = jsonParser.parse(responseText);
                                        ArrayList<String> categories = new ArrayList<>();
                                        for (int i = 0; i < crimeList.size(); i++) {
                                            String category = crimeList.get(i).getCategory();

                                            if (!categories.contains(category)){
                                                categories.add(category);
                                            }
                                        }

                                } else {
                                    Log.i("MapActivity", "No crimes found");
                                }
                            }
                        }
                    }
                    return true;
                }
            });
        }


        httpService = new CloudService(httpRequestHandler);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshMap();
    }


    private void setUpClusterer() {
        clusterManager = new ClusterManager<>(this, map);
        map.setOnCameraIdleListener(clusterManager);
        map.setOnMarkerClickListener(clusterManager);

        clusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<CrimeClusterIcon>() {
            @Override
            public boolean onClusterClick(Cluster<CrimeClusterIcon> cluster) {
                ArrayList<CrimeClusterIcon> items = new ArrayList<>(cluster.getItems());
                Intent clusteringActivity = new Intent(getApplicationContext(), ClusteringActivity.class);
                clusteringActivity.putParcelableArrayListExtra("clusteredCrimes", items);
                startActivity(clusteringActivity);
                return false;

            }
        });

    }


    private void addPointOnMap(List<Crime> crimeList, int pos, String category, int id,
                               String date, String street, String outcome, float HUE){
        map.addMarker(new MarkerOptions().position(new LatLng(crimeList.get(pos).getLatitude(),
                        crimeList.get(pos).getLongitude()))
                .icon(BitmapDescriptorFactory.defaultMarker(HUE))
                .title(category)
                .snippet(id + ", " + date + ", "
                        + street + ", " + outcome));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.getUiSettings().setMapToolbarEnabled(false);

        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
            return;
        }

        map.setMyLocationEnabled(true);

        markerPos = new LatLng[1];

        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng point) {
                newMapEntry(point);
            }
        });

        setUpClusterer();

    }

    private void newMapEntry(LatLng point){
        clusterManager.clearItems();
        map.clear();

        progressBar.setVisibility(View.VISIBLE);

        markerPos[0] = new LatLng(point.latitude, point.longitude);

        addCircle(markerPos[0]);

        httpService.getData(point.latitude, point.longitude);

        final Double latitude = point.latitude;
        final Double longitude = point.longitude;

        sharedPreferences.edit().putString("point_latitude", ""+ latitude).apply();
        sharedPreferences.edit().putString("point_longitude", ""+ longitude).apply();


        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                progressBar.setVisibility(View.GONE);
            }

        }, 5000);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                addPointSet(latitude, longitude);
                saveCategories();
                saveYears();
                saveMonths();
            }
        }, 5000);

        sharedPreferences.edit().putBoolean("dataSetHasChanged", true).apply();
    }

    private void refreshMap(){
        if (sharedPreferences.getBoolean("refreshMap", false)){
            progressBar.setVisibility(View.VISIBLE);


            String lastLatitudeString = sharedPreferences.getString("point_latitude", null);
            String lastLongitudeString = sharedPreferences.getString("point_longitude", null);


            Intent i = getIntent();

            if (i.getExtras() != null) {
                final LatLng pos = i.getExtras().getParcelable("LatLng");
                if (pos!=null) newMapEntry(pos);

            } else {
                Log.d("EXTRAS", "EXTRAS ARE NULL");
                if (lastLatitudeString != null && lastLongitudeString != null) {
                    final Double lastLatitude = Double.parseDouble(lastLatitudeString);
                    final Double lastLongitude = Double.parseDouble(lastLongitudeString);

                    new Handler().postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);
                        }

                    }, 5000);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            clusterManager.clearItems();
                            map.clear();
                            markerPos[0] = new LatLng(lastLatitude, lastLongitude);
                            addCircle(markerPos[0]);
                            addPointSet(lastLatitude, lastLongitude);
                            saveCategories();
                            saveYears();
                            saveMonths();
                        }
                    }, 5000);

                }
            }
            sharedPreferences.edit().putBoolean("refreshMap", false).apply();
        }

    }

    private void addPointSet(Double pointLat, Double pointLng){
        boolean firstLaunch = sharedPreferences.getBoolean("firstLaunch", false);
        ArrayList<String> selectedYears;
        if (sharedPreferences.getStringSet("saved_years", null) == null && firstLaunch){
            saveYears();
        }
        Set<String> yearsSet = sharedPreferences.getStringSet("saved_years", null);



        ArrayList<String> selectedMonths;
        if (sharedPreferences.getStringSet("saved_months", null) == null && firstLaunch){
            saveMonths();
        }

        Set<String> monthsSet = sharedPreferences.getStringSet("saved_months", null);

        ArrayList<String> selectedCategories;
        if (sharedPreferences.getStringSet("saved_categories", null) == null && firstLaunch){
            saveCategories();
        }

        sharedPreferences.edit().putBoolean("firstLaunch", false).apply();



        Set<String> set = sharedPreferences.getStringSet("saved_categories", null);
        Cursor data = databaseHelper.getData();

        HashMap<String,LatLng> markersAdded = new HashMap<>();
        heatmapLocations = new ArrayList<>();


        try {
            while (data.moveToNext()) {
                if (set != null && yearsSet !=null && monthsSet !=null) {
                    selectedCategories = new ArrayList<>(set);
                    selectedYears = new ArrayList<>(yearsSet);
                    selectedMonths = new ArrayList<>(monthsSet);

                    // if crime is within that category
                    if (selectedCategories.contains(data.getString(2)) &&
                            selectedYears.contains(data.getString(1).substring(0,4)) &&
                            selectedMonths.contains(data.getString(1).substring(5,7))
                    ) {

                        // add marker if within range
                        LatLng[] crimePosition = new LatLng[1];
                        crimePosition[0] = new LatLng(data.getDouble(4), data.getDouble(5));
                        markerPos[0] = new LatLng(pointLat, pointLng);

                        if (isCrimeWithinRange(markerPos[0], crimePosition[0], 0.7)){


                            String location = data.getString(3);
                            String date = data.getString(1).substring(0,7);
                            CrimeClusterIcon offsetItem = new
                                    CrimeClusterIcon(crimePosition[0].latitude,
                                    crimePosition[0].longitude,
                                    data.getString(2),date + ", " + location,
                                    location,
                                    date);

                            clusterManager.addItem(offsetItem);
                            heatmapLocations.add(crimePosition[0]);

                        }

                    }
                } else {
                    System.out.println("Shared prefs are null");
                }

            }
            markersAdded.clear();

            data.close();

            clusterManager.cluster();


        } catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    /**
     * Method used when comparing the user-drawn circle to the crime locations
     * @param centerOfCircle latitude and longitude
     * @param crime latidue and longitude
     * @param distInKilometers configured by the user
     * @return true or false depending if the crime is within the circles
     */
    private boolean isCrimeWithinRange(LatLng centerOfCircle, LatLng crime, double distInKilometers){
        // calculate the kilometres per °
        double kmY = 40000/360;
        double kmX = Math.cos((centerOfCircle.longitude / 180)*Math.PI) * kmY;

        // calculate the distance between the two points
        double distY = Math.abs(centerOfCircle.latitude - crime.latitude) * kmY;
        double distX = Math.abs(centerOfCircle.longitude - crime.longitude) * kmX;

        // return true if the crime is in the circle
        return Math.sqrt((distX*distX) + (distY*distY)) <= distInKilometers;
    }

    private void addCircle(LatLng center){
        circle = map.addCircle(new CircleOptions()
                .center(center)
                .radius(700)
                .strokeColor(R.color.colorBlue)
                .strokeWidth(5)
                .fillColor(0x200000ff));

        String circlePoly = getCirclePoly(center, 300);
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(center, setZoom(circle)));

    }

    private int setZoom(Circle placedCircle) {
        int zoom = MapCircle.ZOOM_LEVEL;
        if (placedCircle != null) {
            double r = placedCircle.getRadius() + placedCircle.getRadius() / 2; // rad
            double s = r / MapCircle.SCALAR; // scale
            zoom = (int) (16 - Math.log(s) / Math.log(2));
        }
        return zoom;
    }

    private String getCirclePoly(LatLng center, int radius) {
        int points = 50; // number of corners of inscribed polygon

        double radiusLatitude = Math.toDegrees(radius / (float) 6371000);
        double radiusLongitude = radiusLatitude / Math.cos(Math.toRadians(center.latitude));

       // List<LatLng> result = new ArrayList<>(points);

        String poly = "poly=";
        double anglePerCircleRegion = 2 * Math.PI / points;

        for (int i = 0; i < points; i++) {
            double theta = i * anglePerCircleRegion;
            double latitude = center.latitude + (radiusLatitude * Math.sin(theta));
            double longitude = center.longitude + (radiusLongitude * Math.cos(theta));
            poly = poly + latitude + "," + longitude + ":";
        }

        poly = poly.substring(0, poly.length() - 1);

        return poly;
    }

    private void loadHeatmap(ArrayList<LatLng> locations){
        // set up heatmap
        if (locations !=null) {
            heatmapTileProvider = new HeatmapTileProvider.Builder().data(locations).build();
            heatmapTileProvider.setRadius(HeatmapTileProvider.DEFAULT_RADIUS);
            tileOverlay = map.addTileOverlay(new TileOverlayOptions().tileProvider(heatmapTileProvider));
        }

    }

    private static class MapCircle {
        static int ZOOM_LEVEL = 11;
        static int SCALAR = 500;
    }
}