package com.naveen.hitch;

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

public class SignupActivity extends AppCompatActivity {

    private EditText firstName, lastName, signUpEmail, signUpPhone, signUpPassword;
    private Button register;
    private FirebaseAuth auth;



    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference mUsersRef = mRootRef.child("users");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        firstName = (EditText) findViewById(R.id.FirstName);
        lastName = (EditText) findViewById(R.id.LastName);
        signUpEmail = (EditText) findViewById(R.id.SignUpEmail);
        signUpPassword = (EditText) findViewById(R.id.SignUpPassword);
        signUpPhone = (EditText) findViewById(R.id.SignUpPhoneNumber);
        register = (Button) findViewById(R.id.SignUpButton);


        auth = FirebaseAuth.getInstance();

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = signUpEmail.getText().toString().trim();
                String password = signUpPassword.getText().toString().trim();
                String phone = signUpPhone.getText().toString().trim();
                String firstNameString = firstName.getText().toString().trim();
                String lastNameString = lastName.getText().toString().trim();

                boolean cancel = false;
                View focusView = null;

                // Check for a valid password, if the user entered one.
                if (!TextUtils.isEmpty(password) && ! isPasswordValid(password)) {
                    signUpPassword.setError(getString(R.string.error_invalid_password));
                    focusView = signUpPassword;
                    cancel = true;
                }

                // Check for a valid email address.
                if (TextUtils.isEmpty(email)) {
                    signUpEmail.setError(getString(R.string.error_field_required));
                    focusView = signUpEmail;
                    cancel = true;
                } else if (!isEmailValid(email)) {
                    signUpEmail.setError(getString(R.string.error_invalid_email));
                    focusView = signUpEmail;
                    cancel = true;
                }

                if (cancel) {
                    // There was an error; don't attempt login and focus the first
                    // form field with an error.
                    focusView.requestFocus();
                } else {
                    // Show a progress spinner, and kick off a background task to
                    // perform the user login attempt.
                    System.out.println("Attempting to login....");

                    auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    Toast.makeText(SignupActivity.this, "createUserWithEmail:onComplete:" + task.isComplete(), Toast.LENGTH_SHORT).show();
                                    if (!task.isSuccessful()) {
                                        Toast.makeText(SignupActivity.this, "Authentication failed." + task.getException(),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                    String userID = auth.getUid();
                    mUsersRef.child(userID).child("email").setValue(email);
                    mUsersRef.child(userID).child("lastName").setValue(lastNameString);
                    mUsersRef.child(userID).child("firstName").setValue(firstNameString);
                    mUsersRef.child(userID).child("phoneNumber").setValue(phone);
                    mUsersRef.child(userID).child("type").setValue("rider");

                }

            }
        });
    }
    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

}
