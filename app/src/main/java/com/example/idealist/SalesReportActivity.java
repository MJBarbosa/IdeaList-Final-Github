package com.example.idealist;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SalesReportActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_report);

        getSupportActionBar().setTitle("Sales");

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.menu_sales_report);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_home_so) {
                // Handle the Home menu item click here
                Intent intent = new Intent(SalesReportActivity.this, MainSOActivity.class);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.menu_inventory) {
                // You are already in the Inventory tab, no action needed
                Intent intent = new Intent(SalesReportActivity.this, ManageInventoryActivity.class);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.menu_pos) {
                Intent intent = new Intent(SalesReportActivity.this, PointOfSaleActivity.class);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.menu_qr_gene) {
                Intent intent = new Intent(SalesReportActivity.this, QrGeneratorActivity.class);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.menu_sales_report) {
                // You are already in the Sales tab, no action needed
                return true;
            }
            return false;
        });
    }
}