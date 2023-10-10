package com.example.idealist;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AddToCartActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private RecyclerView recyclerView;
    private CartAdapter cartAdapter;
    private List<PointOfSaleActivity.Product> cartItems;
    private TextView totalTextView;
    private static final String CART_PREFERENCES = "cart_preferences";
    private static final String CART_ITEMS_KEY = "cart_items";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_to_cart);

        getSupportActionBar().setTitle("Shopping Cart");

        sharedPreferences = getSharedPreferences(CART_PREFERENCES, MODE_PRIVATE);


        // Initialize recyclerView and totalTextView
        recyclerView = findViewById(R.id.recyclerViewCart);
        totalTextView = findViewById(R.id.textViewTotal);

        // Get the cart items passed from PointOfSaleActivity
        Intent intent = getIntent();
        cartItems = intent.getParcelableArrayListExtra("cart");

        // Initialize the adapter and set it to the RecyclerView
        cartAdapter = new CartAdapter(cartItems);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(cartAdapter);

        // Call the method to display cart items
        displayCartItems();

        // Calculate and display the total price
        double total = calculateTotal(cartItems);
        totalTextView.setText("Total: $" + formatPrice(total));

        // Handle checkout button click
        Button checkoutButton = findViewById(R.id.buttonCheckout);
        checkoutButton.setOnClickListener(v -> {
            // Implement checkout logic here
            // You can proceed with the payment process or any other actions
            // you want to perform when the user checks out
            // For this example, let's just display a message
            Toast.makeText(AddToCartActivity.this, "Checkout clicked", Toast.LENGTH_SHORT).show();
        });
    }

    private void displayCartItems() {
        Set<String> cartItemsSet = sharedPreferences.getStringSet(CART_ITEMS_KEY, new HashSet<>());
        List<PointOfSaleActivity.Product> updatedCartItems = new ArrayList<>();

        // Iterate through cart items and create Product objects
        for (String cartItem : cartItemsSet) {
            String[] cartItemParts = cartItem.split(",");
            // Extract product details from cartItemParts and create a Product object
            PointOfSaleActivity.Product product = new PointOfSaleActivity.Product(
                    cartItemParts[0], cartItemParts[1], cartItemParts[2], cartItemParts[3], cartItemParts[4]
            );
            updatedCartItems.add(product);
        }

        // Update the cart items in the adapter
        cartAdapter.updateCartItems(updatedCartItems);
    }


    private double calculateTotal(List<PointOfSaleActivity.Product> cartItems) {
        double total = 0.0;
        for (PointOfSaleActivity.Product product : cartItems) {
            try {
                double price = Double.parseDouble(product.getPrice());
                total += price;
            } catch (NumberFormatException e) {
                // Handle the case where the price cannot be parsed as a double
                e.printStackTrace();
            }
        }
        return total;
    }

    private String formatPrice(double price) {
        // Format the price with two decimal places
        DecimalFormat decimalFormat = new DecimalFormat("#0.00");
        return decimalFormat.format(price);
    }

    // Adapter for displaying cart items in RecyclerView
    private class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

        private List<PointOfSaleActivity.Product> cartItems;

        public CartAdapter(List<PointOfSaleActivity.Product> cartItems) {
            this.cartItems = cartItems;
        }

        @NonNull
        @Override
        public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Inflate the layout for each cart item view
            return new CartViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.cart_item_layout, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
            // Bind data to the cart item view
            PointOfSaleActivity.Product product = cartItems.get(position);
            holder.bind(product);
        }

        @Override
        public int getItemCount() {
            return cartItems.size();
        }

        public void updateCartItems(List<PointOfSaleActivity.Product> updatedCartItems) {
            // Update the cart items and refresh the adapter
            cartItems = updatedCartItems;
            notifyDataSetChanged();
        }

        // ViewHolder for each cart item in RecyclerView
        class CartViewHolder extends RecyclerView.ViewHolder {

            private TextView productNameTextView;
            private TextView productDescriptionTextView;
            private TextView productCategoryTextView;
            private TextView productQuantityTextView;
            private TextView productPriceTextView;

            public CartViewHolder(@NonNull View itemView) {
                super(itemView);
                productNameTextView = itemView.findViewById(R.id.itemNameTextView);
                productDescriptionTextView = itemView.findViewById(R.id.itemDescriptionTextView);
                productCategoryTextView = itemView.findViewById(R.id.itemCategoryTextView);
                productQuantityTextView = itemView.findViewById(R.id.itemQuantityTextView);
                productPriceTextView = itemView.findViewById(R.id.itemPriceTextView);
            }

            public void bind(PointOfSaleActivity.Product product) {
                productNameTextView.setText(product.getProductName());
                productDescriptionTextView.setText(product.getProductDescription());
                productCategoryTextView.setText(product.getCategory());
                productQuantityTextView.setText(product.getQuantity());
                productPriceTextView.setText(product.getPrice());
            }
        }
    }
}

