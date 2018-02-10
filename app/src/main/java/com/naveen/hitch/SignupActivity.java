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
                final String email = signUpEmail.getText().toString().trim();
                final String password = signUpPassword.getText().toString().trim();
                final String phone = signUpPhone.getText().toString().trim();
                final String firstNameString = firstName.getText().toString().trim();
                final String lastNameString = lastName.getText().toString().trim();

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

                    auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    Toast.makeText(SignupActivity.this, "Created a user", Toast.LENGTH_SHORT).show();
                                    if (!task.isSuccessful()) {
                                        Toast.makeText(SignupActivity.this, "Error while signing up" + task.getException(),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                    else {
                                        String userID = auth.getCurrentUser().getUid();
                                        mRootRef.child("Users").child(userID).child("email").setValue(email);
                                        mRootRef.child("Users").child(userID).child("lastName").setValue(lastNameString);
                                        mRootRef.child("Users").child(userID).child("firstName").setValue(firstNameString);
                                        mRootRef.child("Users").child(userID).child("phoneNumber").setValue(phone);
                                        mRootRef.child("Users").child(userID).child("type").setValue("rider");

                                        Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                                        startActivity(intent);
                                        finish();
                                        return;

                                    }
                                }
                            });
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
