package gr.uoa.di.std08169.mobile.media.share.android;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

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

import gr.uoa.di.std08169.mobile.media.share.android.FileExplorer.FileChooser;


public class Upload extends MobileMediaShareActivity implements GoogleMap.OnMapClickListener, TextWatcher,
        View.OnClickListener {

    public static final float GOOGLE_MAPS_ZOOM = 8.0f;
    public static final double GOOGLE_MAPS_LATITUDE = 37.968546;	//DIT lat
    public static final double GOOGLE_MAPS_LONGITUDE = 23.766968;	//DIT lng
    private static final int REQUEST_PATH = 1;
    private static final float MARKER_ANCHOR_X = 0.5f;
    private static final float MARKER_ANCHOR_Y = 1.0f;
    private static final BigDecimal DEGREES_BASE = new BigDecimal(60);

    private EditText fileName;
    private EditText title;
    private CheckBox isPublic;
    private TextView latlng;
    private Button browseButton;
    private Button ok;
    private Button reset;

    private String currentFileName;
    private String absolutePath;

    private ProgressDialog progress;

    private Marker marker;
    private String latitude;
    private String longitude;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upload);

        fileName = (EditText) findViewById(R.id.title);
        fileName.setOnClickListener(this);

        title = (EditText) findViewById(R.id.title);
        title.addTextChangedListener(this);

        isPublic = (CheckBox) findViewById(R.id.isPublic);
        isPublic.setOnClickListener(this);

        latlng = (TextView) findViewById(R.id.latlng);

        browseButton = (Button) findViewById(R.id.browseButton);
        browseButton.setOnClickListener(this);

        ok = (Button) findViewById(R.id.ok);
        ok.setOnClickListener(this);

        reset = (Button) findViewById(R.id.reset);
        reset.setOnClickListener(this);

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
            latlng.setText("(" + formatLatitude(new BigDecimal(GOOGLE_MAPS_LATITUDE)) + ", " +
                    formatLongitude(new BigDecimal(GOOGLE_MAPS_LONGITUDE)) +")");
            this.latitude = String.valueOf(GOOGLE_MAPS_LATITUDE);
            this.longitude = String.valueOf(GOOGLE_MAPS_LONGITUDE);
        } catch (GooglePlayServicesNotAvailableException e) {
            error(R.string.errorRetrievingMedia, "error loading Google Maps");
        }

        progress = new ProgressDialog(this);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.setMessage("Waiting...");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.upload, menu);
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

    public void getfile(final View view){
        Intent fileChooserIntent = new Intent(this, FileChooser.class);
        startActivityForResult(fileChooserIntent, REQUEST_PATH);
    }

    // Listen for results. apo to activity fileChooser
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        // See which child activity is calling us back.
        if (requestCode == REQUEST_PATH){
            if (resultCode == RESULT_OK) {
                currentFileName = data.getStringExtra("GetFileName");
                absolutePath = data.getStringExtra("GetPath") + File.separator + currentFileName;
                fileName.setText(currentFileName);
Log.d("--> Absolute Path: ", absolutePath);
            }
        }
    }

    //OnClickListener
    @Override
    public void onClick(View view) {
        if (view == fileName) {
            getfile(view);
        } else if (view == browseButton) {
            getfile(view);
        } else if (view == ok) {
            upload();
        } else if (view == reset) {

        }
    }

    //TextChangedListener
    @Override
    public void beforeTextChanged(final CharSequence charSequence, final int start, final int count, final int after) {

    }

    //TextChangedListener
    @Override
    public void onTextChanged(final CharSequence charSequence, final int start, final int count, final int after) {

    }

    //TextChangedListener
    @Override
    public void afterTextChanged(Editable editable) {

    }

    //OnMapClickListener
    @Override
    public void onMapClick(final LatLng latLng) {
        latlng.setText("(" + formatLatitude(new BigDecimal(latLng.latitude)) + ", " +
            formatLongitude(new BigDecimal(latLng.longitude)) +")");
        marker.setPosition(latLng);
        this.latitude = String.valueOf(marker.getPosition().latitude);
        this.longitude = String.valueOf(marker.getPosition().longitude);
    }

    private void upload() {
        final String absoluteFilePath = absolutePath;
        final String title = this.title.getText().toString();
        final boolean publik = (isPublic.isChecked()) ? true : false;
        final String latitude = this.latitude;
        final String longitude = this.longitude;

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

//        new PostAsyncTask(getApplicationContext()){
//
//            @Override
//            protected void onPostExecute(HttpsResponse response) {
//                if (!response.isSuccess()) {
//                    Toast.makeText(Upload.this, "Error Sending Media", Toast.LENGTH_LONG).show();
//Log.d(Upload.class.getName(), response.getResponse());
//                    return;
//                }
//
//                progress.cancel();
//                progressThread.interrupt();
//
//                Toast.makeText(Upload.this, "File Uploaded Successfully", Toast.LENGTH_LONG).show();
//Log.d(Upload.class.getName(), response.getResponse());
//                final Intent activityIntent = new Intent(getApplicationContext(), Map.class);
//                activityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                startActivity(activityIntent);
//                Upload.this.finish();
//            }
//        }.execute(absoluteFilePath, title, Boolean.toString(publik), latitude, longitude);
    }

    public static String formatLatitude(final BigDecimal latitude) {
        // 1 moira = 60 prwta lepta
        // 1 prwto lepto = 60 deutera
        // to divideAndRemainder kanei tin diairesh kai epistefei:
        // temp1[0]: phliko
        // temp1[1]: upoloipo
        final BigDecimal[] temp1 = latitude.multiply(DEGREES_BASE).multiply(DEGREES_BASE).divideAndRemainder(DEGREES_BASE);
        final int seconds = temp1[1].intValue();
        final BigDecimal[] temp2 = temp1[0].divideAndRemainder(DEGREES_BASE);
        final int minutes = temp2[1].intValue();
        final int degrees = temp2[0].intValue();
        //an einai arnhtiko-> einai notia, alliws voreia
        return (latitude.compareTo(BigDecimal.ZERO) < 0) ?
                -degrees + "° " + (-minutes) + "′" + (-seconds) + "″ S" :
                degrees + "° " + minutes + "′" + seconds + "″ S";
    }

    public static String formatLongitude(final BigDecimal longitude) {
        final BigDecimal[] temp1 = longitude.multiply(DEGREES_BASE).multiply(DEGREES_BASE).divideAndRemainder(DEGREES_BASE);
        final int seconds = temp1[1].intValue();
        final BigDecimal[] temp2 = temp1[0].divideAndRemainder(DEGREES_BASE);
        final int minutes = temp2[1].intValue();
        final int degrees = temp2[0].intValue();
        //an einai arnhtiko-> einai notia, alliws voreia
        return (longitude.compareTo(BigDecimal.ZERO) < 0) ?
                -degrees + "° " + (-minutes) + "′" + (-seconds) + "″ W" :
                degrees + "° " + minutes + "′" + seconds + "″ W";
    }
}
