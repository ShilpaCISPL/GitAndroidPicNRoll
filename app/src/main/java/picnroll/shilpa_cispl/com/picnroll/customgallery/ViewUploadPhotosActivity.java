package picnroll.shilpa_cispl.com.picnroll.customgallery;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

import picnroll.shilpa_cispl.com.picnroll.R;
import picnroll.shilpa_cispl.com.picnroll.navigationFiles.Utility;

public class ViewUploadPhotosActivity extends AppCompatActivity implements View.OnClickListener {

    FloatingActionButton fab;
    private int REQUEST_CAMERA = 0, SELECT_FILE = 1;
    private static int RESULT_LOAD_IMG = 1;

    GridView gridGallery;
    Handler handler;
    GalleryAdapter adapter;

    ImageView imgSinglePick;

    ViewSwitcher viewSwitcher;
    ImageLoader imageLoader;

    //creating reference to firebase storage
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReferenceFromUrl("gs://pick-n-roll.appspot.com");


    DatabaseReference ref ;

    Uri filePath;
    String userId,selectedFolderName,selectedFolderIndex,uniquekey;
    FirebaseUser currentFirebaseUser;
    private Firebase mRef;
    ArrayList<CustomGallery> dataT = new ArrayList<CustomGallery>();
    CustomGallery item = new CustomGallery();
    ArrayList<String> imageKeys = new ArrayList<>();
    ArrayList<String> imageValues = new ArrayList<>();
    ArrayList<String> folderImageValues = new ArrayList<>();
    String extension;
    UUID uidKey;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_upload_photos);
        currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
        userId =  currentFirebaseUser.getUid();
        ref =  FirebaseDatabase.getInstance().getReference();
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);


        selectedFolderName = getIntent().getStringExtra("selectedFolderName");
        selectedFolderIndex = getIntent().getStringExtra("selectedFolderPosition");

        Log.d("tag","folders"+selectedFolderName +selectedFolderIndex );

        initImageLoader();
        init();


        mRef = new Firebase("https://pick-n-roll.firebaseio.com/Files/"+userId+"");

        mRef.addValueEventListener(new com.firebase.client.ValueEventListener() {
            @Override
            public void onDataChange(com.firebase.client.DataSnapshot dataSnapshot) {


                for (com.firebase.client.DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                    imageKeys.add(childDataSnapshot.getKey());
                    imageValues.add(String.valueOf(childDataSnapshot.getValue()));
                    Log.d("tag","images-->"+childDataSnapshot.getKey() +"\n" + String.valueOf(childDataSnapshot.getValue()) );
                }

                for (int a=0; a<imageKeys.size(); a++){
                    if(imageKeys.get(a).contains(selectedFolderName)){

                        folderImageValues.add(imageValues.get(a));
                        Log.d("tag","folderImages-->"+folderImageValues.size());
                    }
                }

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });


    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_view_upload_photo, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add) {
            Intent i = new Intent(Action.ACTION_MULTIPLE_PICK);
            startActivityForResult(i, 200);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initImageLoader() {
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheOnDisc().imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
                .bitmapConfig(Bitmap.Config.RGB_565).build();
        ImageLoaderConfiguration.Builder builder = new ImageLoaderConfiguration.Builder(
                this).defaultDisplayImageOptions(defaultOptions).memoryCache(
                new WeakMemoryCache());

        ImageLoaderConfiguration config = builder.build();
        imageLoader = ImageLoader.getInstance();
        imageLoader.init(config);
    }

    private void init() {

        handler = new Handler();
        gridGallery = (GridView) findViewById(R.id.gridGallery);
        gridGallery.setFastScrollEnabled(true);
        adapter = new GalleryAdapter(getApplicationContext(), imageLoader);
        adapter.setMultiplePick(false);
        gridGallery.setAdapter(adapter);

        viewSwitcher = (ViewSwitcher) findViewById(R.id.viewSwitcher);
        viewSwitcher.setDisplayedChild(1);

        imgSinglePick = (ImageView) findViewById(R.id.imgSinglePick);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            adapter.clear();


        } else if (requestCode == 200 && resultCode == Activity.RESULT_OK) {
            String[] all_path = data.getStringArrayExtra("all_path");

            for (String string : all_path) {


                int lastDot = string.lastIndexOf('/');
                if (lastDot == -1) {
                    // No dots - what do you want to do?
                } else {
                     extension = string.substring(lastDot);

                }


                item.sdcardPath = string;
                 filePath = Uri.fromFile(new File(string));
                if(filePath != null) {
                    // pd.show();

                    StorageReference childRef = storageRef.child("Files").child(userId).child(uniquekey).child(extension);
                    //uploading the image
                    UploadTask uploadTask = childRef.putFile(filePath);

                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //  pd.dismiss();
                            Uri downloadUrl = taskSnapshot.getDownloadUrl();
                            ref.child("Files").child(userId).child(selectedFolderIndex+selectedFolderName+userId+String.valueOf(UUID.randomUUID())).setValue(downloadUrl.toString());
                            Toast.makeText(ViewUploadPhotosActivity.this, "Upload successful", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //  pd.dismiss();
                            Toast.makeText(ViewUploadPhotosActivity.this, "Upload Failed -> " + e, Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                dataT.add(item);
            }
            adapter.addAll(dataT);
        }

        else if(requestCode == REQUEST_CAMERA) {

                onCaptureImageResult(data);

        }
    }


        @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Utility.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                      //  cameraIntent();

                } else {
                    //code for deny
                }
                break;
        }
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.fab) {
            boolean result = Utility.checkPermission(ViewUploadPhotosActivity.this);
            if (result)
                cameraIntent();
        }


    }

    private void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }


    private void onCaptureImageResult(Intent data) {
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

        File destination = new File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis() + ".jpg");

        item.sdcardPath = String.valueOf(destination);
        dataT.add(item);
        adapter.addAll(dataT);

        filePath = data.getData();
        Log.d("tag","filepath"+filePath +data);

        int lastDot = String.valueOf(destination).lastIndexOf('/');
        if (lastDot == -1) {
            // No dots - what do you want to do?
        } else {
            extension = String.valueOf(destination).substring(lastDot);
            Log.d("tag","camera extension"+extension);
        }
        if(filePath != null) {
            // pd.show();

            StorageReference childRef = storageRef.child("Files").child(userId).child(String.valueOf(UUID.randomUUID())).child(extension);

            //uploading the image
            UploadTask uploadTask = childRef.putFile(filePath);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                  //  ref.child("Files").child(userId).child(uniquekey).setValue(downloadUrl.toString());

                    ref.child("Files").child(userId).child(selectedFolderIndex+selectedFolderName+userId+String.valueOf(UUID.randomUUID())).setValue(downloadUrl.toString());
                    //  pd.dismiss();
                    Toast.makeText(ViewUploadPhotosActivity.this, "Upload successful", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //  pd.dismiss();
                    Toast.makeText(ViewUploadPhotosActivity.this, "Upload Failed -> " + e, Toast.LENGTH_SHORT).show();
                }
            });
        }


    }


}


