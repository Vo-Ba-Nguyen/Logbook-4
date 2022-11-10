package com.example.logbook4;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.PackageManagerCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_PERM_CODE = 101;
    private static final int CAMERA_CODE_OPEN_REQUEST = 102;

    ImageView imageView;
    EditText addLink_txt;
    Button back_button, next_button, add_link_button, cameraBtn;
    String currentImagePath;
    ArrayList<String> arrayList;
    int index;
    Database db;
    String url;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.imageView);
        back_button = findViewById(R.id.back_button);
        next_button = findViewById(R.id.next_button);
        add_link_button = findViewById(R.id.add_link_button);
        addLink_txt = findViewById(R.id.addLink_txt);
        cameraBtn = findViewById(R.id.cameraBtn);

        add_link_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(IsValidUrl(addLink_txt.getText().toString().trim())){
                    Database db = new Database(MainActivity.this);
                    db.addLink(addLink_txt.getText().toString().trim());


                    Glide.with(getApplicationContext())
                            .load(addLink_txt.getText().toString().trim())
                            .placeholder(R.drawable.ic_baseline_image_24).into(imageView);
                    Toast.makeText(MainActivity.this, "Add Successfully!!!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "URL not Valid", Toast.LENGTH_SHORT).show();
                }

            }
        });

         db = new Database(MainActivity.this);

 //      db.addLink("https://images.vexels.com/media/users/3/263340/isolated/preview/92d75abef1c7523630339a2793eba5eb-pizza-color-stroke-slice.png");
 //      db.addLink("https://img.freepik.com/premium-psd/fresh-vegetable-pepperoni-mushroom-pizza-transparent-background_670625-101.jpg?w=2000");
  //     db.addLink("https://toppng.com/uploads/preview/pizza-11527809195frqp1qz4zd.png");

        Glide.with(getApplicationContext())
                .load(loadLastImg())
                .placeholder(R.drawable.ic_baseline_image_24).into(imageView);


        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Glide.with(getApplicationContext())
                        .load(back_button())
                        .placeholder(R.drawable.ic_baseline_image_24).into(imageView);
            }
        });

        next_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Glide.with(getApplicationContext())
                        .load(next_button())
                        .placeholder(R.drawable.ic_baseline_image_24).into(imageView);
            }
        });

        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                askCameraPermission();
            }
        });
    }

    private void askCameraPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.CAMERA}, CAMERA_PERM_CODE);
        }else {
            dispatchTakePictureIntent();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_CODE_OPEN_REQUEST) {
            if(resultCode == Activity.RESULT_OK) {
                File f = new File(currentImagePath);
                imageView.setImageURI(Uri.fromFile(f));
                Log.d("tag", "Absolute Url of Image is " + Uri.fromFile(f));
            }
        }
    }


    private File createImageFile() throws IOException{
        //create name of file's image
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageNameFile = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageNameFile,
                ".jpg",
                storageDir
        );
        currentImagePath = image.getAbsolutePath();
        db.addLink(currentImagePath);
        return image;
    }



    private void dispatchTakePictureIntent(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePictureIntent.resolveActivity(getPackageManager()) != null){
            File photoFile = null;
            try{
                photoFile = createImageFile();
            } catch (IOException exc){

            }
            if(photoFile != null){
                Uri photoURI = FileProvider.getUriForFile(this, "com.example.android.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAMERA_CODE_OPEN_REQUEST);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERM_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // openCamera();
            } else {
                Toast.makeText(this, "Camera Permission is Required to Use camera", Toast.LENGTH_SHORT).show();
            }
        }
    }

    String loadLastImg(){
        Database db = new Database(MainActivity.this);
        Cursor cursor = db.getAllLink();

        cursor.moveToLast();
        url = cursor.getString(1);
        index = cursor.getPosition();
        return url;
    }

    String next_button(){
        Database db = new Database(MainActivity.this);
        Cursor cursor = db.getAllLink();
        cursor.moveToLast();
        int last = cursor.getPosition();

        if(index == last){
            cursor.moveToFirst();
            index = cursor.getPosition();

        } else {
            index++;
            cursor.moveToPosition(index);

        }
        url = cursor.getString(1);
        return url;
    }

    String back_button(){
        Database db = new Database(MainActivity.this);
        Cursor cursor = db.getAllLink();


        if(index == 0){
            cursor.moveToLast();
            index = cursor.getPosition();
        } else {
            index--;
            cursor.moveToPosition(index);
        }
        url = cursor.getString(1);
        return url;
    }

    public static boolean IsValidUrl(String urlString) {
        try {
            URL url = new URL(urlString);
            return URLUtil.isValidUrl(urlString) && Patterns.WEB_URL.matcher(urlString).matches();
        } catch (MalformedURLException ignored) {
        }
        return false;
    }

}