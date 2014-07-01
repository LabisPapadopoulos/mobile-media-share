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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


public class List extends ActionBarActivity implements TextWatcher, View.OnClickListener, AdapterView.OnItemSelectedListener {

    private EditText title;
    private EditText user;
    private EditText createdFrom;
    private EditText createdTo;
    private EditText editedFrom;
    private EditText editedTo;
    private Calendar calendar;
    private View currentView;
    private Spinner type;
    private Spinner publik;
    private Spinner pageSize;
    private Button download;
    private Button edit;
    private Button delete;

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
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.list, menu);
        return true;
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
    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

    }
    //TextChangedListener
    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

    }
    //TextChangedListener
    @Override
    public void afterTextChanged(Editable editable) {

    }

    DatePickerDialog.OnDateSetListener currentDate = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(final DatePicker view, final int year, final int monthOfYear, final int dayOfMonth) {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, monthOfYear);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            final String dateFormat = "dd/MM/yy"; //In which you need put here
            final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat, Locale.US);

            if (currentView.getId() == createdFrom.getId()) {
                createdFrom.setText(simpleDateFormat.format(calendar.getTime()));
            } else if (currentView.getId() == createdTo.getId()) {
                createdTo.setText(simpleDateFormat.format(calendar.getTime()));
            } else if (currentView.getId() == editedFrom.getId()) {
                editedFrom.setText(simpleDateFormat.format(calendar.getTime()));
            } else if (currentView.getId() == editedTo.getId()) {
                editedTo.setText(simpleDateFormat.format(calendar.getTime()));
            }
        }
    };

    //ClickListener
    @Override
    public void onClick(View view) {
Log.d("DEBUG", "view: " + view);
        if (view != null) {
            currentView = view;
            calendar = Calendar.getInstance();
        }

        new DatePickerDialog(List.this, currentDate, calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    //ItemSelectedListener
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

    }

    //ItemSelectedListener
    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
