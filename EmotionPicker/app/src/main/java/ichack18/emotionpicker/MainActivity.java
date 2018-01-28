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
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import static android.view.View.GONE;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private ImageView mImageView;
    private String TAG = MainActivity.class.getSimpleName();
    //public static final String SERVER_IP = "http://129.31.206.169:8080";
    public static final String SERVER_IP = "https://emotion-picker.herokuapp.com";
    private Place[] placesArray = new Place[3];

    private Socket socket;
    private View titleView;
    private View imagesView;
    private View ratingsView;
    private GoogleMap mMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

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

    public void titleView(Place place) {
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

        titleView.setVisibility(View.VISIBLE);
        ratingsView.setVisibility(GONE);
        imagesView.setVisibility(GONE);
    }

    public void ratingsView(Place place) {
        RatingBar ratingbar = findViewById(R.id.ratingbar);
        ScrollView reviewsScroll = findViewById(R.id.reviews_scroll);
        final LinearLayout ll = findViewById(R.id.reviews_container);
        final GeoDataClient mGeoDataClient = Places.getGeoDataClient(this, null);
        ll.removeAllViewsInLayout();

        //Set the rating
        ratingbar.setRating(place.getRating());

        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="https://maps.googleapis.com/maps/api/place/details/json?placeid=" + place.getPlaceID() + "&key=AIzaSyDyz2RwNSa3qDRfnclcIgUUWS7Fn5NexfA";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url,
                null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray reviews = response.getJSONObject("result").getJSONArray("reviews");
                    LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    llp.setMargins(0,0,0,20);
                    for (int i = 0; i < 5 || i < reviews.length(); i++) {
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

        titleView.setVisibility(GONE);
        ratingsView.setVisibility(View.VISIBLE);
        imagesView.setVisibility(GONE);

        //takeSnapShots();
    }

    public void imagesView(Place place) {
        final LinearLayout ll = findViewById(R.id.images_container);
        ll.removeAllViewsInLayout();
        final GeoDataClient mGeoDataClient = Places.getGeoDataClient(this, null);

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
                    if (count > 10) break;
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
                            ll.addView(imageView);
                        }
                    });
                }
                photoMetadataBuffer.release();
            }
        });

        titleView.setVisibility(GONE);
        ratingsView.setVisibility(GONE);
        imagesView.setVisibility(View.VISIBLE);
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
            Log.e(TAG, "SUCCESSFULLY CONNECTED");
            socket.on("emotions", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d("EMOTIONS", args[0].toString());
                }
            });
            socket.on("best-place", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    int placeid = (int) args[0];
                    titleView(placesArray[placeid]);
                }
            });
        } catch (URISyntaxException e) {

        }

        titleView = findViewById(R.id.title_address);
        imagesView = findViewById(R.id.images);
        ratingsView = findViewById(R.id.reviews);

        HashSet<Place> places = (HashSet<Place>) getIntent().getSerializableExtra("places");
        int time = 0;
        int placeindex = 0;
        final Handler handler = new Handler();
        for (final Place place : places) {
            placesArray[placeindex] = place;
            placeindex++;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    titleView(place);
                }
            }, time);
            time += 3000;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                   new CameraTask(MainActivity.this, socket).execute();
                }
            }, time);
            time += 7000;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    imagesView(place);
                }
            }, time);
            time += 3000;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    new CameraTask(MainActivity.this, socket).execute();
                }
            }, time);
            time += 7000;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    ratingsView(place);
                }
            }, time);
            time += 3000;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    new CameraTask(MainActivity.this, socket).execute();
                }
            }, time);
            time += 7000;
        }
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ratingsView.setVisibility(GONE);
                TextView titletext = findViewById(R.id.title);
                titletext.setVisibility(GONE);
            }
        }, time);
    }
}
