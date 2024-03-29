package gr.uoa.di.std08169.mobile.media.share.android;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

import org.apache.http.HttpResponse;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import gr.uoa.di.std08169.mobile.media.share.android.http.GetAsyncTask;
import gr.uoa.di.std08169.mobile.media.share.android.http.HttpClient;
import gr.uoa.di.std08169.mobile.media.share.android.http.PutAsyncTask;
import gr.uoa.di.std08169.mobile.media.share.android.media.Media;
import gr.uoa.di.std08169.mobile.media.share.android.user.User;
import gr.uoa.di.std08169.mobile.media.share.android.user.UserStatus;

public class EditMedia extends MobileMediaShareActivity implements TextWatcher, View.OnClickListener, GoogleMap.OnMapClickListener {
    private EditText title;
    private CheckBox isPublic;
    private TextView latlng;
    private Button ok;
    private Button reset;
    private String id;
    private Media media;
    private Marker marker;

    //TextChangedListener
    @Override
    public void beforeTextChanged(final CharSequence charSequence, final int start, final int count, final int after) {}

    //TextChangedListener
    @Override
    public void afterTextChanged(final Editable editable) {
        enableOkReset();
    }

    //OnClickListener
    @Override
    public void onClick(final View view) {
        if (isPublic.equals(view)) {
            enableOkReset();
        } else if (ok.equals(view)) {
            editMedia();
        } else if (reset.equals(view)) {
            title.setText(media.getTitle());
            isPublic.setChecked(media.isPublic());
            final GoogleMap map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
            map.clear();
            marker = map.addMarker(new MarkerOptions().
                    icon(BitmapDescriptorFactory.fromResource(R.drawable.upload_marker)).
                    anchor(Map.MARKER_ANCHOR_X, Map.MARKER_ANCHOR_Y).
                    position(new LatLng(media.getLatitude().doubleValue(), media.getLongitude().doubleValue())).
                    title(media.getTitle()));
            enableOkReset();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_media);
        final Bundle bundle = getIntent().getExtras();
        id = bundle.getString(ID);
        if (id == null)
            finish();
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
        try {
            MapsInitializer.initialize(getApplicationContext());
            final GoogleMap map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            map.setOnMapClickListener(this);
        } catch (GooglePlayServicesNotAvailableException e) {
            error(R.string.errorEditingMedia, e.getMessage());
        }
        getMedia();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == R.id.el) {
            setLocale(GREEK, EditMedia.class, id);
            return true;
        } else if (itemId == R.id.en) {
            setLocale(Locale.ENGLISH, EditMedia.class, id);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //TextChangedListener
    @Override
    public void onTextChanged(final CharSequence charSequence, final int start, final int count, final int after) {}

    //OnMapClickListener
    @Override
    public void onMapClick(final LatLng latLng) {
        latlng.setText(formatLocation(new BigDecimal(latLng.latitude), new BigDecimal(latLng.longitude)));
        marker.setPosition(latLng);
        enableOkReset();
    }

    private void getMedia() {
        try {
            final String url = String.format(getResources().getString(R.string.getMediaUrl),
                    getResources().getString(R.string.secureBaseUrl), URLEncoder.encode(id, UTF_8));
            final HttpResponse response = new GetAsyncTask(this, new URL(url)).execute().get();
            if (response == null) //An null, den exei diktuo
                error(R.string.errorEditingMedia, getResources().getString(R.string.connectionError));
            else if ((response.getStatusLine().getStatusCode() == HttpClient.HTTP_UNAUTHORIZED) && login()) { //paei gia login
                getMedia(); //kalei anadromika ton eauto ths gia na kanei to arxiko Download
            } else if (response.getStatusLine().getStatusCode() != HttpClient.HTTP_OK) //Den einai oute POST oute Unauthorized
                error(R.string.errorEditingMedia, response.getStatusLine().getReasonPhrase());
            else {
                final StringBuilder json = new StringBuilder();
                final BufferedReader input = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                try {
                    String line;
                    while ((line = input.readLine()) != null)
                        json.append(line);
                } finally {
                    input.close();
                }
                final JSONObject jsonMedia = new JSONObject(json.toString());
                final String type = jsonMedia.getString("type");
                final int size = Integer.valueOf(jsonMedia.getString("size"));
                final int duration = Integer.valueOf(jsonMedia.getString("duration"));
                final JSONObject jsonUser = new JSONObject(jsonMedia.getString("user"));
                final String email = jsonUser.getString("email");
                final UserStatus status = UserStatus.valueOf(jsonUser.getString("status"));
                final String name = jsonUser.has("name") ? jsonUser.getString("name") : null;
                final String photo = jsonUser.has("photo") ? jsonUser.getString("photo") : null;
                final User user = new User(email, status, name, photo);
                final Date created = new Date(jsonMedia.getLong("created"));
                final Date edited = new Date(jsonMedia.getLong("edited"));
                final String title = jsonMedia.getString("title");
                final BigDecimal latitude = new BigDecimal(jsonMedia.getDouble("latitude"));
                final BigDecimal longitude = new BigDecimal(jsonMedia.getDouble("longitude"));
                final boolean publik = jsonMedia.getBoolean("public");
                media = new Media(id, type, size, duration, user, created, edited, title, latitude, longitude, publik);
                this.title.setText(media.getTitle());
                this.isPublic.setChecked(media.isPublic());
                latlng.setText(formatLocation(media.getLatitude(), media.getLongitude()));
                final GoogleMap map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(media.getLatitude().doubleValue(),
                        media.getLongitude().doubleValue()), Map.GOOGLE_MAPS_ZOOM));
                marker = map.addMarker(new MarkerOptions().
                        icon(BitmapDescriptorFactory.fromResource(R.drawable.upload_marker)).
                        anchor(Map.MARKER_ANCHOR_X, Map.MARKER_ANCHOR_Y).
                        position(new LatLng(media.getLatitude().doubleValue(),
                                media.getLongitude().doubleValue())).title(media.getTitle()));
            }
        } catch (final ExecutionException e) {
            error(R.string.errorEditingMedia, e.getMessage());
        } catch (final InterruptedException e) {
            error(R.string.errorEditingMedia, e.getMessage());
        } catch (final JSONException e) {
            error(R.string.errorEditingMedia, e.getMessage());
        } catch (final IOException e) {
            error(R.string.errorEditingMedia, e.getMessage());
        }
    }

    private void enableOkReset() {
        if (marker != null) {
            final boolean modified = !(title.getText().toString().equals(media.getTitle()) &&
                    (isPublic.isChecked() == media.isPublic()) &&
                    (Math.abs(marker.getPosition().latitude - media.getLatitude().doubleValue()) < Map.MIN_DISTANCE) &&
                    (Math.abs(marker.getPosition().longitude - media.getLongitude().doubleValue()) < Map.MIN_DISTANCE));
            ok.setEnabled(modified);
            reset.setEnabled(modified);
        }
    }

    private void editMedia() {
        try {
            //Apostolh san query parameters gia na diavazontai apo to servlet ws parametroi apo to request.
            //Me tin tin entity tha eprepe na diavazotan apo to servlet san inptut stream
            final String url = String.format(getResources().getString(R.string.editMediaUrl),
                    getResources().getString(R.string.secureBaseUrl),
                    URLEncoder.encode(id, UTF_8),
                    URLEncoder.encode(title.getText().toString(), UTF_8),
                    URLEncoder.encode(Boolean.toString(isPublic.isChecked()), UTF_8),
                    URLEncoder.encode(Double.toString(marker.getPosition().latitude), UTF_8),
                    URLEncoder.encode(Double.toString(marker.getPosition().longitude), UTF_8));
            final HttpResponse response = new PutAsyncTask(this, new URL(url)).execute().get();
            if (response == null) //An null, den exei diktuo
                error(R.string.errorEditingMedia, getResources().getString(R.string.connectionError));
            else if ((response.getStatusLine().getStatusCode() == HttpClient.HTTP_UNAUTHORIZED) && login()) //paei gia login
                editMedia(); //kalei anadromika ton eauto ths gia na kanei to arxiko Download
            else if (response.getStatusLine().getStatusCode() != HttpClient.HTTP_OK) //Den einai oute PUT oute Unauthorized
                error(R.string.errorEditingMedia, response.getStatusLine().getReasonPhrase());
            else
                finish();
        } catch (final ExecutionException e) {
            error(R.string.errorEditingMedia, e.getMessage());
        } catch (final InterruptedException e) {
            error(R.string.errorEditingMedia, e.getMessage());
        } catch (final IOException e) {
            error(R.string.errorEditingMedia, e.getMessage());
        }
    }
}
