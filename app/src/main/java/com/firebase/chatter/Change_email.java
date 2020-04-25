package com.firebase.chatter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Change_email extends AppCompatDialogFragment {


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final EditText updated_email , current_password ;
        final Button btn_update , btn_cancel;

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.change_email,null);

        builder.setView(view).setTitle("Update Your Email");
        updated_email = view.findViewById(R.id.change_email);
        current_password = view.findViewById(R.id.change_email_password);
        btn_update = view.findViewById(R.id.email_update);
        btn_cancel = view.findViewById(R.id.email_cancel);

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = updated_email.getText().toString();
                String password = current_password.getText().toString();

                if (email.length() > 0 && password.length() > 5) {
                    final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

                    final String current_email = firebaseUser.getEmail();
                    AuthCredential authCredential = EmailAuthProvider.getCredential(current_email, password);
                    firebaseUser.reauthenticate(authCredential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull final Task<Void> task) {
                            if (task.isSuccessful()) {
                                firebaseUser.updateEmail(email).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        if(task.isSuccessful()) {
                                            dismiss();
                                        } else {
                                            updated_email.setError("Invalid Email");
                                        }
                                    }
                                });
                            } else {
                                current_password.setError("Authentication Failed");
                            }
                        }
                    });
                } else {
                    updated_email.setError("Error");
                }
            }
            });


        return builder.create();
    }
}
