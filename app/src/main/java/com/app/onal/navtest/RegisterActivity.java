package com.app.onal.navtest;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private EditText mRegisterName;
    private EditText mRegisterEmail;
    private EditText mRegisterPassword;
    private EditText mRegisterSurname;
    private EditText mRegisterCity;
    private EditText mRegisterPhone;
    private Button mRegisterButton;

    private FirebaseAuth mAuth;
    private DatabaseReference mUserRef;

    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mRegisterName = (EditText) findViewById(R.id.registerName);
        mRegisterEmail = (EditText) findViewById(R.id.registerEmail);
        mRegisterPassword = (EditText) findViewById(R.id.registerPassword);
        mRegisterSurname = (EditText) findViewById(R.id.registerSurname);
        mRegisterCity = (EditText) findViewById(R.id.registerCity);
        mRegisterPhone = (EditText) findViewById(R.id.registerPhone);
        mRegisterButton = (Button) findViewById(R.id.btnRegister);

        mAuth = FirebaseAuth.getInstance();
        mProgress = new ProgressDialog(this);

        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startRegister();
            }
        });
    }

    private void startRegister() {

        final String name = mRegisterName.getText().toString().trim();
        final String surname = mRegisterSurname.getText().toString().trim();
        final String city = mRegisterCity.getText().toString().trim();
        final String phone = mRegisterPhone.getText().toString().trim();
        String email = mRegisterEmail.getText().toString().trim();
        String password = mRegisterPassword.getText().toString().trim();

        if(!TextUtils.isEmpty(name) && !TextUtils.isEmpty(surname) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){
            mProgress.setMessage("Signing in...");
            mProgress.show();

            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if(task.isSuccessful()){

                        String user_id = mAuth.getCurrentUser().getUid();

                        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users");
                        DatabaseReference currentUserDb = mUserRef.child(user_id);
                        currentUserDb.child("name").setValue(name);
                        currentUserDb.child("surname").setValue(surname);
                        if(!TextUtils.isEmpty(city)){
                            currentUserDb.child("city").setValue(city);
                        }else{
                            currentUserDb.child("city").setValue("default");
                        }
                        if(!TextUtils.isEmpty(phone)){
                            currentUserDb.child("phone").setValue(phone);
                        }else{
                            currentUserDb.child("phone").setValue("default");
                        }
                        currentUserDb.child("image").setValue("default");

                        mProgress.dismiss();

                        Intent mainIntent = new Intent(RegisterActivity.this,MainActivity.class);
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(mainIntent);
                    }
                }
            });

        }else{
            Toast.makeText(RegisterActivity.this,"Fill all necessary area",Toast.LENGTH_LONG).show();
        }

    }
}
