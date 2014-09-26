package gr.uoa.di.std08169.mobile.media.share.android;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import org.apache.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;

import gr.uoa.di.std08169.mobile.media.share.android.http.DeleteAsyncTask;
import gr.uoa.di.std08169.mobile.media.share.android.http.GetAsyncTask;
import gr.uoa.di.std08169.mobile.media.share.android.http.HttpClient;
import gr.uoa.di.std08169.mobile.media.share.android.media.Media;
import gr.uoa.di.std08169.mobile.media.share.android.user.User;
import gr.uoa.di.std08169.mobile.media.share.android.user.UserStatus;


public class List extends MobileMediaShareActivity implements AdapterView.OnItemClickListener,
        AdapterView.OnItemSelectedListener, DatePickerDialog.OnDateSetListener, TextWatcher,
        View.OnClickListener {
    private EditText title;
    private EditText user;
    private EditText createdFrom;
    private EditText createdTo;
    private EditText editedFrom;
    private EditText editedTo;
    private Spinner type;
    private Spinner publik;
    private Spinner pageSize;
    private Button view;
    private Button edit;
    private Button delete;
    private Button download;
    private EditText selectedDateField;
    private Media selectedMedia;

    private ListView listView;
    private java.util.List<Media> mediaList;
    private ListViewAdapter listViewAdapter;

    //TextChangedListener
    @Override
    public void afterTextChanged(Editable editable) {
        updateList();
    }

    //TextChangedListener
    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
    }

    //createdFrom (DatePicker) - ClickListener
    @Override
    public void onClick(final View view) {
        if (view instanceof EditText) {
            selectedDateField = (EditText) view;
            final Calendar calendar = Calendar.getInstance();
            //1o this: olh h clash pou ulopoiei to DatePickerDialog.OnDateSetListener
            //2o this: callback
            new DatePickerDialog(this, this, calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        } else if (view == this.view) {
            selectedDateField = null;
            final Intent activityIntent = new Intent(getApplicationContext(), ViewMedia.class);
            activityIntent.putExtra("id", selectedMedia.getId());
            activityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(activityIntent);
        } else if (view == edit) {
            selectedDateField = null;
            final Intent activityIntent = new Intent(getApplicationContext(), EditMedia.class);
            activityIntent.putExtra("id", selectedMedia.getId());
            activityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(activityIntent);
        } else if (view == this.delete) {
            selectedDateField = null;
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(List.this);
            alertDialogBuilder.setTitle(getResources().getString(R.string.areYouSureYouWantToDeleteThisMedia));
            alertDialogBuilder.setMessage(String.format(getResources().getString(R.string.areYouSureYouWantToDeleteMedia_),
                    selectedMedia.getTitle()));
            alertDialogBuilder.setCancelable(false);
            alertDialogBuilder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            deleteMedia();
                        }
                    });
            alertDialogBuilder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);

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


        pageSize = (Spinner) findViewById(R.id.pageSize);
        final ArrayAdapter<CharSequence> adapterPageSize = ArrayAdapter.createFromResource(this,
                //times pou tha exei to drop down list (media_type)
                R.array.page_size, android.R.layout.simple_spinner_item);//mia grammh ston spinner
        //default vertical gia to drop down spinner (pws tha fenetai)
        adapterPageSize.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //efarmogh tou adapter ston spinner
        pageSize.setAdapter(adapterPageSize);
        pageSize.setOnItemSelectedListener(this);

        view = (Button) findViewById(R.id.view);
        view.setEnabled(false);
        view.setOnClickListener(this);
        edit = (Button) findViewById(R.id.edit);
        edit.setEnabled(false);
        edit.setOnClickListener(this);
        delete = (Button) findViewById(R.id.delete);
        delete.setEnabled(false);
        delete.setOnClickListener(this);
        download = (Button) findViewById(R.id.download);
        download.setEnabled(false);
        download.setOnClickListener(this);
        listView = (ListView) findViewById(R.id.list);
        listView.setOnItemClickListener(this);
        mediaList = new ArrayList<Media>();
        selectedDateField = null;
        selectedMedia = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.list, menu);
        return true;
    }

    @Override
    public void onDateSet(final DatePicker datePicker, final int year, final int monthOfYear, final int dayOfMonth) {
        if (selectedDateField != null) {
            final Calendar calendar = Calendar.getInstance();
            calendar.clear(); //default timh (gia na einai orismena mono ta pedia tou xrhsth)
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, monthOfYear);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            selectedDateField.setText(new SimpleDateFormat(getResources().getString(R.string.dateFormat)).
                    format(calendar.getTime()));
        }
    }

    //ItemSelectedListener
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        updateList();
    }

    //ItemSelectedListener
    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        updateList();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return (item.getItemId() == R.id.settings) || super.onOptionsItemSelected(item);
    }

    //TextChangedListener
    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

    }

    private void updateList() {
        mediaList.clear();
        final String title = (this.title.getText().toString().trim().length() == 0) ? null :
                this.title.getText().toString().trim();
        final String user = (this.user.getText().toString().trim().length() == 0) ? null :
                this.user.getText().toString().trim();
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
            case 0: //epillegmeno to any type
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
        Integer pageSize = null;
        switch (this.pageSize.getSelectedItemPosition()) {
            case AdapterView.INVALID_POSITION:
                break;
            default:
                pageSize = (this.pageSize.getSelectedItemPosition() + 1) * 10;
        }

        final StringBuilder url = new StringBuilder(String.format(getResources().getString(R.string.getResultUrl),
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

        //TODO paging
        url.append("&start=0");
        url.append("&length=").append(pageSize);
        url.append("&ascending=asc");
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

            final JSONObject list = new JSONObject(json.toString());
            final JSONArray mediaArray = list.getJSONArray("media");
            for(int i = 0; i < mediaArray.length(); i++) {
                final JSONObject jsonMedia = mediaArray.getJSONObject(i);
                final String id = jsonMedia.getString("id");
                final String jsonType = jsonMedia.getString("type");
                final int size = jsonMedia.getInt("size");
                final int duration = jsonMedia.getInt("duration");
                final JSONObject jsonUser = jsonMedia.getJSONObject("user");
                final String email = jsonUser.getString("email");
                final UserStatus status = UserStatus.valueOf(jsonUser.getString("status"));
                final String name = jsonUser.has("name") ? jsonUser.getString("name") : null;
                final String photo = jsonUser.has("photo") ? jsonUser.getString("photo") : null;
                final User mediaUser = new User(email, status, name, photo);
                final Date created = new Date(jsonMedia.getLong("created"));
                final Date edited = new Date(jsonMedia.getLong("edited"));
                final String jsonTitle = jsonMedia.getString("title");
                final BigDecimal latitude = new BigDecimal(jsonMedia.getDouble("latitude"));
                final BigDecimal longitude = new BigDecimal(jsonMedia.getDouble("longitude"));
                final Boolean jsonPublic = jsonMedia.getBoolean("public");
                final Media media = new Media(id, jsonType, size, duration, mediaUser, created, edited,
                        jsonTitle, latitude, longitude, jsonPublic);
                mediaList.add(media);
            }
            final int total = list.getInt("total"); //TODO use for paging
            listViewAdapter = new ListViewAdapter(this, mediaList);
            listView.setAdapter(listViewAdapter);
            //metraei to megethos tis listView gia na topothetithei oloklhrhs mesa sto ScrollView
            getListViewSize(listView);
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

    /**
     * @see <a href="http://www.androidhub4you.com/2012/12/listview-into-scrollview-in-android.html">ListView into ScrollView in Android</a>
     * @param listView
     */
    public void getListViewSize(final ListView listView) {
        if (listView.getAdapter() == null) //do nothing return null
            return;
        //set listAdapter in loop for getting final size
        int totalHeight = 0;
        for (int i = 0; i < listView.getAdapter().getCount(); i++) {
            final View listItem = listView.getAdapter().getView(i, null, listView);
            //mhdenizei to megethos pou tou leei o pateras tou me apotelesma na pairnei oso thelei
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight(); //metraei to neo upsos
        }
        //setting listview item in adapter
        final ViewGroup.LayoutParams params = listView.getLayoutParams();
        //dunamika h allagh tou upsous kai me prosthikh twn dividers
        params.height = totalHeight + (listView.getDividerHeight() * (listView.getAdapter().getCount() - 1));
        listView.setLayoutParams(params);
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
                updateList();
        } catch (final IOException e) {
            error(R.string.errorDeletingMedia, e.getMessage());
        } catch (final InterruptedException e) {
            error(R.string.errorDeletingMedia, e.getMessage());
        } catch (final ExecutionException e) {
            error(R.string.errorDeletingMedia, e.getMessage());
        }
    }

    private View selectedView = null;

    //Adapter.OnItemClickListener
    //Listener stin listView, opote to kathe stoixeio tis listas erxetai ws eisodo me to view kai stin thesh position
    @Override
    public void onItemClick(final AdapterView<?> adapterView, final View view, final int position, final long id) {
        if (selectedView != null) {
            selectedView.setBackgroundColor(Color.TRANSPARENT);
        }
        selectedView = view;
        selectedView.setBackgroundColor(getResources().getColor(R.color.selectedListItem));
        selectedMedia = listViewAdapter.getItem(position);
        this.view.setEnabled(true);
        download.setEnabled(true);
        final boolean enabled = selectedMedia.getUser().equals(currentUser) || (currentUser.getStatus() == UserStatus.ADMIN);
        edit.setEnabled(enabled);
        delete.setEnabled(enabled);
    }
}
