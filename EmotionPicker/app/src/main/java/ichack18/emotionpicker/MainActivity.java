package ichack18.emotionpicker;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResponse;
import com.google.android.gms.location.places.PlacePhotoResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import static android.view.View.GONE;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private ImageView mImageView;
    private String TAG = MainActivity.class.getSimpleName();
    //public static final String SERVER_IP = "http://129.31.206.169:8080";
    public static final String SERVER_IP = "https://emotion-picker.herokuapp.com";

    private Socket socket;
    private GoogleMap mMap;
    private HashMap<String, Place> placeHashMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void showToast(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void setupViews(final Place place) {
        TextView title = findViewById(R.id.title);
        TextView address = findViewById(R.id.address);
        title.setVisibility(View.VISIBLE);
        //set the title and address
        title.setText(place.getTitle());
        address.setText(place.getAddress());


        mMap.clear();
        LatLng location = new LatLng(place.getLat(), place.getLongi());
        mMap.addMarker(new MarkerOptions().position(location));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15));

        RatingBar ratingbar = findViewById(R.id.ratingbar);
        final LinearLayout ll = findViewById(R.id.reviews_container);
        final GeoDataClient mGeoDataClient = Places.getGeoDataClient(this, null);
        ll.removeAllViewsInLayout();

        //Set the rating
        ratingbar.setRating(place.getRating());

        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="https://maps.googleapis.com/maps/api/place/details/json?placeid=" + place.getPlaceID() + "&key=AIzaSyDbsQI6jFs8RzwS7JkWtYcydRFaYchv2IU";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url,
                null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray reviews = response.getJSONObject("result").getJSONArray("reviews");
                    LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    llp.setMargins(0,0,0,20);
                    for (int i = 0; i < 4 && i < reviews.length(); i++) {
                        TextView text = new TextView(MainActivity.this);
                        text.setBackgroundResource(R.color.white);
                        text.setElevation(4);
                        text.setTextSize(18);
                        text.setPadding(16, 0, 16, 0);

                        JSONObject review = reviews.getJSONObject(i);
                        text.setText(review.getString("text") + " " + review.getString("rating") + "/5");

                        ll.addView(text, llp);
                    }
                } catch (JSONException e) {

                }
            }
        }, null);
        queue.add(request);

        final LinearLayout ll2 = findViewById(R.id.images_container);
        ll2.removeAllViewsInLayout();

        //Add images to container
        final Task<PlacePhotoMetadataResponse> photoMetadataResponse = mGeoDataClient.getPlacePhotos(place.getPlaceID());
        photoMetadataResponse.addOnCompleteListener(new OnCompleteListener<PlacePhotoMetadataResponse>() {
            @Override
            public void onComplete(@NonNull Task<PlacePhotoMetadataResponse> task) {
                // Get the list of photos.
                PlacePhotoMetadataResponse photos = task.getResult();
                // Get the PlacePhotoMetadataBuffer (metadata for all of the photos).
                PlacePhotoMetadataBuffer photoMetadataBuffer = photos.getPhotoMetadata();
                // Get the first photo in the list.
                int count = 0;
                for (PlacePhotoMetadata photoMetadata : photoMetadataBuffer) {
                    if (count > 5) break;
                    count++;
                    // Get the attribution text.
                    CharSequence attribution = photoMetadata.getAttributions();
                    // Get a full-size bitmap for the photo.
                    Task<PlacePhotoResponse> photoResponse = mGeoDataClient.getPhoto(photoMetadata);
                    photoResponse.addOnCompleteListener(new OnCompleteListener<PlacePhotoResponse>() {
                        @Override
                        public void onComplete(@NonNull Task<PlacePhotoResponse> task) {
                            PlacePhotoResponse photo = task.getResult();
                            Bitmap bitmap = photo.getBitmap();
                            ImageView imageView = new ImageView(MainActivity.this);
                            imageView.setPadding(2, 2, 2, 2);
                            imageView.setAdjustViewBounds(true);
                            imageView.setMaxWidth(700);
                            imageView.setImageBitmap(bitmap);
                            ll2.addView(imageView);
                        }
                    });
                }
                photoMetadataBuffer.release();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(false);

        try {
            socket = IO.socket(SERVER_IP);
            socket.connect();
            socket.on("best-place", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    String placeid = (String) args[0];
                    Place place = placeHashMap.get(placeid);
                    if (place != null) {
                        socket.disconnect();
                        Intent i = new Intent(MainActivity.this, ResultActivity.class);
                        i.putExtra("place", place);
                        startActivity(i);
                    } else {
                        Log.d(TAG, "Place was null!");
                    }
                }
            });
        } catch (URISyntaxException e) {

        }

        ArrayList<Place> places = (ArrayList<Place>) getIntent().getSerializableExtra("places");
        Log.d(TAG, "number of places: " + places.size());

        int time = 0;
        final Handler handler = new Handler();

        for (final Place place : places) {
            placeHashMap.put(place.getPlaceID(), place);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    ScrollView sv = findViewById(R.id.main_scroll);
                    sv.fullScroll(ScrollView.FOCUS_UP);

                    setupViews(place);
                }
            }, time);
            time += 1000;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                   new CameraTask(MainActivity.this, place.getPlaceID(), socket).execute();
                }
            }, time);
            time += 6000;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    new CameraTask(MainActivity.this, place.getPlaceID(), socket).execute();
                }
            }, time);
            time += 3000;
        }
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.reviews).setVisibility(GONE);
                findViewById(R.id.title_address).setVisibility(GONE);
                findViewById(R.id.images).setVisibility(GONE);
                findViewById(R.id.title).setVisibility(GONE);
            }
        }, time);
    }

    @Override
    public void onDestroy() {
        socket.disconnect();
        super.onDestroy();
    }


}
