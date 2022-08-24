package com.example.newfoodappserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import dmax.dialog.SpotsDialog;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.newfoodappserver.common.Common;
import com.example.newfoodappserver.model.ServerUserModel;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private  static int APP_REQUEST_CODE = 7171;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener listener;
    private AlertDialog dialog;
    private DatabaseReference serverRef;
    private List<AuthUI.IdpConfig> providers;


    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(listener);
    }

    @Override
    protected void onStop() {
        if (listener!=null)
            firebaseAuth.removeAuthStateListener(listener);
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();

    }

    private void init() {

        providers  = Arrays.asList(new AuthUI.IdpConfig.PhoneBuilder().build());

        serverRef = FirebaseDatabase.getInstance().getReference(Common.SERVER_REF);
        firebaseAuth  = firebaseAuth.getInstance();

        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        listener = firebaseAuthLocal->{

            FirebaseUser user = firebaseAuthLocal.getCurrentUser();
            if (user!=null){

                //CHecking user from Firebase
                checkServerUserFromFirebase(user);

            }else {

                phoneLogin();

            }


        };

    }

    private void checkServerUserFromFirebase(FirebaseUser user) {
        dialog.show();
        serverRef.child(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                                ServerUserModel userModel = dataSnapshot.getValue(ServerUserModel.class);
                                if (userModel.isActive())
                                {

                                    goToHomeActivity(userModel);

                                }else {
                                        dialog.dismiss();
                                    Toast.makeText(MainActivity.this,"You should be accepted by admin to enter this app",Toast.LENGTH_SHORT).show();

                                }

                        }else {
                            //User does not exist in the Firebase
                                dialog.dismiss();
                                showRegisterDialog(user);
                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                            dialog.dismiss();
                            Toast.makeText(MainActivity.this,""+databaseError.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });



    }

    private void showRegisterDialog(FirebaseUser user) {

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Register");
        builder.setMessage("Please fill on these information \n and  you will be accepted by admin later");

        View itemView  = LayoutInflater.from(this).inflate(R.layout.layout_register,null);
        EditText my_name = (EditText)itemView.findViewById(R.id.myName);
        EditText my_phone = (EditText)itemView.findViewById(R.id.myPhone);

        //Setting data
        my_phone.setText(user.getPhoneNumber());
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Register", (dialogInterface, which) -> {

                    if (TextUtils.isEmpty(my_name.getText().toString())){

                        Toast.makeText(MainActivity.this,"Please enter your name here ",Toast.LENGTH_SHORT).show();
                        return;

                    }

                    ServerUserModel serverUserModel = new ServerUserModel();
                    serverUserModel.setUid(user.getUid());
                    serverUserModel.setName(my_name.getText().toString());
                    serverUserModel.setPhone(my_phone.getText().toString());
                    serverUserModel.setRestaurant("restauranta");
                    serverUserModel.setActive(false); // By default , if we want that true we do it manually on Firebase

                    dialog.show();

                    serverRef.child(serverUserModel.getUid())
                            .setValue(serverUserModel)
                            .addOnFailureListener(e -> {
                                dialog.dismiss();
                                Toast.makeText(MainActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                            }).addOnCompleteListener(task -> {
                                dialog.dismiss();
                                Toast.makeText(MainActivity.this,"Congrats you got Registered " +
                                        "Succesfully!! Admin will check and activate you asap",Toast.LENGTH_SHORT).show();
                                //goToHomeActivity(serverUserModel);
                            });


                });

        builder.setView(itemView);

        androidx.appcompat.app.AlertDialog registerDialog = builder.create();
        registerDialog.show();


    }

    private void goToHomeActivity(ServerUserModel serverUserModel) {
        dialog.dismiss();

        Common.currentServerUser = serverUserModel;
        Intent intent = new Intent(this,HomeActivity.class);
        intent.putExtra(Common.IS_OPEN_ACTIVITY_NEW_ORDER,getIntent().getBooleanExtra(Common.IS_OPEN_ACTIVITY_NEW_ORDER,false));

        startActivity(intent);
        finish();
    }

    private void phoneLogin()  {

        startActivityForResult(AuthUI.getInstance()
              .createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                      .build(),APP_REQUEST_CODE);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==APP_REQUEST_CODE){

            IdpResponse response = IdpResponse .fromResultIntent(data);
            if (resultCode ==RESULT_OK ){

                FirebaseUser user  = FirebaseAuth.getInstance().getCurrentUser();
            }else {

                Toast.makeText(this,"Failed to Sign in ",Toast.LENGTH_SHORT).show();

            }

        }



    }



}
