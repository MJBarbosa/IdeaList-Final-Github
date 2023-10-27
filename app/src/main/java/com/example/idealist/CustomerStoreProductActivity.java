package com.example.idealist;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CustomerStoreProductActivity extends AppCompatActivity {
    private RecyclerView productsRecyclerView;
    private ProductsAdapter productsAdapter;
    private String selectedUserUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_store_product);

        String storeName = getIntent().getStringExtra("storeName");

        getSupportActionBar().setTitle("Available Products " + storeName);

        productsRecyclerView = findViewById(R.id.productRecyclerView);
        productsRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        // Initialize your adapter
        ProductsAdapter productsAdapter = new ProductsAdapter(this);

        // Get a reference to the RecyclerView
        RecyclerView recyclerView = findViewById(R.id.productRecyclerView);

        // Set the adapter for the RecyclerView
        recyclerView.setAdapter(productsAdapter);

        // Fetch and display store products
        String userUid = getIntent().getStringExtra("userUid");
        fetchAndDisplayStoreProducts(productsAdapter, userUid);
    }


    private void fetchAndDisplayStoreProducts(ProductsAdapter productsAdapter, String userUid) {
        DatabaseReference productReference = FirebaseDatabase.getInstance().getReference("ProductInventory").child(userUid);
        // No need for the query since you're directly accessing the user's products
        Log.d(TAG, "Fetching products for User UID: " + userUid);


        productReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "Data retrieval successful.");

                List<PointOfSaleActivity.Product> productList = new ArrayList<>();

                for (DataSnapshot productSnapshot : dataSnapshot.getChildren()) {
                    HashMap<String, String> productData = (HashMap<String, String>) productSnapshot.getValue();

                    if (productData != null) {
                        String productId = productData.get("productId");
                        String productName = productData.get("productName");
                        String productDescription = productData.get("productDescription");
                        String category = productData.get("category");
                        String quantity = productData.get("quantity");
                        String productPrice = productData.get("price");

                        Log.d(TAG, "Product ID: " + productId);
                        Log.d(TAG, "Product Name: " + productName);
                        Log.d(TAG, "Product Description: " + productDescription);
                        Log.d(TAG, "Category: " + category);
                        Log.d(TAG, "Quantity: " + quantity);
                        Log.d(TAG, "Product Price: " + productPrice);

                        PointOfSaleActivity.Product product = new PointOfSaleActivity.Product(productName, productDescription, category, quantity, productPrice, productId);
                        productList.add(product);
                    }
                }

                // Set the product list to the adapter
                productsAdapter.setProductList(productList);
                selectedUserUid = userUid;
                Log.d(TAG, "Selected userUid: " + selectedUserUid);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Data retrieval error: " + databaseError.getMessage());
                Toast.makeText(CustomerStoreProductActivity.this, "Data retrieval error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == android.R.id.home) {
            Intent intent = new Intent(CustomerStoreProductActivity.this, MainActivity.class);
            startActivity(intent);
            finish();// Close the current activity and return to the previous one
            return true;
        }

        // Handle other menu items if needed
        // ...

        return super.onOptionsItemSelected(item);
    }


    public class ProductsAdapter extends RecyclerView.Adapter<ProductsAdapter.ProductViewHolder> {
        private List<PointOfSaleActivity.Product> productList;
        private Context context;

        public ProductsAdapter(Context context) {
            this.context = context;
            productList = new ArrayList<>();
        }

        public void setProductList(List<PointOfSaleActivity.Product> productList) {
            this.productList = productList;
            notifyDataSetChanged(); // Notify the adapter that data has changed
        }

        @NonNull
        @Override
        public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.customer_product_item, parent, false);
            return new ProductViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
            PointOfSaleActivity.Product product = productList.get(position);
            holder.bind(product);
        }

        @Override
        public int getItemCount() {
            return productList.size();
        }

        public class ProductViewHolder extends RecyclerView.ViewHolder {
            private ImageView productImage;
            private TextView productName;
            private TextView productDescription;
            private TextView category;
            private TextView quantity;
            private TextView price;

            public ProductViewHolder(@NonNull View itemView) {
                super(itemView);
                productImage = itemView.findViewById(R.id.productImage);
                productName = itemView.findViewById(R.id.productName);
                productDescription = itemView.findViewById(R.id.productDescription);
                category = itemView.findViewById(R.id.category);
                quantity = itemView.findViewById(R.id.quantity);
                price = itemView.findViewById(R.id.price);
            }

            public void bind(PointOfSaleActivity.Product product) {
                // Bind product data to the views
                productName.setText(product.getProductName());
                productDescription.setText(product.getProductDescription());
                category.setText(product.getCategory());
                quantity.setText(product.getQuantity());
                price.setText(product.getPrice());

                displayProductImage(product, productImage); // Pass the product and ImageView

            }

            private void displayProductImage(PointOfSaleActivity.Product product, ImageView imageView) {
                // Retrieve userUid and productId from the product
                String userUid = selectedUserUid;
                String productId = product.getProductId();

                // Create a reference to the specific image in the "ProductImages" folder
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference imageRef = storage.getReference().child("ProductImages/" + userUid + "/" + productId);

                Log.d(TAG, "UserUid Image: " + userUid);
                Log.d(TAG, "Product ID Image: " + productId);
                // Get the download URL for the image
                imageRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            // Load the image into the ImageView using Picasso
                            Picasso.get().load(uri).into(imageView);
                        })
                        .addOnFailureListener(e -> {
                            if (e instanceof StorageException && ((StorageException) e).getErrorCode() == StorageException.ERROR_OBJECT_NOT_FOUND) {
                                // Handle the case where the file does not exist
                                Log.e(TAG, "Image does not exist at location.");
                            } else {
                                // Handle other errors
                                Log.e(TAG, "Error getting image URL: " + e.getMessage());
                            }
                        });
            }
        }
    }
}