package com.example.diplom;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.example.diplom.models.Item;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ItemList extends Fragment {

    private DrawerLayout drawer;
    private FirebaseAuth auth;
    private FirebaseDatabase db;
    private DatabaseReference users, items;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> listData;
    private List<Item> listTemp;
    private List<Double> listLatitude, listLongitude, listLong;
    private FragmentManager fm;
    private FragmentTransaction ft;
    private FrameLayout frame;
    private EditText editSearch;
    private Button buttonSearch;
    private String searchValue;
    private double mLatitude, mLongitude;
    private LocationManager locationManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LayoutInflater lfi = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_item_list, container, false);

//        getActivity().setContentView(R.layout.fragment_item_list);

        editSearch = (EditText) view.findViewById(R.id.editSearchValue);
        editSearch.setText("");
        drawer = getActivity().findViewById(R.id.drawer_layout);
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        users = db.getReference("users");
        items = db.getReference("items");
        listView = view.findViewById(R.id.listViewMain);
        listData = new ArrayList<>();
        listTemp = new ArrayList<>();
        listLatitude = new ArrayList<>();
        listLongitude = new ArrayList<>();
        listLong = new ArrayList<>();
        adapter = new ArrayAdapter<>(getActivity().getApplicationContext(), android.R.layout.simple_list_item_1, listData);
        listView.setAdapter(adapter);
        fm = getActivity().getSupportFragmentManager();
        ft = fm.beginTransaction();
        frame = getActivity().findViewById(R.id.containerItem);
        buttonSearch = getActivity().findViewById(R.id.buttonSearch);
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);




        getDataFromDB();
        setOnClickItem();
        search();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            return;}
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 10, locationListener);
    }

    private void search() {
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if(s.toString().equals("")){
                    getDataFromDB();
                }
                else{
                    adapter.getFilter().filter(s);
//                    searchItem(s.toString());
                }

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.toString().equals(" ")){
                    getDataFromDB();
                }
            }
        });
    }

    public void searchItem(String textToSearch){
        for(String item : listData){
        String textToSearch1 = textToSearch.toLowerCase();
        if(!item.toLowerCase().contains(textToSearch1)){
        listData.remove(item);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void getDataFromDB(){

        ValueEventListener vListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(listData.size() > 0 )
                    listData.clear();
                if(listTemp.size() > 0 )
                    listTemp.clear();
                if(listLatitude.size() > 0)
                    listLatitude.clear();
                if(listLongitude.size() > 0)
                    listLongitude.clear();
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    Item item = ds.getValue(Item.class);
                    assert item != null;
                    listData.add(item.itemName);
                    listTemp.add(item);
                    listLatitude.add(Double.parseDouble(String.valueOf(item.latitude)));
                    listLongitude.add(Double.parseDouble(String.valueOf(item.longitude)));
                }

                adapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        for (int i = 0; i>listData.size()-1; i++){
            listLong.add(Math.sqrt(Math.pow(listLatitude.get(i) - mLatitude, 2) + Math.pow(listLongitude.get(i) - mLongitude , 2)));
        }

        for(int i = listData.size()-1; i > 0; i--)
            for(int j = 0; j < i; j++)
                if(listLong.get(j)  > listLong.get(j++)){
                    String tmpD0 = listData.get(j);
                    String tmpD1 = listData.get(j++);
                    listData.set(j, tmpD1);
                    listData.set(j++, tmpD0);
                    Double tmpL0 = listLong.get(j);
                    Double tmpL1 = listLong.get(j++);
                    listLong.set(j, tmpL1);
                    listLong.set(j++, tmpL0);
                }

        items.addValueEventListener(vListener);

    }

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if(location != null){
                mLatitude = Double.parseDouble(String.valueOf(location.getLatitude()));
                mLongitude = Double.parseDouble(String.valueOf(location.getLongitude()));
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    private void setOnClickItem() {

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                frame.removeAllViews();

                Item item = listTemp.get(position);

                Fragment fragment = null;
                fragment = new item_fragment();

                Bundle args = new Bundle();
                args.putString("item_id", item.id);
                args.putString("item_url", item.url);
                args.putString("item_item_name", item.itemName);
                args.putString("item_city", item.city);
                args.putString("item_date", item.date);
                args.putString("item_info", item.info);
                fragment.setArguments(args);

                editSearch.setText("");
                ft.addToBackStack(null);
                ft.replace(R.id.containerItem, fragment);
                ft.commit();

            }
        });

    }

}
