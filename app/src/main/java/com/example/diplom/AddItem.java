package com.example.diplom;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.diplom.models.Item;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddItem extends Fragment {

    private EditText edItemName, edDate, edCity, edInfo, edLatitude, edLongitude, edUrl;
    private FirebaseDatabase db;
    private DatabaseReference items;
    private Button btSaveItem;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        LayoutInflater lfi = getActivity().getLayoutInflater();
        View view = lfi.inflate(R.layout.fragment_add_item, container, false);

        btSaveItem = (Button) view.findViewById(R.id.btSaveItem);
        db = FirebaseDatabase.getInstance();
        items = db.getReference("items");
        edItemName = getActivity().findViewById(R.id.edItemName);
        edDate = getActivity().findViewById(R.id.edDate);
        edCity = getActivity().findViewById(R.id.edCity);
        edInfo = getActivity().findViewById(R.id.edInfo);
        edLatitude = getActivity().findViewById(R.id.edLatitude);
        edLongitude = getActivity().findViewById(R.id.edLongitude);
        edUrl = getActivity().findViewById(R.id.edUrl);

        btSaveItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String id = items.getKey();
                String itemName = edItemName.getText().toString();
                String date = edDate.getText().toString();
                String city = edCity.getText().toString();
                String info = edInfo.getText().toString();
                String latitude = edLatitude.getText().toString();
                String longitude = edLongitude.getText().toString();
                String url = edUrl.getText().toString();

                Item newItem = new Item(id, itemName, date, city, info, latitude, longitude, url, "");

                items.push().setValue(newItem);
            }
        });

        return view;
    }

}
