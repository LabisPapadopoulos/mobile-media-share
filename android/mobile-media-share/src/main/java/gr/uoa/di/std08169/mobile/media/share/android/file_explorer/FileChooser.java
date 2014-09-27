package gr.uoa.di.std08169.mobile.media.share.android.file_explorer;

import java.io.File;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.text.DateFormat;
import android.os.Bundle;
import android.app.ListActivity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import gr.uoa.di.std08169.mobile.media.share.android.R;

/**
 * @see <a href="http://custom-android-dn.blogspot.gr/2013/01/create-simple-file-explore-in-android.html">Create File Explorer in Android</a>
 */

public class FileChooser extends ListActivity {

    private File currentDir;
    private FileArrayAdapter adapter;
    private List<Item> directories;
    private List<Item> files;
//    String START_PATH = Environment.getExternalStorageDirectory().getPath(); // /storage/sdcard0/
    private String START_PATH = "storage";
//    String START_PATH = "/sdcard/";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        currentDir = new File(START_PATH); //"/sdcard/"
        directories = new ArrayList<Item>();
        files = new ArrayList<Item>();

        fill(currentDir);
    }
    private void fill(final File directory) {
        directories.clear();
        files.clear();
        File[] filesArray = directory.listFiles();
        if (directory.listFiles() == null) {
            Toast.makeText(this, "Apagoreuetai h prosvash!!!", Toast.LENGTH_SHORT).show();
            return;
        }
        this.setTitle("Current Dir: " + directory.getName());

        for(File file: filesArray) {
            Date lastModDate = new Date(file.lastModified());
            DateFormat formater = DateFormat.getDateTimeInstance();
            String date_modify = formater.format(lastModDate);
            if(file.isDirectory() && !file.getName().startsWith("UsbDrive")) {
                File[] listFiles = file.listFiles();
                int fileItems = (listFiles != null) ? listFiles.length : 0;

                String numberOfItem = String.valueOf(fileItems);
                if(fileItems == 0)
                    numberOfItem = numberOfItem + " item";
                else
                    numberOfItem = numberOfItem + " items";

                //String formated = lastModDate.toString();
                directories.add(new Item(file.getName(), numberOfItem, date_modify, file.getAbsolutePath(), "directory_icon"));
            } else if (file.isFile() && !file.getName().startsWith("UsbDrive")) { //einai arxeio
                files.add(new Item(file.getName(), file.length() + " Byte", date_modify, file.getAbsolutePath(), "file_icon"));
            }
        }
        Collections.sort(directories);
        Collections.sort(files);
        directories.addAll(files);
Log.d("--->Current Directory", directory.getName());
        if(!directory.getName().equalsIgnoreCase(START_PATH)) //"sdcard"
            directories.add(0, new Item("..", "Parent Directory", "", directory.getParent(), "directory_up"));

        adapter = new FileArrayAdapter(FileChooser.this, R.layout.file_view, directories);
        setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        // TODO Auto-generated method stub
        super.onListItemClick(l, v, position, id);

        Item item = adapter.getItem(position);
        if (item.getPath() != null) {
Log.d(FileChooser.class.getName(), "Item Path: " + item.getPath());
            if (item.getImage().equalsIgnoreCase("directory_icon") ||
                    item.getImage().equalsIgnoreCase("directory_up")) {
                currentDir = new File(item.getPath());
                fill(currentDir);
            } else {
                onFileClick(item);
            }
        } else {
            Toast.makeText(this, "Den exei allo epipedo pio panw", Toast.LENGTH_SHORT).show();
        }
    }
    private void onFileClick(Item item) {
        //Toast.makeText(this, "Folder Clicked: "+ currentDir, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent();
        intent.putExtra("GetPath",currentDir.toString());
        intent.putExtra("GetFileName", item.getName());
        setResult(RESULT_OK, intent);
        finish();
    }
}