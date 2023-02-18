package com.example.blogpostapp;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.security.cert.CollectionCertStoreParameters;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class RegistrationActivity extends AppCompatActivity {
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    EditText name, email, password;
    TextView registerText;
    Button register;
    TextInputLayout passwordLayout;
    ImageView profilePic;
    FirebaseUser currentUser = mAuth.getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageReference = storage.getReference();
    Uri imageUri;
    Bitmap bm;
    String uuid;
    Bundle extras;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        extras = getIntent().getExtras();
        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        register = findViewById(R.id.button);
        registerText = findViewById(R.id.textView9);
        profilePic = findViewById(R.id.profilepic);
        passwordLayout = findViewById(R.id.layoutPassword);
        if (extras != null) {
            uuid = extras.getString("UUID");
            password.setVisibility(View.GONE);
            register.setText("update");
            registerText.setText("Update");
            passwordLayout.setVisibility(View.GONE);
            populateFields();
        }
        register.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            if (extras == null) {
                                                createUser();
                                            }
                                            else {
                                                updateUser();
                                                startActivity(new Intent(RegistrationActivity.this, MainActivity.class));
                                            }
                                        }
                                    });
        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("REAHCED", "REACHEd");
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Select Image File"), 1);
            }
        });
    }

    private void populateFields() {
        if (currentUser.getPhotoUrl() != null) {
            Log.d("IM SHOWING IMAGE", "IM SHOWING IMAGE");
            Picasso.get().load(currentUser.getPhotoUrl()).resize(400,400).into(profilePic);
        }
        name.setText(currentUser.getDisplayName());
        email.setText(currentUser.getEmail());

    }

    private void updateUser() {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(name.getText().toString())
                .setPhotoUri(currentUser.getPhotoUrl())
                .build();

        currentUser.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        CollectionReference blogPosts = db.collection("blogPosts");
                        blogPosts.whereEqualTo("uuid", uuid)
                                .get()
                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        for (QueryDocumentSnapshot document: queryDocumentSnapshots) {
                                            DocumentReference doc = document.getReference();
                                            doc.update("username", currentUser.getDisplayName());
                                        }
                                    }
                                });
                    }
                });
    }

    private void initializeUserData() {
        Map<String, Object> map = new HashMap<>();
        map.put("followers", Arrays.asList());
        map.put("posts", 0);
        map.put("numFollowers", 0);
        map.put("following", 0);
        CollectionReference userData = db.collection("userData");
        userData.document(currentUser.getUid()).set(map);
    }

    private void createUser() {
        mAuth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            currentUser = FirebaseAuth.getInstance().getCurrentUser();

                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name.getText().toString()).build();

                            currentUser.updateProfile(profileUpdates)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d(TAG, "User profile updated.");
                                                if (bm != null) {
                                                    firebaseUploadImage(bm);
                                                }
                                                initializeUserData();
                                                startActivity(new Intent(RegistrationActivity.this, MainActivity.class));
                                            }
                                        }
                                    });
                        }
                    }
                });
    }


    private void firebaseUploadImage(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        String uuid = currentUser.getUid();
        StorageReference reference = FirebaseStorage.getInstance().getReference()
                .child("profileImages")
                .child(uuid + ".jpeg");
        reference.putBytes(baos.toByteArray())
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        getDownloadUrl(reference);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "failure", e.getCause());
                    }
                });

    }

    private void getDownloadUrl(StorageReference ref) {
        ref.getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Log.d(TAG, "OnSucess: " + uri);
                        setUserImageUrl(uri);
                    }
                });
    }

    private void setUserImageUrl(Uri uri) {
        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setPhotoUri(uri)
                .build();
        currentUser.updateProfile(request)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("Profile Image Sucess", "Image Valid");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Profile Image fialed", "Image not valid");
                    }
                });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            imageUri = data.getData();
            try{
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                bm = BitmapFactory.decodeStream(inputStream);
                Picasso.get().load(imageUri).resize(400, 400).into(profilePic);
                if (extras != null){
                    firebaseUploadImage(bm);
                }
            }catch (Exception e) {

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}