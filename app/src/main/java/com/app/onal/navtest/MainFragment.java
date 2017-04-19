package com.app.onal.navtest;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment {

    private RecyclerView mAdList;
    private LinearLayoutManager mLayoutManager;

    private DatabaseReference mAdRef;
    private DatabaseReference mUserRef;
    private DatabaseReference mCommentRef;
    private FirebaseAuth mAuth;
    private Query mQuery;

    private ProgressDialog mProgress;

    private String uid;
    private String ad_key;

    private Activity mActivity;

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdRef = FirebaseDatabase.getInstance().getReference().child("Ads");
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mCommentRef = mAdRef.child("userComments");
        mCommentRef.keepSynced(true);
        mUserRef.keepSynced(true);
        mAdRef.keepSynced(true);

        mProgress = new ProgressDialog(getActivity());

        mAuth = FirebaseAuth.getInstance();

        Bundle args = getArguments();
        int tab = args.getInt("TAB");

        MainActivity activity = (MainActivity) getActivity();

        if(tab == 0){
            mQuery = activity.getMyQuery();
        }else {

            mQuery = mAdRef.orderByChild("category").equalTo("Estate");

        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main, menu);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }else if(id == R.id.action_logout){
            logout();
        }


        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        mAuth.signOut();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Main Page");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        mAdList = (RecyclerView) view.findViewById(R.id.ad_list);
        mAdList.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this.getActivity());
        Log.d("debugMode", "The application stopped after this");
        mAdList.setLayoutManager(mLayoutManager);

        if(mAuth.getCurrentUser() != null) {
            uid = mAuth.getCurrentUser().getUid();
        }


        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        mProgress.setMessage("Yükleniyor...");
        mProgress.show();

        FirebaseRecyclerAdapter<Ad,BlogViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Ad, BlogViewHolder>(
                Ad.class,
                R.layout.ad_row,
                BlogViewHolder.class,
                mQuery
        ) {
            @Override
            protected void populateViewHolder(BlogViewHolder viewHolder, final Ad model, int position) {

                final String ad_key = getRef(position).getKey(); // ad'in id sini aldı

                viewHolder.setDesc(model.getDescription());
                viewHolder.setCategory(model.getCategory());
                viewHolder.setTitle(model.getTitle());
                viewHolder.setImage(getContext() ,model.getHeadImage());

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent adpageIntent = new Intent(getActivity(),AdPageActivity.class);
                        Bundle extras = new Bundle();
                        extras.putString("ad_key",ad_key);
                        extras.putString("ad_title",model.getTitle());
                        adpageIntent.putExtras(extras);
                        startActivity(adpageIntent);


                    }
                });
            }
        };

        mAdList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.notifyDataSetChanged();
        mProgress.dismiss();
    }


    public static class BlogViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public BlogViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }
        public void setCategory(String category){
            TextView ad_category = (TextView) mView.findViewById(R.id.ad_category);
            ad_category.setText(category);
        }
        public void setDesc(String desc){
            TextView ad_desc = (TextView) mView.findViewById(R.id.ad_desc);
            ad_desc.setText(desc);
        }
        public void setImage(Context ctx, String image){
            ImageView ad_image = (ImageView) mView.findViewById(R.id.ad_image);
            Picasso.with(ctx).load(image).into(ad_image);
        }
        public void setTitle (String title){
            TextView ad_title = (TextView) mView.findViewById(R.id.ad_title);
            ad_title.setText(title);
        }
    }

}
