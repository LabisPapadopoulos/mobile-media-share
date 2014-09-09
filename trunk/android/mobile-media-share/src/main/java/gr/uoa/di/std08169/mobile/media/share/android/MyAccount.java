package gr.uoa.di.std08169.mobile.media.share.android;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

//import gr.uoa.di.std08169.mobile.media.share.android.http.HttpsPostEditAsyncTask;
import gr.uoa.di.std08169.mobile.media.share.android.user.User;


public class MyAccount extends MobileMediaShareActivity implements View.OnClickListener, TextWatcher {

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

//        requestUser();

        name = (EditText) findViewById(R.id.name);
        name.addTextChangedListener(this);
        status = (TextView) findViewById(R.id.status);
        email = (TextView) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
        password.addTextChangedListener(this);
        password2 = (EditText) findViewById(R.id.confirmPassword);
        password2.addTextChangedListener(this);
        ok = (Button) findViewById(R.id.ok);
        ok.setOnClickListener(this);
        ok.setEnabled(false);
        reset = (Button) findViewById(R.id.reset);
        reset.setOnClickListener(this);

        myAccount();
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
        if (id == R.id.settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //OnClickListener
    @Override
    public void onClick(View view) {
        if (ok.equals(view)) {
            editUser();
        } else if (reset.equals(view)) {

        }
    }

    //TextChangedListener
    @Override
    public void beforeTextChanged(final CharSequence charSequence, final int start, final int count, final int after) {
    }

    //TextChangedListener
    @Override
    public void onTextChanged(final CharSequence charSequence, final int start, final int count, final int after) {
        if ((!((password.getText().toString().trim().length() == 0) && (password2.getText().toString().trim().length() == 0))) &&
                (!password.getText().toString().equals(password2.getText().toString()))) {
            ok.setEnabled(false);
        } else
            ok.setEnabled(true);
    }

    //TextChangedListener
    @Override
    public void afterTextChanged(final Editable editable) {
        if (editable.toString().equals(user.getName()) || (editable.toString().length() == 0))
            ok.setEnabled(false);
    }

    private void myAccount() {
//        try {
//            new GetAsyncTask(getApplicationContext()) {
//                @Override
//                protected void onPostExecute(HttpsResponse response) {
//                    if (!response.isSuccess()) {
//                        error(R.string.authenticationError, response.getResponse());
//                        return;
//                    }
//                    try {
//                        //[ ... ] -> json Array
//                        final JSONObject user = new JSONObject(response.getResponse());
//                        //{"email":"haralambos9094@gmail.com","name":"labis","status":"NORMAL"}
//                        final String email = user.getString("email");
//                        final UserStatus status = (user.getString("status") == null) ? null : UserStatus.valueOf(user.getString("status"));
//                        final String name = (user.toString().contains("\"name\"")) ? user.getString("name") : null;
//                        final String photo = (user.toString().contains("\"photo\"")) ? user.getString("photo") : null;
//                        Log.d(MobileMediaShareActivity.class.getName(), "Retrieved user");
//                        MyAccount.this.user = ((email == null) || (status == null)) ? null : new User(email, status, name, photo);
//                        Log.d(MobileMediaShareActivity.class.getName(), "Updated list");
//                        MyAccount.this.name.setText(name);
//                        MyAccount.this.email.setText(MyAccount.this.user.getEmail());
//                        MyAccount.this.status.setText("" + MyAccount.this.user.getStatus()); //TODO
//
//                        if ((MyAccount.this.name.getText().toString().trim().length() != 0) &&
//                                MyAccount.this.name.getText().toString().trim().equals(MyAccount.this.user.getName()) ||
//                                (MyAccount.this.name.getText().toString().trim().length() == 0))
//                            ok.setEnabled(false);
//                        else
//                            ok.setEnabled(true);
//
//                    } catch (final JSONException e) {
//                        error(R.string.authenticationError, e.getMessage());
//                    }
//                }
//            }.execute(new URL(String.format(getResources().getString(R.string.userServletUrl),
//                    getResources().getString(R.string.secureBaseUrl))));
//        } catch (IOException e) {
//            error(R.string.authenticationError, e.getMessage());
//        }
    }

    private void editUser() {
//        final String userServlet = "userServlet";
//        final String name = this.name.getText().toString().trim();
//        final String password = (this.password.getText().toString().trim().length() == 0) ? null : this.password.getText().toString().trim(); //TODO password2 check
//        //action=editUser&email=haralambos9094%40gmail.com&password=321&name=...&photo=...
//        final StringBuilder parameters = new StringBuilder();//(String.format(getResources().getString(R.string.editUserUrl),
//                //getResources().getString(R.string.secureBaseUrl)));
//
//        parameters.append("action=").append("editUser");
//        parameters.append("&email=").append("haralambos9094%40gmail.com"); //TODO encode
//
//        if (name != null)
//            parameters.append("&name=").append(name);
//        if (password != null)
//            parameters.append("&password=").append(password);
//        //TODO photo
//Log.d(MyAccount.class.getName(), parameters.toString());
//        new HttpsPostEditAsyncTask(getApplicationContext()) {
//
//            @Override
//            protected void onPostExecute(HttpsResponse response) {
//                if (!response.isSuccess()) {
//                    error(R.string.errorEditingUser, response.getResponse());
//                    return;
//                }
//Toast.makeText(MyAccount.this, response.getResponse(), Toast.LENGTH_LONG).show();
//Log.d(MyAccount.class.getName(), response.getResponse());
//                final Intent activityIntent = new Intent(getApplicationContext(), Map.class);
//                activityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                MyAccount.this.finish();
//                startActivity(activityIntent);
//
//            }
//        }.execute(userServlet, parameters.toString());
    }
}
