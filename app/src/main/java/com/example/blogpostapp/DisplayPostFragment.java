package com.example.blogpostapp;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class DisplayPostFragment extends Fragment {
    TextView title, content, username;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Bundle bundle;
    String key, uuid;
    ImageView profilePic;
    StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_display_post, container, false);
        title = view.findViewById(R.id.textView7);
        content = view.findViewById(R.id.textView8);
        username = view.findViewById(R.id.textView6);
        profilePic = view.findViewById(R.id.profile);
        bundle = getArguments();
        key = bundle.getString("POST_ID");
        uuid = bundle.getString("USER_ID");
        db.collection("blogPosts").document(key).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                username.setText(document.get("username").toString());
                                content.setText(document.get("content").toString());
                                title.setText(document.get("title").toString());
                            }
                        }
                    }
                });
        storageReference.child("profileImages/" + uuid + ".jpeg").getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri).resize(300,300 ).into(profilePic);
                    }
                });
        return view;
    }
}