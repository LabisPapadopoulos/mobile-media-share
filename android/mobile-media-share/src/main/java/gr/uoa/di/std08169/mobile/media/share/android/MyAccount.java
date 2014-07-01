package gr.uoa.di.std08169.mobile.media.share.android;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Text;


public class MyAccount extends ActionBarActivity implements View.OnClickListener, TextWatcher {

    private EditText name;
    private TextView status;
    private TextView email;
    private EditText password;
    private EditText confirmPassword;
    private Button ok;
    private Button reset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_account);

        name = (EditText) findViewById(R.id.name);
        name.addTextChangedListener(this);

        status = (TextView) findViewById(R.id.status);

        email = (TextView) findViewById(R.id.email);

        password = (EditText) findViewById(R.id.password);
        password.addTextChangedListener(this);

        confirmPassword = (EditText) findViewById(R.id.confirmPassword);
        confirmPassword.addTextChangedListener(this);

        ok = (Button) findViewById(R.id.ok);
        ok.setOnClickListener(this);

        reset = (Button) findViewById(R.id.reset);
        reset.setOnClickListener(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my_account, menu);
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
