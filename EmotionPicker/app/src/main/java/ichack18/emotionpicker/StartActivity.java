package ichack18.emotionpicker;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.HashSet;

public class StartActivity extends AppCompatActivity {

    Socket socket;
    FusedLocationProviderClient mFusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getSupportActionBar().hide();

        try {
            socket = IO.socket(MainActivity.SERVER_IP);
            socket.connect();
            Log.e("StartActivity", "SUCCESSFULLY CONNECTED");

            socket.on("place-results", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    JSONArray array = (JSONArray) args[0];
                    HashSet<Place> places = new HashSet<>();

                    try {
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            Place place = new Place();
                            place.setAddress(obj.getString("formatted_address"));
                            place.setOpennow(obj.getJSONObject("opening_hours").getBoolean("open_now"));
                            place.setPlaceID(obj.getString("place_id"));
                            place.setRating(obj.getInt("rating"));
                            place.setTitle(obj.getString("name"));
                            JSONObject location = obj.getJSONObject("geometry").getJSONObject("location");
                            place.setLat(location.getDouble("lat"));
                            place.setLongi(location.getDouble("lng"));

                            places.add(place);
                        }


                        Intent i = new Intent(StartActivity.this, MainActivity.class);

                        i.putExtra("places", places);
                        startActivity(i);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        Button button = (Button) findViewById(R.id.start);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText text = (EditText) findViewById(R.id.location_text);
                if (text.getText().toString().length() > 0) {
                    if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        mFusedLocationClient.getLastLocation().addOnSuccessListener(StartActivity.this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if (socket != null) {
                                    try {
                                        JSONObject obj = new JSONObject();
                                        obj.put("lat", location.getLatitude());
                                        obj.put("lng", location.getLongitude());
                                        String txt = text.getText().toString();
                                        socket.emit("search-places", txt, obj);

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });
                    }
                }
            }
        });
    }
}
