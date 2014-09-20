package gr.uoa.di.std08169.mobile.media.share.android;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
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

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;

import java.io.File;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Date;

import gr.uoa.di.std08169.mobile.media.share.android.FileExplorer.FileChooser;
import gr.uoa.di.std08169.mobile.media.share.android.http.HttpClient;
import gr.uoa.di.std08169.mobile.media.share.android.http.PostAsyncTask;


public class Upload extends MobileMediaShareActivity implements GoogleMap.OnMapClickListener, TextWatcher,
        View.OnClickListener {

    private static final int REQUEST_PATH = 1;

    private EditText fileName;
    private Button browse;
    private File file;
    private EditText title;
    private CheckBox isPublic;
    private TextView latlng;
    private Button ok;
    private Button reset;
    private Marker marker;
    private ProgressDialog progress;

    //TextChangedListener
    @Override
    public void afterTextChanged(final Editable editable) {
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
            file = new File(data.getStringExtra("GetPath") + File.separator + data.getStringExtra("GetFileName"));
            fileName.setText(file.getName());
            enableOkReset();
        }
    }

    //OnClickListener
    @Override
    public void onClick(final View view) {
        if (view == fileName) {
            browse(view);
        } else if (view == browse) {
            browse(view);
        } else if (view == isPublic) {
            enableOkReset();
        } else if (view == ok) {
            //progress.show();
            //Toast.makeText(Upload.this, "Uploading file, please wait...", Toast.LENGTH_LONG).show();
            upload();
        } else if (view == reset) {
            fileName.setText("");
            file = null;
            title.setText("");
            isPublic.setChecked(false);
            final LatLng latLng = getLatLng();
            marker.setPosition(latLng);
            latlng.setText(formatLocation(new BigDecimal(latLng.latitude), new BigDecimal(latLng.longitude)));
            enableOkReset();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upload);
        fileName = (EditText) findViewById(R.id.file);
        fileName.setOnClickListener(this);
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
            final LatLng latLng = getLatLng();
            final GoogleMap map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, Map.GOOGLE_MAPS_ZOOM));
            //prosthikh marker ston xarth me tis default suntetagmenes
            marker = map.addMarker(new MarkerOptions().
                    icon(BitmapDescriptorFactory.fromResource(R.drawable.upload_marker)).
                    anchor(Map.MARKER_ANCHOR_X, Map.MARKER_ANCHOR_Y).
                    position(latLng));
            map.setOnMapClickListener(this);
            latlng.setText(formatLocation(new BigDecimal(latLng.latitude), new BigDecimal(latLng.longitude)));
        } catch (final GooglePlayServicesNotAvailableException e) {
            error(R.string.errorUploadingMedia, e.getMessage());
        }
        progress = new ProgressDialog(this);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.setTitle(getResources().getString(R.string.uploadingFile));
        progress.setMessage(getResources().getString(R.string.pleaseWait));
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
        latlng.setText(formatLocation(new BigDecimal(latLng.latitude), new BigDecimal(latLng.longitude)));
        marker.setPosition(latLng);
        enableOkReset();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return (item.getItemId() == R.id.settings) || super.onOptionsItemSelected(item);
    }

    //TextChangedListener
    @Override
    public void onTextChanged(final CharSequence charSequence, final int start, final int count, final int after) {}

    private void enableOkReset() {
        final boolean enable = !((file == null) || title.getText().toString().isEmpty());
        ok.setEnabled(enable);
        reset.setEnabled(enable);
    }

    private void upload() {
        try {
            progress.show();
            final String title = this.title.getText().toString();
            final boolean publik = isPublic.isChecked();
            final BigDecimal latitude = new BigDecimal(marker.getPosition().latitude);
            final BigDecimal longitude = new BigDecimal(marker.getPosition().longitude);
            final String url = String.format(getResources().getString(R.string.uploadMediaUrl), getResources().getString(R.string.baseUrl));
            final ContentType type = ContentType.create(
                    MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                            MimeTypeMap.getFileExtensionFromUrl(file.toURI().toURL().toString())));
            final HttpEntity httpEntity = MultipartEntityBuilder.create().setCharset(Charset.forName(UTF_8)).
                    setStrictMode().
                    addBinaryBody("file", file, type, file.getName()).
                    addTextBody("title", title).
                    addTextBody("public", Boolean.toString(publik)).
                    addTextBody("latitude", latitude.toString()).
                    addTextBody("longitude", longitude.toString()).build();
            new PostAsyncTask(this, new URL(url), httpEntity, httpEntity.getContentType().getValue()) {
                @Override
                protected void onPostExecute(final HttpResponse response) {
                    if (response == null) {
                        error(R.string.errorUploadingMedia, getResources().getString(R.string.connectionError));
                        return;
                    }
                    if ((response.getStatusLine().getStatusCode() == HttpClient.HTTP_UNAUTHORIZED) && login()) {
                        upload();
                    }
                    if (response.getStatusLine().getStatusCode() != HttpClient.HTTP_OK) {
                        error(R.string.errorUploadingMedia, response.getStatusLine().getReasonPhrase());
                        return;
                    }
                    progress.dismiss();
                    finish();
                }
            }.execute();
        } catch (final MalformedURLException e) {
            error(R.string.errorUploadingMedia, e.getMessage());
        }
    }
}
