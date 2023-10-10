package com.example.idealist;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.idealist.model.Product;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AddToCartActivity extends AppCompatActivity {

    private Cart cart; // Use PointOfSaleActivity.Cart
    private PointOfSaleActivity.Product selectedProduct;
    private int currentQuantity = 0;
    private CartItemAdapter adapter; // Declare the adapter

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_to_cart); // Set the content view to add_to_cart.xml
        getSupportActionBar().setTitle("Shopping Cart"); // Set the title as needed

        // Retrieve the selected product and cart items from the intent
        // Retrieve the selected product and cart items from the intent using type-safe method
        Intent intent = getIntent();
        //ArrayList<Product> productList = intent.getParcelableArrayListExtra("productList", Product.class);
        cart = (Cart) intent.getSerializableExtra("cart");


        if (selectedProduct != null) {
            Log.d(TAG, "Selected Product received: " + selectedProduct.getProductName());
        } else {
            Log.d(TAG, "Selected Product is null.");
            // Handle the case where selectedProduct is null
        }

        if (cart == null) {
            Log.d(TAG, "Cart is null.");
            cart = new Cart();
        }

        List<PointOfSaleActivity.Product> cartItems = cart.getItems();

        for (PointOfSaleActivity.Product cartItem : cartItems) {
            String productName = cartItem.getProductName();
            String price = cartItem.getPrice();
            // Access other properties as needed
            Log.d(TAG, "Product Name: " + productName);
            Log.d(TAG, "Price: " + price);
        }




        // Create a list containing the selected product
        List<PointOfSaleActivity.Product> productList = new ArrayList<>();
        if (selectedProduct != null) {
            productList.add(selectedProduct);
        }

        // Find views from the 'add_to_cart' layout
        TextView textViewTotalAmount = findViewById(R.id.textViewTotalAmount);
        Button buttonCheckout = findViewById(R.id.buttonCheckout);

        // Initialize your RecyclerView and set up the adapter
        RecyclerView recyclerViewCart = findViewById(R.id.recyclerViewCart);
        recyclerViewCart.setLayoutManager(new LinearLayoutManager(this));

        // Create and set the CartItemAdapter with the items in the cart
        adapter = new CartItemAdapter(cart.getItems(), productList);
        recyclerViewCart.setAdapter(adapter);

        // Set the total amount text based on the cart items
        // Calculate the total amount based on cartItems
        double totalAmount = calculateTotalAmount(cartItems);
        textViewTotalAmount.setText("Total Amount: $" + totalAmount);

        // Handle the "Checkout" button click
        buttonCheckout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the checkout action, e.g., start a new activity or show a dialog
                // based on the items in the cart.
            }
        });
    }

    // Add this method to calculate the total amount
    private double calculateTotalAmount(List<PointOfSaleActivity.Product> cartItems) {
        if (cartItems == null) {
            return 0.0; // Handle the case where cartItems is null
        }
        double totalAmount = 0.0;
        for (PointOfSaleActivity.Product cartItem : cartItems) {
            totalAmount += Double.parseDouble(cartItem.getPrice());
        }
        return totalAmount;
    }

    public class Cart {
        private List<PointOfSaleActivity.Product> items;

        public Cart() {
            items = new ArrayList<>();
        }

        public void addItem(PointOfSaleActivity.Product product) {
            items.add(product);
        }

        public void removeItem(PointOfSaleActivity.Product product) {
            items.remove(product);
        }

        public List<PointOfSaleActivity.Product> getItems() {
            return items;
        }

        public void clearCart() {
            items.clear();
        }

        public double calculateTotal(PointOfSaleActivity.Product selectedProduct) {
            double total = 0.0;
            try {
                double selectedProductPrice = Double.parseDouble(selectedProduct.getPrice());
                for (PointOfSaleActivity.Product product : items) {
                    // Calculate the total price by summing up the prices of all items in the cart
                    total += Double.parseDouble(product.getPrice());
                }
            } catch (NumberFormatException e) {
                // Handle the case where the price cannot be parsed as a double
                e.printStackTrace();
            }
            return total;
        }
    }

    public class Product implements Serializable {
        private String productId;
        private String userUid;
        private String productName;
        private String supplierName;
        private String category;
        private String productDescription;
        private String quantity;
        private String price;

        // Constructors, getters, setters, and other methods go here
        public Product() {
            this.productId = ""; // Initialize productId to an empty string
        }


        public String getUserUid() {
            return userUid;
        }

        public void setUserUid(String userUid) {
            this.userUid = userUid;
        }

        // Add getters and setters for each field
        public String getProductId() {
            return productId;
        }

        public void setProductId(String productId) {
            this.productId = productId;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public String getSupplierName() {
            return supplierName;
        }

        public void setSupplierName(String supplierName) {
            this.supplierName = supplierName;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getProductDescription() {
            return productDescription;
        }

        public void setProductDescription(String productDescription) {
            this.productDescription = productDescription;
        }

        public String getQuantity() {
            return quantity;
        }

        public void setQuantity(String quantity) {
            this.quantity = quantity;
        }

        public String getPrice() {
            return price;
        }

        public void setPrice(String price) {
            this.price = price;
        }
    }
}
