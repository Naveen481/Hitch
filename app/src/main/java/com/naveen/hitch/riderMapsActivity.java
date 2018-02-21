package com.naveen.hitch;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationListener;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.sql.SQLOutput;
import java.util.HashMap;
import java.util.List;

public class riderMapsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Location mLastKnownLocation;
    Location mDropoffLocation = new Location("Rider Dropoff Location");
    LocationRequest mLocationRequest;
    private Button mRequest;
    private LatLng pickUpLocation;
    private float cameraZoom = 13;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private Boolean isLoggingOut = false;
    private Boolean isDestSet = false;
    private Marker mRiderMarker;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.riderDrawerLayout);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
        Menu menu = navigationView.getMenu();
        MenuItem nav_driverMode = menu.findItem(R.id.nav_driverMode);
        nav_driverMode.setTitle("Switch to Driver Mode");

        mRequest = (Button) findViewById(R.id.request);
        mRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!isDestSet){
                    Toast.makeText(riderMapsActivity.this, "Please enter your destination", Toast.LENGTH_SHORT).show();
                }
                else {
//                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
//                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("RiderPosition");
//                    GeoFire geoFire = new GeoFire(ref);
//
//                    geoFire.setLocation(userId, new GeoLocation(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()));

                    pickUpLocation = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
                    if (mRiderMarker != null){
                        mRiderMarker.remove();
                    }
                    mRiderMarker = mMap.addMarker(new MarkerOptions().position(pickUpLocation).title("Pick Up Location"));
                    cameraZoom = 17;
                    mRequest.setText("Finding drivers...");

//                    String riderId = FirebaseAuth.getInstance().getCurrentUser().getUid();
//                    DatabaseReference riderLocationRef = FirebaseDatabase.getInstance().getReference().child("RiderDropoffPosition").child(riderId).child("l");
//                    riderLocationRef.addValueEventListener(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(DataSnapshot dataSnapshot) {
//                            if(dataSnapshot.exists()){
//
//                                List<Object> map = (List<Object>) dataSnapshot.getValue();
//                                double lat = 0;
//                                double lng = 0;
//                                if (map.get(0) != null && map.get(1) != null){
//                                    lat = Double.parseDouble(map.get(0).toString());
//                                    lng = Double.parseDouble(map.get(1).toString());
//                                }
//                                mDropoffLocation = new Location("Dropoff Location");
//                                mDropoffLocation.setLatitude(lat);
//                                mDropoffLocation.setLongitude(lng);
//                            }
//                        }
//
//                        @Override
//                        public void onCancelled(DatabaseError databaseError) {
//
//                        }
//                    });

                    findClosestDriver();

                    if(driverFound){
                        Toast.makeText(riderMapsActivity.this, "Driver found at ", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(riderMapsActivity.this, "No drivers found", Toast.LENGTH_SHORT).show();
                        mRequest.setText("Request ride");
                    }
                }
            }
        });

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment_rider);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("RiderDropoffPosition");

                GeoFire geoFire = new GeoFire(ref);
                geoFire.setLocation(userId, new GeoLocation(place.getLatLng().latitude, place.getLatLng().longitude) );
                isDestSet = true;
                mDropoffLocation.setLatitude(place.getLatLng().latitude);
                mDropoffLocation.setLongitude(place.getLatLng().longitude);
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
//                Log.i(TAG, "An error occurred: " + status);
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(mToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private int radius = 5;
    private Boolean driverFound = false;
    private String driverId;
    private double minDist = Double.MAX_VALUE;
    private double tempDist;

    private void findClosestDriver(){
        DatabaseReference driversAvailable = FirebaseDatabase.getInstance().getReference().child("AvailableDrivers");
        GeoFire geoFire = new GeoFire(driversAvailable);
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(pickUpLocation.latitude, pickUpLocation.longitude), radius);

        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {

                Location location1 = new Location("Driver location");
                location1.setLatitude(location.latitude);
                location1.setLongitude(location.longitude);

                tempDist = mLastKnownLocation.distanceTo(location1);

//                DatabaseReference driverLocationRef = FirebaseDatabase.getInstance().getReference().child("DriverDestination").child(key).child("l");
//                driverLocationRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        if(dataSnapshot.exists()){
//                            List<Object> map = (List<Object>) dataSnapshot.getValue();
//                            double lat = 0;
//                            double lng = 0;
//                            mRequest.setText("Driver Found...");
//                            if (map.get(0) != null && map.get(1) != null){
//                                lat = Double.parseDouble(map.get(0).toString());
//                                lng = Double.parseDouble(map.get(1).toString());
//                            }
//
//                            Location drivertempDestination = new Location("Driver Dropoff Temp");
//                            drivertempDestination.setLatitude(lat);
//                            drivertempDestination.setLongitude(lng);
//
//                            tempDist += drivertempDestination.distanceTo(mDropoffLocation);
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//
//                    }
//                });


                if (tempDist < minDist){
                    driverId = key;
                    minDist = tempDist;
                    driverFound = true;
                }

//                System.out.println("Found driver "+key);
//                if (!driverFound){
//                    driverFound = true;
//                    driverId = key;
//
//                    DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child(driverId);
//                    String riderId = FirebaseAuth.getInstance().getCurrentUser().getUid();
//
//                    HashMap map = new HashMap();
//                    map.put("RiderId", riderId);
//                    driverRef.updateChildren(map);
//
//                    getDriverLocation();
//                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if (driverFound){
                    System.out.println("Driver found with id "+ driverId);
                    DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child(driverId);
                    String riderId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    HashMap map = new HashMap();
                    map.put("RiderId", riderId);
                    driverRef.updateChildren(map);

                    getDriverLocation();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.nav_Profile){
            Toast.makeText(this, "You are trying to access your profile", Toast.LENGTH_LONG).show();
        }
        if(id == R.id.nav_driverMode){
            isLoggingOut = true;
            disconnectRider();
            Intent intent = new Intent(riderMapsActivity.this, DriverMapsActivity.class);
            startActivity(intent);
            finish();
        }
        if(id == R.id.nav_logout){
            isLoggingOut = true;
            disconnectRider();
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(riderMapsActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
        return false;
    }

    @Override
    public void onPostCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onPostCreate(savedInstanceState, persistentState);
        mToggle.syncState();
    }

    private Marker mDriverMarker;
    private void getDriverLocation(){
        DatabaseReference driverLocationRef = FirebaseDatabase.getInstance().getReference().child("WorkingDrivers").child(driverId).child("l");
        driverLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double lat = 0;
                    double lng = 0;
                    if (map.get(0) != null && map.get(1) != null){
                        lat = Double.parseDouble(map.get(0).toString());
                        lng = Double.parseDouble(map.get(1).toString());
                    }
                    mRequest.setText("Driver Found...");
                    LatLng driverLatLng = new LatLng(lat, lng);
                    if (mDriverMarker != null){
                        mDriverMarker.remove();
                    }
                    mDriverMarker = mMap.addMarker(new MarkerOptions().position(driverLatLng).title("Driver Location"));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));


        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);



    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastKnownLocation = location;

        if(!isLoggingOut){

            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(cameraZoom));

            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("RiderPosition");

            GeoFire geoFire = new GeoFire(ref);
            geoFire.setLocation(userId, new GeoLocation(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()));
        }
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {



    }

    private void disconnectRider(){

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("RiderPosition");

        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId);
        DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("RiderDropoffPosition");

        GeoFire geoFire1 = new GeoFire(ref1);
        geoFire1.removeLocation(userId);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(!isLoggingOut){
            disconnectRider();
        }
    }
}
