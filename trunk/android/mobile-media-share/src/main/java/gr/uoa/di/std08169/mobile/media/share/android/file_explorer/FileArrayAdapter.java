package gr.uoa.di.std08169.mobile.media.share.android.file_explorer;

import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import gr.uoa.di.std08169.mobile.media.share.android.R;


public class FileArrayAdapter extends ArrayAdapter<Item> {

    private Context context;
    private int id;
    private List<Item> items;

    public FileArrayAdapter(Context context, int textViewResourceId, List<Item> items) {
        super(context, textViewResourceId, items);
        this.context = context;
        id = textViewResourceId;
        this.items = items;
    }

    public Item getItem(int i) {
        return items.get(i);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(id, null);
        }

               /* create a new view of my layout and inflate it in the row */
        //convertView = ( RelativeLayout ) inflater.inflate( resource, null );

        Item item = items.get(position);
        if (item == null) {
            return null;
        } else {
            final TextView fileName = (TextView) view.findViewById(R.id.title);
            final TextView numberOfItems = (TextView) view.findViewById(R.id.user);
            final TextView fileDate = (TextView) view.findViewById(R.id.duration);
                       /* Take the ImageView from layout and set the city's image */
            final ImageView imageCity = (ImageView) view.findViewById(R.id.fileImage);
            String uri = "drawable/" + item.getImage();
            int imageResource = context.getResources().getIdentifier(uri, null, context.getPackageName());
            Drawable image = context.getResources().getDrawable(imageResource);
            imageCity.setImageDrawable(image);

            if(fileName != null)
                fileName.setText(item.getName());
            if(numberOfItems != null)
                numberOfItems.setText(item.getData());
            if(fileDate != null)
                fileDate.setText(item.getDate());
//Log.d(FileArrayAdapter.class.getName(), "getView: " + view.toString());
        }
        return view;
    }
}
