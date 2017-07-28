package com.example.mapdemo;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

// What's in this file? Since I (Maya) made a lot of changes and didn't write a lot of comments... the TLDR version is:
// 0. the base map fragment (courtesy CodePath)
// 1. connecting with a Shake Listener that, when you shake your phone while in this app, lets you change the type of map you see (i.e. Satellite, Hybrid, Road, etc.)
// 2. a button to search (that is self-explanatory)
// 3. a button to refresh (since markers are being loaded in loadMap(map) and are not updated in real-time (i.e. when someone else adds a marker, you won't be able to see it immediately b/c we don't have push notifications);
// this refresh button is the next best option.
// 4. code that supports deleting a marker (locally AND from parse -- it persists across devices) when you drag & drop it somewhere else
// 5. code that supports adding a marker (locally AND to parse -- it persists across devices)
// 6. back button goes back to HomeGroupActivity.

@RuntimePermissions
public class MapDemoActivity extends AppCompatActivity implements
        GoogleMap.OnMapLongClickListener {

    private SupportMapFragment mapFragment;
    private GoogleMap map;
    private LocationRequest mLocationRequest;
    Location mCurrentLocation;
    private long UPDATE_INTERVAL = 60000;  /* 60 secs */
    private long FASTEST_INTERVAL = 5000; /* 5 secs */
    public List<Marker> markerList;

    // used for loading the correct markers; unwrap from HomeGroupActivity intent
    public String groupID = "";
    String fullName;

    // Shake detection
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;

    // Refresh
    public ImageButton ibRefresh;
    // Filter
    public ImageButton ibFilter;
    // Search
    public ImageButton ibSearch;
    public EditText etSearchQuery;

    private final static String KEY_LOCATION = "location";

    /*
     * Define a request code to send to Google Play services This code is
     * returned in Activity.onActivityResult
     */

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_demo_activity);

        // get right groupID
        groupID = getIntent().getStringExtra("groupId");
        fullName = getIntent().getStringExtra("fullName");

        // initialize list of markers
        markerList = new ArrayList<>();

        if (TextUtils.isEmpty(getResources().getString(R.string.google_maps_api_key))) {
            throw new IllegalStateException("You forgot to supply a Google Maps API key");
        }

        if (savedInstanceState != null && savedInstanceState.keySet().contains(KEY_LOCATION)) {
            // Since KEY_LOCATION was found in the Bundle, we can be sure that mCurrentLocation
            // is not null.
            mCurrentLocation = savedInstanceState.getParcelable(KEY_LOCATION);
        }

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();

        // If you shake your phone, you're prompted to choose the type of map
        mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {
            @Override
            public void onShake(int count) {
                showMapTypeSelectorDialog();
            }
        });

        mapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));
        if (mapFragment != null) {
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap map) {
                    map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    loadMap(map);
                }
            });
        } else {
            Toast.makeText(this, "Error - Map Fragment was null!!", Toast.LENGTH_SHORT).show();
        }

        // Set refresh button
        ibRefresh = (ImageButton) findViewById(R.id.ibRefresh);
        ibRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                map.clear();
                loadMap(map);
            }
        });

        ibFilter = (ImageButton) findViewById(R.id.ibFilter);
        ibFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFilterSelectorDialog();
            }
        });

        // Set search button & EditText
        ibSearch = (ImageButton) findViewById(R.id.ibSearch);
        etSearchQuery = (EditText) findViewById(R.id.etSearchQuery);
        ibSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Set text to visible so user can input place
                etSearchQuery.setVisibility(View.VISIBLE);
                // Do search
                String searchMe = etSearchQuery.getText().toString();
                Geocoder geocoder = new Geocoder(getBaseContext());
                List<Address> addresses = null;
                try {
                    // Getting a maximum of 3 Address that matches the input
                    // text
                    addresses = geocoder.getFromLocationName(searchMe, 3);
                    if (addresses != null && !addresses.equals(""))
                        search(addresses);
                } catch (Exception e) {
                }
                // Set text to invisible once we're done --> moved to search method b/c asynchronous causing issues
                // etSearchQuery.setVisibility(View.GONE);
            }
        });
        Toast.makeText(this, "Shake your phone to change the type of map you see!", Toast.LENGTH_SHORT).show();
    }

    protected void loadMap(GoogleMap googleMap) {

        map = googleMap;

        if (map != null) {
            // Map is ready
            // Toast.makeText(this, "Map Fragment was loaded properly!", Toast.LENGTH_SHORT).show();
            map.setOnMapLongClickListener(this);
            // When the marker is clicked, show details about it
            map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    // Toast.makeText(MapDemoActivity.this, "Clicked a marker!", Toast.LENGTH_SHORT).show();
                    Intent markerDetailsIntent = new Intent(getApplicationContext(), MarkerDetailsActivity.class);
                    markerDetailsIntent.putExtra("title", marker.getTitle());
                    markerDetailsIntent.putExtra("snippet", marker.getSnippet());
                    markerDetailsIntent.putExtra("location", String.valueOf(marker.getPosition()));
                    markerDetailsIntent.putExtra("fullName", fullName);
                    markerDetailsIntent.putExtra("groupID", groupID);
                    startActivity(markerDetailsIntent);
                    return false;
                }
            });
            map.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                @Override
                public void onMarkerDrag(Marker marker) {
                }

                @Override
                public void onMarkerDragEnd(Marker marker) {
                    // when the marker is done being dragged, delete it from Parse using the snippet as a key.
                    final String snippet = marker.getSnippet();
                    // Get all markers from this groupID from Parse, then get the marker from that ID matching the current snippet
                    ParseQuery<ParseObject> query  = ParseQuery.getQuery("Markers");
                    query.whereEqualTo("groupID", groupID);
                    // Delete it
                    query.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> parseObjects, com.parse.ParseException e) {
                            if (e==null){
                                int size = parseObjects.size();
                                if (size > 0) {
                                    for (int i = 0; i < size; i++) {
                                        String curr = parseObjects.get(i).getString("Snippet");
                                        if (curr.equals(snippet)) {
                                            ParseObject deleteMe = parseObjects.get(i);
                                            try {
                                                deleteMe.delete();
                                                deleteMe.saveInBackground();
                                                break;
                                            } catch (ParseException e1) {
                                                e1.printStackTrace();
                                            }
                                        }
                                    }
                                }
                                // else log the error
                            } else {
                                Log.e("ERROR:", "" + e.getMessage());
                            }
                        }
                    });
                    // Remove the actual marker object from the map for real-time result
                    marker.remove();
                }

                @Override
                public void onMarkerDragStart(Marker marker) {
                }
            });
            ParseQuery<ParseObject> query  = ParseQuery.getQuery("Markers");
            query.whereEqualTo("groupID", groupID);
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> parseObjects, com.parse.ParseException e) {
                    if (e==null){
                        int size = parseObjects.size();
                        if (size > 0) {
                            for (int i = 0; i < size; i++) {
                                // get the current object (ie marker)
                                ParseObject current = parseObjects.get(i);
                                // extract attributes: title, snippet, position
                                String title = current.getString("Title");
                                String snippet = current.getString("Snippet");
                                String location = current.getString("Location");
                                // strip extraneous pieces off string
                                location = location.substring(10, location.length() - 1);
                                String[] latlong =  location.split(",");
                                double latitude = Double.parseDouble(latlong[0]);
                                double longitude = Double.parseDouble(latlong[1]);
                                // convert to latlng so we can place the marker there
                                LatLng position = new LatLng(latitude, longitude);
                                // add attributes to marker
                                Marker marker = map.addMarker(new MarkerOptions()
                                        .draggable(true)
                                        .position(position)
                                        .title(title)
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.mapicon))
                                        .snippet(snippet));
                                // String itemId = parseObjects.get(i).getObjectId();
                                // Toast.makeText(MapDemoActivity.this, "Loaded from PARSE: object " + itemId, Toast.LENGTH_SHORT).show();
                            }
                        }
                        // else don't load any image & wait for the user to upload one
                    } else {
                        Log.e("ERROR:", "" + e.getMessage());
                    }
                }
            });
            MapDemoActivityPermissionsDispatcher.getMyLocationWithCheck(this);
            MapDemoActivityPermissionsDispatcher.startLocationUpdatesWithCheck(this);

        } else {
            Toast.makeText(this, "Error - Map was null!!", Toast.LENGTH_SHORT).show();
        }
    }

    private void dropPinEffect(final Marker marker) {
        // Handler allows us to repeat a code block after a specified delay
        final android.os.Handler handler = new android.os.Handler();
        final long start = SystemClock.uptimeMillis();
        final long duration = 1500;

        // Use the bounce interpolator
        final android.view.animation.Interpolator interpolator =
                new BounceInterpolator();

        // Animate marker with a bounce updating its position every 15ms
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                // Calculate t for bounce based on elapsed time
                float t = Math.max(
                        1 - interpolator.getInterpolation((float) elapsed
                                / duration), 0);
                // Set the anchor
                marker.setAnchor(0.5f, 1.0f + 14 * t);

                if (t > 0.0) {
                    // Post this event again 15ms from now.
                    handler.postDelayed(this, 15);
                } else { // done elapsing, show window
                    marker.showInfoWindow();
                }
            }
        });
    }

    // Display the alert that adds the marker
    private void showAlertDialogForPoint(final LatLng point) {
        // inflate message_item.xml view
        View messageView = LayoutInflater.from(MapDemoActivity.this).
                inflate(R.layout.message_item, null);
        // Create alert dialog builder
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        // set message_item.xml to AlertDialog builder
        alertDialogBuilder.setView(messageView);

        // Create alert dialog
        final AlertDialog alertDialog = alertDialogBuilder.create();

        // Configure dialog button (OK)
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Define color of marker icon
                        BitmapDescriptor defaultMarker =
                                BitmapDescriptorFactory.fromResource(R.drawable.mapicon);
                        // Extract content from alert dialog
                        String title = ((EditText) alertDialog.findViewById(R.id.etTitle)).
                                getText().toString();
                        String snippet = ((EditText) alertDialog.findViewById(R.id.etSnippet)).
                                getText().toString();
                        // Creates and adds marker to the map
                        Marker marker = map.addMarker(new MarkerOptions()
                                .draggable(true)
                                .position(point)
                                .title(title)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.mapicon))
                                .snippet(snippet));

                        markerList.add(marker);

                        // Saves marker to parse
                        String timeStamp = new SimpleDateFormat("HH:mm MM/dd/yyyy").format(new Date());
                        ParseObject testObject = new ParseObject("Markers");
                        testObject.put("Title", marker.getTitle());
                        testObject.put("Snippet", marker.getSnippet());
                        testObject.put("Location", String.valueOf(marker.getPosition()));
                        testObject.put("groupID", groupID);
                        testObject.put("Timestamp", timeStamp);
                        testObject.saveInBackground();

                        // Animate marker using drop effect
                        // --> Call the dropPinEffect method here
                        dropPinEffect(marker);
                    }
                });

        // Configure dialog button (Cancel)
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) { dialog.cancel(); }
                });

        // Display the dialog
        alertDialog.show();
    }


    @Override
    public void onMapLongClick(final LatLng point) {
        // Toast.makeText(this, "Long Press", Toast.LENGTH_LONG).show();
        showAlertDialogForPoint(point);
        // Custom code here...
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MapDemoActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @NeedsPermission({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    void getMyLocation() {
        //noinspection MissingPermission
        map.setMyLocationEnabled(true);

        FusedLocationProviderClient locationClient = getFusedLocationProviderClient(this);
        //noinspection MissingPermission
        locationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            onLocationChanged(location);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("MapDemoActivity", "Error trying to get last GPS location");
                        e.printStackTrace();
                    }
                });
    }

    /*
     * Called when the Activity becomes visible.
    */
    @Override
    protected void onStart() {
        super.onStart();
    }

    /*
     * Called when the Activity is no longer visible.
	 */
    @Override
    protected void onStop() {
        super.onStop();
    }

    private boolean isGooglePlayServicesAvailable() {
        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d("Location Updates", "Google Play services is available.");
            return true;
        } else {
            // Get the error dialog from Google Play services
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                    CONNECTION_FAILURE_RESOLUTION_REQUEST);

            // If Google Play services can provide an error dialog
            if (errorDialog != null) {
                // Create a new DialogFragment for the error dialog
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                errorFragment.setDialog(errorDialog);
                errorFragment.show(getSupportFragmentManager(), "Location Updates");
            }

            return false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(mShakeDetector, mAccelerometer,	SensorManager.SENSOR_DELAY_UI);

        // Display the connection status

        if (mCurrentLocation != null) {
            // Toast.makeText(this, "GPS location was found!", Toast.LENGTH_SHORT).show();
            LatLng latLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            // these two lines cause a LOT of problems with cameraUpdate being null. Removed b/c zoom is annoying and unnecessary.
            // CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17);
            // map.animateCamera(cameraUpdate);
        } else {
            // Toast.makeText(this, "Current location was null, enable GPS on emulator!", Toast.LENGTH_SHORT).show();
        }
        MapDemoActivityPermissionsDispatcher.startLocationUpdatesWithCheck(this);
    }

    @NeedsPermission({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    protected void startLocationUpdates() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);
        //noinspection MissingPermission
        getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        onLocationChanged(locationResult.getLastLocation());
                    }
                },
                Looper.myLooper());
    }

    @Override
    public void onPause() {
        // Add the following line to unregister the Sensor Manager onPause
        mSensorManager.unregisterListener(mShakeDetector);
        super.onPause();
    }

    public void onLocationChanged(Location location) {
        // GPS may be turned off
        if (location == null) {
            return;
        }
        // Removed for now because annoying: report to the UI that the location was updated
        mCurrentLocation = location;
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        // Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelable(KEY_LOCATION, mCurrentLocation);
        super.onSaveInstanceState(savedInstanceState);
    }

    // Define a DialogFragment that displays the error dialog
    public static class ErrorDialogFragment extends android.support.v4.app.DialogFragment {

        // Global field to contain the error dialog
        private Dialog mDialog;

        // Default constructor. Sets the dialog field to null
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        // Set the dialog to display
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        // Return a Dialog to the DialogFragment
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }

    private static final CharSequence[] FILTER_ITEMS =
            {"Last day", "Last month", "Last year"};

    // determine if a given marker has been placed within the X period (relative to current time)
    private void filterQuery(final String filterFlag) {
        // Get current time
        final String currentTime = new SimpleDateFormat("HH:mm MM/dd/yyyy").format(new Date());
        // Query ALL group's markers from Parse & load only corresponding ones
        ParseQuery<ParseObject> query  = ParseQuery.getQuery("Markers");
        query.whereEqualTo("groupID", groupID);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e==null){
                    int size = parseObjects.size();
                    if (size > 0) {
                        for (int i = 0; i < size; i++) {
                            // get the current object (ie marker)
                            ParseObject current = parseObjects.get(i);
                            // extract attributes: title, snippet, position, timestamp
                            String title = current.getString("Title");
                            String snippet = current.getString("Snippet");
                            String location = current.getString("Location");
                            String timeStamp = current.getString("Timestamp");
                            // strip extraneous pieces off string
                            location = location.substring(10, location.length() - 1);
                            String[] latlong =  location.split(",");
                            double latitude = Double.parseDouble(latlong[0]);
                            double longitude = Double.parseDouble(latlong[1]);
                            // convert to latlng so we can place the marker there
                            LatLng position = new LatLng(latitude, longitude);
                            // add attributes to marker if within year
                            if (filterFlag.equals("year")) {
                                if (isWithinYear(currentTime, timeStamp)) {
                                    Marker marker = map.addMarker(new MarkerOptions()
                                            .draggable(true)
                                            .position(position)
                                            .title(title)
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.mapicon))
                                            .snippet(snippet));
                                }
                            }
                            else if (filterFlag.equals("month")) {
                                if (isWithinMonth(currentTime, timeStamp)) {
                                    Marker marker = map.addMarker(new MarkerOptions()
                                            .draggable(true)
                                            .position(position)
                                            .title(title)
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.mapicon))
                                            .snippet(snippet));
                                }
                            }
                            else if (filterFlag.equals("day")) {
                                if (isWithinDay(currentTime, timeStamp)) {
                                    Marker marker = map.addMarker(new MarkerOptions()
                                            .draggable(true)
                                            .position(position)
                                            .title(title)
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.mapicon))
                                            .snippet(snippet));
                                }
                            }
                        }
                    }
                } else {
                    Log.e("ERROR:", "" + e.getMessage());
                }
            }
        });


    }

    public boolean isWithinYear(String currentTime, String markerTime) {
        // current year is always greater than or equal to than marker year
        String currentYear = currentTime.substring(12);
        String markerYear = markerTime.substring(12);
        int currYear = Integer.valueOf(currentYear);
        int markYear = Integer.valueOf(markerYear);
        // 2017 vs. 2015, for example
        if (currYear - markYear > 1) {
            return false;
        }
        // then move on to month; since we are already within a <2 year period, if markerMonth > currentMonth, return false.
        // OFF BY ONE
        String currentMonth = currentTime.substring(6, 8);
        String markerMonth = markerTime.substring(6, 8);
        int currMonth = Integer.valueOf(currentMonth);
        int markMonth = Integer.valueOf(markerMonth);
        if (markMonth > currMonth) {
            return false;
        }
        // then move on to day; if markerDay > currentDay, return false.
        String currentDay = currentTime.substring(9, 11);
        String markerDay = markerTime.substring(9, 11);
        int currDay = Integer.valueOf(currentDay);
        int markDay = Integer.valueOf(markerDay);
        if (markDay > currDay) {
            return false;
        }
        // if you make it this far
        return true;
    }

    public boolean isWithinMonth(String currentTime, String markerTime) {
        if (!isWithinYear(currentTime, markerTime)) {
            return false;
        }
        return true;
    }

    public boolean isWithinDay(String currentTime, String markerTime) {
        if (!isWithinYear(currentTime, markerTime) || !isWithinMonth(currentTime, markerTime)) {
            return false;
        }
        return true;
    }

    private void showFilterSelectorDialog () {
        final String filterDialogTitle = "What do you want to filter by?";
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(filterDialogTitle);
        // configure OnClickListener
        builder.setSingleChoiceItems(
                FILTER_ITEMS,
                -1,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        // Filter!
                        if (item == 2) {
                            // by year
                            map.clear();
                            filterQuery("year");
                        }
                        else if (item == 1) {
                            map.clear();
                            filterQuery("month");
                        }
                        else if (item == 0) {
                            map.clear();
                            filterQuery("day");
                        }
                        dialog.dismiss();
                    }
                }
        );
        // Build & show dialog
        AlertDialog fFilterDialog = builder.create();
        fFilterDialog.setCanceledOnTouchOutside(true);
        fFilterDialog.show();
    }

    private static final CharSequence[] MAP_TYPE_ITEMS =
            {"Road Map", "Hybrid", "Satellite", "Terrain"};

    private void showMapTypeSelectorDialog() {
        // Builder for dialog
        final String fDialogTitle = "Select Map Type";
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(fDialogTitle);

        // Find the current map type
        int checkItem = map.getMapType() - 1;

        // configure OnClickListener
        builder.setSingleChoiceItems(
                MAP_TYPE_ITEMS,
                checkItem,
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int item) {
                        // Change type of map
                        switch (item) {
                            case 1:
                                map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                                break;
                            case 2:
                                map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                                break;
                            case 3:
                                map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                                break;
                            default:
                                map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        }
                        dialog.dismiss();
                    }
                }
        );
        // Build & show dialog
        AlertDialog fMapTypeDialog = builder.create();
        fMapTypeDialog.setCanceledOnTouchOutside(true);
        fMapTypeDialog.show();
    }

    protected void search(List<Address> addresses) {
        int size = addresses.size();
        Address address = (Address) addresses.get(size - 1);
        Double longitude = address.getLongitude();
        Double latitude = address.getLatitude();
        LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

        String addressText = String.format(
                "%s, %s",
                address.getMaxAddressLineIndex() > 0 ? address
                        .getAddressLine(size - 1) : "", address.getCountryName());

        map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        map.animateCamera(CameraUpdateFactory.zoomTo(15));

        // Cleanup before next query & remove visibility
        etSearchQuery.setText("");
        etSearchQuery.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        Intent homeGroupIntent = new Intent(MapDemoActivity.this, HomeGroupActivity.class);
        startActivity(homeGroupIntent);
    }
}
