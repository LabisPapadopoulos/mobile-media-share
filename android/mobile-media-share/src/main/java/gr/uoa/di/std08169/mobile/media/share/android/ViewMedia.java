package gr.uoa.di.std08169.mobile.media.share.android;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import gr.uoa.di.std08169.mobile.media.share.android.http.DeleteAsyncTask;
import gr.uoa.di.std08169.mobile.media.share.android.http.GetAsyncTask;
import gr.uoa.di.std08169.mobile.media.share.android.http.HttpClient;
import gr.uoa.di.std08169.mobile.media.share.android.media.Media;
import gr.uoa.di.std08169.mobile.media.share.android.media.MediaType;
import gr.uoa.di.std08169.mobile.media.share.android.user.User;
import gr.uoa.di.std08169.mobile.media.share.android.user.UserStatus;


public class ViewMedia extends MobileMediaShareActivity implements View.OnClickListener {
    private ImageView mediaImage;
    private Button download;
    private Button edit;
    private Button delete;
    private String id;
    private Media media;
    private TextView title;
    private TextView type;
    private TextView size;
    private TextView duration;
    private TextView user;
    private TextView created;
    private TextView edited;
    private TextView isPublic;
    private TextView latlng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_media);
        id = (getIntent().getExtras() == null) ? null : getIntent().getExtras().getString(ID);
        if (id == null) {
            error(R.string.errorRetrievingMedia, R.string.notFound);
            return;
        }
        mediaImage = (ImageView) findViewById(R.id.mediaImage);
        mediaImage.setOnClickListener(this);
        download = (Button) findViewById(R.id.download);
        download.setOnClickListener(this);
        edit = (Button) findViewById(R.id.edit);
        edit.setEnabled(false);
        edit.setOnClickListener(this);
        delete = (Button) findViewById(R.id.delete);
        delete.setEnabled(false);
        delete.setOnClickListener(this);

        title = (TextView) findViewById(R.id.title);
        type = (TextView) findViewById(R.id.type);
        size = (TextView) findViewById(R.id.size);
        duration = (TextView) findViewById(R.id.duration);
        user = (TextView) findViewById(R.id.user);
        created = (TextView) findViewById(R.id.created);
        edited = (TextView) findViewById(R.id.edited);
        isPublic = (TextView) findViewById(R.id.publik);
        latlng = (TextView) findViewById(R.id.latlng);

        getMedia();

        try {
            MapsInitializer.initialize(getApplicationContext());
            final GoogleMap map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(getLatLng(), GOOGLE_MAPS_ZOOM));
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == R.id.el) {            //gia to id tou media
            setLocale(GREEK, ViewMedia.class, id);
            return true;
        } else if (itemId == R.id.en) {
            setLocale(Locale.ENGLISH, ViewMedia.class, id);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        if (view == download) {
            downloadMedia(media);
        } else if (view == edit) {
            final Intent activityIntent = new Intent(getApplicationContext(), EditMedia.class);
            activityIntent.putExtra("id", media.getId());
            activityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(activityIntent);
        } else if (view == delete) {
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle(getResources().getString(R.string.areYouSureYouWantToDeleteThisMedia));
            alertDialogBuilder.setMessage(String.format(getResources().getString(R.string.areYouSureYouWantToDeleteMedia_), media.getTitle()));
            alertDialogBuilder.setCancelable(false);
            alertDialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(final DialogInterface dialog, final int id) {
                    deleteMedia();
                }
            });
            alertDialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(final DialogInterface dialog, final int id) {
                    dialog.cancel();
                }
            });
            alertDialogBuilder.create().show();
        } else if (view == mediaImage) { //TODO
//Android media player
//            final StringBuilder uri = new StringBuilder(String.format(getResources().getString(R.string.getMediaUrl),
//                    getResources().getString(R.string.secureBaseUrl)));
//            uri.append("?action=").append("downloadMedia");
//            uri.append("&id=").append(id);
//            final String url = uri.toString();
//
//            MediaPlayer mPlayer = new MediaPlayer();
//            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//            try {
//                mPlayer.setDataSource(url);
//                mPlayer.prepare();
//                mPlayer.start();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//----------------------------------------------------
//--> Default android media player
//            String extension = MimeTypeMap.getFileExtensionFromUrl(url);
//            String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
//            Intent mediaIntent = new Intent(Intent.ACTION_VIEW);
//            mediaIntent.setDataAndType(Uri.parse(url), mimeType);
//            startActivity(mediaIntent);
        }
    }

    private void getMedia() {
        try {
            final String url = String.format(getResources().getString(R.string.getMediaUrl),
                    getResources().getString(R.string.secureBaseUrl), URLEncoder.encode(id, UTF_8));
            final HttpResponse response = new GetAsyncTask(this, new URL(url)).execute().get();
            if (response == null) //An null, den exei diktuo
                error(R.string.errorRetrievingMedia, getResources().getString(R.string.connectionError));
            else if ((response.getStatusLine().getStatusCode() == HttpClient.HTTP_UNAUTHORIZED) && login()) { //paei gia login
                getMedia(); //kalei anadromika ton eauto ths gia na kanei to arxiko Download
            } else if (response.getStatusLine().getStatusCode() != HttpClient.HTTP_OK) //Den einai oute POST oute Unauthorized
                error(R.string.errorRetrievingMedia, response.getStatusLine().getReasonPhrase());
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
                final DateFormat dateFormat = new SimpleDateFormat(getResources().getString(R.string.dateFormat));
                final boolean enabled = media.getUser().equals(currentUser) || (currentUser.getStatus() == UserStatus.ADMIN);
                edit.setEnabled(enabled);
                delete.setEnabled(enabled);
                this.mediaImage.setImageBitmap(BitmapFactory.decodeResource(getResources(), MediaType.getMediaType(media.getType()).getLargeDrawable()));
                this.title.setText(media.getTitle());
                this.type.setText(getResources().getStringArray(R.array.media_type)[MediaType.getMediaType(media.getType()).ordinal() + 1]);
                this.size.setText(formatSize(media.getSize()));
                this.duration.setText(formatDuration(media.getDuration()));
                this.user.setText((media.getUser().getName() == null) ? formatEmail(media.getUser().getEmail()) : media.getUser().getName());
                this.created.setText(dateFormat.format(media.getCreated()));
                this.edited.setText(dateFormat.format(media.getEdited()));
                this.isPublic.setText(getResources().getString(media.isPublic() ? R.string.publik : R.string.private_));
                latlng.setText(formatLocation(media.getLatitude(), media.getLongitude()));
                final GoogleMap map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(media.getLatitude().doubleValue(),
                        media.getLongitude().doubleValue()), Map.GOOGLE_MAPS_ZOOM));
                map.addMarker(new MarkerOptions().
                        icon(BitmapDescriptorFactory.fromResource(R.drawable.upload_marker)).
                        anchor(Map.MARKER_ANCHOR_X, Map.MARKER_ANCHOR_Y).
                        position(new LatLng(media.getLatitude().doubleValue(),
                                media.getLongitude().doubleValue())).title(media.getTitle()));
            }
        } catch (final ExecutionException e) {
            error(R.string.errorRetrievingMedia, e.getMessage());
        } catch (final InterruptedException e) {
            error(R.string.errorRetrievingMedia, e.getMessage());
        } catch (final JSONException e) {
            error(R.string.errorRetrievingMedia, e.getMessage());
        } catch (final IOException e) {
            error(R.string.errorRetrievingMedia, e.getMessage());
        }
    }

    private void deleteMedia() {
        try {
            final String url = String.format(getResources().getString(R.string.deleteMediaUrl),
                    getResources().getString(R.string.secureBaseUrl),
                    URLEncoder.encode(media.getId(), UTF_8));
            final HttpResponse response = new DeleteAsyncTask(this, new URL(url)).execute().get();
            if (response == null) //An null, den exei diktuo
                error(R.string.errorDeletingMedia, getResources().getString(R.string.connectionError));
            else if ((response.getStatusLine().getStatusCode() == HttpClient.HTTP_UNAUTHORIZED) && login()) //paei gia login
                deleteMedia(); //kalei anadromika ton eauto ths gia na kanei to arxiko Delete
            else if (response.getStatusLine().getStatusCode() != HttpClient.HTTP_OK) //Den einai oute GET oute Unauthorized
                error(R.string.errorDeletingMedia, response.getStatusLine().getReasonPhrase());
            else
                finish();
        } catch (final IOException e) {
            error(R.string.errorDeletingMedia, e.getMessage());
        } catch (final InterruptedException e) {
            error(R.string.errorDeletingMedia, e.getMessage());
        } catch (final ExecutionException e) {
            error(R.string.errorDeletingMedia, e.getMessage());
        }
    }
}
