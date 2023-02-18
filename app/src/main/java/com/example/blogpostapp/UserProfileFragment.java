package com.example.blogpostapp;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Document;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class UserProfileFragment extends Fragment {
    RecyclerView recyclerView;
    BlogPostAdapter blogPostAdapter;
    ArrayList<BlogPost> blogPostList = new ArrayList<>();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = mAuth.getCurrentUser();
    TextView username, posts, followers, following;
    ImageView profilePic;
    FloatingActionButton editProfile;
    Button follow;
    Bundle data;
    String uuid, name;
    boolean contains;
    List<String> followerList;
    CollectionReference userData = db.collection("userData");
    StorageReference storageReference = FirebaseStorage.getInstance().getReference();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_profile, container, false);
        follow = view.findViewById(R.id.button2);
        followers = view.findViewById(R.id.textView3);
        following = view.findViewById(R.id.textView2);
        data = getArguments();
        if (data != null) {
            uuid = data.getString("USER_ID");
            name = data.getString("USER_NAME");
        }
        else {
            uuid = currentUser.getUid();
            name = currentUser.getDisplayName();
        }
        recyclerView = view.findViewById(R.id.recyclerView);
        profilePic = view.findViewById(R.id.imageView1);
        editProfile = view.findViewById(R.id.editprofile);
        posts = view.findViewById(R.id.posts);
        if (!currentUser.getUid().equals(uuid)) {
            editProfile.setVisibility(View.GONE);
        }
        if (currentUser.getUid().equals(uuid)) {
            Log.d("CANNOT FOLLOW YOURSELF", "HEHE");
            follow.setEnabled(false);
        }
        updateUserData();
        checkFollowStatus();
        follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (follow.getText().toString().equals("Follow")) {
                    follow.setBackgroundResource(R.drawable.roundbuttonpressed);
                    follow.setText("Unfollow");
                    userData.document(uuid).update("followers", FieldValue.arrayUnion(currentUser.getUid()));
                    userData.document(uuid).update("numFollowers", FieldValue.increment(1));
                    userData.document(currentUser.getUid()).update("following", FieldValue.increment(1));
                }
                else {
                    follow.setBackgroundResource(R.drawable.roundbuttonunpressed);
                    follow.setText("Follow");
                    userData.document(uuid).update("followers", FieldValue.arrayRemove(currentUser.getUid()));
                    userData.document(uuid).update("numFollowers", FieldValue.increment(-1));
                    userData.document(currentUser.getUid()).update("following", FieldValue.increment(-1));
                }
                updateUserData();
            }
        });
        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uuid = currentUser.getUid();
                Intent i = new Intent(getContext(), RegistrationActivity.class);
                i.putExtra("UUID", uuid);
                startActivity(i);
            }
        });
        Query blogPosts = db.collection("blogPosts").whereEqualTo("uuid", uuid);
        FirestoreRecyclerOptions<BlogPost> options = new FirestoreRecyclerOptions.Builder<BlogPost>()
                .setQuery(blogPosts, BlogPost.class)
                .build();
        blogPostAdapter = new BlogPostAdapter(options);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(blogPostAdapter);
        username = view.findViewById(R.id.textView);
        username.setText(name);
        storageReference.child("profileImages/" + uuid + ".jpeg").getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri).resize(300,300 ).into(profilePic);
                    }
                });

        return view;
    }

    private void updateUserData() {
        db.collection("userData").document(uuid)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        posts.setText(documentSnapshot.get("posts").toString() + " posts");
                        followers.setText(documentSnapshot.get("numFollowers").toString() + " followers");
                        following.setText(documentSnapshot.get("following").toString() + " following");
                    }
                });
    }

    private void checkFollowStatus() {
        userData.document(uuid).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                List<String> followerList = (List<String>) document.get("followers");
                                for (String user: followerList) {
                                    if (user.equals(currentUser.getUid())) {
                                        follow.setBackgroundResource(R.drawable.roundbuttonpressed);
                                        follow.setText("Unfollow");
                                    }
                                }
                            }
                        }
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        blogPostAdapter.startListening();

    }

    @Override
    public void onStop() {
        super.onStop();
        blogPostAdapter.stopListening();
    }
}