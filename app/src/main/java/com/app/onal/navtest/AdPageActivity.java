package com.app.onal.navtest;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseIndexRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;
import com.viewpagerindicator.CirclePageIndicator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class AdPageActivity extends AppCompatActivity {

    private TextView mAdDesc;
    private Button mPostComment;
    private EditText mPriceRecommend;
    private EditText mComment;

    private DatabaseReference mAdRef;
    private DatabaseReference mCommentRef;
    private DatabaseReference mAgreeRef;
    private DatabaseReference mDisagreeRef;
    private FirebaseAuth mAuth;
    private ProgressDialog mProgress;
    private Query query;

    private RecyclerView mCommentList;
    private LinearLayoutManager mLayoutManager;

    private boolean mAdAgree = false;
    private boolean mAdDisagree = false;
    private boolean isAgree = false;
    private boolean isDisagree = false;

    private String ad_key;
    private String uid;
    private String ad_title;

    private static ViewPager mPager;
    private static int currentPage = 0;
    private static int NUM_PAGES = 0;
    private static String[] IMAGES = new String[5];
    private ArrayList<String> imagesArray = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad_page);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        ad_key = extras.getString("ad_key");
        ad_title = extras.getString("ad_title");

        mAdDesc = (TextView) findViewById(R.id.tvAdPage);
        //mAdImage = (ImageView) findViewById(R.id.imageViewAd);
        mPostComment = (Button) findViewById(R.id.btnPostComment);
        mPriceRecommend = (EditText) findViewById(R.id.commentPriceRecommend);
        mComment = (EditText) findViewById(R.id.commentUser);

        mAdRef = FirebaseDatabase.getInstance().getReference().child("Ads");
        mCommentRef = mAdRef.child(ad_key).child("userComments");
        mAgreeRef = FirebaseDatabase.getInstance().getReference().child("Agrees");
        mDisagreeRef = FirebaseDatabase.getInstance().getReference().child("Disagrees");

        mAuth = FirebaseAuth.getInstance();

        uid = mAuth.getCurrentUser().getUid();

        query = mCommentRef.orderByChild("adid").equalTo(ad_key); //?

        mCommentRef.keepSynced(true);
        mAgreeRef.keepSynced(true);
        mDisagreeRef.keepSynced(true);


        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(ad_title);

        mCommentList = (RecyclerView) findViewById(R.id.comment_list);
        mProgress = new ProgressDialog(this);

        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);

        mCommentList.setHasFixedSize(true);
        mCommentList.setLayoutManager(mLayoutManager);

        mAdRef.child(ad_key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String ad_desc = (String) dataSnapshot.child("description").getValue();
                String img1 = (String) dataSnapshot.child("images").child("img1").getValue();
                String img2 = (String) dataSnapshot.child("images").child("img2").getValue();
                String img3 = (String) dataSnapshot.child("images").child("img3").getValue();
                String img4 = (String) dataSnapshot.child("images").child("img4").getValue();
                String img5 = (String) dataSnapshot.child("images").child("img5").getValue();

                IMAGES[0] = img1;
                IMAGES[1] = img2;
                IMAGES[2] = img3;
                IMAGES[3] = img4;
                IMAGES[4] = img5;
                init();
                mAdDesc.setText(ad_desc);

                //StorageReference httpsReference = mStorage.getReferenceFromUrl(ad.getImage());

                //Picasso.with(AdPageActivity.this).load(ad_image).into(mAdImage);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        mPostComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mProgress.setMessage("Posting comment...");
                mProgress.show();

                String price_recommend = mPriceRecommend.getText().toString();
                String user_comment = mComment.getText().toString();

                final DatabaseReference newComment = mCommentRef.push();
                newComment.child("priceRecommend").setValue(price_recommend);
                newComment.child("comment").setValue(user_comment);
                newComment.child("cuid").setValue(uid);
                newComment.child("adid").setValue(ad_key);
                newComment.child("agree").setValue("0");
                newComment.child("disagree").setValue("0");
                newComment.child("ratio").setValue("0");

                mProgress.dismiss();
                finish();
                startActivity(getIntent());

            }
        });

    }

    private void init() {

        for(int i=0;i<IMAGES.length;i++) {
            if(!IMAGES[i].equals("null")){
                imagesArray.add(IMAGES[i]);
            }
        }
        mPager = (ViewPager) findViewById(R.id.pager);

        mPager.setAdapter(new SlidingImage_Adapter(AdPageActivity.this,imagesArray));

        CirclePageIndicator indicator = (CirclePageIndicator)
                findViewById(R.id.indicator);

        indicator.setViewPager(mPager);


        final float density = getResources().getDisplayMetrics().density;

//Set circle indicator radius
        indicator.setRadius(5 * density);

        NUM_PAGES = imagesArray.size();

        // Auto start of viewpager
       /* final Handler handler = new Handler();
        final Runnable Update = new Runnable() {
            public void run() {
                if (currentPage == NUM_PAGES) {
                    currentPage = 0;
                }
                mPager.setCurrentItem(currentPage++, true);
            }
        };
        Timer swipeTimer = new Timer();
        swipeTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(Update);
            }
        }, 3000, 3000);*/

        // Pager listener over indicator
        indicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                currentPage = position;

            }

            @Override
            public void onPageScrolled(int pos, float arg1, int arg2) {

            }

            @Override
            public void onPageScrollStateChanged(int pos) {

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Comment,BlogViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Comment, BlogViewHolder>(
                Comment.class,
                R.layout.comment_row,
                BlogViewHolder.class,
                query
        ) {

            @Override
            protected void populateViewHolder(final BlogViewHolder viewHolder, final Comment model, final int position) {

                final String comment_key = getRef(position).getKey();

                viewHolder.setComment(model.getComment());
                viewHolder.setPriceRecommend(model.getPriceRecommend());
                viewHolder.setAgree(model.getAgree());
                viewHolder.setDisagree(model.getDisagree());
                viewHolder.setAgreeButton(comment_key);
                viewHolder.setDisagreeButton(comment_key);
                viewHolder.agree = Integer.parseInt(model.getAgree());
                viewHolder.disagree = Integer.parseInt(model.getDisagree());
                viewHolder.ratio = Integer.parseInt(model.getRatio());

                viewHolder.mAgreeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        mAdAgree = true;

                        mAgreeRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (mAdAgree && !isDisagree) {
                                    if (dataSnapshot.child(comment_key).hasChild(uid)) {
                                        mAgreeRef.child(comment_key).child(uid).removeValue();
                                        mAdAgree = false;
                                        isAgree = false;
                                        viewHolder.agree = viewHolder.agree - 1;
                                        viewHolder.setAgree(Integer.toString(viewHolder.agree));
                                        viewHolder.setRatio();
                                        viewHolder.update(model.getComment(),model.getPriceRecommend()
                                                ,Integer.toString(viewHolder.agree),Integer.toString(viewHolder.disagree),
                                                Integer.toString(viewHolder.ratio),comment_key,ad_key,uid);

                                    } else {
                                        mAgreeRef.child(comment_key).child(uid).setValue("xD");
                                        isAgree = true;
                                        mAdAgree = false;
                                        viewHolder.agree = viewHolder.agree + 1;
                                        viewHolder.setAgree(Integer.toString(viewHolder.agree));
                                        viewHolder.setRatio();

                                        viewHolder.update(model.getComment(),model.getPriceRecommend()
                                                ,Integer.toString(viewHolder.agree),Integer.toString(viewHolder.disagree),
                                                Integer.toString(viewHolder.ratio),comment_key,ad_key,uid);
                                    }

                                }
                            }
                            @Override
                            public void onCancelled (DatabaseError databaseError){

                            }
                        });
                    }
                });

                viewHolder.mDisagreeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        mAdDisagree = true;

                        mDisagreeRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(mAdDisagree && !isAgree){

                                    if(dataSnapshot.child(comment_key).hasChild(uid)){
                                        mDisagreeRef.child(comment_key).child(uid).removeValue();
                                        mAdDisagree = false;
                                        isDisagree = false;
                                        viewHolder.disagree = viewHolder.disagree - 1;
                                        viewHolder.setDisagree(Integer.toString(viewHolder.disagree));
                                        viewHolder.setRatio();
                                        viewHolder.update(model.getComment(),model.getPriceRecommend()
                                                ,Integer.toString(viewHolder.agree),Integer.toString(viewHolder.disagree),
                                                Integer.toString(viewHolder.ratio),comment_key,ad_key,uid);

                                    }else{
                                        mDisagreeRef.child(comment_key).child(uid).setValue(":(");
                                        mAdDisagree = false;
                                        isDisagree = true;
                                        viewHolder.disagree = viewHolder.disagree + 1;
                                        viewHolder.setDisagree(Integer.toString(viewHolder.disagree));
                                        viewHolder.setRatio();
                                        viewHolder.update(model.getComment(),model.getPriceRecommend()
                                                ,Integer.toString(viewHolder.agree),Integer.toString(viewHolder.disagree),
                                                Integer.toString(viewHolder.ratio),comment_key,ad_key,uid);

                                    }

                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                });

                viewHolder.setRatio();

            }
        };

        mCommentList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.notifyDataSetChanged();
    }

    public static class BlogViewHolder extends RecyclerView.ViewHolder{

        View mView;
        ImageButton mAgreeButton;
        ImageButton mDisagreeButton;

        DatabaseReference mAgreeRef;
        DatabaseReference mDisagreeRef;
        DatabaseReference mCommentAgreeRef;
        DatabaseReference mCommentDisagreeRef;
        DatabaseReference mDatabase;
        FirebaseAuth mAuth;

        int agree;
        int disagree;
        int ratio;

        public BlogViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            mAgreeButton = (ImageButton) mView.findViewById(R.id.btnAgree);
            mDisagreeButton = (ImageButton) mView.findViewById(R.id.btnDisagree);

            mAgreeRef = FirebaseDatabase.getInstance().getReference().child("Agrees");
            mAuth = FirebaseAuth.getInstance();
            mAgreeRef.keepSynced(true);
            mDisagreeRef = FirebaseDatabase.getInstance().getReference().child("Disagrees");
            mCommentAgreeRef = FirebaseDatabase.getInstance().getReference().child("Comments").child("agree");
            mCommentDisagreeRef = FirebaseDatabase.getInstance().getReference().child("Comments").child("disagree");
            mDatabase = FirebaseDatabase.getInstance().getReference();
            mDisagreeRef.keepSynced(true);


        }
        public void setComment(String comment){
            TextView comment_user = (TextView) mView.findViewById(R.id.tvComment);
            comment_user.setText(comment);
        }
        public void setPriceRecommend(String priceRecommend){
            TextView price_recommend = (TextView) mView.findViewById(R.id.tvPriceRecommend);
            price_recommend.setText(priceRecommend);
        }
        public void setAgreeButton(final String comment_key){

            mAgreeRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if(dataSnapshot.child(comment_key).hasChild(mAuth.getCurrentUser().getUid())){
                        mAgreeButton.setImageResource(R.drawable.ic_thumb_up_red_24dp);

                    }else{
                        mAgreeButton.setImageResource(R.drawable.ic_thumb_up_grey_24dp);
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

        public void setDisagreeButton(final String comment_key){

            mDisagreeRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.child(comment_key).hasChild(mAuth.getCurrentUser().getUid())){
                        mDisagreeButton.setImageResource(R.drawable.ic_thumb_down_red_24dp);
                    }else{
                        mDisagreeButton.setImageResource(R.drawable.ic_thumb_down_grey_24dp);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        public void getAgree(){

            mCommentAgreeRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String numagree = dataSnapshot.getValue(String.class);
                    agree = Integer.parseInt(numagree);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

        public void getDisagree(){

            mCommentDisagreeRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String numdisagree = dataSnapshot.getValue(String.class);
                    disagree = Integer.parseInt(numdisagree);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        public void update(String comment, String priceRecommend, String agree, String disagree, String ratio,String comment_key,String ad_key,String uid) {

            // Create new post at /user-posts/$userid/$postid and at
            // /posts/$postid simultaneously
            Comment updateComment = new Comment(comment, priceRecommend, agree, disagree,ratio,ad_key,uid);
            Map<String, Object> postValues = updateComment.toMap();

            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put("/Comments/" + comment_key, postValues);
            //childUpdates.put(  + key, postValues);

            mDatabase.updateChildren(childUpdates);

        }

        public void setRatio(){

            if(agree == 0 && disagree == 0 ){

                ratio = 0;
            }else{
                ratio = (agree/(agree+disagree))*100;
            }

            TextView comment_ratio = (TextView) mView.findViewById(R.id.tvRatio);
            String comRatio = "%" + Integer.toString(ratio);
            comment_ratio.setText(comRatio);
        }

        public void setImage(Context ctx, String image){
            ImageView user_image = (ImageView) mView.findViewById(R.id.user_image);
            Picasso.with(ctx).load(image).into(user_image);
        }
        public void setAgree(String agree){
            TextView comment_agree = (TextView) mView.findViewById(R.id.tvAgree);
            comment_agree.setText(agree);
        }
        public void setDisagree(String disagree){
            TextView comment_disagree = (TextView) mView.findViewById(R.id.tvDisagree);
            comment_disagree.setText(disagree);

        }
    }

}