package com.example.idealist;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {
    private FirebaseUser firebaseUser;
    private TextView textView;
    private FirebaseAuth authProfile;
    // Declare the RecyclerView and its adapter as class members
    private RecyclerView storeRecyclerView;
    private StoreAdapter storeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        authProfile = FirebaseAuth.getInstance();
        textView = findViewById(R.id.textView);
        firebaseUser = authProfile.getCurrentUser();
        storeRecyclerView = findViewById(R.id.storeRecyclerView);

        if (firebaseUser == null) {
            // User is not authenticated, redirect to login
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        } else {
            textView.setText(firebaseUser.getEmail());
            fetchStoreOwnerData();
        }
    }

    private void fetchStoreOwnerData() {
        DatabaseReference userRolesReference = FirebaseDatabase.getInstance().getReference("UserRolesCus");
        String currentUserId = firebaseUser.getUid();

        // Check if the current user has the role of "customer"
        userRolesReference.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    DataSnapshot userSnapshot = dataSnapshot.child("role"); // Replace with your actual child name
                    String userUid = userSnapshot.getKey(); // Get the user's UID
                    String storeName = userSnapshot.child("storeName").getValue(String.class);

                    if (userSnapshot.exists()) {
                        String role = userSnapshot.getValue(String.class);
                        if (role != null && role.equals("customer")) {
                            // The user has the role of "customer," so proceed to fetch store owner data
                            DatabaseReference storeOwnerReference = FirebaseDatabase.getInstance().getReference("Registered Store Owner Users");

                            storeOwnerReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    HashMap<String, String> storeMap = new HashMap<>();
                                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                        String userUid = userSnapshot.getKey(); // Get the user's UID

                                        if (userSnapshot.hasChild("storeName")) {
                                            String storeName = userSnapshot.child("storeName").getValue(String.class);

                                            if (storeName != null && userUid != null) {
                                                storeMap.put(storeName, userUid);
                                            }
                                        }
                                    }

                                    storeAdapter = new StoreAdapter(storeMap, (storeName, userUid) -> {
                                        fetchAndDisplayStoreData(storeName, userUid);
                                    });

                                    storeRecyclerView.setAdapter(storeAdapter);
                                    storeRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    // Handle errors during data retrieval
                                    Log.e("Firebase", "Data retrieval error: " + databaseError.getMessage());
                                    Toast.makeText(MainActivity.this, "Data retrieval error", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            // The user does not have the role of "customer." Handle this case.
                            Log.d(TAG, "User does not have the role of 'customer'");
                            Toast.makeText(MainActivity.this, "You do not have the role of 'customer'", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Handle the case where "role" child doesn't exist
                        // ...
                    }
                } else {
                    // Handle the case where the dataSnapshot doesn't exist
                    // ...
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors during role check
                Log.e("Firebase", "Role check error: " + databaseError.getMessage());
                Toast.makeText(MainActivity.this, "Role check error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchAndDisplayStoreData(String storeName, String userUid) {
        // Fetch product information for the selected store based on storeName and userUid
        DatabaseReference productReference = FirebaseDatabase.getInstance().getReference("ProductInventory");

        Query query = productReference.orderByChild("storeName").equalTo(storeName).orderByChild("userUid").equalTo(userUid);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<PointOfSaleActivity.Product> products = new ArrayList<>();
                for (DataSnapshot productSnapshot : dataSnapshot.getChildren()) {
                    String productName = productSnapshot.child("productName").getValue(String.class);
                    String productDescription = productSnapshot.child("productDescription").getValue(String.class);
                    String category = productSnapshot.child("category").getValue(String.class);
                    String quantity = productSnapshot.child("quantity").getValue(String.class);
                    String productPrice = productSnapshot.child("productPrice").getValue(String.class);

                    products.add(new PointOfSaleActivity.Product(productName, productDescription, category, quantity, productPrice));
                }

                // Update the RecyclerView with the products
                storeAdapter.updateProducts(products);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors during data retrieval
                Log.e("Firebase", "Data retrieval error: " + databaseError.getMessage());
                Toast.makeText(MainActivity.this, "Data retrieval error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Creating ActionBar Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate menu items
        getMenuInflater().inflate(R.menu.common_menu_home, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //When any menu item is selected
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menuManageProfile) {
            Intent intent = new Intent(MainActivity.this, UsersProfileActivity.class);
            startActivity(intent);
        } else if (id == R.id.menuLogoutHome) {
            authProfile.signOut();
            Toast.makeText(MainActivity.this, "Logged Out", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(MainActivity.this, Login.class);

            //Clear stack to prevent user coming back to UserProfileActivity on pressing back button after Logging out
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(MainActivity.this, "Something Went Wrong!", Toast.LENGTH_LONG).show();
        }
        return super.onOptionsItemSelected(item);
    }

    public static class StoreAdapter extends RecyclerView.Adapter<StoreAdapter.StoreViewHolder> {

        private HashMap<String, String> storeMap;
        private List<PointOfSaleActivity.Product> products;
        private StoreClickListener clickListener;

        public StoreAdapter(HashMap<String, String> storeMap, StoreClickListener clickListener) {
            this.storeMap = storeMap;
            this.clickListener = clickListener;
            this.products = new ArrayList<>();
        }

        // Add this method to update the products in the adapter
        public void updateProducts(List<PointOfSaleActivity.Product> updatedProducts) {
            products.clear();
            products.addAll(updatedProducts);
            notifyDataSetChanged();
        }

        public interface StoreClickListener {
            void onStoreClick(String storeName, String userUid);
        }

        @NonNull
        @Override
        public StoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.store_button, parent, false);
            return new StoreViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull StoreViewHolder holder, int position) {
            Map.Entry<String, String> entry = getItemAtPosition(position);
            holder.bind(entry.getKey(), entry.getValue());
        }

        @Override
        public int getItemCount() {
            return storeMap.size();
        }

        private Map.Entry<String, String> getItemAtPosition(int position) {
            int index = 0;
            for (Map.Entry<String, String> entry : storeMap.entrySet()) {
                if (index == position) {
                    return entry;
                }
                index++;
            }
            throw new IndexOutOfBoundsException("Position not found in storeMap");
        }

        class StoreViewHolder extends RecyclerView.ViewHolder {
            private TextView storeNameTextView;

            StoreViewHolder(@NonNull View itemView) {
                super(itemView);
                storeNameTextView = itemView.findViewById(R.id.textViewStoreNameCus);

                itemView.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Map.Entry<String, String> entry = getItemAtPosition(position);
                        clickListener.onStoreClick(entry.getKey(), entry.getValue());
                    }
                });
            }

            void bind(String storeName, String userUid) {
                storeNameTextView.setText(storeName);
            }
        }
    }

    public class Store {
        private String storeName;
        private String userUid;

        public Store(String storeName, String userUid) {
            this.storeName = storeName;
            this.userUid = userUid;
        }

        public String getStoreName() {
            return storeName;
        }

        public String getUserUid() {
            return userUid;
        }
    }


}