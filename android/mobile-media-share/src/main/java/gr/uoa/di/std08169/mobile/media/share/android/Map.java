package gr.uoa.di.std08169.mobile.media.share.android;

import android.app.DatePickerDialog;
import android.support.v7.app.ActionBarActivity;
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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import gr.uoa.di.std08169.mobile.media.share.android.https.HttpsAsyncTask;
import gr.uoa.di.std08169.mobile.media.share.android.https.HttpsResponse;


public class Map extends MobileMediaShareActivity implements AdapterView.OnItemSelectedListener, DatePickerDialog.OnDateSetListener, TextWatcher, View.OnClickListener {
    public static final float GOOGLE_MAPS_ZOOM = 8.0f;
    public static final double GOOGLE_MAPS_LATITUDE = 37.968546;	//DIT lat
    public static final double GOOGLE_MAPS_LONGITUDE = 23.766968;	//DIT lng

    private EditText title;
    private EditText user;
    private EditText createdFrom;
    private EditText createdTo;
    private EditText editedFrom;
    private EditText editedTo;
    private Spinner type;
    private Spinner publik;
    private Button download;
    private Button edit;
    private Button delete;
    private MapFragment map;
    private EditText selectedDateField;

    //EditText
    @Override
    public void afterTextChanged(final Editable editable) {
        updateMap();
    }

    //EditText
    @Override
    public void beforeTextChanged(final CharSequence charSequence, final int start, final int count, final int after) {
    }

    //createdFrom (DatePicker) - ClickListener
    @Override
    public void onClick(final View view) {
        Log.d("DEBUG", "view: " + view);
        if (view instanceof EditText)
            selectedDateField = (EditText) view;
        final Calendar calendar = Calendar.getInstance();
        //this: olh h clash pou ulopoiei to DatePickerDialog.OnDateSetListener
        new DatePickerDialog(this, this, calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);

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

        download = (Button) findViewById(R.id.download);
        edit = (Button) findViewById(R.id.edit);
        delete = (Button) findViewById(R.id.edit);

        selectedDateField = null;

        final GoogleMap map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(GOOGLE_MAPS_LATITUDE, GOOGLE_MAPS_LONGITUDE), GOOGLE_MAPS_ZOOM));
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

    //Spinner
    @Override
    public void onNothingSelected(final AdapterView<?> adapterView) {
        updateMap();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //EditText
    @Override
    public void onTextChanged(final CharSequence charSequence, final int start, final int count, final int after) {
    }


    private void updateMap() {
        final String title = (this.title.getText().toString().trim().length() == 0) ? null : this.title.getText().toString().trim();
        final String user = (this.user.getText().toString().trim().length() == 0) ? null : this.user.getText().toString().trim();
        final DateFormat dateFormat = new SimpleDateFormat(getResources().getString(R.string.dateFormat));
        Date createdFrom = null;
        try {
            createdFrom = dateFormat.parse(this.createdFrom.getText().toString().trim());
        } catch (final ParseException e) {
            createdFrom = null;
        }
        Date createdTo = null;
        try {
            createdTo = dateFormat.parse(this.createdTo.getText().toString().trim());
        } catch (final ParseException e) {
            createdTo = null;
        }
        Date editedFrom = null;
        try {
            editedFrom = dateFormat.parse(this.editedFrom.getText().toString().trim());
        } catch (final ParseException e) {
            editedFrom = null;
        }
        Date editedTo = null;
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
                        for (int i = 0; i < list.length(); i++) {
                            // { ... } -> json object
                            final JSONObject media = list.getJSONObject(i);
                            final String id = media.getString("id");
                            final double latitude = media.getDouble("latitude");
                            final double longitude = media.getDouble("longitude");
                            final String title1 = media.getString("title");
                            final String type1 = media.getString("type");
                            Toast.makeText(Map.this, "id: " + id + ", title: " + title1 + ", type: " + type1 +
                                    ", latitude: " + latitude + ", longitude: " + longitude, Toast.LENGTH_SHORT).show();
                            map.addMarker(new MarkerOptions()
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.map)) //TODO
                                    .anchor(0.5f, 1.0f) // Anchors the marker on the bottom center
                                    .position(new LatLng(latitude, longitude)));
                        }
                    } catch (final JSONException e) {
                        error(R.string.errorRetrievingMedia, e.getMessage());
                        return;
                    }
                }
            }.execute(new URL(url.toString()));
        } catch (final IOException e) {
            error(R.string.errorRetrievingMedia, e.getMessage());
            return;
        }
    }
}
























//        connection.setReq

        // kaloume service me bash tis times

//        final BigDecimal minLatitude = new BigDecimal(googleMap.getBounds().getSouthWest().lat());
//        final BigDecimal minLongitude = new BigDecimal(googleMap.getBounds().getSouthWest().lng());
//        //epistrefei ta voreio-anatolika (suntetagmenes tetragwnou pou kaluptei o xarths)
//        final BigDecimal maxLatitude = new BigDecimal(googleMap.getBounds().getNorthEast().lat());
//        final BigDecimal maxLongitude = new BigDecimal(googleMap.getBounds().getNorthEast().lng());
//        MEDIA_SERVICE.getMedia(currentUser, title, type, (selectedUser == null) ? null : selectedUser.getEmail(),
//                createdFrom, createdTo, editedFrom, editedTo, publik, minLatitude, minLongitude, maxLatitude, maxLongitude,
//                new AsyncCallback<List<Media>>() {
//                    @Override
//                    public void onFailure(final Throwable throwable) {//se front-end ston browser
//                        for (java.util.Map.Entry<Marker, Media> marker : markers.entrySet())
//                            //svinei ton marker apo ton xarth
//                            marker.getKey().setMap((GoogleMap) null);
//                        markers.clear(); //adeiasma listas apo markers
//                        selectedMarker = null;
//                        download.setEnabled(false);
//                        edit.setEnabled(false);
//                        delete.setEnabled(false);
//                    }
//
//                    @Override
//                    public void onSuccess(final List<Media> result) {
//                        for (java.util.Map.Entry<Marker, Media> marker : markers.entrySet())
//                            //svinei ton marker apo ton xarth
//                            marker.getKey().setMap((GoogleMap) null);
//                        markers.clear(); //adeiasma tou map apo markers
//                        //Ruthmiseis gia ta shmeia ston xarth
//                        final MarkerOptions options = MarkerOptions.create();
//                        options.setMap(googleMap);
//                        options.setClickable(true);
//                        //Shmeia ston xarth gia kathe media
//                        for (Media media : result) {
//                            final Marker marker = Marker.create(options);
//                            //doubleValue: to gurnaei se double apo bigDecimal
//                            marker.setPosition(LatLng.create(media.getLatitude().doubleValue(), media.getLongitude().doubleValue()));
//                            marker.setTitle(media.getTitle());
//                            //vriskei tin katallhlh eikona gia sugkekrimeno tupo antikeimenou apo to hashMap
//                            marker.setIcon(markerImages.get(MediaType.getMediaType(media.getType())));
//                            marker.addClickListener(Map.this);
//                            marker.addDblClickListener(Map.this.new DoubleClickHandler());
//                            markers.put(marker, media);
//                        }
//                        selectedMarker = null;
//                        download.setEnabled(false);
//                        edit.setEnabled(false);
//                        delete.setEnabled(false);
//                    }
//                });
