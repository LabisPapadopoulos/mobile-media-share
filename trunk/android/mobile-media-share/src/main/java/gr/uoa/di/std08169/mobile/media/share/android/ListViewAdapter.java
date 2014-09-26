package gr.uoa.di.std08169.mobile.media.share.android;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

import gr.uoa.di.std08169.mobile.media.share.android.MobileMediaShareActivity;
import gr.uoa.di.std08169.mobile.media.share.android.R;
import gr.uoa.di.std08169.mobile.media.share.android.media.Media;
import gr.uoa.di.std08169.mobile.media.share.android.media.MediaType;

/**
 * Created by labis on 8/6/14.
 */
//Xrhsh tis ListViewAdapter gia pio periploko layout
public class ListViewAdapter extends BaseAdapter {
    private final MobileMediaShareActivity activity; //Opws kai to: Context context;
    private final List<Media> media;

    public ListViewAdapter(final MobileMediaShareActivity activity, final List<Media> media) {
        this.activity = activity;
        this.media = media;
    }

    @Override
    public int getCount() {
        return media.size();
    }

    @Override
    public Media getItem(final int i) {
        return media.get(i);
    }

    @Override
    public long getItemId(final int i) { //kaleitai epilegontas media apo tin lista
        return i;
    }

    //2o orisma einai h default view pou tha parousiaze mono tou
    @Override
    public View getView(final int position, View view, final ViewGroup parent) { //epistrefei to View gia kathe grammh
        if (view == null) { //an den uparxei default view
            //ftiaxnei UI apo to xml me to service LAYOUT INFLATER
            final LayoutInflater layoutInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            //ftiaxnei view apo xml dunamika me to R.layout.list_view.
            view = layoutInflater.inflate(R.layout.list_view, parent, false);
        }
        final Media media = this.media.get(position);
        ((ImageView) view.findViewById(R.id.fileImage)).setImageDrawable(
                activity.getResources().getDrawable(MediaType.getMediaType(media.getType()).getDrawable()));
        final SimpleDateFormat dateFormat = new SimpleDateFormat(activity.getResources().getString(R.string.dateFormat));
                ((TextView) view.findViewById(R.id.title)).setText(media.getTitle());
        ((TextView) view.findViewById(R.id.size)).setText(activity.formatSize(media.getSize()));
        ((TextView) view.findViewById(R.id.user)).setText((media.getUser().getName() == null) ? activity.formatEmail(media.getUser().getEmail()) :
                                                                                                media.getUser().getName());
        ((TextView) view.findViewById(R.id.duration)).setText(activity.formatDuration(media.getDuration()));
        ((TextView) view.findViewById(R.id.created)).setText(dateFormat.format(media.getCreated()));
        ((TextView) view.findViewById(R.id.edited)).setText(dateFormat.format(media.getEdited()));
        ((TextView) view.findViewById(R.id.latitude)).setText(activity.formatLatitude(media.getLatitude()));
        ((TextView) view.findViewById(R.id.longitude)).setText(activity.formatLongitude(media.getLongitude()));
        ((TextView) view.findViewById(R.id.publik)).setText((media.isPublic() ? activity.getResources().getString(R.string.publik) :
                                                                activity.getResources().getString(R.string.private_)));


        return view;
    }
}
