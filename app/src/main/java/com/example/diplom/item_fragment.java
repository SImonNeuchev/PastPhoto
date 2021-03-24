package com.example.diplom;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.diplom.models.Item;
import com.example.diplom.models.ItemComment;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class item_fragment extends Fragment {

    private TextView textItemName, textCity, textDate, textInfo;
    private ImageView imageViewItem;
    private String url, comment, id;
    private Fragment fragment;
    private FirebaseAuth auth;
    private FirebaseDatabase db;
    private DatabaseReference items;
    private EditText edComment;
    private Button  btSend;
    private List<String> listData;
    private List<Item> listTemp;
    private ArrayAdapter <String> adapter;
    private DatabaseReference commentRef;
    private ListView listComment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LayoutInflater lfi = getActivity().getLayoutInflater();
        View view = lfi.inflate(R.layout.fragment_item_fragment, container, false);

        fragment = new item_fragment();
        textItemName = (TextView)view.findViewById(R.id.textItemName);
        textCity = (TextView)view.findViewById(R.id.textCity);
        textDate = (TextView)view.findViewById(R.id.textDate);
        textInfo = (TextView)view.findViewById(R.id.textInfo);
        imageViewItem = (ImageView)view.findViewById(R.id.imageViewItem);
        edComment = view.findViewById(R.id.comment);
        btSend = view.findViewById(R.id.send);
        db = FirebaseDatabase.getInstance();
        items = db.getReference();
        auth = FirebaseAuth.getInstance();
        final FirebaseUser cUser = auth.getCurrentUser();
        listComment = view.findViewById(R.id.listComment);
        listData = new ArrayList<>();
        listTemp = new ArrayList<>();
        adapter = new ArrayAdapter<>(getActivity().getApplicationContext(), android.R.layout.simple_list_item_1, listData);
        listComment.setAdapter(adapter);


        getIntentMain();

        commentRef = db.getReference().child("items").child(id).child("comments");

        getComments();

        btSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseUser cUser = auth.getCurrentUser();
                comment = edComment.getText().toString();

                if (comment.equals("")){
                    Snackbar.make(getActivity().findViewById(R.id.root_element), "Вам необходимо авторизоваться", Snackbar.LENGTH_SHORT);
                    Toast.makeText(getActivity().getApplicationContext(), "Введите сообщение", Toast.LENGTH_SHORT);
                    return;
                }
                if (comment.length() > 128){
                    Snackbar.make(getActivity().findViewById(R.id.root_element), "Вам необходимо авторизоваться", Snackbar.LENGTH_SHORT);
                    Toast.makeText(getActivity().getApplicationContext(), "Слишком длинное сообщение", Toast.LENGTH_SHORT);
                    return;
                }
                if(cUser == null){
                    Snackbar.make(getActivity().findViewById(R.id.root_element), "Вам необходимо авторизоваться", Snackbar.LENGTH_SHORT);
                    return;
                }

//                FirebaseDatabase.getInstance().getReference().child("items").child(id)
                String id = commentRef.getKey();
                comment = edComment.getText().toString();
                String userName = cUser.getEmail();

                ItemComment newItemComment = new ItemComment(id, userName, comment);

                commentRef.push().setValue(newItemComment);

                edComment.setText("");

                getComments();

            }
        });

        return view;
    }

    private void getComments() {

        ValueEventListener vListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(listData.size() > 0 )
                    listData.clear();
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    ItemComment itemComment = ds.getValue(ItemComment.class);
                    assert itemComment != null;
                    listData.add(itemComment.comment);
                }

                adapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        commentRef.addValueEventListener(vListener);

    }


    private void getIntentMain(){

        Intent i = getActivity().getIntent();
        Bundle args = this.getArguments();
        if(args != null){
            url = args.getString("item_url");
            Glide.with(getActivity().getApplicationContext().getApplicationContext()).load(url).into(imageViewItem);
            id = args.getString("item_id");
            textItemName.setText(args.getString("item_item_name"));
            textCity.setText(args.getString("item_city"));
            textDate.setText(args.getString("item_date"));
            textInfo.setText(args.getString("item_info"));

        }

    }

}
