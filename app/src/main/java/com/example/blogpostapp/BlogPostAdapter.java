package com.example.blogpostapp;

import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.StringTokenizer;

//public class BlogPostAdapter extends RecyclerView.Adapter<BlogPostAdapter.BlogPostHolder>{
//    ArrayList<BlogPost> dataList;
//    FirebaseUser currentUser;
//    View.OnClickListener changePostListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            switch (v.getId()) {
//                case R.id.edit:
//                    //open edit view
//                    break;
//                case R.id.delete:
//                    //delete view
//
//            }
//        }
//    };
//    public BlogPostAdapter(ArrayList<BlogPost> dataList, FirebaseUser currentUser) {
//        this.dataList = dataList;
//        this.currentUser = currentUser;
//    }
//
//    @NonNull
//    @Override
//    public BlogPostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_home, parent, false);
//        return new BlogPostHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull BlogPostHolder holder, int position) {
//        holder.title.setText(dataList.get(position).getTitle());
//        holder.text.setText(dataList.get(position).getText());
//            if (currentUser.getEmail().compareTo(dataList.get(position).getEmail()) != 0) {
//                Log.d("CHEKC", "CHECK");
//                holder.edit.setVisibility(View.GONE);
//                holder.delete.setVisibility(View.GONE);
//            }
//            else {
//                holder.edit.setVisibility(View.VISIBLE);
//                holder.delete.setVisibility(View.VISIBLE);
//            }
//    }
//
//    @Override
//    public int getItemCount() {
//        return dataList.size();
//    }
//
//    class BlogPostHolder extends RecyclerView.ViewHolder {
//        TextView title, text;
//        ImageView edit, delete;
//        public BlogPostHolder(@NonNull View itemView) {
//            super(itemView);
//            title = itemView.findViewById(R.id.textView);
//            text = itemView.findViewById(R.id.textView2);
//            edit = itemView.findViewById(R.id.edit);
//            delete = itemView.findViewById(R.id.delete);
//        }
//    }
//}
public class BlogPostAdapter extends FirestoreRecyclerAdapter<BlogPost, BlogPostAdapter.BlogPostHolder> {
    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageReference = storage.getReference();
    public BlogPostAdapter(@NonNull FirestoreRecyclerOptions<BlogPost> options) {
        super(options);
    }
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    @Override
    protected void onBindViewHolder(@NonNull BlogPostHolder holder, int position, @NonNull BlogPost model) {
        holder.title.setText(model.getTitle());
        int n = countWords(model.getContent());
        holder.text.setText(shorten(model.getContent(), n/2));
        holder.username.setText(model.getUsername());
        String uuid = model.getUUID();
        storageReference.child("profileImages/" + uuid + ".jpeg").getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri).resize(300,300).into(holder.profilePic);
                    }
                });
        holder.profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppCompatActivity activity = (AppCompatActivity) v.getContext();
                Bundle bundle = new Bundle();
                UserProfileFragment userProfileFragment = new UserProfileFragment();
                userProfileFragment.setArguments(bundle);
                bundle.putString("USER_ID", model.getUUID());
                bundle.putString("USER_NAME", model.getUsername());
                activity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, userProfileFragment)
                        .commit();
            }
        });
        holder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppCompatActivity activity = (AppCompatActivity) v.getContext();
                Bundle bundle = new Bundle();
                CreatePostFragment postFragment = new CreatePostFragment();
                postFragment.setArguments(bundle);
                bundle.putString("POST_TITLE", model.getTitle());
                bundle.putString("POST_CONTENT", model.getContent());
                bundle.putString("POST_ID", model.getKey());
                activity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, postFragment)
                        .commit();
            }
        });
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CollectionReference blogPosts = db.collection("blogPosts");
                blogPosts.document(model.getKey()).delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                db.collection("userData").document(currentUser.getUid()).update("posts", FieldValue.increment(-1));
                                Log.d("DELETED", "DELETED");
                            }
                        });
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppCompatActivity activity = (AppCompatActivity) v.getContext();
                Bundle bundle = new Bundle();
                bundle.putString("POST_ID", model.getKey());
                bundle.putString("USER_ID", model.getUUID());
                DisplayPostFragment displayPostFragment = new DisplayPostFragment();
                displayPostFragment.setArguments(bundle);
                activity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, displayPostFragment)
                        .commit();
            }
        });
        if (currentUser.getUid().compareTo(model.getUUID()) != 0) {
            holder.edit.setVisibility(View.GONE);
            holder.delete.setVisibility(View.GONE);
        }
        else {
            holder.edit.setVisibility(View.VISIBLE);
            holder.delete.setVisibility(View.VISIBLE);
        }
    }

    private String shorten(String str, int n) {
        String[] sArr = str.split(" ");
        String firstStrs = "";
        for(int i = 0; i < n; i++)
            firstStrs += sArr[i] + " ";
        return firstStrs.trim() + "...";
    }
    private int countWords(String str) {
        StringTokenizer st = new StringTokenizer(str);
        return st.countTokens();
    }
    @NonNull
    @Override
    public BlogPostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_home, parent, false);
        return new BlogPostHolder(view);
    }


    class BlogPostHolder extends RecyclerView.ViewHolder {
        TextView title, text, username;
        ImageView edit, delete, profilePic;
        public BlogPostHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.textView);
            text = itemView.findViewById(R.id.textView2);
            username = itemView.findViewById(R.id.textView5);
            edit = itemView.findViewById(R.id.edit);
            delete = itemView.findViewById(R.id.delete);
            profilePic = itemView.findViewById(R.id.profile);
        }
    }
}
