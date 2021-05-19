package com.flumine.typeone;

import android.Manifest;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.InputConfiguration;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;

import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import androidx.annotation.NonNull;

import static android.graphics.ImageFormat.JPEG;
import static android.hardware.camera2.CameraDevice.TEMPLATE_ZERO_SHUTTER_LAG;

public class PhotoActivity extends BaseActivity {

    List<Surface> surfaces;

    CaptureRequest captureRequest;

    CameraCaptureSession session;

    Handler handler;

    int counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        surfaces = new ArrayList<Surface>();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        HandlerThread mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        handler = new Handler(mBackgroundThread.getLooper());
        SurfaceView surface = (SurfaceView)findViewById(R.id.surface);
        RelativeLayout layout = (RelativeLayout)findViewById(R.id.layout);
        layout.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            int size = Math.min(bottom-top, right-left);
            surface.setLayoutParams(new RelativeLayout.LayoutParams(size, size));
        });
        String cameraId = "1";
        openCamera(cameraId);
        surfaces.clear();
        surfaces.add(surface.getHolder().getSurface());
    }

    private void openCamera(String id) {
        report("openCamera");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.CAMERA},2);
        }
        CameraManager manager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);
        try {
            manager.openCamera(id, cameraDeviceStateCallback, null);
        }
        catch (CameraAccessException e) {

        }
        catch (SecurityException e) {

        }

    }

    /////////////////////////  CALLBACKS  //////////////////////////////////////////////////////////

    CameraDevice.StateCallback cameraDeviceStateCallback = new CameraDevice.StateCallback() {
        @Override public void onOpened(@NonNull CameraDevice camera) {report("onOpened");
            CaptureRequest.Builder builder = null;
            try {
                camera.createCaptureSession(surfaces, cameraCaptureSessionStateCallback, null);
            } catch (Exception e) {
                report(e.getMessage());
            }
            try {
                builder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            } catch (Exception e) {report(e.getMessage());}
            builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
            builder.set(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_AUTO);
            builder.addTarget(surfaces.get(0));
            captureRequest = builder.build();
        }
        @Override public void onDisconnected(@NonNull CameraDevice camera) {report("onDisconnected"); camera.close();}
        @Override public void onError(@NonNull CameraDevice camera, int error) {report("StateCallback.onError #" + error);}
    };

    CameraCaptureSession.StateCallback cameraCaptureSessionStateCallback = new CameraCaptureSession.StateCallback() {
        @Override public void onSurfacePrepared(@NonNull CameraCaptureSession session, @NonNull Surface surface) {
            super.onSurfacePrepared(session, surface);
            report("StateCallback.onConfigured1");
        }
        @Override public void onConfigured(@NonNull CameraCaptureSession session) {report("StateCallback.onConfigured3");
            PhotoActivity.this.session = session;
            try {
                //PhotoActivity.this.session.capture(captureRequest, cameraCaptureSessionCaptureCallback, null);
                PhotoActivity.this.session.setRepeatingRequest(captureRequest, cameraCaptureSessionCaptureCallback, null);
            } catch (Exception e) {
                report ("StateCallback.onConfigured2 " + e.getMessage());
            }
        }
        @Override public void onConfigureFailed(@NonNull CameraCaptureSession session) {report("StateCallback.onConfigureFailed");}
    };

    CameraCaptureSession.CaptureCallback cameraCaptureSessionCaptureCallback = new CameraCaptureSession.CaptureCallback() {
//        @Override public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) { super.onCaptureStarted(session, request, timestamp, frameNumber); report("CaptureCallback.onCaptureStarted");}
//        @Override public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) { super.onCaptureProgressed(session, request, partialResult);report("CaptureCallback.onCaptureProcessed"); }
//        @Override public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) { super.onCaptureCompleted(session, request, result); report("CaptureCallback.onCaptureCompleted");}
//        @Override public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) { super.onCaptureFailed(session, request, failure); report("CaptureCallback.onCaptureFailed");}
//        @Override public void onCaptureSequenceCompleted(@NonNull CameraCaptureSession session, int sequenceId, long frameNumber) { super.onCaptureSequenceCompleted(session, sequenceId, frameNumber); report("CaptureCallback.onCaptureSequenceCompleted");}
//        @Override public void onCaptureSequenceAborted(@NonNull CameraCaptureSession session, int sequenceId) { super.onCaptureSequenceAborted(session, sequenceId); report("CaptureCallback.onCaptureSequenceAborted");}
//        @Override public void onCaptureBufferLost(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull Surface target, long frameNumber) { super.onCaptureBufferLost(session, request, target, frameNumber);report("CaptureCallback.onCaptureBufferLost");}
    };


//    ////////////////////////////////////////////////////////////////////////////////////////////////
//
//    class MyImageAvailableListener implements ImageReader.OnImageAvailableListener {
//        @Override
//        public void onImageAvailable(ImageReader reader) {
//            Image image = null;
//            try {
//                image = reader.acquireLatestImage();
//                Log.d(getClass().getName(), "Bitmap # " + counter++);
//                try {
//                    PhotoActivity.this.session.capture(captureRequest, cameraCaptureSessionCaptureCallback, null);
//                }
//                catch (Exception e) {report ("StateCallback.onConfigured " + e.getMessage());}
//            } catch (Exception e) {
//                e.printStackTrace();
//            } finally {
//                image.close();
//            }
//        }
//    }

    public void addPhoto(View view) {
    }

    private void report (String str) {
        Log.d(getClass().getName(), "CAMERA " + str);
    }

}