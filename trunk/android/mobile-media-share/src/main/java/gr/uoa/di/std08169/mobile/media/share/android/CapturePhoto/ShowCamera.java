package gr.uoa.di.std08169.mobile.media.share.android.CapturePhoto;

import java.io.IOException;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class ShowCamera extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder surfaceHolder;
    private Camera camera;

    public ShowCamera(final Context context, final Camera camera) {
        super(context);
        this.camera = camera;
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);//gia palies ekdoseis < 3.0
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try   {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch (IOException e) {
            Log.d(ShowCamera.class.getName(), "Error setting camera preview: " + e.getMessage());
        }
    }

    //otan peristrefetai h othonh xeirizetai to preview
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (surfaceHolder.getSurface() == null)
            return;
        try {
            camera.stopPreview();
        } catch (final Exception e) {
            e.printStackTrace();
        }

        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        } catch (final IOException e) {
            Log.d(ShowCamera.class.getName(), "Error starting camera preview: " + e.getMessage());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder arg0) {
        //apodesmeush kameras
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }
}
