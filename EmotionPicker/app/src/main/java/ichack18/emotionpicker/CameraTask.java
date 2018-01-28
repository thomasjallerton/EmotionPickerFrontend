package ichack18.emotionpicker;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.Toast;

import com.github.nkzawa.socketio.client.Socket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static com.google.android.gms.internal.zzagr.runOnUiThread;

/**
 * Created by Thomas on 28/01/2018.
 */

public class CameraTask extends AsyncTask<Void, Void, Void> {
    private Context context;
    private String place;
    private Socket socket;
    private Camera camera;


    public CameraTask(Context context, String place, Socket socket) {
        this.context = context;
        this.place = place;
        this.socket = socket;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        takeSnapShots();
        return null;
    }

    private void takeSnapShots()
    {
        if (Looper.myLooper() == null)
        {
            Looper.prepare();
        }
        SurfaceView surface = new SurfaceView(context);
        SurfaceTexture st = new SurfaceTexture(10);
        newOpenCamera();
        try {
            List<Camera.Size> sizes = camera.getParameters().getSupportedPictureSizes();
            int width = sizes.get(0).width;
            int height = sizes.get(0).height;
            Camera.Parameters parameters = camera.getParameters();
            parameters.set("jpeg-quality", 70);
            parameters.setPictureFormat(PixelFormat.JPEG);
            parameters.setPictureSize(width, height);
            camera.setParameters(parameters);
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
            try {

                Bitmap realImage = BitmapFactory.decodeByteArray(data, 0, data.length);
                realImage = rotate(realImage, 270);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                realImage.compress(Bitmap.CompressFormat.JPEG, 70, stream);
                byte[] byteArray = stream.toByteArray();

                socket.emit("image", byteArray, place);
            } catch (Exception e) {

            } finally {
                camera.stopPreview();
                camera.release();
                camera = null;
            }
        }
    };

    public static Bitmap rotate(Bitmap bitmap, int degree) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        Matrix mtx = new Matrix();
        //       mtx.postRotate(degree);
        mtx.setRotate(degree);

        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
    }

    private void newOpenCamera() {
        if (mThread == null) {
            mThread = new CameraHandlerThread();
        }

        synchronized (mThread) {
            mThread.openCamera();
        }
    }

    private CameraHandlerThread mThread = null;
    private class CameraHandlerThread extends HandlerThread {
        Handler mHandler = null;

        CameraHandlerThread() {
            super("CameraHandlerThread");
            start();
            mHandler = new Handler(getLooper());
        }

        synchronized void notifyCameraOpened() {
            notify();
        }

        void openCamera() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        camera = Camera.open(1);
                    }
                    catch (RuntimeException e) {
                        Log.e("Camera", "failed to open front camera");
                    }                    notifyCameraOpened();
                }
            });
            try {
                wait();
            }
            catch (InterruptedException e) {
                Log.w("Camera", "wait was interrupted");
            }
        }
    }

}
