package gr.uoa.di.std08169.mobile.media.share.android;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

/**
 * @see <a href="http://stackoverflow.com/questions/14117476/how-to-quit-an-application-programmatically-through-button-click">Quit application programmatically</a>
 */
public class MainMenu extends MobileMediaShareActivity implements View.OnClickListener {
    private LinearLayout map;
    private LinearLayout list;
    private LinearLayout newPhoto;
    private LinearLayout newVideo;
    private LinearLayout upload;
    private LinearLayout myAccount;
    private LinearLayout logout;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu);

        map = (LinearLayout) findViewById(R.id.map);
        map.setOnClickListener(this);
        list = (LinearLayout) findViewById(R.id.list);
        list.setOnClickListener(this);
        newPhoto = (LinearLayout) findViewById(R.id.newPhoto);
        newPhoto.setOnClickListener(this);
        newVideo = (LinearLayout) findViewById(R.id.newVideo);
        newVideo.setOnClickListener(this);
        upload = (LinearLayout) findViewById(R.id.upload);
        upload.setOnClickListener(this);
        myAccount = (LinearLayout) findViewById(R.id.myAccount);
        myAccount.setOnClickListener(this);
        logout = (LinearLayout) findViewById(R.id.logout);
        logout.setOnClickListener(this);
    }

    @Override
    public void onClick(final View view) {
        Class<?> clazz = null;
        if (map.equals(view))
            clazz = Map.class;
        else if (list.equals(view))
            clazz = List.class;
        else if (newPhoto.equals(view))
            clazz = NewPhoto.class;
        else if (newVideo.equals(view))
            clazz = NewVideo.class;
        else if (upload.equals(view))
            clazz = Upload.class;
        else if (myAccount.equals(view))
            clazz = MyAccount.class;
        else if (logout.equals(view))
            finish();
        if (clazz != null) {
            final Intent activityIntent = new Intent(getApplicationContext(), clazz);
            activityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(activityIntent);
        }
    }
}
