package ichack18.emotionpicker;

import android.*;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import static android.view.View.GONE;

public class ResultActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Place place = (Place) getIntent().getSerializableExtra("place");

        mMap = googleMap;
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }
        mMap.getUiSettings().setMapToolbarEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(false);

        titleView(place);
    }

    public void titleView(final Place place) {
        TextView title = findViewById(R.id.title);
        TextView address = findViewById(R.id.address);
        title.setVisibility(View.VISIBLE);
        //set the title and address
        title.setText("You are going to:\n" + place.getTitle());
        address.setText(place.getAddress());


        mMap.clear();
        LatLng location = new LatLng(place.getLat(), place.getLongi());
        mMap.addMarker(new MarkerOptions().position(location));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15));

    }
}
