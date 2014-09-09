package gr.uoa.di.std08169.mobile.media.share.android;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;

import gr.uoa.di.std08169.mobile.media.share.android.ListView.ListMedia;
import gr.uoa.di.std08169.mobile.media.share.android.ListView.ListViewAdapter;
import gr.uoa.di.std08169.mobile.media.share.android.http.GetAsyncTask;
import gr.uoa.di.std08169.mobile.media.share.android.http.HttpClient;
import gr.uoa.di.std08169.mobile.media.share.android.user.User;
import gr.uoa.di.std08169.mobile.media.share.android.user.UserStatus;


public class List extends MobileMediaShareActivity implements /* AdapterView.OnItemClickListener, */
        AdapterView.OnItemSelectedListener, DatePickerDialog.OnDateSetListener, TextWatcher,
        View.OnClickListener {
    private static final String UTF_8 = "UTF-8";

    private EditText title;
    private EditText user;
    private EditText createdFrom;
    private EditText createdTo;
    private EditText editedFrom;
    private EditText editedTo;
    private View currentView;
    private Spinner type;
    private Spinner publik;
    private Spinner pageSize;
    private Button view;
    private Button edit;
    private Button delete;
    private Button download;
    private EditText selectedDateField;

    private ListView listView;
    private java.util.List<ListMedia> mediaList;
    private ListViewAdapter listViewAdapter;

    final Context context = this;
    private String selectedId;
    private boolean selectedMedia;

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
            selectedId = listViewAdapter.getMediaId();
            if (selectedId == null) {
                Toast.makeText(this, "Please select a media to view", Toast.LENGTH_LONG).show();
                return;
            }
            final Intent activityIntent = new Intent(getApplicationContext(), ViewMedia.class);
            activityIntent.putExtra("id", selectedId);
            activityIntent.putExtra("currentUser", currentUser.getEmail());
            activityIntent.putExtra("userStatus", currentUser.getStatus().toString());
            activityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(activityIntent);
        } else if (view == edit) {
            selectedDateField = null;
            selectedId = listViewAdapter.getMediaId();
            if (selectedId == null) {
                Toast.makeText(this, "Please select a media to edit", Toast.LENGTH_LONG).show();
                return;
            }
            final Intent activityIntent = new Intent(getApplicationContext(), EditMedia.class);
            activityIntent.putExtra("id", selectedId);
            activityIntent.putExtra("currentUser", currentUser.getEmail());
            activityIntent.putExtra("userStatus", currentUser.getStatus().toString());
            activityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(activityIntent);
        } else if (view == this.delete) {
            selectedDateField = null;
            selectedId = listViewAdapter.getMediaId();
            if (selectedId == null) {
                Toast.makeText(this, "Please select a media to delete", Toast.LENGTH_LONG).show();
                return;
            }
            deleteMedia();
        } else if (view == download) {
            selectedDateField = null;
            selectedId = listViewAdapter.getMediaId();
            if (selectedId == null) {
                Toast.makeText(this, "Please select a media to download", Toast.LENGTH_LONG).show();
                return;
            }
Toast.makeText(this, "Download under construction", Toast.LENGTH_LONG).show();
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
        view.setOnClickListener(this);
        edit = (Button) findViewById(R.id.edit);
        edit.setOnClickListener(this);
        delete = (Button) findViewById(R.id.delete);
        delete.setOnClickListener(this);
        download = (Button) findViewById(R.id.download);
        download.setOnClickListener(this);

        listView = (ListView) findViewById(R.id.list);
//        listView.setOnItemClickListener(this);
        mediaList = new ArrayList<ListMedia>();
        selectedMedia = false;

        selectedDateField = null;
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
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
        //TODO
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
                getResources().getString(R.string.baseUrl)));
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

        //TODO
        url.append("&start=0");
        url.append("&length=").append(pageSize);
        url.append("&ascending=asc");

Log.i("URL: ", url.toString());
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
                final JSONObject media = mediaArray.getJSONObject(i);
                final String id = media.getString("id");
                final String mediaType = media.getString("type");
                final int size = media.getInt("size");
                final double duration = media.getDouble("duration");

                final JSONObject jsonUser = media.getJSONObject("user");
                final String email = jsonUser.getString("email");
                final String status = jsonUser.getString("status");

                final String created = media.getString("created");
                final String edited = media.getString("edited");
                final String mediaTitle = media.getString("title");
                final double latitude = media.getDouble("latitude");
                final double longitude = media.getDouble("longitude");
                final boolean mediaPublik = media.getBoolean("publik");

                final User mediaUser = new User(email, UserStatus.valueOf(status), null, null);
                mediaList.add(new ListMedia(id, mediaTitle, mediaType, mediaUser, latitude, longitude,
                        size, duration, created, edited, mediaPublik));
            }

            listViewAdapter = new ListViewAdapter(List.this, R.layout.list_view, mediaList);
            listView.setAdapter(listViewAdapter);

            final int total = list.getInt("total");
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
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle("Are tou sure you want to delete this media?");

        alertDialogBuilder.setMessage("Click yes to delete!").setCancelable(false).
                setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, close
                        // current activity
                        //Delete Media
//                        try {
//                            final String url = String.format(getResources().getString(R.string.deleteMediaUrl),
//                                    getResources().getString(R.string.secureBaseUrl),
//                                    URLEncoder.encode(List.this.selectedId, UTF_8));
//
//                            new HttpsDeleteAsyncTask(getApplicationContext()) {
//
//                                @Override
//                                protected void onPostExecute(HttpsResponse response) {
//                                    if (!response.isSuccess()) {
//                                        error(R.string.errorDeletingMedia, response.getResponse());
//                                        return;
//                                    }
//                                    Toast.makeText(List.this, response.getResponse(), Toast.LENGTH_LONG).show();
//                                    Log.d(List.class.getName(), response.getResponse());
//                                    final Intent activityIntent = new Intent(getApplicationContext(), Map.class);
//                                    activityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                                    List.this.finish();
//                                    startActivity(activityIntent);
//                                }
//                            }.execute(new URL(url));
//
//                        } catch (final IOException e) {
//                            error(R.string.errorDeletingMedia, e.getMessage());
//                        }
                    }
                }).
                setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        dialog.cancel();
                    }
                });
        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show();
    }

//    //Adapter.OnItemClickListener
//    @Override
//    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
////        ListMedia mediaList = (ListMedia) listViewAdapter.getItem(position);
//        if ((position == listViewAdapter.getSelectedPosition()) && (selectedMedia == false)) { //TODO
//            view.setBackgroundColor(Color.YELLOW);
//            selectedMedia = true;
//        }
//
//        if ((position == listViewAdapter.getSelectedPosition()) && (selectedMedia == true)) {
//            view.setBackgroundColor(Color.TRANSPARENT);
//            selectedMedia = false;
//        }
//
//    }
}
