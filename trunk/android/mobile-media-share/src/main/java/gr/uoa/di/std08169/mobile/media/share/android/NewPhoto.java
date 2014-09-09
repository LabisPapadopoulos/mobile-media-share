package gr.uoa.di.std08169.mobile.media.share.android;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Environment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
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
import java.math.BigDecimal;

import gr.uoa.di.std08169.mobile.media.share.android.CapturePhoto.ShowCamera;

/**
 * @see <a href="http://www.tutorialspoint.com/android/android_camera.htm">Android Capture Photo</a>
 */
public class NewPhoto extends MobileMediaShareActivity implements GoogleMap.OnMapClickListener, TextWatcher, View.OnClickListener {

    public static final String FILE_EXTENSION = ".jpg";
    public static final float GOOGLE_MAPS_ZOOM = 8.0f;
    public static final double GOOGLE_MAPS_LATITUDE = 37.968546;	//DIT lat
    public static final double GOOGLE_MAPS_LONGITUDE = 23.766968;	//DIT lng
    private static final float MARKER_ANCHOR_X = 0.5f;
    private static final float MARKER_ANCHOR_Y = 1.0f;

    private Button capturePhoto;
    private EditText title;
    private CheckBox isPublic;
    private TextView latlng;
    private Button ok;
    private Button reset;

    private Camera cameraObject;
    private ShowCamera showCamera;
    private FrameLayout cameraPreview;
    private Bitmap bitmap;
    private String mediaStoragePath;

    private ProgressDialog progress;

    private Marker marker;
    private String latitude;
    private String longitude;

    public static Camera isCameraAvailable(){
        Camera camera = null;
        try {
            camera = Camera.open();
            camera.setDisplayOrientation(90);
        }
        catch (Exception e){
        }
        return camera;
    }

    private PictureCallback capturedIt = new PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            Toast.makeText(getApplicationContext(), "taken", Toast.LENGTH_SHORT).show();
            cameraObject.release();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_photo);

        capturePhoto = (Button) findViewById(R.id.capturePhoto);
        capturePhoto.setOnClickListener(this);

        title = (EditText) findViewById(R.id.title);
        title.addTextChangedListener(this);

        isPublic = (CheckBox) findViewById(R.id.isPublic);
        isPublic.setOnClickListener(this);

        latlng = (TextView) findViewById(R.id.latlng);

        ok = (Button) findViewById(R.id.ok);
        ok.setOnClickListener(this);

        reset = (Button) findViewById(R.id.reset);
        reset.setOnClickListener(this);

        cameraObject = isCameraAvailable();
        showCamera = new ShowCamera(this, cameraObject);
        cameraPreview = (FrameLayout) findViewById(R.id.camera_preview);
        cameraPreview.addView(showCamera);

        mediaStoragePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath(); ///storage/sdcard0/Pictures

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

    public void snapIt(View view){
        cameraObject.takePicture(null, null, capturedIt);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.new_photo, menu);
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

    //OnMapClickListener
    @Override
    public void onMapClick(LatLng latLng) {
        latlng.setText("(" + Upload.formatLatitude(new BigDecimal(latLng.latitude)) + ", " +
                Upload.formatLongitude(new BigDecimal(latLng.longitude)) +")");
        marker.setPosition(latLng);
        this.latitude = String.valueOf(marker.getPosition().latitude);
        this.longitude = String.valueOf(marker.getPosition().longitude);
    }

    //ClickListener
    @Override
    public void onClick(View view) {
        if (view == capturePhoto) {
            snapIt(view);
        } else if (view == reset) {
        } else if (view == ok) {
            uploadPhoto();
        }
    }

    private void uploadPhoto(){
        final String title = this.title.getText().toString();
        final boolean publik = (isPublic.isChecked()) ? true : false;
        final String latitude = this.latitude;
        final String longitude = this.longitude;
        final String absoluteFilePath = mediaStoragePath + File.separator + title + FILE_EXTENSION;

        if(bitmap != null){

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


            final File mediaFile = new File(absoluteFilePath);
//            try {
//                final FileOutputStream fileOutputStream = new FileOutputStream(mediaFile);
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fileOutputStream);
//                fileOutputStream.flush();
//                fileOutputStream.close();
//
//                new PostAsyncTask(getApplicationContext()) {
//
//                    @Override
//                    protected void onPostExecute(HttpsResponse response) {
//                        if (!response.isSuccess()) {
//                            Toast.makeText(NewPhoto.this, "Error Sending Media", Toast.LENGTH_LONG).show();
//                            Log.d(NewPhoto.class.getName(), response.getResponse());
//                            return;
//                        }
//
//                        progress.cancel();
//                        progressThread.interrupt();
//
//                        Toast.makeText(NewPhoto.this, "Photo Uploaded Successfully", Toast.LENGTH_LONG).show();
//Log.d(NewPhoto.class.getName(), response.getResponse());
//
//                        mediaFile.delete();
//
//                        final Intent activityIntent = new Intent(getApplicationContext(), Map.class);
//                        activityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                        startActivity(activityIntent);
//                        NewPhoto.this.finish();
//                    }
//                }.execute(absoluteFilePath, title, Boolean.toString(publik), latitude, longitude);
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        } else {
            Toast.makeText(getApplicationContext(), "not taken", Toast.LENGTH_LONG).show();
        }
    }
}
