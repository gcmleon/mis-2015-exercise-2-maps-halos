package com.example.mmbuw.hellomaps;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class MapsActivity extends FragmentActivity implements GoogleMap.OnMapLongClickListener, GoogleMap.OnCameraChangeListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private LatLng lastKnownPosition;
    private LocationManager locationManager;
    private String locationProvider;
    private EditText customMessage;
    private HashMap<String, Circle> mapCircles = new HashMap<String, Circle>();
    private String setId = "markers";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        /*GoogleApiClient client = new GoogleApiClient.Builder(this).addConnectionCallbacks(this)
                                                                  .addOnConnectionFailedListener(this)
                                                                  .addApi(LocationServices.API)
                                                                  .build();*/
        customMessage = (EditText) findViewById(R.id.markerMsg);

        setUpMapIfNeeded();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences markerInfo = this.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = markerInfo.edit();
        editor.clear();
        editor.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // getMap() is deprecated though, getMapAsync(callback) is the recommended for not null
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {

        //mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));

        // Button in the upper-right corner to show my current location working =)
        mMap.setMyLocationEnabled(true);

        // It is activated every time the current location changes
        // This one works if I send my location through telnet command "geo fix lat long"
        GoogleMap.OnMyLocationChangeListener mapListener = new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                lastKnownPosition = new LatLng(location.getLatitude(), location.getLongitude());
                //mMap.addMarker(new MarkerOptions().position(lastKnownPosition).title("My Position"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(lastKnownPosition));
            }
        };

        mMap.setOnMyLocationChangeListener(mapListener);

        // To get current location, only once
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        locationProvider = locationManager.getBestProvider(criteria, true);
        // After doing the telnet, this one gives that location...
        Location currentLocation = locationManager.getLastKnownLocation(locationProvider);
        if (currentLocation != null) {
            lastKnownPosition = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            //mMap.addMarker(new MarkerOptions().position(lastKnownPosition).title("My Position Outside"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(lastKnownPosition));
        }
        //locationManager.requestLocationUpdates(); -- I need a listener for that
        // Based on code from http://developer.android.com/guide/topics/location/strategies.html

        // To associate the listeners to the map
        mMap.setOnMapLongClickListener(this);
        mMap.setOnCameraChangeListener(this);


    }

    @Override
    public void onMapLongClick(LatLng latLng) {

        StringBuilder stringBuilder;
        String coordinate = latLng.toString();
        String markerName = customMessage.getText().toString();
        //SharedPreferences markerInfo = getPreferences(MODE_PRIVATE);
        SharedPreferences markerInfo = this.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = markerInfo.edit();
        Set coordinates =  new HashSet<String>();
        Set old_coordinates; // =  new HashSet<String>();
        int infoLength = 0;

        System.out.println("The saved coordinates as string: " + coordinate);
        infoLength = markerName.length() + coordinate.length() + 1;
        stringBuilder = new StringBuilder(infoLength);
        stringBuilder.append(coordinate).append(";").append(markerName);
        System.out.println("The complete saved string: " + stringBuilder.toString());

        old_coordinates = markerInfo.getStringSet(setId, null);

        // if there are already saved markers, they will be kept
        if (old_coordinates != null) {
            old_coordinates.add(stringBuilder.toString());
            editor.putStringSet(setId, old_coordinates);
        } else {
            coordinates.add(stringBuilder.toString());
            editor.putStringSet(setId, coordinates);
        }

        editor.commit();

        // add circle and marker's coordinates in the hashMap here?
        mMap.addMarker(new MarkerOptions().position(latLng).title(markerName));
        CircleOptions circleOptions = new CircleOptions().center(latLng)
                .radius(0) // meters
                .strokeColor(Color.RED)
                .strokeWidth(5);
        Circle addedCircle = mMap.addCircle(circleOptions);
        mapCircles.put(latLng.toString(), addedCircle); // maybe better to have hashMap<latlng, circle> ?

        // or maybe it's better that the key is the latLng and the Set has only marker names
        // editor.putStringSet(coordinate, <set with markerName>);

    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {

        Set<String> coordinates;
        //Circle markerCircle = null;

        //SharedPreferences markerInfo = getPreferences(MODE_PRIVATE);
        SharedPreferences markerInfo = this.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        coordinates = markerInfo.getStringSet(setId, null);
        System.out.println("Camera changed");

        // Going through all saved markers
        if (coordinates != null) {
            for (String marker:coordinates) {

                // Getting latitude and longitude coordinates of the marker
                System.out.println("I got the marker: " + marker);
                int start = marker.indexOf("(") + 1;
                int end = marker.indexOf(")");
                /*System.out.println("Length of marker: " + marker.length());
                System.out.println("Start of latitude: " + start);
                System.out.println("End of longitude: " + end);*/
                String onlyCoordinates = String.copyValueOf(marker.toCharArray(), 0, end+1);
                System.out.println("Only coordinates of the marker: " + onlyCoordinates);
                String strCoordinates = String.copyValueOf(marker.toCharArray(), start, end-start);
                System.out.println("Coordinate information of the marker: " + strCoordinates);
                String[] latLong =  strCoordinates.split(",");
                double latitude = Double.parseDouble(latLong[0]);
                double longitude = Double.parseDouble(latLong[1]);

                LatLng markerPoint = new LatLng(latitude, longitude);
                Location markerLocation = new Location("markerLocation");
                markerLocation.setLatitude(latitude);
                markerLocation.setLongitude(longitude);

                // Circle located in those coordinates
                Circle circleMarker = mapCircles.get(onlyCoordinates);

                if (circleMarker != null) {
                    System.out.println("There's a circle there!");

                    // Draw circle for the marker if it's not currently visible
                    if (!mMap.getProjection().getVisibleRegion().latLngBounds.contains(markerPoint)) {

                        // http://stackoverflow.com/questions/20422701/retrieve-distance-from-visible-part-of-google-map
                        Location visibleCenter = new Location("visibleCenter");
                        visibleCenter.setLatitude(mMap.getProjection().getVisibleRegion().latLngBounds.getCenter().latitude);
                        visibleCenter.setLongitude(mMap.getProjection().getVisibleRegion().latLngBounds.getCenter().longitude);
                        float distanceTo = visibleCenter.distanceTo(markerLocation);
                        System.out.println("Distance from center to marker: " + distanceTo);
                        float distanceToScreen = 0;

                        // North of the screen
                        if (latitude > mMap.getProjection().getVisibleRegion().farRight.latitude) {
                            // North east of the screen
                            if (longitude > mMap.getProjection().getVisibleRegion().farRight.longitude) {

                                LatLng ne = mMap.getProjection().getVisibleRegion().farRight;
                                Location neLocation = new Location("northeast");
                                neLocation.setLatitude(ne.latitude);
                                neLocation.setLongitude(ne.longitude);
                                distanceToScreen = markerLocation.distanceTo(neLocation);
                                distanceToScreen = (float) (distanceToScreen + 0.1*visibleCenter.distanceTo(neLocation));
                            }
                            // North west of the screen
                            if (longitude < mMap.getProjection().getVisibleRegion().farLeft.longitude) {

                                LatLng nw = mMap.getProjection().getVisibleRegion().farLeft;
                                Location nwLocation = new Location("northwest");
                                nwLocation.setLatitude(nw.latitude);
                                nwLocation.setLongitude(nw.longitude);
                                distanceToScreen = markerLocation.distanceTo(nwLocation);
                                distanceToScreen = (float) (distanceToScreen + 0.1*visibleCenter.distanceTo(nwLocation));
                            }
                            // North middle
                            if (longitude > mMap.getProjection().getVisibleRegion().farLeft.longitude
                                    && longitude < mMap.getProjection().getVisibleRegion().farRight.longitude) {
                                Location nmLocation = new Location("northMiddle");
                                nmLocation.setLatitude(mMap.getProjection().getVisibleRegion().farRight.latitude);
                                nmLocation.setLongitude(longitude);
                                distanceToScreen = markerLocation.distanceTo(nmLocation);
                                distanceToScreen = (float) (distanceToScreen + 0.1*visibleCenter.distanceTo(nmLocation));
                            }
                        }

                        // South of the screen
                        if (latitude < mMap.getProjection().getVisibleRegion().nearRight.latitude) {
                            // South east of the screen
                            if (longitude > mMap.getProjection().getVisibleRegion().nearRight.longitude) {

                                LatLng se = mMap.getProjection().getVisibleRegion().nearRight;
                                Location seLocation = new Location("southeast");
                                seLocation.setLatitude(se.latitude);
                                seLocation.setLongitude(se.longitude);
                                distanceToScreen = markerLocation.distanceTo(seLocation);
                                distanceToScreen = (float) (distanceToScreen + 0.1*visibleCenter.distanceTo(seLocation));
                            }
                            // South west of the screen
                            if (longitude < mMap.getProjection().getVisibleRegion().nearLeft.longitude) {

                                LatLng sw = mMap.getProjection().getVisibleRegion().nearLeft;
                                Location swLocation = new Location("southwest");
                                swLocation.setLatitude(sw.latitude);
                                swLocation.setLongitude(sw.longitude);
                                distanceToScreen = markerLocation.distanceTo(swLocation);
                                distanceToScreen = (float) (distanceToScreen + 0.1*visibleCenter.distanceTo(swLocation));
                            }
                            // South middle of the screen
                            if (longitude > mMap.getProjection().getVisibleRegion().nearLeft.longitude
                                    && longitude < mMap.getProjection().getVisibleRegion().nearRight.longitude) {
                                Location smLocation = new Location("southMiddle");
                                smLocation.setLatitude(mMap.getProjection().getVisibleRegion().nearRight.latitude);
                                smLocation.setLongitude(longitude);
                                distanceToScreen = markerLocation.distanceTo(smLocation);
                                distanceToScreen = (float) (distanceToScreen + 0.1*visibleCenter.distanceTo(smLocation));
                            }
                        }

                        // Middle of the screen
                        if (latitude < mMap.getProjection().getVisibleRegion().farLeft.latitude &&
                                latitude > mMap.getProjection().getVisibleRegion().nearLeft.latitude) {
                            // Middle west
                            if (longitude < mMap.getProjection().getVisibleRegion().farLeft.longitude) {
                                Location mwLocation = new Location("middleWest");
                                mwLocation.setLatitude(latitude);
                                mwLocation.setLongitude(mMap.getProjection().getVisibleRegion().farLeft.longitude);
                                distanceToScreen = markerLocation.distanceTo(mwLocation);
                                distanceToScreen = (float) (distanceToScreen + 0.1*visibleCenter.distanceTo(mwLocation));
                            }

                            // Middle east
                            if (longitude > mMap.getProjection().getVisibleRegion().nearRight.longitude) {
                                Location meLocation = new Location("middleEast");
                                meLocation.setLatitude(latitude);
                                meLocation.setLongitude(mMap.getProjection().getVisibleRegion().farRight.longitude);
                                distanceToScreen = markerLocation.distanceTo(meLocation);
                                distanceToScreen = (float) (distanceToScreen + 0.1*visibleCenter.distanceTo(meLocation));
                            }

                        }
                        circleMarker.setRadius(distanceToScreen);
                    } else {
                        // No need to show circle if marker point is visible
                        circleMarker.setRadius(0);
                    }
                }

            }

        } else {
            System.out.println("There are no coordinates saved in SharedPreferences");
        }
        // calculations of distances between my markers and 'cameraPosition'

    }
}
