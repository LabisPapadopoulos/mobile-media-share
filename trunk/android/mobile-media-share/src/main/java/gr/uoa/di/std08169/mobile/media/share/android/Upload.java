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

    private static final int REQUEST_PATH = 1;
    private static final BigDecimal DEGREES_BASE = new BigDecimal(60);

    private EditText file;
    private Button browse;
    private EditText title;
    private CheckBox isPublic;
    private TextView latlng;
    private Button ok;
    private Button reset;
    private Marker marker;
    private ProgressDialog progress;
    private String currentFileName;
    private String absolutePath;

    //TextChangedListener
    @Override
    public void afterTextChanged(final Editable editable) { // TODO
        enableOkReset();
    }

    //TextChangedListener
    @Override
    public void beforeTextChanged(final CharSequence charSequence, final int start, final int count, final int after) {}

    public void browse(final View view) {
        Intent fileChooserIntent = new Intent(this, FileChooser.class);
        startActivityForResult(fileChooserIntent, REQUEST_PATH);
    }

    // Listen for results. apo to activity fileChooser
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        // See which child activity is calling us back.
        if ((requestCode == REQUEST_PATH) && (resultCode == RESULT_OK)) {
            currentFileName = data.getStringExtra("GetFileName");
            absolutePath = data.getStringExtra("GetPath") + File.separator + currentFileName;
            file.setText(currentFileName); // TODO
            Log.d("--> Absolute Path: ", absolutePath);
            enableOkReset();
        }
    }

    //OnClickListener
    @Override
    public void onClick(View view) { // TODO
        if (view == file) {
            browse(view);
        } else if (view == browse) {
            browse(view);
        } else if (view == isPublic) {
            enableOkReset();
        } else if (view == ok) {
            upload();
        } else if (view == reset) {

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upload);
        file = (EditText) findViewById(R.id.file);
        file.setOnClickListener(this);
        browse = (Button) findViewById(R.id.browse);
        browse.setOnClickListener(this);
        title = (EditText) findViewById(R.id.title);
        title.addTextChangedListener(this);
        isPublic = (CheckBox) findViewById(R.id.isPublic);
        latlng = (TextView) findViewById(R.id.latlng);
        ok = (Button) findViewById(R.id.ok);
        ok.setEnabled(false);
        ok.setOnClickListener(this);
        reset = (Button) findViewById(R.id.reset);
        reset.setEnabled(false);
        reset.setOnClickListener(this);
        try {
            MapsInitializer.initialize(getApplicationContext());
            final GoogleMap map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Map.GOOGLE_MAPS_LATITUDE, Map.GOOGLE_MAPS_LONGITUDE), Map.GOOGLE_MAPS_ZOOM));
            //prosthikh marker ston xarth me tis default suntetagmenes
            marker = map.addMarker(new MarkerOptions().
                    icon(BitmapDescriptorFactory.fromResource(R.drawable.upload_marker)).
                    anchor(Map.MARKER_ANCHOR_X, Map.MARKER_ANCHOR_Y).
                    position(new LatLng(Map.GOOGLE_MAPS_LATITUDE, Map.GOOGLE_MAPS_LONGITUDE)));
            map.setOnMapClickListener(this);
            latlng.setText("(" + formatLatitude(new BigDecimal(Map.GOOGLE_MAPS_LATITUDE)) + ", " +
                    formatLongitude(new BigDecimal(Map.GOOGLE_MAPS_LONGITUDE)) +")"); // TODO
        } catch (GooglePlayServicesNotAvailableException e) {
            error(R.string.errorRetrievingMedia, e.getMessage());
        }
        progress = new ProgressDialog(this);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.setTitle("Uploading File"); // TODO
        progress.setMessage("Waiting..."); // TODO
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.upload, menu);
        return true;
    }

    //OnMapClickListener
    @Override
    public void onMapClick(final LatLng latLng) {
        latlng.setText("(" + formatLatitude(new BigDecimal(latLng.latitude)) + ", " +
                formatLongitude(new BigDecimal(latLng.longitude)) +")");
        marker.setPosition(latLng);
        enableOkReset();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return (item.getItemId() == R.id.settings) || super.onOptionsItemSelected(item);
    }

    //TextChangedListener
    @Override
    public void onTextChanged(final CharSequence charSequence, final int start, final int count, final int after) {}

    private void enableOkReset() {

    }

    private void upload() {
        final String absoluteFilePath = absolutePath;
        final String title = this.title.getText().toString();
        final boolean publik = (isPublic.isChecked()) ? true : false;
//        final String latitude = this.latitude; //TODO
//        final String longitude = this.longitude; //TODO

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

    public static String formatLatitude(final BigDecimal latitude) { // TODO
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

    public static String formatLongitude(final BigDecimal longitude) { // TODO
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
