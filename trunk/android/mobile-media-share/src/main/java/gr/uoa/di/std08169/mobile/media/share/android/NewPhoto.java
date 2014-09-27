package gr.uoa.di.std08169.mobile.media.share.android;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Environment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.charset.Charset;

import gr.uoa.di.std08169.mobile.media.share.android.capture_photo.ShowCamera;
import gr.uoa.di.std08169.mobile.media.share.android.http.HttpClient;
import gr.uoa.di.std08169.mobile.media.share.android.http.PostAsyncTask;

/**
 * @see <a href="http://www.tutorialspoint.com/android/android_camera.htm">Android Capture Photo</a>
 */
public class NewPhoto extends MobileMediaShareActivity implements GoogleMap.OnMapClickListener,
        TextWatcher, View.OnClickListener, PictureCallback {
    private static final int ROTATION = 90;
    private static final String PREFIX = "photo";
    private static final String SUFFIX = ".jpg";
    private static final int QUALITY = 85;

    private FrameLayout photo;
    private Button capture;
    private EditText title;
    private CheckBox isPublic;
    private TextView latlng;
    private Button ok;
    private Button reset;
    private Bitmap bitmap;
    private Camera camera;
    private ProgressDialog progress;
    private Marker marker;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_photo);
        photo = (FrameLayout) findViewById(R.id.photo);
        capture = (Button) findViewById(R.id.capture);
        capture.setOnClickListener(this);
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
        // initialize camera
        initializeCamera();
        // initialize map
        try {
            MapsInitializer.initialize(getApplicationContext());
            final LatLng latLng = getLatLng();
            final GoogleMap map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, GOOGLE_MAPS_ZOOM));
            //prosthikh marker ston xarth me tis default suntetagmenes
            marker = map.addMarker(new MarkerOptions().
                    icon(BitmapDescriptorFactory.fromResource(R.drawable.upload_marker)).
                    anchor(MARKER_ANCHOR_X, MARKER_ANCHOR_Y).
                    position(latLng));
            map.setOnMapClickListener(this);
            latlng.setText(formatLocation(new BigDecimal(latLng.latitude), new BigDecimal(latLng.longitude)));
        } catch (final GooglePlayServicesNotAvailableException e) {
            error(R.string.errorCapturingPhoto, e.getMessage());
        }
        progress = new ProgressDialog(this);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.setTitle(getResources().getString(R.string.uploadingFile));
        progress.setMessage(getResources().getString(R.string.pleaseWait));
    }

    //TextChangedListener
    @Override
    public void beforeTextChanged(final CharSequence charSequence, final int i, final int i2, final int i3) {}

    //TextChangedListener
    @Override
    public void onTextChanged(final CharSequence charSequence, final int i, final int i2, final int i3) {

    }

    //TextChangedListener
    @Override
    public void afterTextChanged(final Editable editable) {
        enableOkReset();
    }

    //OnMapClickListener
    @Override
    public void onMapClick(final LatLng latLng) {
        latlng.setText(formatLocation(new BigDecimal(latLng.latitude), new BigDecimal(latLng.longitude)));
        marker.setPosition(latLng);
    }

    //ClickListener
    @Override
    public void onClick(final View view) {
        if (view == capture) {
            //to antikeimeno this (NewPhoto) ulopoiei to interface PictureCallback
            camera.takePicture(null, null, this);
        } else if (view == reset) {
            initializeCamera();
            title.setText("");
            onMapClick(getLatLng());
            enableOkReset();
            capture.setEnabled(true);
        } else if (view == ok) {
            upload();
        }
    }

    private void upload() {
        try {
            progress.show();
            //Dhmiourgeia enos temp file sto directory me tis eikones
            final File file = File.createTempFile(PREFIX, SUFFIX, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES));
            final FileOutputStream output = new FileOutputStream(file);
            try {
                bitmap.compress(Bitmap.CompressFormat.JPEG, QUALITY, output);
            } finally {
                output.close();
            }
            final String title = this.title.getText().toString();
            final boolean publik = isPublic.isChecked();
            final BigDecimal latitude = new BigDecimal(marker.getPosition().latitude);
            final BigDecimal longitude = new BigDecimal(marker.getPosition().longitude);
            final String url = String.format(getResources().getString(R.string.uploadMediaUrl), getResources().getString(R.string.secureBaseUrl));
            final ContentType type = ContentType.create(
                    MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                            MimeTypeMap.getFileExtensionFromUrl(file.toURI().toURL().toString()))
            );
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
        } catch (final IOException e) {
            error(R.string.errorUploadingMedia, e.getMessage());
        }
    }

    //PictureCallback
    @Override
    public void onPictureTaken(byte[] data, final Camera camera) {
        bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        camera.release();
        capture.setEnabled(false);
        enableOkReset();
    }

    private void enableOkReset() {
        final boolean enabled = (bitmap != null) && (!title.getText().toString().isEmpty());
        ok.setEnabled(enabled);
        reset.setEnabled(enabled);
    }

    private void initializeCamera() {
        try {
            camera = Camera.open();
        } catch (final Exception e) {
            error(R.string.errorCapturingPhoto, e.getMessage());
        }
        final Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        if (size.x < size.y)
            camera.setDisplayOrientation(ROTATION); //peristrofh 90 moires gia Portrait (katakorufo) mode
        photo.addView(new ShowCamera(this, camera));
        bitmap = null;
    }
}
