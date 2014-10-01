package gr.uoa.di.std08169.mobile.media.share.android;

import android.app.ProgressDialog;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Environment;
import android.view.SurfaceHolder;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.SurfaceView;
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
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Date;

import gr.uoa.di.std08169.mobile.media.share.android.http.HttpClient;
import gr.uoa.di.std08169.mobile.media.share.android.http.PostAsyncTask;

/**
 * @see <a href="http://stackoverflow.com/questions/1817742/how-can-i-capture-a-video-recording-on-android">capture a video recording</a>
 * @see <a href="http://www.tutorialspoint.com/android/android_progressbar.htm">Progress Bar</a>
 */

public class NewVideo extends MobileMediaShareActivity implements GoogleMap.OnMapClickListener, TextWatcher,
        View.OnClickListener, SurfaceHolder.Callback {
    private static final int ROTATION = 90;
    private static final String PREFIX = "video";
    private static final String SUFFIX = ".mp4";
    private SurfaceView video;
    private Button startRecording;
    private Button stopRecording;
    private EditText title;
    private CheckBox isPublic;
    private TextView latlng;
    private Button ok;
    private Button reset;
    private MediaRecorder recorder;
    private Date startTime;
    private long duration;
    private File file;
    private Camera camera;
    private ProgressDialog progress;
    private Marker marker;

    //TextChangedListener
    @Override
    public void afterTextChanged(final Editable editable) {
        enableOkReset();
    }

    //TextChangedListener
    @Override
    public void beforeTextChanged(final CharSequence charSequence, final int i, final int i2, final int i3) {}

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_video);
        video = (SurfaceView) findViewById(R.id.surface_camera);
        video.getHolder().addCallback(this);
//      video.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); TODO
        startRecording = (Button) findViewById(R.id.startRecording);
        startRecording.setOnClickListener(this);
        stopRecording = (Button) findViewById(R.id.stopRecording);
        stopRecording.setEnabled(false);
        stopRecording.setOnClickListener(this);
        title = (EditText) findViewById(R.id.title);
        title.addTextChangedListener(this);
        isPublic = (CheckBox) findViewById(R.id.isPublic);
        isPublic.setOnClickListener(this);
        latlng = (TextView) findViewById(R.id.latlng);
        ok = (Button) findViewById(R.id.ok);
        ok.setEnabled(false);
        ok.setOnClickListener(this);
        reset = (Button) findViewById(R.id.reset);
        reset.setEnabled(false);
        reset.setOnClickListener(this);
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
            error(R.string.errorCapturingVideo, e.getMessage());
        }
        progress = new ProgressDialog(this);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.setTitle(getResources().getString(R.string.uploadingFile));
        progress.setMessage(getResources().getString(R.string.pleaseWait));
    }

    //OnClickListener
    @Override
    public void onClick(final View view) {
        if (view == startRecording) {
            try {
                camera = Camera.open();
                camera.setDisplayOrientation(ROTATION);
                recorder = new MediaRecorder();
                recorder.setCamera(camera);
                recorder.setPreviewDisplay(video.getHolder().getSurface());
                recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
                recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                recorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_480P));
                if ((file != null) && file.exists())
                    file.delete();
                file = File.createTempFile(PREFIX, SUFFIX, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES));
                recorder.setOutputFile(file.getPath());
                recorder.prepare();
                startTime = new Date();
                duration = 0;
                camera.unlock(); //unlock h camera gia na parei ton elegxo o recorder
                recorder.start();
                startRecording.setEnabled(false);
                stopRecording.setEnabled(true);
            } catch (final Exception e) {
                error(R.string.errorCapturingVideo, e.getMessage());
            }
        } else if (view == stopRecording) {
            recorder.stop();
            recorder.release();
            camera.release();
            duration = (new Date().getTime() - startTime.getTime()) / 1000l; //seconds
            startTime = null;
            stopRecording.setEnabled(false);
            startRecording.setEnabled(true);
            enableOkReset();
        } else if (view == ok) {
            upload();
        } else if (view == reset) {
            if (stopRecording.isEnabled())
                recorder.stop();
            recorder.release();
            camera.release();
            duration = 0l; //seconds
            startTime = null;
            stopRecording.setEnabled(false);
            if ((file != null) && file.exists())
                file.delete();
            ok.setEnabled(false);
            reset.setEnabled(false);
            startRecording.setEnabled(true);
            title.setText("");
            isPublic.setChecked(false);
            final LatLng position = getLatLng();
            latlng.setText(formatLocation(new BigDecimal(position.latitude), new BigDecimal(position.longitude)));
            marker.setPosition(position);

        }
    }

    //OnMapClickListener
    @Override
    public void onMapClick(final LatLng latLng) {
        latlng.setText(formatLocation(new BigDecimal(latLng.latitude), new BigDecimal(latLng.longitude)));
        marker.setPosition(latLng);
    }

    //TextChangedListener
    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {}

    //SurfaceHolder.Callback
    @Override
    public void surfaceCreated(final SurfaceHolder surfaceHolder) {}

    //SurfaceHolder.Callback
    @Override
    public void surfaceChanged(final SurfaceHolder holder, final int format, final int width, final int height) {}

    //SurfaceHolder.Callback
    @Override
    public void surfaceDestroyed(final SurfaceHolder surfaceHolder) {
        if (stopRecording.isEnabled()) {
            recorder.stop();
            recorder.release();
            camera.release();
        }
        if ((file != null) && file.exists())
            file.delete();
    }

    private void enableOkReset() {
        final boolean enabled = (file != null) && (file.exists()) && (!title.getText().toString().isEmpty());
        ok.setEnabled(enabled);
        reset.setEnabled(enabled);
    }

    private void upload() {
        try {
            progress.show();
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
                    addTextBody("duration", Long.toString(duration)).
                    addTextBody("title", title).
                    addTextBody("public", Boolean.toString(publik)).
                    addTextBody("latitude", latitude.toString()).
                    addTextBody("longitude", longitude.toString()).build();
            new PostAsyncTask(this, new URL(url), httpEntity, httpEntity.getContentType().getValue()) {
                @Override
                protected void onPostExecute(final HttpResponse response) {
                    file.delete();
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
}
