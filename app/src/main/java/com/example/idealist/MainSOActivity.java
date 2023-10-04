package com.example.idealist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainSOActivity extends AppCompatActivity {

    private FirebaseUser firebaseUserSO;
    private TextView textViewSO;
    private FirebaseAuth authProfileSO;
    private SwipeRefreshLayout swipeContainer;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_soactivity);

        getSupportActionBar().setTitle("Main SO Activity");

        swipeToRefresh();

        authProfileSO = FirebaseAuth.getInstance();
        textViewSO = findViewById(R.id.textViewSO);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                R.string.open,
                R.string.close
        );

        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.menuManageProfileSO) {
                    Toast.makeText(MainSOActivity.this, "Manage Profile Selected", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainSOActivity.this, StoreOwnerProfileActivity.class);
                    startActivity(intent);
                    finish();
                } else if (itemId == R.id.menuLogout) {
                    authProfileSO = FirebaseAuth.getInstance();
                    authProfileSO.signOut();
                    Intent intent = new Intent(MainSOActivity.this, LoginAsStoreOwner.class);

                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.menu_home_so);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_home_so) {
                // Handle the Home menu item click here
                return true;
            } else if (itemId == R.id.menu_inventory) {
                Toast.makeText(MainSOActivity.this, "Manage Inventory Selected", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainSOActivity.this, ManageInventoryActivity.class);
                startActivity(intent);
                finish();
            } else if (itemId == R.id.menu_pos) {
                // Handle the Profile menu item click here
                Intent intent = new Intent(MainSOActivity.this, PointOfSaleActivity.class);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.menu_qr_gene) {
                // Handle the QR menu item click here
                Intent intent = new Intent(MainSOActivity.this, QrGeneratorActivity.class);
                startActivity(intent);
                finish();
                return true;
            }
            return false;
        });

        firebaseUserSO = authProfileSO.getCurrentUser();
        if (firebaseUserSO == null) {
            Intent intent = new Intent(getApplicationContext(), LoginAsStoreOwner.class);
            startActivity(intent);
            finish();
        } else {
            textViewSO.setText(firebaseUserSO.getEmail());
        }
    }

    private void swipeToRefresh() {
        swipeContainer = findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(() -> {
            startActivity(getIntent());
            finish();
            overridePendingTransition(0, 0);
            swipeContainer.setRefreshing(false);
        });

        swipeContainer.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
        );
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
