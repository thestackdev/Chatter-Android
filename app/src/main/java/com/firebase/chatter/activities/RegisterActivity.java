package com.firebase.chatter.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.firebase.chatter.R;
import com.firebase.chatter.helper.AppAccents;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    private LinearLayout linearLayout;
    private Button register;
    private TextInputLayout user_name , user_email , user_password , user_password_1;
    private String  name, email , password , password_1;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private ProgressDialog progressDialog;
    private InputMethodManager inputMethodManager;
    private TextView go_to_login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        setUpUiViews();

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputMethodManager.hideSoftInputFromWindow(linearLayout.getWindowToken(),0);
                name = user_name.getEditText().getText().toString();
                email = user_email.getEditText().getText().toString();
                password = user_password.getEditText().getText().toString();
                password_1 = user_password_1.getEditText().getText().toString();

                try {

                    if (TextUtils.isEmpty(name) || name.length() > 15) {
                        Toast.makeText(RegisterActivity.this , "Invalid user name",Toast.LENGTH_SHORT).show();
                    } else if (TextUtils.isEmpty(email)) {
                        Toast.makeText(RegisterActivity.this , "Invalid email",Toast.LENGTH_SHORT).show();
                    } else if(TextUtils.isEmpty(password) || TextUtils.isEmpty(password_1)) {
                        Toast.makeText(RegisterActivity.this , "Invalid Password",Toast.LENGTH_SHORT).show();
                    } else if (!password.equals(password_1)) {
                        Toast.makeText(RegisterActivity.this, "Check your Password", Toast.LENGTH_SHORT).show();
                    } else {
                        progressDialog.setMessage("Please wait...");
                        progressDialog.setCanceledOnTouchOutside(false);
                        progressDialog.show();
                        gotoreg(name, email, password);
                    }
                } catch (Exception e) {
                    Toast.makeText(RegisterActivity.this,"Error",Toast.LENGTH_SHORT).show();
                }
            }
        });
        go_to_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this , LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void gotoreg(final String name, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String uid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
                            String device_token = FirebaseInstanceId.getInstance().getToken();

                            HashMap<String,String> hashMap = new HashMap<>();
                            hashMap.put("name" , name);
                            hashMap.put("status","hey , I'm Using Chatter");
                            hashMap.put("image","default");
                            hashMap.put("thumbnail","default");
                            hashMap.put("online","true");
                            hashMap.put("device_token",device_token);

                            databaseReference.child(uid).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()) {
                                        Intent main_Activity = new Intent(RegisterActivity.this, MainActivity.class);
                                        main_Activity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(main_Activity);
                                        finish();
                                        progressDialog.dismiss();
                                    }
                                }
                            });

                        } else {
                            progressDialog.hide();
                            Toast.makeText(RegisterActivity.this, "Authentication failed.",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void setUpUiViews() {
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        register =  findViewById(R.id.btn_register);
        user_name =  findViewById(R.id.register_user_name);
        user_email =  findViewById(R.id.register_email);
        user_password = findViewById(R.id.register_password);
        user_password_1 = findViewById(R.id.register_password_1);
        linearLayout = findViewById(R.id.register_layout);
        go_to_login = findViewById(R.id.go_to_login);
        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        Toolbar toolbar = findViewById(R.id.register_bar);
        setSupportActionBar(toolbar);
        TextView textView = toolbar.findViewById(R.id.title_toolbar);
        textView.setText(getString(R.string.register));
        getSupportActionBar().setTitle("");
        progressDialog = new ProgressDialog(this);

        AppAccents appAccents = new AppAccents(this);
        appAccents.init();

        toolbar.setBackgroundColor(Color.parseColor(appAccents.getAccentColor()));
        toolbar.setTitleTextColor(Color.parseColor(appAccents.getTextColor()));

        register.setBackgroundColor(Color.parseColor(appAccents.getAccentColor()));
        register.setTextColor(Color.parseColor(appAccents.getTextColor()));

        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.parseColor(appAccents.getAccentColor()));

    }
}