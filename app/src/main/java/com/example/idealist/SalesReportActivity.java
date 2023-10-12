package com.example.idealist;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SalesReportActivity extends AppCompatActivity {

    private Map<String, Sale> salesMap = new HashMap<>();

    private String userUid;
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
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userUid = currentUser.getUid(); // Initialize userUid with the UID of the currently logged-in user
            fetchSalesData(userUid); // Now userUid is initialized and can be used to fetch data
        }
    }

    // Function to get sales data for a specific user
    private void fetchSalesData(String userUid) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            String currentUid = currentUser.getUid();

            // Check if the provided userUid matches the current user's UID
            if (userUid.equals(currentUid)) {
                DatabaseReference salesRef = FirebaseDatabase.getInstance().getReference("Sales");

                // Add a query to filter the sales data by the user's UID
                Query userSalesQuery = salesRef.orderByChild("userUid").equalTo(userUid);

                userSalesQuery.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Clear the existing data in the HashMap
                        salesMap.clear();

                        for (DataSnapshot saleSnapshot : dataSnapshot.getChildren()) {
                            String saleId = saleSnapshot.getKey();
                            String productNameFull = saleSnapshot.child("productName").getValue(String.class);
                            // Extract the actual product name without "Product Name:"
                            String productName = productNameFull.replace("Product Name: ", "");
                            double price = saleSnapshot.child("price").getValue(Double.class);
                            int quantity = saleSnapshot.child("quantity").getValue(Integer.class);
                            String timestamp = saleSnapshot.child("timestamp").getValue(String.class);

                            // Log the fetched data
                            Log.d(TAG, "Sales Product Name: " + productName);
                            Log.d(TAG, "Sales Price: " + price);
                            Log.d(TAG, "Sales Quantity: " + quantity);
                            Log.d(TAG, "Sales Timestamp: " + timestamp);

                            // Store the data in the HashMap
                            Sale sale = new Sale(productName, price, quantity, timestamp);
                            salesMap.put(saleId, sale);
                        }

                        // Do something with the data, e.g., display it in a table or list
                        displaySalesData();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Handle any errors that occur during the database query
                        Log.e(TAG, "Sales Error: " + databaseError.getMessage());
                        Log.e(TAG, "Sales Error Details: " + databaseError.getDetails());
                        Toast.makeText(SalesReportActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                // Handle the case where the userUid does not match the current user's UID
                Log.e(TAG, "User UID does not match the currently logged-in user.");
            }
        } else {
            // Handle the case where no user is currently logged in
            Log.e(TAG, "No user is currently logged in.");
        }
    }


    // Helper function to display sales data
    private void displaySalesData() {
        TableLayout tableLayout = findViewById(R.id.salesTableLayout);

        // Clear the existing data in the TableLayout
        tableLayout.removeAllViews();

        for (Map.Entry<String, Sale> entry : salesMap.entrySet()) {
            Sale sale = entry.getValue();

            TableRow row = new TableRow(this);

            // Add plain text without customization
            addTextView(row, sale.getProductName());
            addTextView(row, String.valueOf(sale.getPrice()));
            addTextView(row, String.valueOf(sale.getQuantity()));
            addTextView(row, sale.getTimestamp());

            // Add the TableRow to the TableLayout
            tableLayout.addView(row);
        }
    }

    // Helper method to add a plain TextView to a TableRow
    private void addTextView(TableRow row, String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        row.addView(textView);
    }



    public class Sale {
        private String productName;
        private double price;
        private int quantity;
        private String timestamp;

        public Sale(String productName, double price, int quantity, String timestamp) {
            this.productName = productName;
            this.price = price;
            this.quantity = quantity;
            this.timestamp = timestamp;
        }

        // Add getter methods for the fields (productName, price, quantity, timestamp)
        public String getProductName() {
            return productName;
        }

        public double getPrice() {
            return price;
        }

        public int getQuantity() {
            return quantity;
        }

        public String getTimestamp() {
            return timestamp;
        }
    }
}