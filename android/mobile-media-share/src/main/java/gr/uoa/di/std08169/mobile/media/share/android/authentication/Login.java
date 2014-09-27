package gr.uoa.di.std08169.mobile.media.share.android.authentication;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
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

import java.util.Locale;

import gr.uoa.di.std08169.mobile.media.share.android.MainMenu;
import gr.uoa.di.std08169.mobile.media.share.android.Map;
import gr.uoa.di.std08169.mobile.media.share.android.MobileMediaShareActivity;
import gr.uoa.di.std08169.mobile.media.share.android.R;


public class Login extends ActionBarActivity implements View.OnClickListener, TextWatcher {
    private EditText email;
    private EditText password;
    private Button ok;
    private Button reset;
    private Button cancel;
    private TextView newUser;
    private TextView forgotPassword;

    @Override
    public void afterTextChanged(final Editable editable) {
        final boolean enabled = (email.getText().length() > 0) && (password.getText().length() > 0);
        ok.setEnabled(enabled);
        reset.setEnabled(enabled);
    }
    @Override
    public void beforeTextChanged(final CharSequence charSequence, final int start, final int count, final int after) {}

    @Override
    public void onClick(final View view) {
        if (view == ok) {
            final AccountManager accountManager = AccountManager.get(this);
            // katharisma paliwn logariasmwn
            for (Account oldAccount : accountManager.getAccountsByType(Authenticator.class.getName())) {
                accountManager.removeAccount(oldAccount, null, null);
            }
            //Dhmiourgeia enos kainouriou logariasmou me to email pou edwse o xrhsths kai tupou Authenticator
            final Account account = new Account(email.getText().toString(), Authenticator.class.getName());
            //apothikeush logariasmou sto kinito monima exwntas ola ta stoixeia tou (username, password)
            accountManager.addAccountExplicitly(account, password.getText().toString(), null);
            startActivity(new Intent(this, MainMenu.class));
            finish();
        } else if (view == reset) {
            email.setText("");
            password.setText("");
            ok.setEnabled(false);
            reset.setEnabled(false);
        } else if (view == cancel) {
            finish(); //termatizei to activity kai ara tin efarmogh
        } else if (view == newUser) {
            final Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(String.format(getResources().getString(R.string.newUserUrl),
                    getResources().getString(R.string.secureBaseUrl))));
            startActivity(intent);
        } else if (view == forgotPassword) {
            final Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(String.format(getResources().getString(R.string.forgotPasswordUrl),
                    getResources().getString(R.string.secureBaseUrl))));
            startActivity(intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initLocale();
        setContentView(R.layout.login);
        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
        ok = (Button) findViewById(R.id.ok);
        reset = (Button) findViewById(R.id.reset);
        cancel = (Button) findViewById(R.id.cancel);
        newUser = (TextView) findViewById(R.id.newUser);
        forgotPassword = (TextView) findViewById(R.id.forgotPassword);
        email.addTextChangedListener(this);
        password.addTextChangedListener(this);
        ok.setOnClickListener(this);
        reset.setOnClickListener(this);
        cancel.setOnClickListener(this);
        newUser.setOnClickListener(this);
        forgotPassword.setOnClickListener(this);
        ok.setEnabled(false);
        reset.setEnabled(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the locale; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.locale, menu);
        //To locale to vlepei o kathe xrhsths stin glwssa tou
        menu.findItem(R.id.en).setTitle(Locale.ENGLISH.getDisplayName(Locale.ENGLISH));
        menu.findItem(R.id.el).setTitle(MobileMediaShareActivity.GREEK.getDisplayName(MobileMediaShareActivity.GREEK));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == R.id.el) {
            setLocale(MobileMediaShareActivity.GREEK, Login.class);
        } else if (itemId == R.id.en) {
            setLocale(Locale.ENGLISH, Login.class);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTextChanged(final CharSequence charSequence, final int start, final int before, final int count) {}

    //allagh glwssas enos Activity (othonhs)
    private void setLocale(final Locale locale, final Class<?> clazz) {
        saveLocale(locale);
        final Configuration configuration = getResources().getConfiguration();
        configuration.locale = locale;
        //kanei update tis ruthmiseis me to neo locale kai xane upologizei xana tis diastaseis
        //tis othonhs gia na ta emfanisei
        getResources().updateConfiguration(configuration, getResources().getDisplayMetrics());
        final Intent intent = new Intent(this, clazz);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    //allagh glwssas enos Activity (othonhs)
    private void initLocale() {
        final Configuration configuration = getResources().getConfiguration();
        configuration.locale = loadLocale();
        //kanei update tis ruthmiseis me to neo locale kai xane upologizei xana tis diastaseis
        //tis othonhs gia na ta emfanisei
        getResources().updateConfiguration(configuration, getResources().getDisplayMetrics());
    }

    private Locale loadLocale() {
        //epistrefei tin proepilegmenh ruthmish gia tin glwssa (to locale)
        return new Locale(getSharedPreferences(MobileMediaShareActivity.class.getName(),
                Activity.MODE_PRIVATE).getString(Locale.class.getName(), Locale.ENGLISH.getLanguage()));
    }

    private void saveLocale(final Locale locale) {
        //Apothikeush proepilegmenwn ruthmisewn xrhsth gia tin glwssa
        final SharedPreferences.Editor editor = getSharedPreferences(MobileMediaShareActivity.class.getName(),
                Activity.MODE_PRIVATE).edit();
        editor.putString(Locale.class.getName(), locale.getLanguage());
        editor.commit();
    }
}
