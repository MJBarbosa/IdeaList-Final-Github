package com.example.idealist;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ManageInventoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_inventory);

        getSupportActionBar().setTitle("Manage Inventory");

        Toast.makeText(ManageInventoryActivity.this, "You can Manage Product now", Toast.LENGTH_LONG).show();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_home_so) {
                // Handle the Home menu item click here
                Intent intent = new Intent(ManageInventoryActivity.this, MainSOActivity.class);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.menu_inventory) {
                // You are already in the Inventory tab, no action needed
                return true;
            } else if (itemId == R.id.menu_pos) {
                // Handle the Profile menu item click here
                Intent intent = new Intent(ManageInventoryActivity.this, PointOfSaleActivity.class);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.menu_qr_gene) {
                // Handle the QR menu item click here
                Intent intent = new Intent(ManageInventoryActivity.this, QrGeneratorActivity.class);
                startActivity(intent);
                finish();
                return true;
            }
            return false;
        });

        ImageButton imageButtonAddProduct = findViewById(R.id.imageButtonAddProduct);
        ImageButton imageButtonUpdateProduct = findViewById(R.id.imageButtonUpdateProduct);
        ImageButton imageButtonDeleteProduct = findViewById(R.id.imageButtonDeleteProduct);
        ImageButton imageButtonViewProduct = findViewById(R.id.imageButtonViewProduct);

        imageButtonAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManageInventoryActivity.this, AddProductActivity.class);
                startActivity(intent);
                finish();
            }
        });

        imageButtonUpdateProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManageInventoryActivity.this, UpdateProductActivity.class);
                startActivity(intent);
                finish();
            }
        });

        imageButtonDeleteProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManageInventoryActivity.this, DeleteProductActivity.class);
                startActivity(intent);
                finish();
            }
        });

        imageButtonViewProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManageInventoryActivity.this, ViewProductActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
