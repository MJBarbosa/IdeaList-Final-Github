package com.example.idealist;

import static android.content.ContentValues.TAG;

import android.content.Intent;
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
import java.util.List;

public class AddToCartActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CartAdapter cartAdapter;
    private List<PointOfSaleActivity.Product> cartItems;
    private TextView totalTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_to_cart);

        getSupportActionBar().setTitle("Shopping Cart");

        // Initialize recyclerView and totalTextView
        recyclerView = findViewById(R.id.recyclerViewCart);
        totalTextView = findViewById(R.id.textViewTotal);

        // Get the cart items passed from PointOfSaleActivity
        Intent intent = getIntent();
        cartItems = intent.getParcelableArrayListExtra("cart");

        if (cartItems.isEmpty()) {
            // Handle the case when the cart is empty (e.g., display a message)
            Toast.makeText(this, "Your cart is empty.", Toast.LENGTH_SHORT).show();
            // Optionally, you can finish the activity or take other actions
        } else {
            // Set up RecyclerView for displaying the cart items
            cartAdapter = new CartAdapter(cartItems);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(cartAdapter);

            // Calculate and display the total price
            double total = calculateTotal(cartItems);
            totalTextView.setText("Total: $" + formatPrice(total));
        }

        // Handle checkout button click
        Button checkoutButton = findViewById(R.id.buttonCheckout);
        checkoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Implement checkout logic here
                // You can proceed with the payment process or any other actions
                // you want to perform when the user checks out
                // For this example, let's just display a message
                Toast.makeText(AddToCartActivity.this, "Checkout clicked", Toast.LENGTH_SHORT).show();
            }
        });
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
    private class CartAdapter extends RecyclerView.Adapter<CartViewHolder> {

        private List<PointOfSaleActivity.Product> cartItems;

        public CartAdapter(List<PointOfSaleActivity.Product> cartItems) {
            this.cartItems = cartItems;
        }

        @NonNull
        @Override
        public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_item_layout, parent, false);
            return new CartViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
            PointOfSaleActivity.Product product = cartItems.get(position);
            holder.bind(product);
        }

        @Override
        public int getItemCount() {
            return cartItems.size();
        }
    }

    // ViewHolder for each cart item in RecyclerView
    private class CartViewHolder extends RecyclerView.ViewHolder {

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

