package com.example.blogpostapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.model.Document;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    RecyclerView recyclerView;
    BlogPostAdapter blogPostAdapter;
    ArrayList<BlogPost> blogPostList = new ArrayList<>();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    ImageView edit, delete;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
//        blogPostAdapter = new BlogPostAdapter(blogPostList, currentUser);
//        recyclerView.setAdapter(blogPostAdapter);
//        blogPostAdapter.notifyDataSetChanged();
        Query blogPosts = db.collection("blogPosts");
        FirestoreRecyclerOptions<BlogPost> options = new FirestoreRecyclerOptions.Builder<BlogPost>()
                .setQuery(blogPosts, BlogPost.class)
                .build();
        blogPostAdapter = new BlogPostAdapter(options);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(blogPostAdapter);
//
//        users.get()
//                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                    @Override
//                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//                        List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
//                        Log.d("LIST", String.valueOf(queryDocumentSnapshots.getDocuments()));
//                        for (DocumentSnapshot d: list) {
//                            Query blogPosts = d.getReference().collection("blogPosts");
//                            FirestoreRecyclerOptions<BlogPost> options = new FirestoreRecyclerOptions.Builder<BlogPost>()
//                                    .setQuery(blogPosts, BlogPost.class)
//                                    .build();
//                        }
//                    }
//                });
        return view;
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