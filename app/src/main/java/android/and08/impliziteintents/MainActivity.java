package android.and08.impliziteintents;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.os.Environment;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    final int REQUEST_CALL_PHONE = 0;
    final int REQUEST_IMAGE_CAPTURE_AND_WRITE_EXTERNAL_STORAGE = 1;
    String[] PERMISSIONS = {
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA
    };
    private Intent callIntent;
    private Intent takePictureIntent;
    static final String imageFilePath = Environment.getExternalStorageDirectory().getPath() + "/ImpliziteIntents/picture.png";
    Bitmap imageBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        //AND08D S.46 Auf.2.2.
        //Cast the image into a Bitmap as it is said in Auf.2.2. S.46 AND08D, in order to then save
        //it into an InstsanceState. In this way, we are able to recover the image in the method
        //onRestoureInstanceState when the activity is created again after having rotated the phone
        Drawable imageDrawable =  ((ImageView) findViewById(R.id.imageview_thumbnail)).getDrawable();
        if (imageDrawable != null){
            BitmapDrawable imageBitmapDrawable = (BitmapDrawable) imageDrawable;
            Bitmap imageBitmap = imageBitmapDrawable.getBitmap();
            outState.putParcelable("image", imageBitmap);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);
        Bitmap imageBitmap = savedInstanceState.getParcelable("image");
        if (imageBitmap != null) ((ImageView) findViewById(R.id.imageview_thumbnail)).setImageBitmap(imageBitmap);
    }

    public void onButtonCallClick(View view) {
        //define an implicit intent to call the number 012345 6789
        callIntent = new Intent(Intent.ACTION_CALL);
        //ATTENTION! With the intent ACTION_DIAL it is also possible to make a phone call, in this
        //case the dialer will be called before making the phone call
        //callIntent = new Intent(Intent.ACTION_DIAL);
        //The intent ACTION_CALL needs a telephone number as a uri, in order to be able
        //to make the phone call. If we don't set it in with the setData method of the implicit
        //intent, there will be no phone call done
        callIntent.setData(Uri.parse("tel: 012345 6789"));
        //check if there is any app installed that enables phone calls
        if (callIntent.resolveActivity(getPackageManager()) != null) {
            //Check if the permission to make phone calls was already granted
            if (hasPermissions(this, new String [] {Manifest.permission.CALL_PHONE})) {
                //Permissions were already granted -> start implicit intent (phone call)
                startActivity(callIntent);
            }else{
                //Permissions were NOT already granted -> ask for permissions on runtime
                //But first give an explanation why, on a toast.
                //if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CALL_PHONE)) {
                //    Toast.makeText(this,"Calling permission is needed order for the app to call", Toast.LENGTH_LONG).show();
                //}
                requestPermissions(new String[]{ Manifest.permission.CALL_PHONE}, REQUEST_CALL_PHONE);
            }
        }else{
            //there is no app installed that enables phone calls
            Toast.makeText(this, "Keine geeignete Anwendung installiert", Toast.LENGTH_LONG).show();
        }
    }

    //This is the callback methode of the implicit event started with startActivityForResult(). This
    //method is called by android when the user completes the task of taking a photo and accepting it
    //as valid on the ok button of the camera app.
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE_AND_WRITE_EXTERNAL_STORAGE && resultCode == RESULT_OK) {
            /*
            //If we didnt pass an uri using the method putExtra in the method onButtonCameraClick(),
            //with the location where we want to save the picture, than a thumbnail of the foto
            //is returned in the bundle extras, and we can use it to se
            Bundle extras = data.getExtras();
            Bitmap image = (Bitmap) extras.get("data");
            ((ImageView) findViewById(R.id.imageview_thumbnail)).setImageBitmap(image);
            */

            //If we passed an uri using the method putExtra in the method onButtonCameraClick(),
            //with the location where we want to save the picture, then no thumbnail is returned
            //in a bundle as extra. So we set the image to the ImageView like this:
            imageBitmap = BitmapFactory.decodeFile(imageFilePath);
            ((ImageView) findViewById(R.id.imageview_thumbnail)).setImageBitmap(imageBitmap);

            //Save the bitmap into an InstanceState in order for the image in the ImageView not
            //to be deleted when the mobile phone is rotated:

        }
    }

    //This is callback method is called by android when the users accepts or declines a new
    //premission dialog. That means, every time we use requestPermissions in order to let the user
    //decie that on runtime, we MUST override the callback method onRequestPermissionResult.
    //In the implementation of the overriden methode we define what happens when the user
    //accepts or declines the given permission.
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == REQUEST_CALL_PHONE) {
            //If request is cancelled, the result arrays are empty
            //In case the arrays are not empty, check if the user granted the permission
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Permission was granted, we can now do the phone call
                startActivity(callIntent);
            }
        } else if (requestCode == REQUEST_IMAGE_CAPTURE_AND_WRITE_EXTERNAL_STORAGE){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Permission was granted, we can now make the photo
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE_AND_WRITE_EXTERNAL_STORAGE);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void onButtonCameraClick(View view) {
        //These next two lines solve this issue when trying to get access to the external storage:
        //www.stackoverflow.com/questions/48117511
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        //Show the path on the log, where the picture will be saved
        Log.e(getClass().getSimpleName(), "imageFilePath = " + imageFilePath);
        //First check if the External Storage is available. (When, for example, the Smartphone is
        //connected to a computer with the USB Cable, the External Storage is not available).
        if(!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
            Toast.makeText(this, "External storage nicht verfügbar", Toast.LENGTH_LONG).show();
            return;
        }
        //AND08D S.46 Aufg.2.3.->  Create subfolder with the name of the app in the EXTERNAL STORAGE
        File publicAppDirectory = new File(Environment.getExternalStorageDirectory().getPath(), "ImpliziteIntents");
        publicAppDirectory.mkdirs();
        //create intent
        takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //putting as Extra the path where we want to save the picture, otherwise a thumbnail of the
        //photo will be created (AND08D S.41)
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.parse("file://" + imageFilePath));
        if (takePictureIntent.resolveActivity(getPackageManager()) != null){
            //In case there is an app for taking photos, start it as a implicit event
            //Unlike the phone call implicit event, here we need to come back to our activity
            //after the action in the external app was done (making a photo in this case)
            //Therefore, we start the implicit event with startActivityForResult(), instead of with
            // a simple startActivity()
            //As we saw in AND05D Abschn 3.2.2, in case of making use of startActivtyForResult(),
            //it is necessary to implement a call back method "onActivityResult()" to which
            //the activity comes back after taking the photo

            //Check if the permission to use the camera was already granted
            if (hasPermissions(this, PERMISSIONS)){
                //Permissions were already granted -> start implicit intent (take photo)
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE_AND_WRITE_EXTERNAL_STORAGE);
            }else{
                //Permissions were NOT already granted -> ask for permissions on runtime
                //But first give an explanation why, on a toast.
                //if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                //    Toast.makeText(this,"Camera permission is needed order for the app to take photos", Toast.LENGTH_LONG).show();
                //}
                requestPermissions(PERMISSIONS, REQUEST_IMAGE_CAPTURE_AND_WRITE_EXTERNAL_STORAGE);
            }

        }else{
            //there is no app installed that enables making photos
            Toast.makeText(this, "Keine geeignete Anwendung installiert", Toast.LENGTH_LONG).show();
        }
    }

    public void onButtonEmailClick(View view) {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        //MIME Type was changed from text/plain to image/jpg in order to be able to send an attached picture to the email
        //emailIntent.setType("text/plain");
        emailIntent.setType("image/jpg");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {"javier@javiergonzalez.de"});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Este es mi asunto de prueba con archivo adjunto");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Este es mi contenido del email de prueba con archivo adjunto");
        emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + imageFilePath));
        startActivity(Intent.createChooser(emailIntent, "Enviar email"));
    }

    public static boolean hasPermissions(Context context, String permissions[]) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
}
