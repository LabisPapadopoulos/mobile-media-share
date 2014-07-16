package gr.uoa.di.std08169.mobile.media.share.android;

import android.app.DatePickerDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.CamcorderProfile;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import gr.uoa.di.std08169.mobile.media.share.android.https.HttpsAsyncTask;
import gr.uoa.di.std08169.mobile.media.share.android.https.HttpsResponse;


public class Map extends MobileMediaShareActivity implements AdapterView.OnItemSelectedListener,
        DatePickerDialog.OnDateSetListener, GoogleMap.OnMarkerClickListener, TextWatcher, View.OnClickListener {
    public static final float GOOGLE_MAPS_ZOOM = 8.0f;
    public static final double GOOGLE_MAPS_LATITUDE = 37.968546;	//DIT lat
    public static final double GOOGLE_MAPS_LONGITUDE = 23.766968;	//DIT lng
    private static final float MARKER_ANCHOR_X = 0.5f;
    private static final float MARKER_ANCHOR_Y = 1.0f;
    private static final double MIN_DISTANCE = 0.000001;

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
Toast.makeText(this, "View: " + selectedMedia.getId(), Toast.LENGTH_LONG).show();
        } else if (view == edit) {
            selectedDateField = null;
Toast.makeText(this, "Edit: " + selectedMedia.getId(), Toast.LENGTH_LONG).show();
        } else if (view == delete) {
            selectedDateField = null;
Toast.makeText(this, "Delete: " + selectedMedia.getId(), Toast.LENGTH_LONG).show();
        } else if (view == download) {
            selectedDateField = null;
Toast.makeText(this, "Download: " + selectedMedia.getId(), Toast.LENGTH_LONG).show();
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
            requestUser();
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
                final User currentUser = getCurrentUser();
                if (currentUser.equals(medium.getUser()) || (currentUser.getStatus() == UserStatus.ADMIN)) { //TODO
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
        return ((item.getItemId() == R.id.action_settings) || super.onOptionsItemSelected(item));
    }

    //EditText
    @Override
    public void onTextChanged(final CharSequence charSequence, final int start, final int count, final int after) {
    }


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


        final StringBuilder url = new StringBuilder(getResources().getString(R.string.getListUrl));
        if (title != null)
            url.append("&title=").append(title);
        if (type != null)
            url.append("&type=").append(type);
        if (user != null)
            url.append("&user=").append(user);
        if (createdFrom != null)
            url.append("&createdFrom=").append(createdFrom);
        if (createdTo != null)
            url.append("&createdTo=").append(createdTo);
        if (editedFrom != null)
            url.append("&editedFrom=").append(editedFrom);
        if (editedTo != null)
            url.append("&editedTo=").append(editedTo);
        if (publik != null)
            url.append("&publik=").append(publik);
        url.append("&minLatitude=").append(minLatitude);
        url.append("&minLongitude=").append(minLongitude);
        url.append("&maxLatitude=").append(maxLatitude);
        url.append("&maxLongitude=").append(maxLongitude);
        try {
            new HttpsAsyncTask(getApplicationContext()) {
                @Override
                protected void onPostExecute(HttpsResponse response) {
                    if (!response.isSuccess()) {
                        error(R.string.errorRetrievingMedia, response.getResponse());
                        return;
                    }
                    try {
                        //[ ... ] -> json Array
                        final JSONArray list = new JSONArray(response.getResponse());
                        media.clear();
                        for (int i = 0; i < list.length(); i++) {
                            // { ... } -> json object
                            final Media media = new Media(list.getJSONObject(i).getString("id"), list.getJSONObject(i).getString("title"),
                                    list.getJSONObject(i).getString("type"), list.getJSONObject(i).getString("user"),
                                    list.getJSONObject(i).getDouble("latitude"), list.getJSONObject(i).getDouble("longitude"));
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
                    } catch (final JSONException e) {
                        error(R.string.errorRetrievingMedia, e.getMessage());
                    }
                }
            }.execute(new URL(url.toString()));
        } catch (final IOException e) {
            error(R.string.errorRetrievingMedia, e.getMessage());
        }
    }
}
