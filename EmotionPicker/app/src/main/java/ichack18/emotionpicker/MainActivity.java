package ichack18.emotionpicker;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private ImageView mImageView;
    private PictureService camera;
    private boolean safeToTakePicture = false;
    private String TAG = MainActivity.class.getSimpleName();
    public static final String SERVER_IP = "";
    public static final int SERVER_PORT = 7821;
    private Socket socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        camera = new PictureService(this);
        Button button = (Button) findViewById(R.id.take_picture);
        mImageView = (ImageView) findViewById(R.id.image);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takeSnapShots();
            }
        });
        try {
            socket = IO.socket(SERVER_IP);
            socket.connect();

        } catch (URISyntaxException e) {

        }
    }

    private void showToast(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void takeSnapShots()
    {
        Toast.makeText(getApplicationContext(), "Image snapshot Started",Toast.LENGTH_SHORT).show();
        // here below "this" is activity context.
        SurfaceView surface = new SurfaceView(this);
        SurfaceTexture st = new SurfaceTexture(10);
        Camera camera = Camera.open(1);
        try {
            List<Camera.Size> sizes = camera.getParameters().getSupportedPictureSizes();
            camera.getParameters().setPictureSize(sizes.get(0).width, sizes.get(0).height);
            camera.setPreviewTexture(st);
            camera.startPreview();
            camera.takePicture(null, null, jpegCallback);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    /** picture call back */
    Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera)
        {
            FileOutputStream outStream = null;
            try {
                final Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                ImageView image =(ImageView) findViewById(R.id.image1);
                image.setImageBitmap(bitmap);
                showToast("Done capturing photo");
            } finally
            {
                camera.stopPreview();
                camera.release();
                camera = null;
                Toast.makeText(getApplicationContext(), "Image snapshot Done",Toast.LENGTH_LONG).show();


            }
            Log.d(TAG, "onPictureTaken - jpeg");
        }
    };

}
