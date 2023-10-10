package com.example.idealist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.idealist.model.Product;

import java.util.ArrayList;
import java.util.List;

public class CartItemAdapter extends RecyclerView.Adapter<CartItemAdapter.ViewHolder> {
    private List<PointOfSaleActivity.Product> productList;
    private List<PointOfSaleActivity.Product> cartItemList;

    public CartItemAdapter(List<PointOfSaleActivity.Product> cartItems, List<PointOfSaleActivity.Product> productDetails) {
        this.cartItemList = (cartItems != null) ? cartItems : new ArrayList<>();
        this.productList = (productDetails != null) ? productDetails : new ArrayList<>();
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cart_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PointOfSaleActivity.Product product = productList.get(position);

        // Bind product data to the cart_item_layout views
        holder.itemNameTextView.setText(product.getProductName());
        holder.itemDescriptionTextView.setText(product.getProductDescription());
        holder.itemCategoryTextView.setText(product.getCategory());
        holder.itemQuantityTextView.setText(product.getQuantity());
        holder.itemPriceTextView.setText("$" + product.getPrice());
        // You can also load and display the item image here if needed
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView itemNameTextView;
        TextView itemDescriptionTextView;
        TextView itemCategoryTextView;
        TextView itemQuantityTextView;
        TextView itemPriceTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            itemNameTextView = itemView.findViewById(R.id.itemNameTextView);
            itemDescriptionTextView = itemView.findViewById(R.id.itemDescriptionTextView);
            itemCategoryTextView = itemView.findViewById(R.id.itemCategoryTextView);
            itemQuantityTextView = itemView.findViewById(R.id.itemQuantityTextView);
            itemPriceTextView = itemView.findViewById(R.id.itemPriceTextView);
        }
    }
}
