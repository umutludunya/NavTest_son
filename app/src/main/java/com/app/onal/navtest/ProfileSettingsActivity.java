package com.app.onal.navtest;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import id.zelory.compressor.Compressor;
import id.zelory.compressor.FileUtil;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class ProfileSettingsActivity extends AppCompatActivity {

    private EditText mSettingsName;
    private EditText mSettingsSurname;
    private ImageButton mSettingsImage;
    private Button mSaveBtn;

    private Uri mImageUri = null;
    private static final int GALERY_REQUEST = 1;

    private File actualImage;
    private File compressedImage;

    private DatabaseReference mUserRef;
    private DatabaseReference mDatabase;
    private StorageReference mStorage;
    private FirebaseAuth mAuth;

    private ProgressDialog mProgress;
    private String profile_name;
    private String profile_surname;
    private String profile_image;
    private String profile_agree;
    private String profile_disagree;
    private String profile_ratio;
    private String profile_badge;
    private String profile_city;
    private String profile_phone;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_settings);

        mSettingsName = (EditText) findViewById(R.id.settingsName);
        mSettingsSurname = (EditText) findViewById(R.id.settingsSurname);
        mSettingsImage = (ImageButton) findViewById(R.id.settingsImage);
        mSaveBtn = (Button) findViewById(R.id.btnSaveSettings);

        mStorage = FirebaseStorage.getInstance().getReference();
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mUserRef.keepSynced(true);
        mDatabase.keepSynced(true);
        mAuth = FirebaseAuth.getInstance();

        mUserRef.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                profile_name = dataSnapshot.child("name").getValue(String.class);
                profile_surname = dataSnapshot.child("surname").getValue(String.class);
                profile_image = dataSnapshot.child("image").getValue(String.class);
                profile_agree = dataSnapshot.child("agree").getValue(String.class);
                profile_disagree = dataSnapshot.child("disagree").getValue(String.class);
                profile_ratio = dataSnapshot.child("ratio").getValue(String.class);
                profile_badge = dataSnapshot.child("badge").getValue(String.class);
                profile_city = dataSnapshot.child("city").getValue(String.class);
                profile_phone = dataSnapshot.child("phone").getValue(String.class);

                mSettingsName.setText(profile_name);
                mSettingsSurname.setText(profile_surname);

                if(profile_image == "default"){

                }else{
                    Picasso.with(ProfileSettingsActivity.this).load(profile_image).fit().centerCrop().into(mSettingsImage);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mProgress = new ProgressDialog(this);

        mSettingsImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galeryIntent = new Intent();
                galeryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galeryIntent.setType("image/*");
                startActivityForResult(galeryIntent,GALERY_REQUEST);
            }
        });

        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mProgress.setMessage("Saving changes...");
                mProgress.show();

                profile_name = mSettingsName.getText().toString();
                profile_surname = mSettingsSurname.getText().toString();

                if(mImageUri != null){
                    StorageReference filepath = mStorage.child("Profile Images").child(mImageUri.getLastPathSegment());
                    StorageReference deletepath = mStorage.child("Profile Images").child(profile_image);
                    deletepath.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            //success durumunda burda bişeyler
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //error handle burda yapılacak
                        }
                    });
                    filepath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            final Uri downloadUrl = taskSnapshot.getDownloadUrl();
                            profile_image = downloadUrl.toString();

                        }
                    });

                    update(profile_name,profile_surname,profile_image,profile_agree,profile_disagree,
                            profile_ratio,profile_badge,profile_city,profile_phone);

                }

                mProgress.dismiss();
                startActivity(new Intent(ProfileSettingsActivity.this,MainActivity.class));
            }
        });



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALERY_REQUEST && resultCode == RESULT_OK){

            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);

            if (data == null) {
                showError("Failed to open picture!");
                return;
            }
            try {
                actualImage = FileUtil.from(this, imageUri);
                compressImage();
                clearImage();
            } catch (IOException e) {
                showError("Failed to read picture data!");
                e.printStackTrace();
            }
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                Uri mImageUri = result.getUri();

                mSettingsImage.setImageURI(mImageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void update(String name, String surname, String image, String agree, String disagree, String ratio, String badge, String city, String phone) {

        // Create new post at /user-posts/$userid/$postid and at
        // /posts/$postid simultaneously
        String uid = mAuth.getCurrentUser().getUid();
        User updateUser = new User(name,surname,image,agree,disagree,ratio,badge,city,phone);
        Map<String, Object> postValues = updateUser.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/Users/" + uid, postValues);
        //childUpdates.put(  + key, postValues);

        mDatabase.updateChildren(childUpdates);

    }

    public void compressImage() {
        if (actualImage == null) {
            showError("Please choose an image!");
        } else {

            // Compress image in main thread
            // compressedImage = Compressor.getDefault(this).compressToFile(actualImage);
            //setCompressedImage();

            // Compress image to bitmap in main thread
            //mSelectImage.setImageBitmap(Compressor.getDefault(this).compressToBitmap(actualImage));

            // Compress image using RxJava in background thread
            Compressor.getDefault(this)
                    .compressToFileAsObservable(actualImage)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<File>() {
                        @Override
                        public void call(File file) {
                            compressedImage = file;
                            setCompressedImage();
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            showError(throwable.getMessage());
                        }
                    });
        }
    }


    private void setCompressedImage() {
        mSettingsImage.setImageBitmap(BitmapFactory.decodeFile(compressedImage.getAbsolutePath()));
        mImageUri = Uri.fromFile(compressedImage);
        // compressedSizeTextView.setText(String.format("Size : %s", getReadableFileSize(compressedImage.length())));

        //Toast.makeText(this, "Compressed image save in " + compressedImage.getPath(), Toast.LENGTH_LONG).show();
        Log.d("Compressor", "Compressed image save in " + compressedImage.getPath());
    }


    private void clearImage() {
        //actualImageView.setBackgroundColor(getRandomColor());
        mSettingsImage.setImageDrawable(null);
        mSettingsImage.setBackgroundColor(getRandomColor());
        //compressedSizeTextView.setText("Size : -");
    }

    private int getRandomColor() {
        Random rand = new Random();
        return Color.argb(100, rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
    }

    public void showError(String errorMessage) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }
}
