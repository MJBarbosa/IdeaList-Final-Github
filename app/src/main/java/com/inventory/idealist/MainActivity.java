package com.inventory.idealist;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.inventory.idealist.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

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
    // Create a member variable to store the selected store information
    private String selectedStoreName;
    private String selectedUserUid;
    private AutoCompleteTextView autoCompleteTextView;
    private Map<String, StoreAdapter.Store> storeMap = new HashMap<>();
    private String currentStoreLocation;
    private String currentMobile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setTitle("IdealList");

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
            textView.setText(firebaseUser.getDisplayName());
            fetchStoreOwnerData();
        }

        ImageButton buttonCustomerCart = findViewById(R.id.buttonCustomerCart);
        buttonCustomerCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CustomerCartActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Initialize the AutoCompleteTextView
        autoCompleteTextView = findViewById(R.id.autoCompleteTextViewCustomerSearch);
        ArrayAdapter<String> autoCompleteAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, getStoreName());
        autoCompleteTextView.setAdapter(autoCompleteAdapter);

        autoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed, but must be implemented.
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // This method is called whenever the text in the AutoCompleteTextView changes.
                // You can use it to filter the store names.
                filterStoreNames(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not needed, but must be implemented.
            }
        });
    }

    private void filterStoreNames(String userInput) {
        List<String> filteredStoreNames = new ArrayList<>();

        for (StoreAdapter.Store store : storeMap.values()) {
            if (store.getStoreName().toLowerCase().contains(userInput.toLowerCase())) {
                filteredStoreNames.add(store.getStoreName());
            }
        }

        ArrayAdapter<String> adapter = (ArrayAdapter<String>) autoCompleteTextView.getAdapter();
        if (adapter != null) {
            adapter.clear();
            adapter.addAll(filteredStoreNames);
            adapter.notifyDataSetChanged();
        }
    }

    // Get a list of product names for the AutoCompleteTextView
    private List<String> getStoreName() {
        List<String> storeNames = new ArrayList<>();
        for (StoreAdapter.Store store : storeMap.values()) {
            storeNames.add(store.getStoreName());
        }
        return storeNames;
    }

    private void fetchStoreOwnerData() {
        DatabaseReference userRolesReference = FirebaseDatabase.getInstance().getReference("UserRolesCus");
        String currentUserId = firebaseUser.getUid();

        userRolesReference.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String role = dataSnapshot.getValue(String.class);
                    Log.d(TAG, "User Role: " + role);

                    if ("customer".equals(role)) {
                        DatabaseReference storeOwnerReference = FirebaseDatabase.getInstance().getReference("Registered Store Owner Users");
                        Log.d(TAG, "Getting Store Name Link: " + storeOwnerReference);

                        storeOwnerReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                HashMap<String, String> storeMap = new HashMap<>();

                                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                    String userUid = userSnapshot.getKey();
                                    String fullName = userSnapshot.child("fullName").getValue(String.class);
                                    String storeName = userSnapshot.child("storeName").getValue(String.class);
                                    String dob = userSnapshot.child("dob").getValue(String.class);
                                    String gender = userSnapshot.child("gender").getValue(String.class);
                                    String mobile = userSnapshot.child("mobile").getValue(String.class);
                                    String storeLocation = userSnapshot.child("storeLocation").getValue(String.class);

                                    Log.d(TAG, "User UID: " + userUid);
                                    Log.d(TAG, "Full Name: " + fullName);
                                    Log.d(TAG, "Store Name: " + storeName);
                                    Log.d(TAG, "Date of Birth: " + dob);
                                    Log.d(TAG, "Gender: " + gender);
                                    Log.d(TAG, "Mobile: " + mobile);
                                    Log.d(TAG, "Store Location: " + storeLocation);

                                    if (storeName != null) {
                                        storeMap.put(storeName, userUid);
                                        currentStoreLocation = storeLocation;
                                        currentMobile = mobile;
                                    }
                                }

                                for (Map.Entry<String, String> entry : storeMap.entrySet()) {
                                    Log.d(TAG, "Store Name: " + entry.getKey() + ", User UID: " + entry.getValue());
                                }

                                storeAdapter = new StoreAdapter(MainActivity.this, storeMap, (storeName, userUid) -> {
                                    // Handle the click event or navigation if needed
                                    navigateToStoreProducts(storeName, userUid);
                                });

                                storeRecyclerView.setAdapter(storeAdapter);
                                storeRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.e("Firebase", "Data retrieval error: " + databaseError.getMessage());
                                Toast.makeText(MainActivity.this, "Data retrieval error", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Log.d(TAG, "User is not a customer.");
                        Toast.makeText(MainActivity.this, "You are not a customer", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d(TAG, "DataSnapshot does not exist for the current user.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Role check error: " + databaseError.getMessage());
                Toast.makeText(MainActivity.this, "Role check error", Toast.LENGTH_SHORT).show();
            }
        });
    }




    private void navigateToStoreProducts(String storeName, String userUid) {
        Intent intent = new Intent(this, CustomerStoreProductActivity.class);
        intent.putExtra("storeName", storeName);
        Log.d(TAG, "Navigate Store Name: " + storeName);
        intent.putExtra("userUid", userUid);
        Log.d(TAG, "Navigate User UID: " + userUid);
        intent.putExtra("storeLocation", currentStoreLocation);
        Log.d(TAG, "Navigate Store Location: " + currentStoreLocation);
        intent.putExtra("mobile", currentMobile);
        Log.d(TAG, "Navigate Store Location: " + currentMobile);
        startActivity(intent);
    }


    private void fetchAndDisplayStoreData(String storeName, String userUid) {
        selectedStoreName = storeName;
        selectedUserUid = userUid;
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
                    String productId = productSnapshot.child("productId").getValue(String.class);

                    products.add(new PointOfSaleActivity.Product(productName, productDescription, category, quantity, productPrice, productId));
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
        private Context context;

        public StoreAdapter(Context context, HashMap<String, String> storeMap, StoreClickListener clickListener) {
            this.context = context;
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
            private FirebaseStorage storage;
            private ImageView imageView;

            StoreViewHolder(@NonNull View itemView) {
                super(itemView);
                storeNameTextView = itemView.findViewById(R.id.textViewStoreNameCus);
                storage = FirebaseStorage.getInstance();
                imageView = itemView.findViewById(R.id.imageViewStore);

                itemView.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Map.Entry<String, String> entry = getItemAtPosition(position);
                        clickListener.onStoreClick(entry.getKey(), entry.getValue());
                    }
                });
            }

            void bind(String storeName, String userUid) {
                //String storeLocation = "Location Placeholder";

                // Concatenate storeLocation and storeName
                //String storeLocationAndName = storeLocation + " - " + storeName;
                storeNameTextView.setText(storeName);
                displayStoreImage(userUid, storeName, storeMap);
            }
            private void displayStoreImage(String userUid, String storeName, Map<String, String> storeMap) {
                String storeOwnerUid = storeMap.get(storeName);

                if (storeOwnerUid != null && userUid.equals(storeOwnerUid)) {
                    // Create a reference to the specific image in the "DisplayPics" folder
                    StorageReference imageRef = storage.getReference().child("DisplayPics/" + userUid + "/displaypic.jpg");

                    // Get the download URL for the image
                    imageRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                // Load the image into the ImageView using a library like Picasso or Glide
                                Picasso.get().load(uri).into(imageView);
                            })
                            .addOnFailureListener(e -> {
                                // Handle any errors that may occur
                                Log.e(TAG, "Error getting image URL: " + e.getMessage());
                                // You can set a default image or show an error message here
                            });
                } else {
                    // Handle the case where the userUids don't match (e.g., no permission)
                    Log.e(TAG, "UserUid does not match storeName.");
                    // You can set a default image or show an error message here
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
}