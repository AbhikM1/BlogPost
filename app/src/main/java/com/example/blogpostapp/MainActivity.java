package com.example.blogpostapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    Button signOut, makePost;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = mAuth.getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    RecyclerView recyclerView;
    BlogPostAdapter blogPostAdapter;
    ArrayList<BlogPost> blogPostList = new ArrayList<>();
    Toolbar toolbar;
    DrawerLayout drawer;
    NavigationView navigationView;
    View header;
    ImageView navImage;
    TextView navEmail, navUsername;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        header = navigationView.getHeaderView(0);
        navImage = header.findViewById(R.id.navimage);
        navEmail = header.findViewById(R.id.navemail);
        navUsername = header.findViewById(R.id.navusername);
        if (currentUser != null) {
            setNavViews();
        }
        navigationView.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void setNavViews() {
        navUsername.setText(currentUser.getDisplayName());
        navEmail.setText(currentUser.getEmail());
        if (currentUser.getPhotoUrl() != null) {
            Log.d("UPDATED PIC", "UPDATED PIC");
            Picasso.get().load(currentUser.getPhotoUrl()).resize(400, 400).into(navImage);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(MainActivity.this, SignInActivity.class));
        }
        else {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_home:
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new HomeFragment())
                        .commit();
                break;
            case R.id.nav_profile:
                mAuth.signOut();
                onStart();
//                getSupportFragmentManager()
//                        .beginTransaction()
//                        .replace(R.id.fragment_container, new SignInFragment())
//                        .commit();
                break;
            case R.id.nav_makepost:
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new CreatePostFragment())
                        .commit();
                break;
            case R.id.nav_userprofile:
                Log.d("MADE IT", "MADE IT");
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new UserProfileFragment())
                        .commit();
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        else {
            super.onBackPressed();
        }
    }
}
//        signOut = findViewById(R.id.button3);
//        makePost = findViewById(R.id.button4);
//        recyclerView = findViewById(R.id.recyclerView);
//        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
//        blogPostAdapter = new BlogPostAdapter(blogPostList);
//        recyclerView.setAdapter(blogPostAdapter);
//        recyclerView.setItemAnimator(new DefaultItemAnimator());
//        blogPostAdapter.notifyDataSetChanged();
//        CollectionReference users = db.collection("users");
//        users.get()
//                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                    @Override
//                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//                        List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
//                        for (DocumentSnapshot d: list) {
//                            CollectionReference blogPosts = d.getReference().collection("blogPosts");
//                            blogPosts.get()
//                                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                                        @Override
//                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//                                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
//                                            for (DocumentSnapshot d: list) {
//                                                Log.d("DOCUMENT", String.valueOf(d));
//                                                BlogPost obj = d.toObject(BlogPost.class);
//                                                blogPostList.add(obj);
//                                            }
//                                            blogPostAdapter.notifyDataSetChanged();
//                                        }
//                                    });
//                        }
//                    }
//                });
//        signOut.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mAuth.signOut();
//                onStart();
//            }
//        });
//        makePost.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startActivity(new Intent(MainActivity.this, MakePostActivity.class));
//            }
//        });
//    }
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
//        if (currentUser == null) {
//            startActivity(new Intent(MainActivity.this, SignInActivity.class));
//        }
//    }
//}