package com.firebase.chatter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    private FloatingActionButton login;
    private TextInputLayout login_email , login_password;
    private String user_email , user_password;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private ProgressDialog progressDialog;
    private TextView register_activity;
    private InputMethodManager inputMethodManager;
    private LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setUpUiViews();

        register_activity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this , RegisterActivity.class);
                startActivity(intent);
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputMethodManager.hideSoftInputFromWindow(linearLayout.getWindowToken(),0);
                user_email = login_email.getEditText().getText().toString();
                user_password = login_password.getEditText().getText().toString();

                try {
                    if (!TextUtils.isEmpty(user_email) || !TextUtils.isEmpty(user_password)) {
                        progressDialog.setMessage("Please wait...");
                        progressDialog.setCanceledOnTouchOutside(false);
                        progressDialog.show();
                        gotoLogin(user_email, user_password);

                    } else {
                        Toast.makeText(LoginActivity.this, "All fields are Required", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(LoginActivity.this,"Error",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void gotoLogin(String email, String password) {
        mAuth.signInWithEmailAndPassword(email , password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
                    String device_token = FirebaseInstanceId.getInstance().getToken();
                    databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
                    databaseReference.child("device_token").setValue(device_token).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                Intent main_Activity = new Intent(LoginActivity.this, MainActivity.class);
                                main_Activity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(main_Activity);
                                finish();
                                progressDialog.dismiss();
                            }
                        }
                    });
                } else {
                    progressDialog.hide();
                    Toast.makeText(LoginActivity.this, "Authentication failed.",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void setUpUiViews() {
        mAuth = FirebaseAuth.getInstance();
        login =  findViewById(R.id.btn_login);
        login_email =  findViewById(R.id.login_email);
        login_password = findViewById(R.id.login_password);
        register_activity = findViewById(R.id.go_to_reg);
        linearLayout = findViewById(R.id.login_layout);
        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        Toolbar toolbar = findViewById(R.id.login_bar);
        TextView textView = toolbar.findViewById(R.id.title_toolbar);
        textView.setText(getString(R.string.login));
        progressDialog = new ProgressDialog(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
    }
}
