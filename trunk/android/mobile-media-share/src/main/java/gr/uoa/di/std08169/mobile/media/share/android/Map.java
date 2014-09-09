package gr.uoa.di.std08169.mobile.media.share.android;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import gr.uoa.di.std08169.mobile.media.share.android.http.DeleteAsyncTask;
import gr.uoa.di.std08169.mobile.media.share.android.http.GetAsyncTask;
import gr.uoa.di.std08169.mobile.media.share.android.http.HttpClient;
import gr.uoa.di.std08169.mobile.media.share.android.http.PostAsyncTask;
import gr.uoa.di.std08169.mobile.media.share.android.media.Media;
import gr.uoa.di.std08169.mobile.media.share.android.media.MediaType;
import gr.uoa.di.std08169.mobile.media.share.android.user.User;
import gr.uoa.di.std08169.mobile.media.share.android.user.UserStatus;


public class Map extends MobileMediaShareActivity implements AdapterView.OnItemSelectedListener,
        DatePickerDialog.OnDateSetListener, GoogleMap.OnCameraChangeListener, GoogleMap.OnMarkerClickListener,
        TextWatcher, View.OnClickListener {
    public static final float GOOGLE_MAPS_ZOOM = 8.0f;
    public static final double GOOGLE_MAPS_LATITUDE = 37.968546;	//DIT lat
    public static final double GOOGLE_MAPS_LONGITUDE = 23.766968;	//DIT lng
    private static final float MARKER_ANCHOR_X = 0.5f;
    private static final float MARKER_ANCHOR_Y = 1.0f;
    private static final double MIN_DISTANCE = 0.000001;
    private static final String DOWNLOAD_ENTITY = "email=%s&id=%s";

    //antistoixia mediatype se eikonidia gia markers
    private java.util.Map<MediaType, BitmapDescriptor> markerImages;
    //antistoixia mediatype se eikonidia gia selected markers
    private java.util.Map<MediaType, BitmapDescriptor> selectedMarkerImages;
    private EditText title;
    private EditText user;
    private EditText createdFrom;
    private EditText createdTo;
    private EditText editedFrom;
    private EditText editedTo;
    private Spinner type;
    private Spinner publik;
    private Button view;
    private Button edit;
    private Button delete;
    private Button download;
    private EditText selectedDateField;
    private Set<Media> media;
    private Media selectedMedia;

    final Context context = this;

    //EditText
    @Override
    public void afterTextChanged(final Editable editable) {
        updateMap();
    }

    //EditText
    @Override
    public void beforeTextChanged(final CharSequence charSequence, final int start, final int count, final int after) {
    }

    //onClickListener - DatePicker(createdFrom k.l.p.) & buttons
    @Override
    public void onClick(final View view) {
        if (view instanceof EditText) {
            selectedDateField = (EditText) view;
            final Calendar calendar = Calendar.getInstance();
            //this: olh h clash pou ulopoiei to DatePickerDialog.OnDateSetListener
            new DatePickerDialog(this, this, calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        } else if (view == this.view) {
            selectedDateField = null;
            final Intent activityIntent = new Intent(getApplicationContext(), ViewMedia.class);
            activityIntent.putExtra("id", selectedMedia.getId());
            activityIntent.putExtra("currentUser", currentUser.getEmail());
            activityIntent.putExtra("userStatus", currentUser.getStatus().toString());
            activityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(activityIntent);
        } else if (view == edit) {
            selectedDateField = null;
            final Intent activityIntent = new Intent(getApplicationContext(), EditMedia.class);
            activityIntent.putExtra("id", selectedMedia.getId());
            activityIntent.putExtra("currentUser", currentUser.getEmail());
            activityIntent.putExtra("userStatus", currentUser.getStatus().toString());
            activityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(activityIntent);
        } else if (view == delete) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
            alertDialogBuilder.setTitle("Are you sure you want to delete this media?");
            alertDialogBuilder.setMessage("Are you sure you want to delete media " + Map.this.selectedMedia.getTitle() + "?").setCancelable(false).
                    setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, final int id) {
                            selectedDateField = null;
                            deleteMedia();
                        }
                    }).
                    setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, final int id) {
                            // if this button is clicked, just close
                            // the dialog box and do nothing
                            dialog.cancel();
                        }
                    });
            // create alert dialog and show
            alertDialogBuilder.create().show();
        } else if (view == download) {
            selectedDateField = null;
            downloadMedia();
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.map);

        markerImages = new HashMap<MediaType, BitmapDescriptor>();
        selectedMarkerImages = new HashMap<MediaType, BitmapDescriptor>();

        //Gemisma twn hashMaps
        for(MediaType mediaType : MediaType.values()) {
            //Fortwma eikonas san bitmap (apo png) gia allagh megethous
            final Bitmap bitmap = BitmapFactory.decodeResource(getResources(), mediaType.getDrawable());
            //prosthhkh eikonas sta megala eikonidia
            selectedMarkerImages.put(mediaType, BitmapDescriptorFactory.fromBitmap(bitmap));
            //prosthhkh eikonas sta mikra eikonidia
            markerImages.put(mediaType, BitmapDescriptorFactory.fromBitmap(
                    //allagh megethous eikonas sto miso
                    Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() / 2, bitmap.getHeight() / 2, true)));
        }

        title = (EditText) findViewById(R.id.title);
        title.addTextChangedListener(this);

        user = (EditText) findViewById(R.id.user);
        user.addTextChangedListener(this);

        createdFrom = (EditText) findViewById(R.id.createdFrom);
        createdFrom.setOnClickListener(this);

        createdTo = (EditText) findViewById(R.id.createdTo);
        createdTo.setOnClickListener(this);

        editedFrom = (EditText) findViewById(R.id.editedFrom);
        editedFrom.setOnClickListener(this);

        editedTo = (EditText) findViewById(R.id.editedTo);
        editedTo.setOnClickListener(this);

        //DropDown list gia to type
        type = (Spinner) findViewById(R.id.type);
        //dinei dedomena ston spinner
        final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                //times pou tha exei to drop down list (media_type)
                R.array.media_type, android.R.layout.simple_spinner_item);//mia grammh ston spinner
        //default vertical gia to drop down spinner (pws tha fenetai)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //efarmogh tou adapter ston spinner
        type.setAdapter(adapter);
        type.setOnItemSelectedListener(this);

        //DropDown list gia to type
        publik = (Spinner) findViewById(R.id.publik);
        //dinei dedomena ston spinner
        final ArrayAdapter<CharSequence> adapterPublic = ArrayAdapter.createFromResource(this,
                //times pou tha exei to drop down list (media_type)
                R.array.media_privilege, android.R.layout.simple_spinner_item);//mia grammh ston spinner
        //default vertical gia to drop down spinner (pws tha fenetai)
        adapterPublic.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //efarmogh tou adapter ston spinner
        publik.setAdapter(adapterPublic);
        publik.setOnItemSelectedListener(this);

        view = (Button) findViewById(R.id.view);
        edit = (Button) findViewById(R.id.edit);
        delete = (Button) findViewById(R.id.delete);
        download = (Button) findViewById(R.id.download);

        view.setEnabled(false);
        edit.setEnabled(false);
        delete.setEnabled(false);
        download.setEnabled(false);

        view.setOnClickListener(this);
        edit.setOnClickListener(this);
        delete.setOnClickListener(this);
        download.setOnClickListener(this);

        selectedDateField = null;
        media = new HashSet<Media>();
        selectedMedia = null;

        try {
            MapsInitializer.initialize(getApplicationContext());
            final GoogleMap map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(GOOGLE_MAPS_LATITUDE, GOOGLE_MAPS_LONGITUDE), GOOGLE_MAPS_ZOOM));
            map.setOnMarkerClickListener(this);
            map.setOnCameraChangeListener(this);
        } catch (final GooglePlayServicesNotAvailableException e) {
            error(R.string.errorRetrievingMedia, "error loading Google Maps");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.map, menu);
        return true;
    }

    //DatePickerDialog.OnDateSetListener
    @Override
    public void onDateSet(final DatePicker view, final int year, final int monthOfYear, final int dayOfMonth) {
        if (selectedDateField != null) {
            final Calendar calendar = Calendar.getInstance();
            calendar.clear(); //default timh (gia na einai orismena mono ta pedia tou xrhsth)
            // era year month day hour minute second milli
            // AD  XXXX XX    XX  0    0      0     0
            //pedia pou dinei o xrhsths
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, monthOfYear);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            selectedDateField.setText(new SimpleDateFormat(getResources().getString(R.string.dateFormat)).
                    format(calendar.getTime()));
        }
    }

    //Spinner
    @Override
    public void onItemSelected(final AdapterView<?> adapterView, final View view, final int position, final long id) {
        updateMap();
    }

    @Override
    public void onCameraChange(final CameraPosition cameraPosition) {
        updateMap();
    }


    //OnMarkerClickListener
    @Override
    public boolean onMarkerClick(final Marker marker) {
        final GoogleMap map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        //svhsimo olwn twn markers apo to xarth kai apo to hashmap
        map.clear();
        selectedMedia = null;
        view.setEnabled(false);
        download.setEnabled(false);
        edit.setEnabled(false);
        delete.setEnabled(false);
        //prospelash olwn twn media kai xana dhmiourgia twn markers stin idia katastash me ton epilegmeno me megaluterh eikona
        for (Media medium : media) {
            //an to trexon media antistoixei ston epilegmeno marker
            final boolean selected = (Math.abs(marker.getPosition().latitude - medium.getLatitude()) < MIN_DISTANCE) &&
                    (Math.abs(marker.getPosition().longitude - medium.getLongitude()) < MIN_DISTANCE);
            map.addMarker(new MarkerOptions().
                    //eikonidio tou marker analoga to type
                    //an einai epilegmenos o marker, vazei megalh eikona, alliws mikrh
                    icon((selected ? selectedMarkerImages : markerImages).get(MediaType.getMediaType(medium.getType()))).
                    //topothethsh eikonas akrivws panw apo to shmeio pou einai ston xarth,
                    //to "velaki" tis eikonas einai stin mesh tou katw merous
                    anchor(MARKER_ANCHOR_X, MARKER_ANCHOR_Y).
                    //thesh tou marker ston xarth
                    position(new LatLng(medium.getLatitude(), medium.getLongitude())).
                    //titlos marker
                    title(medium.getTitle()));
            if (selected) {
                selectedMedia = medium;
                view.setEnabled(true);
                download.setEnabled(true);
                if (currentUser.getEmail().equals(medium.getUser().getEmail()) || (currentUser.getStatus() == UserStatus.ADMIN)) {
                    edit.setEnabled(true);
                    delete.setEnabled(true);
                }
            }
        }
        return false;
    }

    //Spinner
    @Override
    public void onNothingSelected(final AdapterView<?> adapterView) {
        updateMap();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return ((item.getItemId() == R.id.settings) || super.onOptionsItemSelected(item));
    }

    //EditText
    @Override
    public void onTextChanged(final CharSequence charSequence, final int start, final int count, final int after) {}

    private void updateMap() {
        final String title = (this.title.getText().toString().trim().length() == 0) ? null : this.title.getText().toString().trim();
        final String user = (this.user.getText().toString().trim().length() == 0) ? null : this.user.getText().toString().trim();
        final DateFormat dateFormat = new SimpleDateFormat(getResources().getString(R.string.dateFormat));
        Date createdFrom;
        try {
            createdFrom = dateFormat.parse(this.createdFrom.getText().toString().trim());
        } catch (final ParseException e) {
            createdFrom = null;
        }
        Date createdTo;
        try {
            createdTo = dateFormat.parse(this.createdTo.getText().toString().trim());
        } catch (final ParseException e) {
            createdTo = null;
        }
        Date editedFrom;
        try {
            editedFrom = dateFormat.parse(this.editedFrom.getText().toString().trim());
        } catch (final ParseException e) {
            editedFrom = null;
        }
        Date editedTo;
        try {
            editedTo = dateFormat.parse(this.editedTo.getText().toString().trim());
        } catch (final ParseException e) {
            editedTo = null;
        }
        Integer type = null;
        switch (this.type.getSelectedItemPosition()) {
            case AdapterView.INVALID_POSITION:
            case 0: //epilegmeno to any type
                break;
            default:
                type = this.type.getSelectedItemPosition() - 1;
        }
        Boolean publik = null;
        switch (this.publik.getSelectedItemPosition()) {
            case AdapterView.INVALID_POSITION:
            case 1:
                publik = true;
                break;
            case 2:
                publik = false;
        }
        final GoogleMap map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

        //Fernei mono pragmata pou einai entos ths oraths perioxhs tou xarth (getVisibleRegion())
        //notio-dutika, katw aristera
        final double minLatitude = map.getProjection().getVisibleRegion().latLngBounds.southwest.latitude;
        final double minLongitude = map.getProjection().getVisibleRegion().latLngBounds.southwest.longitude;
        //voreio-anatolika, panw dexia
        final double maxLatitude = map.getProjection().getVisibleRegion().latLngBounds.northeast.latitude;
        final double maxLongitude = map.getProjection().getVisibleRegion().latLngBounds.northeast.longitude;
        final StringBuilder url = new StringBuilder(String.format(getResources().getString(R.string.getListUrl),
                getResources().getString(R.string.baseUrl)));
        if (title != null)
            url.append("&title=").append(title);
        if (type != null)
            url.append("&type=").append(type);
        if (user != null)
            url.append("&user=").append(user);
        if (createdFrom != null)
            url.append("&createdFrom=").append(createdFrom.getTime());
        if (createdTo != null)
            url.append("&createdTo=").append(createdTo.getTime());
        if (editedFrom != null)
            url.append("&editedFrom=").append(editedFrom.getTime());
        if (editedTo != null)
            url.append("&editedTo=").append(editedTo.getTime());
        if (publik != null)
            url.append("&publik=").append(publik);
        url.append("&minLatitude=").append(minLatitude);
        url.append("&minLongitude=").append(minLongitude);
        url.append("&maxLatitude=").append(maxLatitude);
        url.append("&maxLongitude=").append(maxLongitude);
        try {
            final HttpResponse response = new GetAsyncTask(this, new URL(url.toString())).execute().get();
            if (response == null) {
                error(R.string.errorRetrievingMedia, getResources().getString(R.string.connectionError));
                return;
            }
            if (response.getStatusLine().getStatusCode() != HttpClient.HTTP_OK) {
                error(R.string.errorRetrievingMedia, response.getStatusLine().getReasonPhrase());
                return;
            }
            final StringBuilder json = new StringBuilder();
            final BufferedReader input = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            try {
                String line;
                while ((line = input.readLine()) != null)
                    json.append(line);
            } finally {
                input.close();
            }
            //[ ... ] -> json Array
            final JSONArray list = new JSONArray(json.toString());
            media.clear();
            map.clear();
            for (int i = 0; i < list.length(); i++) {
                // { ... } -> json object
                final String id = list.getJSONObject(i).getString("id");
                final String mediaTitle = list.getJSONObject(i).getString("title");
                final String mediaType = list.getJSONObject(i).getString("type");
                //{"status":"NORMAL","photo":"0f80f606-72b7-4618-8688-2b947a989c47","email":"haralambos9094@gmail.com","name":"labis"}
                String userString = list.getJSONObject(i).getString("user");
                JSONObject userJsonObject = new JSONObject(userString);
                final String email = userJsonObject.getString("email");
                final UserStatus status = UserStatus.valueOf(userJsonObject.getString("status"));
                final String name = (userString.contains("\"name\"")) ? userJsonObject.getString("name") : null;
                final String photo = (userString.contains("\"photo\"")) ? userJsonObject.getString("photo") : null;
                final User mediaUser = new User(email, status, name, photo);
                final double latitude = list.getJSONObject(i).getDouble("latitude");
                final double longitude = list.getJSONObject(i).getDouble("longitude");
                final Media media = new Media(id, mediaTitle, mediaType, mediaUser, latitude, longitude);
                Map.this.media.add(media);
                map.addMarker(new MarkerOptions().
                        //eikonidio tou marker analoga to type
                        icon(markerImages.get(MediaType.getMediaType(media.getType()))).
                        //topothethsh eikonas akrivws panw apo to shmeio pou einai ston xarth,
                        //to "velaki" tis eikonas einai stin mesh tou katw merous
                        anchor(MARKER_ANCHOR_X, MARKER_ANCHOR_Y).
                        //thesh tou marker ston xarth
                        position(new LatLng(media.getLatitude(), media.getLongitude())).
                        //titlos marker
                        title(media.getTitle()));
            }
            selectedMedia = null;
            download.setEnabled(false);
            edit.setEnabled(false);
            delete.setEnabled(false);
        } catch (final ExecutionException e) {
            error(R.string.errorRetrievingMedia, e.getMessage());
        } catch (final InterruptedException e) {
            error(R.string.errorRetrievingMedia, e.getMessage());
        } catch (final IOException e) {
            error(R.string.errorRetrievingMedia, e.getMessage());
        } catch (final JSONException e) {
            error(R.string.errorRetrievingMedia, e.getMessage());
        }
    }

    private void deleteMedia() {
        try {
            final String url = String.format(getResources().getString(R.string.deleteMediaUrl),
                    getResources().getString(R.string.baseUrl),
                    URLEncoder.encode(Map.this.selectedMedia.getId(), UTF_8));
            final HttpResponse response = new DeleteAsyncTask(Map.this, new URL(url)).execute().get();
            if (response == null) //An null, den exei diktuo
                error(R.string.errorDeletingMedia, getResources().getString(R.string.connectionError));
            else if ((response.getStatusLine().getStatusCode() == HttpClient.HTTP_UNAUTHORIZED) && login()) { //paei gia login
                deleteMedia(); //kalei anadromika ton eauto ths gia na kanei to arxiko Delete
                return;
            } else if (response.getStatusLine().getStatusCode() != HttpClient.HTTP_OK) //Den einai oute GET oute Unauthorized
                error(R.string.errorDeletingMedia, response.getStatusLine().getReasonPhrase());
            else
                updateMap();
        } catch (final IOException e) {
            error(R.string.errorDeletingMedia, e.getMessage());
        } catch (final InterruptedException e) {
            error(R.string.errorDeletingMedia, e.getMessage());
        } catch (final ExecutionException e) {
            error(R.string.errorDeletingMedia, e.getMessage());
        }
    }

    private void downloadMedia() {
        try {
            final String requestDownloadUrl = String.format(getString(R.string.requestDownloadUrl), getString(R.string.baseUrl));
            final HttpEntity entity = new StringEntity(String.format(DOWNLOAD_ENTITY, currentUser.getEmail(), selectedMedia.getId()));
                                                            /*H get epistrefei to apotelesma tou AsyncTask*/
            final HttpResponse response = new PostAsyncTask(getApplicationContext(), new URL(requestDownloadUrl),
                    entity, APPLICATION_FORM_URL_ENCODED).execute().get();

            if (response == null) //An null, den exei diktuo
                error(R.string.errorDownloadingMedia, getResources().getString(R.string.connectionError));
            else if ((response.getStatusLine().getStatusCode() == HttpClient.HTTP_UNAUTHORIZED) && login()) { //paei gia login
                downloadMedia(); //kalei anadromika ton eauto ths gia na kanei to arxiko Download
                return;
            } else if (response.getStatusLine().getStatusCode() != HttpClient.HTTP_OK) //Den einai oute POST oute Unauthorized
                error(R.string.errorDownloadingMedia, response.getStatusLine().getReasonPhrase());
            else {
                //Apantaei m' ena token
                final StringBuilder downloadToken = new StringBuilder();
                final BufferedReader input = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                try {
                    String line;
                    while ((line = input.readLine()) != null)
                        downloadToken.append(line);
                } finally {
                    input.close();
                }

                final String downloadUrl = String.format(getResources().getString(R.string.downloadUrl),
                        getResources().getString(R.string.baseUrl),
                        URLEncoder.encode(downloadToken.toString(), UTF_8));
                final DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl));
                //apothhkeuetai sta downloads me onoma title
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, selectedMedia.getTitle());
                //ti typou arxeio einai
                request.setMimeType(selectedMedia.getType());
                //to download tha einai orato stin lista twn downloads kai tha eidopoihthei o xrhsths molis katevei
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setTitle(selectedMedia.getTitle());
                //gia na fainetai sta downloads
                request.setVisibleInDownloadsUi(true);

                final MediaType mediaType = MediaType.getMediaType(selectedMedia.getType());

                if ((mediaType == MediaType.AUDIO) || (mediaType == MediaType.IMAGE) || (mediaType == MediaType.VIDEO))
                    //gia na mathei o media scanner oti uparxei to arxeio kai na to provalei argotera
                    request.allowScanningByMediaScanner();

                final DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

                //Xekinaei to download vazontas to sthn oura twn downloads (enqueue)
                final long id = downloadManager.enqueue(request);
                final DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(id);
            }
        } catch (final IOException e){
            error(R.string.errorDownloadingMedia, e.getMessage());
        } catch (final InterruptedException e){
            error(R.string.errorDownloadingMedia, e.getMessage());
        } catch (final ExecutionException e){
            error(R.string.errorDownloadingMedia, e.getMessage());
        }
    }
}
