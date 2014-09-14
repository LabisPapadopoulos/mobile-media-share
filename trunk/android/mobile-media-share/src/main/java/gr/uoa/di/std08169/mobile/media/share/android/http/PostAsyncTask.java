package gr.uoa.di.std08169.mobile.media.share.android.http;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;

import java.io.IOException;
import java.net.URL;

/**
 * @see <href="http://stackoverflow.com/questions/18964288/upload-a-file-through-an-http-form-via-multipartentitybuilder-with-a-progress">upload a file through an http form via multipart-entitybuilder with a progres</href="http://stackoverflow.com/questions/18964288/upload-a-file-through-an-http-form-via-multipartentitybuilder-with-a-progress">
 */
public class PostAsyncTask extends AsyncTask<Void, Void, HttpResponse> {
    private static final String CONTENT_TYPE_HEADER_NAME = "Content-Type";
    private final Context context;
    private final URL url;
    private final HttpEntity httpEntity; /*swma tou post*/
    private final String contentType;


    public PostAsyncTask(final Context context, final URL url, final HttpEntity httpEntity, final String contentType) {
        this.context = context;
        this.url = url;
        this.httpEntity = httpEntity;
        this.contentType = contentType;
    }

    @Override
    protected HttpResponse doInBackground(final Void... _) {
        try {
            final HttpPost request = new HttpPost(url.toString());
            request.setEntity(httpEntity);
            request.addHeader(CONTENT_TYPE_HEADER_NAME, contentType);
            return HttpClient.getClient(context).execute(request);
        } catch (final IOException e) {
            Log.e(PostAsyncTask.class.getName(), "Error connecting to " + url, e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(final HttpResponse _) {}
}
