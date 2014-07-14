package gr.uoa.di.std08169.mobile.media.share.android;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import gr.uoa.di.std08169.mobile.media.share.android.https.HttpsAsyncTask;
import gr.uoa.di.std08169.mobile.media.share.android.https.HttpsResponse;


public class List extends MobileMediaShareActivity implements
        AdapterView.OnItemSelectedListener, DatePickerDialog.OnDateSetListener, TextWatcher,
        View.OnClickListener {

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
    private Button download;
    private Button edit;
    private Button delete;
    private EditText selectedDateField;
    private TableLayout table;

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
Log.d("DEBUG", "view: " + view);
        if (view instanceof EditText)
            selectedDateField = (EditText) view;
        final Calendar calendar = Calendar.getInstance();
        //1o this: olh h clash pou ulopoiei to DatePickerDialog.OnDateSetListener
        //2o this: callback
        new DatePickerDialog(this, this, calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
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

        download = (Button) findViewById(R.id.download);
        edit = (Button) findViewById(R.id.edit);
        delete = (Button) findViewById(R.id.edit);

        table = (TableLayout) findViewById(R.id.table);
        table.removeAllViews();

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
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //TextChangedListener
    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

    }

    private void updateList() {
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

        final StringBuilder url = new StringBuilder(getResources().getString(R.string.getResultUrl));
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
            new HttpsAsyncTask(getApplicationContext()) {

                @Override
                protected void onPostExecute(HttpsResponse response) {
                    table.removeAllViews();
                    if (!response.isSuccess()) {
                        error(R.string.errorRetrievingMedia, response.getResponse());
                        return;
                    }
                    try {
                        final JSONObject list = new JSONObject(response.getResponse());
                        final JSONArray mediaArray = list.getJSONArray("media");
                        for(int i = 0; i < mediaArray.length(); i++) {
                            final JSONObject media = mediaArray.getJSONObject(i);
                            final String id = media.getString("id");
                            final String type = media.getString("type");
                            final int size = media.getInt("size");
                            final double duration = media.getDouble("duration");

                            final JSONObject user = media.getJSONObject("user");
                            final String email = user.getString("email");
                            final String status = user.getString("status");

                            final String created = media.getString("created");
                            final String edited = media.getString("edited");
                            final String title = media.getString("title");
                            final double latitude = media.getDouble("latitude");
                            final double longitude = media.getDouble("longitude");
                            final boolean publik = media.getBoolean("publik");

//Log.d("---DEBUG---", "id: " + id + ", title: " + title + ", type: " + type + ", latitude: " + latitude + ", longitude: " + longitude);
//Title    Type	 Size	Duration	User	Created	Edited	Latitude	Longitude	Public
//                            final View ruler = new View(getApplicationContext());
//                            ruler.setBackgroundColor(0xFF00FF00);
//                            theParent.addView(ruler,
//                                    new ViewGroup.LayoutParams( ViewGroup.LayoutParams.WRAP_CONTENT, 2));

                            final TableRow row1 = new TableRow(getApplicationContext());
//                            TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.)

                            final TextView titleView = new TextView(getApplicationContext());
                            titleView.setText("Title: " + title);
                            titleView.setTextColor(Color.BLACK);
                            titleView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Toast.makeText(List.this, "id: " + id, Toast.LENGTH_SHORT).show();
                                }
                            });
                            row1.addView(titleView);

                            final TextView typeView = new TextView(getApplicationContext());
                            typeView.setText("Type: " + type);
                            row1.addView(typeView);
                            table.addView(row1);

                            final TableRow row2 = new TableRow(getApplicationContext());
                            final TextView sizeView = new TextView(getApplicationContext());
                            sizeView.setText("Size: " + size);
                            row2.addView(sizeView);

                            final TextView durationView = new TextView(getApplicationContext());
                            durationView.setText("Duration: " + duration);
                            row2.addView(durationView);
                            table.addView(row2);

                            final TableRow row3 = new TableRow(getApplicationContext());
                            final TextView userView = new TextView(getApplicationContext());
                            userView.setText("User: " + email);
                            row3.addView(userView);

                            final TextView createdView = new TextView(getApplicationContext());
                            createdView.setText("Created:" + created);
                            row3.addView(createdView);
                            table.addView(row3);

                            final TableRow row4 = new TableRow(getApplicationContext());
                            final TextView editedView = new TextView(getApplicationContext());
                            editedView.setText("Edited: " + edited);
                            row4.addView(editedView);

                            final TextView latitudeView = new TextView(getApplicationContext());
                            latitudeView.setText("Latitude: " + latitude);
                            row4.addView(latitudeView);
                            table.addView(row4);

                            final TableRow row5 = new TableRow(getApplicationContext());
                            final TextView longitudeView = new TextView(getApplicationContext());
                            longitudeView.setText("Longitude: " + longitude);
                            row5.addView(longitudeView);

                            final TextView publicView = new TextView(getApplicationContext());
                            publicView.setText("Public: " + publik);
                            row5.addView(publicView);
                            table.addView(row5);
                        }

                        final int total = list.getInt("total");
                    } catch (final JSONException e) {
                        error(R.string.errorRetrievingMedia, response.getResponse());
                        return;
                    }
                }
            }.execute(new URL(url.toString()));
        } catch (final IOException e) {
            error(R.string.errorRetrievingMedia, e.getMessage());
            return;
        }
    }

    private void clearTable() {
        for (int i = 0; i < table.getChildCount(); i++) {
            final View child = table.getChildAt(i);
            if (child instanceof TableRow)
                ((ViewGroup) child).removeAllViews();
        }
    }
}
