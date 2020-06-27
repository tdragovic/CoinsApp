package com.example.coinsapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.biometrics.BiometricPrompt;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.Layout;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.ActionProvider;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ActionMenuView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.view.ActionProvider;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ShareActionProvider;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.MenuCompat;
import androidx.core.view.MenuItemCompat;


import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import android.util.Base64;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class CameraActivity extends AppCompatActivity {

    private Button btnTakePhoto;
    private TextureView textureView;

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static{
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private String cameraId;
    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSessions;
    private CaptureRequest.Builder captureRequestBuilder;
    private Size imageDimensions;

    private File file;
    private static final int REQUEST_CAMERA_PERMISSION = 101;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundTread;

    private String clientType;
    private Boolean auth;
    private boolean guestUserFields;
    private boolean googleUserFields;
    private Uri googleUserProfilePictureUri;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        toolbar= (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Intent clientIntent = getIntent();
        clientType = clientIntent.getExtras().getString("accountType");
        auth = clientIntent.getExtras().getBoolean("auth");

        guestUserFields = false;
        googleUserFields = false;

        switch(clientType){
            case "GOOGLE":
                GoogleSignInAccount currentUser = GoogleSignIn.getLastSignedInAccount(this);
                googleUserProfilePictureUri = currentUser.getPhotoUrl();
                if(currentUser == null){
                    auth = false;
                }else{
                    auth = true;
                    googleUserFields=true;
                }
                break;
            case "GUEST":
                guestUserFields = true;
                auth = true;
                break;
        }

        if(auth == false){
            Intent i = new Intent( CameraActivity.this, ErrorActivity.class);
            i.putExtra("clientType",clientType);
            i.putExtra("auth", auth);
            i.putExtra("message", "Error with auth. Please check your account settings.");
            ((Activity)CameraActivity.this).startActivity(i);

        }

        textureView = (TextureView) findViewById(R.id.textureView);
        btnTakePhoto = (Button) findViewById(R.id.btnTakePhoto);

            assert textureView != null;

            textureView.setSurfaceTextureListener(textureListener);

            btnTakePhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        takePicture();
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }
            });

    }

    CameraDevice.StateCallback stateCallBack = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            cameraDevice = camera;
            try {
                createCameraPreview();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
        @Override
        public void onDisconnected(CameraDevice camera) {
            cameraDevice.close();
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };

    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            try {
                openCamera();
            } catch (@SuppressLint("NewApi") CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void openCamera() throws CameraAccessException{

        CameraManager manager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);

        cameraId = manager.getCameraIdList()[0];

        CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);

        StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

        imageDimensions = map.getOutputSizes(SurfaceTexture.class)[0];

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, REQUEST_CAMERA_PERMISSION);
            return;
        }

        manager.openCamera(cameraId, stateCallBack, null);

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void takePicture() throws CameraAccessException{
        if(cameraDevice == null)
            return;

        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());

        Size[] jpegSizes = null;


        jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);

        int width = 640;
        int height = 800;

//        if(jpegSizes !=null && jpegSizes.length > 0){
//            width = jpegSizes[0].getWidth();
//            height = jpegSizes[0].getHeight();
//        }

        System.out.println("Display Width : " + width);
        System.out.println("Display Height : " + height);

        ImageReader reader = ImageReader.newInstance(width,height,ImageFormat.JPEG,1);
        List<Surface> outputSurface = new ArrayList<>(2);

        outputSurface.add(reader.getSurface());

        outputSurface.add(new Surface(textureView.getSurfaceTexture()));
        final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);

        captureBuilder.addTarget(reader.getSurface());
        captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

        int rotation = getWindowManager().getDefaultDisplay().getRotation();

        captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));

        String ts = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String photoFileName = "IMG_" + ts + ".jpeg";
        //If we saving image into device storage
        file = getPhotoFileUri(photoFileName);

        ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onImageAvailable(ImageReader reader) {

                Image image = null;
                try {
                    image = reader.acquireLatestImage();
                    ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                    byte[] bytes = new byte[buffer.capacity()];
                    buffer.get(bytes);
                    //save(bytes);

                   AsyncTask<byte[], Void, String> encoding = new AsyncTask<byte[], Void, String>() {
                        @Override
                        protected String doInBackground(byte[]...args) {

                            String encoded = Base64.encodeToString(args[0], Base64.DEFAULT);
                            return encoded;
                        }

                       @Override
                       protected void onPostExecute(String encoded) {
                           super.onPostExecute(encoded);

                           Intent i = new Intent(CameraActivity.this, CoinInfoActivity.class);
                           i.putExtra("capturedPicture", encoded);
                           i.putExtra("clickedCoin","");
                           i.putExtra("accountType", clientType);
                           i.putExtra("auth", auth);
                           i.putExtra("clickedCoinBitmap",new byte[]{});
                           ((Activity)CameraActivity.this).startActivity(i);

                       }
                   };
                   encoding.execute(bytes);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                finally {
                    {
                        if (image != null) {
                            image.close();
                        }
                    }
                }
            }
        };

        reader.setOnImageAvailableListener(readerListener, mBackgroundHandler);

        final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
            @Override
            public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                super.onCaptureCompleted(session, request, result);

                try {
                    createCameraPreview();
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }
        };

        cameraDevice.createCaptureSession(outputSurface, new CameraCaptureSession.StateCallback() {
            @Override
            public void onConfigured(CameraCaptureSession session) {
                try {
                    session.capture(captureBuilder.build(), captureListener, mBackgroundHandler);
                }catch (CameraAccessException e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onConfigureFailed(CameraCaptureSession session) {

            }
        }, mBackgroundHandler);


    }

    //If we store image in storage
    private File getPhotoFileUri(String fileName) {

        String folderName = "CoinsApp";

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), folderName);

        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            Log.d(folderName, "Failed to create directory");
        }

        File photoFile = new File(mediaStorageDir.getPath() + File.separator + fileName);

        return photoFile;
    }


    private void save(byte[] bytes) throws  IOException{

        OutputStream outputStream = null;

        outputStream = new FileOutputStream(file);
        outputStream.write(bytes);

        outputStream.close();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void createCameraPreview() throws CameraAccessException {

        SurfaceTexture texture = textureView.getSurfaceTexture();
        texture.setDefaultBufferSize(imageDimensions.getWidth(),imageDimensions.getHeight());
        Surface surface = new Surface(texture);

        captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        captureRequestBuilder.addTarget(surface);

        cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
            @Override
            public void onConfigured(CameraCaptureSession session) {
                if(cameraDevice == null)
                    return;
                cameraCaptureSessions = session;
                updatePreview();
            }

            @Override
            public void onConfigureFailed(CameraCaptureSession session) {
                Toast.makeText(CameraActivity.this, "Configuration Changed", Toast.LENGTH_SHORT).show();
            }
        }, null);
    }

    private void updatePreview() {
        if(cameraDevice == null){
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
            return;
        }

        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);

        try {
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
        }catch (CameraAccessException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_CAMERA_PERMISSION){
            if(grantResults[0] == PackageManager.PERMISSION_DENIED){
                Toast.makeText(this, "Sorry, camera permission is necessary!", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        startBackgroundThread();

        if(textureView.isAvailable()){
            try {
                openCamera();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }else{
            textureView.setSurfaceTextureListener(textureListener);
        }
    }

    @Override
    protected void onPause() {
        try {
            stopBackgroundTread();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        super.onPause();
    }

    private void startBackgroundThread() {

        mBackgroundTread = new HandlerThread("Camera Background");
        mBackgroundTread.start();
        mBackgroundHandler = new Handler(mBackgroundTread.getLooper());

    }

    private void stopBackgroundTread() throws InterruptedException{
        mBackgroundTread.quitSafely();

        mBackgroundTread.join();
        mBackgroundTread = null;
        mBackgroundHandler = null;

    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        menu.findItem(R.id.action11).setVisible(false);                             //Camera option hidden like current activity
        menu.findItem(R.id.action1).setVisible(true);                               //All coins option is present for all users


        menu.findItem(R.id.profile_picture_google).setVisible(googleUserFields);    // Google profile image
        menu.findItem(R.id.action12).setVisible(googleUserFields);                  // Library option
        menu.findItem(R.id.action21).setVisible(googleUserFields);                  //Sign out option

        menu.findItem(R.id.active_profile_picture).setVisible(guestUserFields);     //Guest icon
        menu.findItem(R.id.action2).setVisible(guestUserFields);                    //Sign in option


        //MenuItem menuItem = menu.findItem(R.id.profile_picture_google);
        //System.out.println("V2" + menuItem.getItemId());
        //View view = MenuItemCompat.getActionView(menuItem);
        //System.out.println("V3" + view.getTransitionName());
        //CircleImageView profileImageView = view.findViewById(R.id.profileImageView);
        //profileImageView.setImageURI(googleUserProfilePictureUri);
        //System.out.println("IMAGE URI" + profileImageView.getImageAlpha());
        //Picasso.get().load(googleUserProfilePictureUri).into(profileImageView);

        return true;
    };


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        Intent newIntent;

        switch(item.getItemId()){
            case R.id.action1:
                newIntent = new Intent( CameraActivity.this, AllCoinsActivity.class);
                newIntent.putExtra("accountType", clientType);
                newIntent.putExtra("auth", auth);
                ((Activity)CameraActivity.this).startActivity(newIntent);
                return true;
            case R.id.action11:
                newIntent = new Intent( CameraActivity.this, CameraActivity.class);
                newIntent.putExtra("accountType", clientType);
                newIntent.putExtra("auth", auth);
                ((Activity)CameraActivity.this).startActivity(newIntent);
                return true;
            case R.id.action12:
                newIntent = new Intent( CameraActivity.this, MyLibraryActivity.class);
                newIntent.putExtra("accountType", clientType);
                newIntent.putExtra("auth", auth);
                ((Activity)CameraActivity.this).startActivity(newIntent);
                return true;
            case R.id.action2:
                newIntent = new Intent( CameraActivity.this, MainActivity.class);
                newIntent.putExtra("accountType", "");
                newIntent.putExtra("auth", false);
                ((Activity)CameraActivity.this).startActivity(newIntent);
                return true;
            case R.id.action21:
                FirebaseAuth.getInstance().signOut();
                newIntent = new Intent( CameraActivity.this, MainActivity.class);
                newIntent.putExtra("accountType", "");
                newIntent.putExtra("auth", false);
                ((Activity)CameraActivity.this).startActivity(newIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

}
