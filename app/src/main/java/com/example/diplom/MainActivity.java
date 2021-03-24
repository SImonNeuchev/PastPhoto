package com.example.diplom;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.example.diplom.models.Item;
import com.example.diplom.models.User;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.Menu;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private MenuItem register, login, exit, nameUserMenu;
    private FirebaseAuth auth;
    private FirebaseDatabase db;
    private DatabaseReference users, items;
    private DrawerLayout drawer;
    private ConstraintLayout root;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> listData;
    private List<Item> listTemp;
    private FragmentManager fm;
    private FragmentTransaction ft;
    private Fragment fragment0, fragmentAdd;
    private FrameLayout frame;
    private GoogleMap googleMap;
    private MapFragment mMapFragment;
    private FirebaseUser cUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fm = getSupportFragmentManager();
        ft = fm.beginTransaction();
        frame = findViewById(R.id.containerItem);
        fragment0 = null;
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.containerItem);
        mMapFragment = MapFragment.newInstance();
        fragment0 = new ItemList();
        fragmentAdd = new AddItem();
        initFragment();
        drawer = findViewById(R.id.drawer_layout);
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        users = db.getReference("users");
        items = db.getReference("items");
        root = findViewById(R.id.root_element);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        register = menu.findItem(R.id.registerItem);
        login = menu.findItem(R.id.loginItem);
        exit = menu.findItem(R.id.exitItem);
        nameUserMenu = menu.findItem(R.id.nameUserMenu);
        cUser = auth.getCurrentUser();

        if (cUser != null){

            register.setVisible(false);
            login.setVisible(false);
            exit.setVisible(true);
            nameUserMenu.setVisible(true);
            nameUserMenu.setTitle("Вы вошли как: " + cUser.getEmail());

        }
        else {

            register.setVisible(true);
            login.setVisible(true);
            exit.setVisible(false);
            nameUserMenu.setVisible(false);


        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private void initFragment() {
        ft.add(R.id.containerItem, fragment0);
        ft.commit();
    }

    public void onClickRegister (MenuItem item){

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Зарегестрироваться");
        dialog.setMessage("Введите данные для регистрации");

        LayoutInflater inflater = LayoutInflater.from(this);
        View registerWindow = inflater.inflate(R.layout.register_window,null);
        dialog.setView(registerWindow);

        final MaterialEditText email = registerWindow.findViewById(R.id.emailField);
        final MaterialEditText password = registerWindow.findViewById(R.id.passField);
        final MaterialEditText name = registerWindow.findViewById(R.id.nameField);

        dialog.setNegativeButton("Отменить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();
            }
        });

        dialog.setPositiveButton("Зарегестрироваться", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                int errors = 0;
                if(TextUtils.isEmpty(email.getText().toString())){
                    Snackbar.make(root, "Введите вашу почту", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if(TextUtils.isEmpty(name.getText().toString())){
                    Snackbar.make(root, "Введите ваше имя", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if(password.getText().toString().length() < 5){
                    Snackbar.make(root, "Введите пароль длиннее 5 символов", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                auth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                User user = new User();
                                user.setEmail(email.getText().toString());
                                user.setName(name.getText().toString());
                                user.setPassword(password.getText().toString());

                                users.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Snackbar.make(root, "Пользователь зарегестрирован!", Snackbar.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Snackbar.make(root, "Ошибка регистрации. " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
            }
        });

        dialog.show();

    }

    public void ocClickLogin (MenuItem item){

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Войти");
        dialog.setMessage("Введите данные для входа");

        LayoutInflater inflater = LayoutInflater.from(this);
        View loginWindow = inflater.inflate(R.layout.login_window,null);
        dialog.setView(loginWindow);

        final MaterialEditText email = loginWindow.findViewById(R.id.emailField);
        final MaterialEditText password = loginWindow.findViewById(R.id.passField);

        dialog.setNegativeButton("Отменить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();
            }
        });

        dialog.setPositiveButton("Войти", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                if(TextUtils.isEmpty(email.getText().toString())){
                    Snackbar.make(root, "Введите вашу почту", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if(password.getText().toString().length() < 5){
                    Snackbar.make(root, "Введите пароль длиннее 5 символов", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                auth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {

                                register.setVisible(false);
                                login.setVisible(false);
                                exit.setVisible(true);
                                nameUserMenu.setVisible(true);
                                nameUserMenu.setTitle("Вы вошли как:" + cUser.getEmail());

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Snackbar.make(root, "Ошибка авторизации. " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                            }
                });

            }
        });

        dialog.show();

    }

    public void onClickExit(MenuItem item){

        FirebaseAuth.getInstance().signOut();
        register.setVisible(true);
        login.setVisible(true);
        exit.setVisible(false);
        nameUserMenu.setVisible(false);

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home){
            fm = getSupportFragmentManager();
            ft = fm.beginTransaction();
            ft.replace(R.id.containerItem, fragment0);
            ft.commit();
        }

        if (id == R.id.mav_add){
            fm = getSupportFragmentManager();
            ft = fm.beginTransaction();
            ft.replace(R.id.containerItem, fragmentAdd);
            ft.commit();
        }

        if (id == R.id.nav_map) {
            fm = getSupportFragmentManager();
            ft = fm.beginTransaction();
//            ft.replace(R.id.containerItem, mMapFragment);
            ft.commit();
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
