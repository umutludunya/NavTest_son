package com.app.onal.navtest;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfilePageFragment extends Fragment {

    private RecyclerView mAdList;
    private LinearLayoutManager mLayoutManager;

    private DatabaseReference mAdRef;
    private DatabaseReference mUserRef;
    private FirebaseAuth mAuth;
    private Query mQuery;

    private TextView mProfileName;
    private TextView mProfileBadge;
    private TextView mProfileAgree;
    private TextView mProfileDisagree;
    private TextView mProfileRatio;
    private CircleImageView mProfileImage;

    private FloatingActionButton fab;

    private Activity mActivity;

    public ProfilePageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdRef = FirebaseDatabase.getInstance().getReference().child("Ads");
        mAdRef.keepSynced(true);
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mUserRef.keepSynced(true);
        mAuth = FirebaseAuth.getInstance();

        mQuery = mAdRef.orderByChild("uid").equalTo(mAuth.getCurrentUser().getUid());



    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Profile Page");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile_page, container, false);

        mAdList = (RecyclerView) view.findViewById(R.id.ad_list_profile);
        mAdList.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this.getActivity());
        Log.d("debugMode", "The application stopped after this");
        mAdList.setLayoutManager(mLayoutManager);

        mProfileName = (TextView) view.findViewById(R.id.tvProfileName);
        mProfileBadge = (TextView) view.findViewById(R.id.tvProfileBadge);
        mProfileAgree = (TextView) view.findViewById(R.id.tvProfileAgree);
        mProfileDisagree = (TextView) view.findViewById(R.id.tvProfileDisagree);
        mProfileRatio = (TextView) view.findViewById(R.id.tvProfileRatio);
        mProfileImage = (CircleImageView) view.findViewById(R.id.imageViewProfile);

        mUserRef.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String profile_name = dataSnapshot.child("name").getValue(String.class) + " " +
                        dataSnapshot.child("surname").getValue(String.class);
                String profile_badge = dataSnapshot.child("badge").getValue(String.class);
                String profile_agree = dataSnapshot.child("agree").getValue(String.class) + " Agree";
                String profile_disagree = dataSnapshot.child("disagree").getValue(String.class) + " Disagree";
                String profile_ratio = "%" + dataSnapshot.child("ratio").getValue(String.class);
                String profile_image = dataSnapshot.child("image").getValue(String.class);

                mProfileName.setText(profile_name);
                mProfileBadge.setText(profile_badge);
                mProfileAgree.setText(profile_agree);
                mProfileDisagree.setText(profile_disagree);
                mProfileRatio.setText(profile_ratio);

                Picasso.with(getActivity()).load(profile_image).fit().centerCrop().into(mProfileImage);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                       // .setAction("Action", null).show();
                startActivity(new Intent(getActivity(),PostAdActivity.class));
            }
        });
        fab.setImageResource(R.drawable.ic_add_white_24dp);


        mProfileImage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                PopupMenu popup = new PopupMenu(getActivity(),mProfileImage);
                popup.getMenuInflater().inflate(R.menu.popup_menu,popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        startActivity(new Intent(getActivity(),ProfileSettingsActivity.class));

                        return true;
                    }
                });

                popup.show();
                return true;
            }
        });



        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }else if(id == R.id.action_profile_settings){
            Toast.makeText(getContext(),"TIKLANDI" , Toast.LENGTH_LONG).show();
            startActivity(new Intent(getActivity(),PostAdActivity.class));
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Ad,MainFragment.BlogViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Ad, MainFragment.BlogViewHolder>(
                Ad.class,
                R.layout.ad_row,
                MainFragment.BlogViewHolder.class,
                mQuery
        ) {
            @Override
            protected void populateViewHolder(MainFragment.BlogViewHolder viewHolder, Ad model, int position) {

                final String ad_key = getRef(position).getKey(); // ad'in id sini aldı

                viewHolder.setDesc(model.getDescription());
                viewHolder.setCategory(model.getCategory());
                viewHolder.setImage(getContext() ,model.getHeadImage());

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent adpageIntent = new Intent(getActivity(),AdPageActivity.class);
                        Bundle extras = new Bundle();
                        extras.putString("ad_key",ad_key);
                        extras.putString("EXTRA_PASSWORD","my_password");
                        adpageIntent.putExtras(extras);
                        startActivity(adpageIntent);


                    }
                });
            }
        };

        mAdList.setAdapter(firebaseRecyclerAdapter);
    }
}
