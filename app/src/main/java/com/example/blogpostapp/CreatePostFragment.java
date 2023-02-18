package com.example.blogpostapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CreatePostFragment extends Fragment {
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = mAuth.getCurrentUser();
    TextInputLayout title, text;
    TextView textState;
    Button post;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_post, container, false);
        Bundle data = getArguments();
        textState = view.findViewById(R.id.textView12);
        title = view.findViewById(R.id.title);
        text = view.findViewById(R.id.content);
        post = view.findViewById(R.id.button3);
        if (data != null) {
            String postContent = data.getString("POST_CONTENT");
            String postTitle = data.getString("POST_TITLE");
            title.getEditText().setText(postTitle);
            text.getEditText().setText(postContent);
            post.setText("Update");
            textState.setText("Update");
        }
        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CollectionReference blogPosts = fStore.collection("blogPosts");
                BlogPost post = new BlogPost(title.getEditText().getText().toString(), text.getEditText().getText().toString(), mAuth.getCurrentUser().getUid(), mAuth.getCurrentUser().getDisplayName());
                if (data == null) {
                    DocumentReference ref = blogPosts.document();
                    post.setKey(ref.getId());
                    ref.set(post)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Log.d("Updating post num", "updating post num");
                                    CollectionReference userData =  fStore.collection("userData");
                                    userData.document(currentUser.getUid())
                                            .update("posts", FieldValue.increment(1));
                                }
                            });
                }
                else{
                    blogPosts.document(data.getString("POST_ID")).update("title", title.getEditText().getText().toString(), "content", text.getEditText().getText().toString())
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Log.d("EDITED", "EDITED");
                                }
                            });
                }
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new HomeFragment())
                        .commit();
            }
        });
        return view;
    }
}