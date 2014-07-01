package gr.uoa.di.std08169.mobile.media.share.android;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;


public class NewVideo extends ActionBarActivity implements View.OnClickListener, TextWatcher {

    private Button startRecording;
    private Button stopRecording;
    private EditText title;
    private CheckBox isPublic;
    private TextView latlng;
    private Button ok;
    private Button reset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_video);

        startRecording = (Button) findViewById(R.id.startRecording);
        startRecording.setOnClickListener(this);

        stopRecording = (Button) findViewById(R.id.stopRecording);
        stopRecording.setOnClickListener(this);

        title = (EditText) findViewById(R.id.title);
        title.addTextChangedListener(this);

        isPublic = (CheckBox) findViewById(R.id.isPublic);
        isPublic.setOnClickListener(this);

        latlng = (TextView) findViewById(R.id.latlng);

        ok = (Button) findViewById(R.id.ok);
        ok.setOnClickListener(this);

        reset = (Button) findViewById(R.id.reset);
        reset.setOnClickListener(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.new_video, menu);
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

    //OnClickListener
    @Override
    public void onClick(View view) {

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
}
