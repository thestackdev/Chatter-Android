package com.firebase.chatter.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.firebase.chatter.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class Change_Password extends AppCompatDialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final EditText oldPass , newPass1 , newPass2;
        final Button btn_update , btn_cancel;
        LayoutInflater inflater = getActivity().getLayoutInflater();
        @SuppressLint("InflateParams") final View view = inflater.inflate(R.layout.change_password,null);

        oldPass = view.findViewById(R.id.old_pass);
        newPass1 = view.findViewById(R.id.new_pass1);
        newPass2 = view.findViewById(R.id.new_pass2);
        btn_update = view.findViewById(R.id.password_update);
        btn_cancel = view.findViewById(R.id.password_cancel);

        builder.setView(view).setTitle("Change Password");

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldPassword = oldPass.getText().toString();
                String newPassword1 = newPass1.getText().toString();
                final String newPassword2 = newPass1.getText().toString();


                if(!newPassword1.equals(newPassword2)) {
                    newPass2.setError("Passwords Didn't Match");


                } else {
                    final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                    String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                    AuthCredential authCredential = EmailAuthProvider.getCredential(email , oldPassword);
                    Log.i(TAG, "onClick: enter 1");
                    assert firebaseUser != null;
                    firebaseUser.reauthenticate(authCredential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                firebaseUser.updatePassword(newPassword2).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()) {
                                            dismiss();
                                        } else {
                                            dismiss();
                                        }
                                    }
                                });
                            } else {
                                dismiss();
                            }
                        }
                    });


                }
            }
        });
        return builder.create();
    }
}
