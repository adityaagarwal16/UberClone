package com.example.uberclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.billy.android.swipe.SmartSwipeRefresh;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class Rideractivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    FusedLocationProviderClient client;
    Location curlocation;
    TextView address,book;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rideractivity);

        address = findViewById(R.id.address);
        book = findViewById(R.id.book);
        book.setText("Book a Ride");

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        client = LocationServices.getFusedLocationProviderClient(Rideractivity.this);
        fetchlocation();

        SmartSwipeRefresh.SmartSwipeRefreshDataLoader loader = new SmartSwipeRefresh.SmartSwipeRefreshDataLoader() {
            @Override
            public void onRefresh(SmartSwipeRefresh ssr) {
                fetchlocation();
                boolean loaded =false;
                ssr.setNoMoreData(loaded);
                ssr.finished(true);
                mMap.clear();
            }

            @Override
            public void onLoadMore(SmartSwipeRefresh ssr) {

            }
        };

        SmartSwipeRefresh.drawerMode(address,false).setDataLoader(loader).disableLoadMore();

        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Requests");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e==null&&objects.size()>0)
                {
                    book.setText("Cancel my Ride");
                }
            }
        });


    }


    public void book(View view) {
        if (book.getText().equals("Book a Ride")) {
            ParseObject request = new ParseObject("Requests");
            request.put("username", ParseUser.getCurrentUser());
            ParseGeoPoint geoPoint = new ParseGeoPoint(curlocation.getLatitude(), curlocation.getLongitude());
            request.put("location", geoPoint);
            request.saveInBackground(new SaveCallback() {
                @SuppressLint("SetTextI18n")
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        Toast.makeText(getApplicationContext(), "Ride is on the way!", Toast.LENGTH_SHORT).show();
                        book.setText("Cancel my Ride");
                    } else {
                        Toast.makeText(getApplicationContext(), e.getMessage() + "\n Please try again", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        else
        {
            new AlertDialog.Builder(this).setTitle("Cancel Ride").
                    setMessage("Are you sure you want to cancel your ride?").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    final ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Requests");
                    query.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> objects, ParseException e) {
                            if (e == null) {
                                for (ParseObject object : objects) {
                                    object.deleteInBackground(new DeleteCallback() {
                                        @SuppressLint("SetTextI18n")
                                        @Override
                                        public void done(ParseException e) {
                                            if (e == null) {
                                                Toast.makeText(getApplicationContext(), "Ride cancelled!", Toast.LENGTH_SHORT).show();
                                                book.setText("Book a Ride");
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    });
                }
            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).create().show();
        }
    }

    private void fetchlocation() {

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        != PackageManager.PERMISSION_GRANTED)
        {
            if(ActivityCompat.shouldShowRequestPermissionRationale(Rideractivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this).setTitle("Location access required!").
                        setMessage("please give permission").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(Rideractivity.this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();
            }
            else {
                ActivityCompat.requestPermissions(Rideractivity.this, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
        else
        {
            //permission granted
            Task<Location> task = client.getLastLocation();
            task.addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if(location!=null)
                    {
                        curlocation = location;
                        Log.i("location",curlocation.getLatitude() + " " + curlocation.getLongitude());
                        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                                .findFragmentById(R.id.map);
                        assert mapFragment != null;
                        mapFragment.getMapAsync(Rideractivity.this);
                    }
                }
            });
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(curlocation.getLatitude(), curlocation.getLongitude());
        mMap.addMarker(new MarkerOptions().position(sydney).title("You are here"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney,16));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        String addressz = "";
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        try {
            List<Address> addressList = geocoder.getFromLocation(curlocation.getLatitude(),curlocation.getLongitude(),1);
            if(addressList!=null && addressList.size()>0){
                addressz = addressList.get(0).getAddressLine(0);
                Log.i("info", addressList.get(0).getAddressLine(0));
            }
            address.setText(addressz);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
        {
            fetchlocation();
        }
    }
}
