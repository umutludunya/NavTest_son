package com.app.onal.navtest;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;
import id.zelory.compressor.Compressor;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;


public class PostAdActivity extends AppCompatActivity {

    //private ImageButton mSelectImage;
    private ImageButton mSelectImage;
    private ImageButton mSelectImage2;
    private ImageButton mSelectImage3;
    private ImageButton mSelectImage4;
    private ImageButton mSelectImage5;
    private EditText mDesc;
    private EditText mTitle;
    private ImageView mVitrin;
    private Button mPostAd;
    private Uri mImageUri = null;
    private static final int GALERY_REQUEST = 1;

    private StorageReference mStorage;
    private ProgressDialog mProgressDialog;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mUserRef;

    private static String category;
    private ArrayList<String> filePaths = new ArrayList<String>();
    private ArrayList<ImageButton> btnUploads = new ArrayList<ImageButton>();
    private ArrayList<Uri> URIS = new ArrayList<Uri>();
    private HashMap<String, String> images = new HashMap<String,String>();

    private File actualImage;
    private File compressedImage;
    private boolean isCategorySelected = false;

    private final String TAG = "PostAdActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_ad);

        //setting storage and database unit
        mStorage = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();

        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid());


        //it will change and retrieve categories from database
        String[] categories = {"Book", "Car", "Estate", "Instrument", "Technology"};
        ListAdapter listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, categories);
        final ListView listViewCategory = (ListView) findViewById(R.id.listViewCategory);
        listViewCategory.setAdapter(listAdapter);

        // colored selected list item for single selection
        listViewCategory.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        listViewCategory.setSelector(android.R.color.holo_red_dark);

        // mSelectImage = (ImageButton) findViewById(R.id.imagebtnPost);
        mDesc = (EditText) findViewById(R.id.textAdDescription);
        mDesc.setInputType(EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE);
        mDesc.setSingleLine(false);
        mTitle = (EditText) findViewById(R.id.textAdTitle);

        mSelectImage = (ImageButton) findViewById(R.id.btnUpload);
        mSelectImage2 = (ImageButton) findViewById(R.id.btnUpload2);
        mSelectImage3 = (ImageButton) findViewById(R.id.btnUpload3);
        mSelectImage4 = (ImageButton) findViewById(R.id.btnUpload4);
        mSelectImage5 = (ImageButton) findViewById(R.id.btnUpload5);

        mVitrin = (ImageView) findViewById(R.id.imageViewVitrin);
        mVitrin.setVisibility(ImageView.GONE);

        btnUploads.add(mSelectImage);
        btnUploads.add(mSelectImage2);
        btnUploads.add(mSelectImage3);
        btnUploads.add(mSelectImage4);
        btnUploads.add(mSelectImage5);

        mPostAd = (Button) findViewById(R.id.btnPostAd);

        mProgressDialog = new ProgressDialog(this);

        listViewCategory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                category = listViewCategory.getItemAtPosition(position).toString();
                isCategorySelected = true;
            }
        });

        filePaths.clear();
        images.clear();

        mSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                chooseImage();

            }
        });

        mSelectImage2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                chooseImage();

            }
        });
        mSelectImage3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                chooseImage();

            }
        });
        mSelectImage4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                chooseImage();

            }
        });
        mSelectImage5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                chooseImage();

            }
        });


        mPostAd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPosting();
            }
        });
    }

    private void chooseImage() {

        FilePickerBuilder.getInstance().setMaxCount(5)
                .setSelectedFiles(filePaths)
                .setActivityTheme(R.style.AppTheme)
                .pickPhoto(PostAdActivity.this);

    }

    private void startPosting() {
        final String desc_value = mDesc.getText().toString().trim();
        final String title_value = mTitle.getText().toString().trim();

        //Description is required.
        if (TextUtils.isEmpty(desc_value)) {
            mDesc.setError("Required!");
            return;
        }

        //Title is required.
        if (TextUtils.isEmpty(title_value)) {
            mTitle.setError("Required!");
            return;
        }

        //At least one image required.
        if (mImageUri == null) {
            Toast.makeText(PostAdActivity.this, "Select at least one image!", Toast.LENGTH_LONG).show();
            return;
        }

        //Category required.
        if (!isCategorySelected) {
            Toast.makeText(PostAdActivity.this,
                    "Select the category!",
                    Toast.LENGTH_LONG).show();
            return;
        }

        // Disable button so there are no multi-posts
        setEditingEnabled(false);
        Toast.makeText(this, "Posting...", Toast.LENGTH_SHORT).show();
        //start posting

        //mProgressDialog.setMessage("Posting your AD...");
        //mProgressDialog.show();

        for (int i = 0; i < URIS.size(); i++) {
            Uri newUri = Uri.parse("file://" + URIS.get(i));

            final String uploadStr = "img" + Integer.toString(i + 1);
            StorageReference filepath = mStorage.child("Ad Images").child(URIS.get(i).getLastPathSegment());

            UploadTask task = filepath.putFile(newUri);

            task.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(PostAdActivity.this,
                            "UPLOAD FAILURE",
                            Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();

                    images.put(uploadStr,downloadUrl.toString());
                }
            });


        }

        for (int k = URIS.size(); k < 5; k++) {
            String child_path = "img" + Integer.toString(k + 1);
            images.put(child_path, "null");
        }

        final String userId = mCurrentUser.getUid();

        mDatabase.child("Users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get user value
                User user = dataSnapshot.getValue(User.class);

                // [START_EXCLUDE]
                if (user == null) {
                    // User is null, error out
                    Log.e(TAG, "User " + userId + " is unexpectedly null");
                    Toast.makeText(PostAdActivity.this,
                            "Error: could not fetch user.",
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Write new post
                    writeNewAd(userId, desc_value, category, title_value, images);
                }

                // Finish this Activity, back to the stream
                setEditingEnabled(true);
                finish();
                // [END_EXCLUDE]
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                // [START_EXCLUDE]
                setEditingEnabled(true);
                // [END_EXCLUDE]
            }
        });

        // [END single_value_read]

        //mProgressDialog.dismiss();

       // startActivity(new Intent(PostAdActivity.this, MainActivity.class));


    }

    private void setEditingEnabled(boolean enabled) {
        mDesc.setEnabled(enabled);
        mTitle.setEnabled(enabled);
        mSelectImage.setEnabled(enabled);
        mSelectImage2.setEnabled(enabled);
        mSelectImage3.setEnabled(enabled);
        mSelectImage4.setEnabled(enabled);
        mSelectImage5.setEnabled(enabled);

        if (enabled) {
            mPostAd.setVisibility(View.VISIBLE);
        } else {
            mPostAd.setVisibility(View.GONE);
        }
    }

    // [START write_fan_out]
    private void writeNewAd(String userId, String desc, String category, String title, HashMap<String, String> images) {
        // Create new post at /user-ads/$userid/$adid and at
        // /ads/$adid simultaneously
        String key = mDatabase.child("Ads").push().getKey();
        Ad ad = new Ad(userId,title,desc,category, images);
        Map<String, Object> postValues = ad.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/Ads/" + key, postValues);
        childUpdates.put("/user-ads/" + userId + "/" + key, postValues);

        mDatabase.updateChildren(childUpdates);
    }
    // [END write_fan_out]


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

       /* if(requestCode == GALERY_REQUEST && resultCode == RESULT_OK){
           //  mImageUri = data.getData();
            // mSelectImage.setImageURI(mImageUri);

            if (data == null) {
                showError("Failed to open picture!");
                return;
            }
            try {
                actualImage = FileUtil.from(this, data.getData());
                compressImage();
                clearImage();
            } catch (IOException e) {
                showError("Failed to read picture data!");
                e.printStackTrace();
            }

        }*/
        URIS.clear();

        switch (requestCode) {
            case FilePickerConst.REQUEST_CODE:

                if (resultCode == RESULT_OK && data != null) {
                    filePaths = data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_PHOTOS);

                    try {
                        int i;
                        Toast.makeText(PostAdActivity.this, "SELECT " + URIS.size(), Toast.LENGTH_LONG).show();
                        for (i = 0; i < filePaths.size(); i++) {
                            mImageUri = Uri.parse(filePaths.get(i));
                            //btnUploads.get(i).setImageURI(uri);
                            actualImage = new File(mImageUri.getPath());
                            compressImage(i);
                            clearImage(i);
                        }
                        if (filePaths.size() != 0)
                            mVitrin.setVisibility(ImageView.VISIBLE);
                        else {
                            mVitrin.setVisibility(ImageView.GONE);
                        }

                        for (int k = i; k < btnUploads.size(); k++) {
                            btnUploads.get(k).setImageResource(R.drawable.default_add);
                        }

                    } catch (Exception e) {
                        showError("Failed to read picture data!");
                        e.printStackTrace();
                    }

                }

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_back, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_back) {
            finish();
        } else if (item.getItemId() == R.id.home) {
            this.finish();
        }

        return super.onOptionsItemSelected(item);
    }

    public void compressImage(final int position) {
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
                            setCompressedImage(position);
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            showError(throwable.getMessage());
                        }
                    });
        }
    }


   /* public void customCompressImage() {
        if (actualImage == null) {
            showError("Please choose an image!");
        } else {
            // Compress image in main thread using custom Compressor
            compressedImage = new Compressor.Builder(this)
                    .setMaxWidth(640)
                    .setMaxHeight(480)
                    .setQuality(75)
                    .setCompressFormat(Bitmap.CompressFormat.WEBP)
                    .setDestinationDirectoryPath(Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_PICTURES).getAbsolutePath())
                    .build()
                    .compressToFile(actualImage);
            setCompressedImage();

        }
    }*/

    private void setCompressedImage(int position) {
        btnUploads.get(position).setImageBitmap(BitmapFactory.decodeFile(compressedImage.getAbsolutePath()));
        mImageUri = Uri.fromFile(compressedImage);
        URIS.add(mImageUri);

    }

    private void clearImage(int position) {
        //actualImageView.setBackgroundColor(getRandomColor());
        btnUploads.get(position).setImageDrawable(null);
        // btnUploads.get(position).setBackgroundColor(getRandomColor());   SET RANDOM BACKGROUND COLOR
        //compressedSizeTextView.setText("Size : -");
    }


    public void showError(String errorMessage) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    private int getRandomColor() {
        Random rand = new Random();
        return Color.argb(100, rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
    }

    /*public String getReadableFileSize(long size) {
        if (size <= 0) {
            return "0";
        }
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }*/
}
