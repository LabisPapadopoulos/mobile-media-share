package gr.uoa.di.std08169.mobile.media.share.android;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import java.math.BigDecimal;
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

import org.apache.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import gr.uoa.di.std08169.mobile.media.share.android.http.DeleteAsyncTask;
import gr.uoa.di.std08169.mobile.media.share.android.http.GetAsyncTask;
import gr.uoa.di.std08169.mobile.media.share.android.http.HttpClient;
import gr.uoa.di.std08169.mobile.media.share.android.media.Media;
import gr.uoa.di.std08169.mobile.media.share.android.media.MediaType;
import gr.uoa.di.std08169.mobile.media.share.android.user.User;
import gr.uoa.di.std08169.mobile.media.share.android.user.UserStatus;


public class Map extends MobileMediaShareActivity implements AdapterView.OnItemSelectedListener,
        DatePickerDialog.OnDateSetListener, GoogleMap.OnCameraChangeListener, GoogleMap.OnMarkerClickListener,
        TextWatcher, View.OnClickListener {

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
            activityIntent.putExtra(ID, selectedMedia.getId());
            activityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(activityIntent);
        } else if (view == edit) {
            selectedDateField = null;
            final Intent activityIntent = new Intent(getApplicationContext(), EditMedia.class);
            activityIntent.putExtra(ID, selectedMedia.getId());
            activityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(activityIntent);
        } else if (view == delete) {
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle(getResources().getString(R.string.areYouSureYouWantToDeleteThisMedia));
            alertDialogBuilder.setMessage(String.format(getResources().getString(R.string.areYouSureYouWantToDeleteMedia_), selectedMedia.getTitle()));
            alertDialogBuilder.setCancelable(false);
            alertDialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, final int id) {
                            selectedDateField = null;
                            deleteMedia();
                        }
                    });
            alertDialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, final int id) {
                            dialog.cancel();
                        }
                    });
            alertDialogBuilder.create().show();
        } else if (view == download) {
            selectedDateField = null;
            downloadMedia(selectedMedia);
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
            final LatLng latLng = getLatLng();
            final GoogleMap map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, GOOGLE_MAPS_ZOOM));
            map.setOnMarkerClickListener(this);
            map.setOnCameraChangeListener(this);
        } catch (final GooglePlayServicesNotAvailableException e) {
            error(R.string.errorRetrievingMedia, e.getMessage());
        }
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
        for (final Media medium : media) {
            //an to trexon media antistoixei ston epilegmeno marker
            final boolean selected = (Math.abs(marker.getPosition().latitude - medium.getLatitude().doubleValue()) < MIN_DISTANCE) &&
                    (Math.abs(marker.getPosition().longitude - medium.getLongitude().doubleValue()) < MIN_DISTANCE);
            map.addMarker(new MarkerOptions().
                    //eikonidio tou marker analoga to type
                    //an einai epilegmenos o marker, vazei megalh eikona, alliws mikrh
                    icon((selected ? selectedMarkerImages : markerImages).get(MediaType.getMediaType(medium.getType()))).
                    //topothethsh eikonas akrivws panw apo to shmeio pou einai ston xarth,
                    //to "velaki" tis eikonas einai stin mesh tou katw merous
                    anchor(MARKER_ANCHOR_X, MARKER_ANCHOR_Y).
                    //thesh tou marker ston xarth
                    position(new LatLng(medium.getLatitude().doubleValue(), medium.getLongitude().doubleValue())).
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
                getResources().getString(R.string.secureBaseUrl)));
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
            url.append("&public=").append(publik);
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
                final String jsonType = list.getJSONObject(i).getString("type");
                final long size = list.getJSONObject(i).getLong("size");
                final int duration = list.getJSONObject(i).getInt("duration");
                final JSONObject jsonUser = list.getJSONObject(i).getJSONObject("user");
                final String email = jsonUser.getString("email");
                final UserStatus status = UserStatus.valueOf(jsonUser.getString("status"));
                final String name = jsonUser.has("name") ? jsonUser.getString("name") : null;
                final String photo = jsonUser.has("photo") ? jsonUser.getString("photo") : null;
                final User mediaUser = new User(email, status, name, photo);
                final Date created = new Date(list.getJSONObject(i).getLong("created"));
                final Date edited = new Date(list.getJSONObject(i).getLong("edited"));
                final String jsonTitle = list.getJSONObject(i).getString("title");
                final BigDecimal latitude = new BigDecimal(list.getJSONObject(i).getDouble("latitude"));
                final BigDecimal longitude = new BigDecimal(list.getJSONObject(i).getDouble("longitude"));
                final Boolean jsonPublic = list.getJSONObject(i).getBoolean("public");
                final Media media = new Media(id, jsonType, size, duration, mediaUser, created, edited,
                        jsonTitle, latitude, longitude, jsonPublic);
                this.media.add(media);
                map.addMarker(new MarkerOptions().
                        //eikonidio tou marker analoga to type
                        icon(markerImages.get(MediaType.getMediaType(media.getType()))).
                        //topothethsh eikonas akrivws panw apo to shmeio pou einai ston xarth,
                        //to "velaki" tis eikonas einai stin mesh tou katw merous
                        anchor(MARKER_ANCHOR_X, MARKER_ANCHOR_Y).
                        //thesh tou marker ston xarth
                        position(new LatLng(media.getLatitude().doubleValue(), media.getLongitude().doubleValue())).
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
                    getResources().getString(R.string.secureBaseUrl),
                    URLEncoder.encode(selectedMedia.getId(), UTF_8));
            final HttpResponse response = new DeleteAsyncTask(this, new URL(url)).execute().get();
            if (response == null) //An null, den exei diktuo
                error(R.string.errorDeletingMedia, getResources().getString(R.string.connectionError));
            else if ((response.getStatusLine().getStatusCode() == HttpClient.HTTP_UNAUTHORIZED) && login()) //paei gia login
                deleteMedia(); //kalei anadromika ton eauto ths gia na kanei to arxiko Delete
            else if (response.getStatusLine().getStatusCode() != HttpClient.HTTP_OK) //Den einai oute GET oute Unauthorized
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
}
