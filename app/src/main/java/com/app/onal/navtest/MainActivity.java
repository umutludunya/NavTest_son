package com.app.onal.navtest;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private DatabaseReference mUserRef;
    private DatabaseReference mAdRef;

    private FloatingActionButton fab;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private TextView mNavUsername;
    private TextView mNavEmail;

    private FragmentManager mFragmentManager;
    private FragmentTransaction mFragmentTransaction;

    DrawerLayout mDrawerLayout;
    NavigationView mNavigationView;

    private Query mQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mNavigationView = (NavigationView) findViewById(R.id.navView) ;

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser() == null){
                    Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);
                }else{
                    //user vardır google sign in için aşağıdaki kısmı buraya taşı
                }

            }
        };


        //google sign in yaparken user id farklı bi şekilde aldığı için database e atarken
        //burayı kullanıyorum.
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mAdRef = FirebaseDatabase.getInstance().getReference().child("Ads");
        mUserRef.keepSynced(true);
        mAdRef.keepSynced(true);

        mQuery = mAdRef;

        mUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(mAuth.getCurrentUser().getUid())){
                    DatabaseReference currentUserDb = mUserRef.child(mAuth.getCurrentUser().getUid());

                    String str = mAuth.getCurrentUser().getDisplayName();
                    String[] splitStr = str.split("\\s+");

                    int size = splitStr.length;
                    String userSurname = splitStr[size-1];
                    String userName="";
                    for(int i = 0;i<size-1;i++){
                        userName = userName + splitStr[i];
                    }
                    currentUserDb.child("name").setValue(userName);
                    currentUserDb.child("surname").setValue(userSurname);
                    currentUserDb.child("image").setValue("default");
                    currentUserDb.child("city").setValue("default");
                    currentUserDb.child("phone").setValue("default");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        /**
         * Setup Drawer Toggle of the Toolbar
         */

        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this,mDrawerLayout, toolbar,R.string.app_name,
                R.string.app_name);

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mDrawerToggle.syncState();


       // View header=mNavigationView.getHeaderView(0);

        //displaySelectedScreen(R.id.nav_main);
        mNavigationView.setItemIconTintList(null);
        mNavigationView.setCheckedItem(R.id.nav_main);

       // mNavUsername = (TextView) header.findViewById(R.id.tvNavUsername);
        //mNavEmail = (TextView) header.findViewById(R.id.tvNavEmail);

        if(mAuth.getCurrentUser() != null){

            mUserRef.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String nav_username = dataSnapshot.child("name").getValue(String.class) + " "
                            + dataSnapshot.child("surname").getValue(String.class);
                    String nav_email = mAuth.getCurrentUser().getEmail();

//                    mNavUsername.setText(nav_username);
  //                  mNavEmail.setText(nav_email);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

        mFragmentManager = getSupportFragmentManager();
        mFragmentTransaction = mFragmentManager.beginTransaction();
        mFragmentTransaction.replace(R.id.containerView,new TabLayoutFragment()).commit();




        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                mDrawerLayout.closeDrawers();

                if (menuItem.getItemId() == R.id.nav_profile) {
                    FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.containerView,new SomeTest())
                            .addToBackStack(null)
                            .commit();
                    mNavigationView.setCheckedItem(R.id.nav_profile);
                }

                if (menuItem.getItemId() == R.id.nav_main) {
                    mQuery = mAdRef;
                    FragmentTransaction xfragmentTransaction = mFragmentManager.beginTransaction();
                    xfragmentTransaction.replace(R.id.containerView,new TabLayoutFragment())
                            .addToBackStack(null)
                            .commit();
                    mNavigationView.setCheckedItem(R.id.nav_main);
                }

                if (menuItem.getItemId() == R.id.nav_sign_out) {
                    signout();
                }

                if (menuItem.getItemId() == R.id.nav_contact) {
                    FragmentTransaction xfragmentTransaction = mFragmentManager.beginTransaction();
                    xfragmentTransaction.replace(R.id.containerView,new ContactUsFragment())
                            .addToBackStack(null)
                            .commit();
                    mNavigationView.setCheckedItem(R.id.nav_contact);
                }

                if (menuItem.getItemId() == R.id.nav_info) {
                    FragmentTransaction xfragmentTransaction = mFragmentManager.beginTransaction();
                    xfragmentTransaction.replace(R.id.containerView,new InfoFragment())
                            .addToBackStack(null)
                            .commit();
                    mNavigationView.setCheckedItem(R.id.nav_info);
                }

                if(menuItem.getItemId() == R.id.nav_cat_book){
                    mQuery = mAdRef.orderByChild("category").equalTo("Book");

                }
                if(menuItem.getItemId() == R.id.nav_cat_car){
                    mQuery = mAdRef.orderByChild("category").equalTo("Car");
                    FragmentTransaction xfragmentTransaction = mFragmentManager.beginTransaction();
                    xfragmentTransaction.replace(R.id.containerView,new TabLayoutFragment())
                            .addToBackStack(null)
                            .commit();
                    mNavigationView.setCheckedItem(R.id.nav_cat_car);
                }

                return false;
            }

        });

    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawerLayout);
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStackImmediate();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
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

        }
        return super.onOptionsItemSelected(item);
    }


    private void signout() {
        mAuth.signOut();
        startActivity(new Intent(MainActivity.this,LoginActivity.class));
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    public Query getMyQuery(){
        return mQuery;
    }

}
