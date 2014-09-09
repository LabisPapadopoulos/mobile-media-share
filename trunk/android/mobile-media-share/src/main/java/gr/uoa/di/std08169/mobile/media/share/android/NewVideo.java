package gr.uoa.di.std08169.mobile.media.share.android;

import android.app.ProgressDialog;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * @see <a href="http://stackoverflow.com/questions/1817742/how-can-i-capture-a-video-recording-on-android">capture a video recording</a>
 * @see <a href="http://www.tutorialspoint.com/android/android_progressbar.htm">Progress Bar</a>
 */

public class NewVideo extends MobileMediaShareActivity implements GoogleMap.OnMapClickListener, TextWatcher,
        View.OnClickListener, SurfaceHolder.Callback {

    private static final String FILE_EXTENSION = ".mp4";
    public static final float GOOGLE_MAPS_ZOOM = 8.0f;
    public static final double GOOGLE_MAPS_LATITUDE = 37.968546;	//DIT lat
    public static final double GOOGLE_MAPS_LONGITUDE = 23.766968;	//DIT lng
    private static final float MARKER_ANCHOR_X = 0.5f;
    private static final float MARKER_ANCHOR_Y = 1.0f;

    private Button startRecording;
    private Button stopRecording;
    private EditText title;
    private CheckBox isPublic;
    private TextView latlng;
    private Button ok;
    private Button reset;

    private SurfaceHolder surfaceHolder;
    private SurfaceView surfaceView;
    private MediaRecorder mediaRecorder;
    private File video;
    private Camera camera;
    private String outputPath;
    private String randomName;
    private String absolutePath;

    private ProgressDialog progress;
    private Marker marker;
    private String latitude;
    private String longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_video);

        startRecording = (Button) findViewById(R.id.startRecording);
        startRecording.setOnClickListener(this);

        stopRecording = (Button) findViewById(R.id.stopRecording);
        stopRecording.setOnClickListener(this);

        title = (EditText) findViewById(R.id.title);
        title.addTextChangedListener(this);

        isPublic = (CheckBox) findViewById(R.id.isPublic);
        isPublic.setOnClickListener(this);

        latlng = (TextView) findViewById(R.id.latlng);

        ok = (Button) findViewById(R.id.ok);
        ok.setOnClickListener(this);

        reset = (Button) findViewById(R.id.reset);
        reset.setOnClickListener(this);

        Log.i(null , "Video starting");
        camera = Camera.open();
        surfaceView = (SurfaceView) findViewById(R.id.surface_camera);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        mediaRecorder = new MediaRecorder();

        outputPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).getPath(); ///storage/sdcard0/Movies
        randomName = UUID.randomUUID().toString();
        absolutePath = outputPath + File.separator + randomName + FILE_EXTENSION;

        try {
            MapsInitializer.initialize(getApplicationContext());
            final GoogleMap map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(GOOGLE_MAPS_LATITUDE, GOOGLE_MAPS_LONGITUDE), GOOGLE_MAPS_ZOOM));
            //prosthikh marker ston xarth me tis default suntetagmenes
            marker = map.addMarker(new MarkerOptions().
                    icon(BitmapDescriptorFactory.fromResource(R.drawable.upload_marker)).
                    anchor(MARKER_ANCHOR_X, MARKER_ANCHOR_Y).
                    position(new LatLng(GOOGLE_MAPS_LATITUDE, GOOGLE_MAPS_LONGITUDE)));
            map.setOnMapClickListener(this);
            latlng.setText("(" + Upload.formatLatitude(new BigDecimal(GOOGLE_MAPS_LATITUDE)) + ", " +
                    Upload.formatLongitude(new BigDecimal(GOOGLE_MAPS_LONGITUDE)) +")");
        } catch (GooglePlayServicesNotAvailableException e) {
            error(R.string.errorRetrievingMedia, "error loading Google Maps");
        }

        progress = new ProgressDialog(this);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.setMessage("Waiting...");
    }

    //OnMapClickListener
    @Override
    public void onMapClick(LatLng latLng) {
        latlng.setText("(" + Upload.formatLatitude(new BigDecimal(latLng.latitude)) + ", " +
                Upload.formatLongitude(new BigDecimal(latLng.longitude)) +")");
        marker.setPosition(latLng);
        this.latitude = String.valueOf(marker.getPosition().latitude);
        this.longitude = String.valueOf(marker.getPosition().longitude);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.new_video, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //OnClickListener
    @Override
    public void onClick(View view) {
        if (view == startRecording) {
            try {
                startRecording();
            } catch (Exception e) {
                String message = e.getMessage();
                Log.i(null, "Problem Start"+message);
                mediaRecorder.release();
            }
        } else if (view == stopRecording) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
        } else if (view == ok) {
            try {
                uploadVideo();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //TextChangedListener
    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

    }

    //TextChangedListener
    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

    }

    //TextChangedListener
    @Override
    public void afterTextChanged(Editable editable) {

    }

    //SurfaceHolder.Callback
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        if (camera != null){
            camera.setDisplayOrientation(90);
            Camera.Parameters params = camera.getParameters();
            camera.setParameters(params);
        }
        else {
            Toast.makeText(getApplicationContext(), "Camera not available!", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    //SurfaceHolder.Callback
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    //SurfaceHolder.Callback
    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        camera.stopPreview();
        camera.release();
    }


    protected void startRecording() throws IOException {
        mediaRecorder = new MediaRecorder();  // Works well
        camera.unlock();

        mediaRecorder.setCamera(camera);

        mediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);

        mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_480P)); //CamcorderProfile.QUALITY_HIGH
        mediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());

        mediaRecorder.setOutputFile(absolutePath); //"/sdcard/zzzz.3gp"

        mediaRecorder.prepare();
        mediaRecorder.start();
    }

    protected void stopRecording() {
        mediaRecorder.stop();
        mediaRecorder.release();
        camera.release();
    }

    private void releaseMediaRecorder(){
        if (mediaRecorder != null) {
            mediaRecorder.reset();   // clear recorder configuration
            mediaRecorder.release(); // release the recorder object
            mediaRecorder = null;
            camera.lock();           // lock camera for later use
        }
    }

    private void releaseCamera(){
        if (camera != null){
            camera.release();        // release the camera for other applications
            camera = null;
        }
    }

    private void uploadVideo() throws IOException {
        String absoluteFilePath = absolutePath;
        final File oldFile = new File(absoluteFilePath);
        final String title = this.title.getText().toString();
        final boolean publik = (isPublic.isChecked()) ? true : false;
        final String latitude = this.latitude;
        final String longitude = this.longitude;

        if (absoluteFilePath.contains(randomName)) {
            absoluteFilePath = absoluteFilePath.replace(randomName, title);
            final File newFile = new File(absoluteFilePath);

            if (newFile.exists())
                throw new IOException("File Already Exists");

            boolean rename = oldFile.renameTo(newFile);
            if (!rename) {
                throw new IOException("File was not successfully renamed");
            }

            /* Process Bar */
            progress.show();
            final int totalProgressTime = 100;

            final Thread progressThread = new Thread(){

                @Override
                public void run(){

                    int jumpTime = 0;
                    while(jumpTime < totalProgressTime){
                        try {
                            sleep(200);
                            jumpTime += 5;
                            progress.setProgress(jumpTime);
                        } catch (InterruptedException e) { }
                    }

                }
            };
            progressThread.start();

//            new PostAsyncTask(getApplicationContext()) {
//
//                @Override
//                protected void onPostExecute(HttpsResponse response) {
//                    if (!response.isSuccess()) {
//                        Toast.makeText(NewVideo.this, "Error Sending Media", Toast.LENGTH_LONG).show();
//                        Log.d(NewPhoto.class.getName(), response.getResponse());
//                        return;
//                    }
//
//                    progress.cancel();
//                    progressThread.interrupt();
//
//                    Toast.makeText(NewVideo.this, "Video Uploaded Successfully", Toast.LENGTH_LONG).show();
//Log.d(NewVideo.class.getName(), response.getResponse());
//
//                    newFile.delete();
//
//                    final Intent activityIntent = new Intent(getApplicationContext(), Map.class);
//                    activityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                    startActivity(activityIntent);
//                    NewVideo.this.finish();
//                }
//            }.execute(absoluteFilePath, title, Boolean.toString(publik), latitude, longitude);

        } else {
            Toast.makeText(NewVideo.this, "Error Uploading Media", Toast.LENGTH_LONG).show();
        }
    }
}
