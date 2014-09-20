package gr.uoa.di.std08169.mobile.media.share.android;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import gr.uoa.di.std08169.mobile.media.share.android.http.HttpClient;
import gr.uoa.di.std08169.mobile.media.share.android.http.PostAsyncTask;
import gr.uoa.di.std08169.mobile.media.share.android.user.User;


public class MyAccount extends MobileMediaShareActivity implements View.OnClickListener, TextWatcher {
    private static final String ENTITY = "action=edit&name=%s%s";
    private static final String PASSWORD = "&password=%s";
    private EditText name;
    private TextView status;
    private TextView email;
    private EditText password;
    private EditText password2;
    private Button ok;
    private Button reset;

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_account);

        name = (EditText) findViewById(R.id.name);
        name.setText(currentUser.getName());
        name.addTextChangedListener(this);
        status = (TextView) findViewById(R.id.status);
        status.setText(getResources().getStringArray(
                R.array.user_status)[currentUser.getStatus().ordinal()]);
        email = (TextView) findViewById(R.id.email);
        email.setText(currentUser.getEmail());
        password = (EditText) findViewById(R.id.password);
        password.addTextChangedListener(this);
        password2 = (EditText) findViewById(R.id.confirmPassword);
        password2.addTextChangedListener(this);
        ok = (Button) findViewById(R.id.ok);
        ok.setEnabled(false);
        ok.setOnClickListener(this);
        reset = (Button) findViewById(R.id.reset);
        reset.setEnabled(false);
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
        return (item.getItemId() == R.id.settings) || super.onOptionsItemSelected(item);
    }

    //OnClickListener
    @Override
    public void onClick(View view) {
        if (ok.equals(view)) {
            editUser();
        } else if (reset.equals(view)) {
            name.setText(currentUser.getName());
            password.setText("");
            password2.setText("");
            ok.setEnabled(false);
            reset.setEnabled(false);
        }
    }

    //TextChangedListener
    @Override
    public void beforeTextChanged(final CharSequence charSequence, final int start, final int count, final int after) {
    }

    //TextChangedListener
    @Override
    public void onTextChanged(final CharSequence charSequence, final int start, final int count, final int after) {}

    //TextChangedListener
    @Override
    public void afterTextChanged(final Editable editable) {
        final boolean enabled = !(name.getText().toString().equals(currentUser.getName()) &&
                password.getText().toString().isEmpty() && password2.getText().toString().isEmpty());
        ok.setEnabled(enabled);
        reset.setEnabled(enabled);
    }

    private void editUser() {
        if (!password.getText().toString().equals(password2.getText().toString())) {
            Toast.makeText(this, getResources().getString(R.string.passwordsDoNotMatch), Toast.LENGTH_LONG).show();
            return;
        }
        try {
            final String url = String.format(getResources().getString(R.string.userServletUrl),
                    getResources().getString(R.string.baseUrl));
            //Swma tou http post
            final HttpEntity entity = new StringEntity(String.format(ENTITY, name.getText().toString(),
                    //an to password keno den to vazei katholou sto entity
                    password.getText().toString().isEmpty() ? "" :
                            String.format(PASSWORD, password.getText().toString())));
            final HttpResponse response = new PostAsyncTask(this,new URL(url), entity,
                    APPLICATION_FORM_URL_ENCODED).execute().get();
            if (response == null) {
                error(R.string.errorEditingUser, getResources().getString(R.string.connectionError));
                return;
            }
            if ((response.getStatusLine().getStatusCode() == HttpClient.HTTP_UNAUTHORIZED) && login()) {
                editUser();
            }
            if (response.getStatusLine().getStatusCode() != HttpClient.HTTP_OK) {
                error(R.string.errorEditingUser, response.getStatusLine().getReasonPhrase());
                return;
            }
            finish();
        } catch (final UnsupportedEncodingException e) {
            error(R.string.errorEditingUser, e.getMessage());
        } catch (final MalformedURLException e) {
            error(R.string.errorEditingUser, e.getMessage());
        } catch (final InterruptedException e) {
            error(R.string.errorEditingUser, e.getMessage());
        } catch (final ExecutionException e) {
            error(R.string.errorEditingUser, e.getMessage());
        }
    }
}
