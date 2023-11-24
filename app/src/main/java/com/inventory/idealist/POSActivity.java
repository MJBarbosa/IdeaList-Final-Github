package com.inventory.idealist;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class POSActivity extends AppCompatActivity implements QRScannerFragment.OnProductScannedListener {

    private ProductAdapter productAdapter;
    private List<QRScannerFragment.Product> productList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posactivity);

        getSupportActionBar().setTitle("Point Of Sales");

        // Initialize productList
        productList = new ArrayList<>();

        // Inside POSActivity.java
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        QRScannerFragment qrScannerFragment = new QRScannerFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.relativeLayoutPOSScannerQR, qrScannerFragment).commit();
        fragmentTransaction.replace(R.id.relativeLayoutPOSScannerQR, qrScannerFragment);
        fragmentTransaction.commit();

        // Initialize productAdapter
        productAdapter = new ProductAdapter(productList, this);
        // Set up RecyclerView in POSActivity
        RecyclerView recyclerView = findViewById(R.id.recyclerViewProducts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(productAdapter);

        Button purchaseButton = findViewById(R.id.purchaseButtonPOS);
        purchaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPurchaseButtonClick(v);
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.menu_pos);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_home_so) {
                // Handle the Home menu item click here
                Intent intent = new Intent(POSActivity.this, MainSOActivity.class);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.menu_inventory) {
                // You are already in the Inventory tab, no action needed
                Intent intent = new Intent(POSActivity.this, ManageInventoryActivity.class);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.menu_pos) {
                // You are already in the POS tab, no action needed
                return true;
            } else if (itemId == R.id.menu_qr_gene) {
                Intent intent = new Intent(POSActivity.this, QrGeneratorActivity.class);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.menu_sales_report) {
                Intent intent = new Intent(POSActivity.this, SalesReportActivity.class);
                startActivity(intent);
                finish();
                return true;
            }
            return false;
        });
    }

    public QRScannerFragment.Product getProductById(String productId) {
        // TODO: Implement the logic to retrieve a product by its ID
        // For now, let's assume productList is a list of products and you have a method getProductById in your data retrieval logic.

        for (QRScannerFragment.Product product : productList) {
            if (product.getProductId().equals(productId)) {
                return product;
            }
        }

        return null;  // Product not found
    }

    public void onPurchaseButtonClick(View view) {
        // Update quantity in ProductInventory and add purchase data to Sales
        purchaseProducts();

        // Display receipt
        displayReceipt();
    }

    private void purchaseProducts() {
        // Iterate through the scanned products and update quantity in ProductInventory
        for (QRScannerFragment.Product product : productList) {
            // Assuming you have a method to update quantity in ProductInventory
            updateQuantityInProductInventory(product.getProductId(), product.getQuantity());

            // Add purchase data to Sales
            addPurchaseDataToSales(product);
        }

        // Update the overall total
        updateOverallTotal();

    }

    private String getCurrentUserUid() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            return currentUser.getUid();
        } else {
            return "";
        }
    }

    private void updateQuantityInProductInventory(String productId, int newQuantity) {
        DatabaseReference productRef = FirebaseDatabase.getInstance()
                .getReference()
                .child("ProductInventory")
                .child(getCurrentUserUid());

        Query query = productRef.orderByChild("productId").equalTo(productId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    DataSnapshot productSnapshot = dataSnapshot.getChildren().iterator().next();
                    String quantityStr = productSnapshot.child("quantity").getValue(String.class);
                    Log.d(TAG, "Quantity Current Value: " + quantityStr);

                    if (quantityStr != null) {
                        int currentQuantity = Integer.parseInt(quantityStr);

                        // Now you have the quantity value as an integer
                        int updatedQuantity = currentQuantity - newQuantity;

                        // Update the product quantity in the database as a string
                        String updatedQuantityStr = Integer.toString(updatedQuantity);
                        productSnapshot.child("quantity").getRef().setValue(updatedQuantityStr);

                        // You can add additional logic here if needed

                    } else {
                        // Handle cases where quantity is not set in the database
                        Log.e(TAG, "Quantity is not set in the database.");
                    }
                } else {
                    // Handle cases where the product with the given productId doesn't exist
                    Log.e(TAG, "Product with productId " + productId + " not found in the database.");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle any errors that occur while retrieving the quantity
                Log.e(TAG, "Failed to retrieve product quantity: " + databaseError.getMessage());
            }
        });
    }


    private void addPurchaseDataToSales(QRScannerFragment.Product product) {
        String userId = getCurrentUserUid();

        if (!userId.isEmpty()) {
            DatabaseReference salesRef = FirebaseDatabase.getInstance().getReference().child("Sales").child(userId);

            String productId = product.getProductId();
            String productName = product.getProductName();
            int quantity = product.getQuantity();
            double price = product.getPrice();
            String timestamp = getCurrentTimestamp();

            // Create a unique key for each purchase
            String purchaseKey = salesRef.push().getKey();

            // Create a map to store purchase data
            Map<String, Object> purchaseData = new HashMap<>();
            purchaseData.put("productId", productId);
            purchaseData.put("productName", productName);
            purchaseData.put("quantity", quantity);
            purchaseData.put("price", price);
            purchaseData.put("timestamp", timestamp);
            purchaseData.put("userUid", userId);

            // Update the Sales node with the new purchase data
            salesRef.child(purchaseKey).setValue(purchaseData);

            // You can add additional logic here if needed
        } else {
            // Handle the case where the user ID is empty
            Log.e(TAG, "User ID is empty. Unable to add purchase data to Sales.");
        }
    }

    private String getCurrentTimestamp() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        return dateFormat.format(calendar.getTime());
    }

    private void displayReceipt() {
        // Log the size of the productList before building the receipt
        Log.d(TAG, "Product List Size Before Receipt: " + productList.size());

        // Check if the productList is not empty before building the receipt
        if (!productList.isEmpty()) {
            // Build the receipt message with item details
            StringBuilder receiptMessage = new StringBuilder();

            // Add the date of purchase
            receiptMessage.append("Date: ").append(getCurrentTimestamp()).append("\n\n");

            for (QRScannerFragment.Product product : productList) {
                String productName = product.getProductName();
                String productDescription = product.getProductDescription();
                double price = product.getPrice();
                int quantity = product.getQuantity();
                double itemTotal = price * quantity;

                // Build the receipt message with item details
                receiptMessage.append("Product Name: ").append(productName).append("\n");
                receiptMessage.append("Product Description: ").append(productDescription).append("\n");
                receiptMessage.append("Quantity: ").append(quantity).append("\n");
                receiptMessage.append("Price: ₱").append(price).append("\n");
                receiptMessage.append("Total Per Item: ₱").append(itemTotal).append("\n\n");
            }

            receiptMessage.append("\n--------------------------\n");  // Add a separator line
            receiptMessage.append("Total Amount: ₱").append(calculateOverallTotal()).append("\n\n");

            Log.d(TAG, "Purchased successful");

            // Clear the scanned products list
            productList.clear();
            // Notify the adapter about the data change
            productAdapter.notifyDataSetChanged();

            // Update the TextView with the overall total
            TextView totalTextView = findViewById(R.id.totalPriceTextView);
            totalTextView.setText("Total: ₱");

            // Show the receipt in a pop-up dialog
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder
                    .setTitle("Purchased Successful")
                    .setMessage(receiptMessage.toString())
                    .setPositiveButton("Print", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            printReceipt(receiptMessage.toString());
                        }
                    })
                    .setNeutralButton("Share", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            shareReceipt(receiptMessage.toString());
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create()
                    .show();
        } else {
            // Display a message or handle the case where productList is empty
            Toast.makeText(this, "Invalid Process!", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareReceipt(String receiptText) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, receiptText);

        // Start an activity to share the receipt
        startActivity(Intent.createChooser(shareIntent, "Share Receipt via"));
    }

    private void printReceipt(String receiptText) {
        // Get a PrintManager instance
        PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);

        String jobName = "Receipt";

        // Create a WebView to render the receipt text as an HTML page
        WebView webView = new WebView(this);
        webView.loadDataWithBaseURL(null, "<html><body>" + receiptText + "</body></html>", "text/HTML", "UTF-8", null);

        // Create a print document adapter
        PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter();

        // Start the print job
        PrintJob printJob = printManager.print(jobName, printAdapter, new PrintAttributes.Builder().build());

        // You can monitor the print job status if needed
        if (printJob.isCompleted()) {
            // Printing is complete
        } else if (printJob.isFailed()) {
            // Printing failed
        }
    }


    private double calculateOverallTotal() {
        double overallTotal = 0.0;
        for (QRScannerFragment.Product product : productList) {
            overallTotal += product.getPrice() * product.getQuantity();
        }
        return overallTotal;
    }

    public List<QRScannerFragment.Product> getProductList() {
        return productList;
    }

    public ProductAdapter getProductAdapter() {
        return productAdapter;
    }

    public void onProductScanned(QRScannerFragment.Product product) {
        Log.d(TAG, "Product scanned: " + product.getProductName());
        // Add the scanned product to the list
        productList.add(product);
        productAdapter.notifyDataSetChanged();

        // Calculate and update the overall total
        updateOverallTotal();

        // Optionally, you can show a Toast or other UI feedback
        Toast.makeText(this, "Product scanned: " + product.getProductName(), Toast.LENGTH_SHORT).show();

        //displayReceipt();
    }

    // Make sure updateOverallTotal() is either public or package-private
    public void updateOverallTotal() {
        double overallTotal = 0.0;

        // Calculate the overall total based on the scanned products
        for (QRScannerFragment.Product product : productList) {
            overallTotal += product.getPrice() * product.getQuantity();
        }

        // Update the TextView with the overall total
        TextView totalTextView = findViewById(R.id.totalPriceTextView);
        totalTextView.setText("Total: ₱" + String.format("%.2f", overallTotal));
    }

    public void handleRemoveItemClick(int position) {
        // Get the product at the selected position
        QRScannerFragment.Product selectedProduct = productList.get(position);

        // Check if the quantity is greater than 1
        if (selectedProduct.getQuantity() > 1) {
            // Decrement the quantity
            selectedProduct.setQuantity(selectedProduct.getQuantity() - 1);

            // Notify the adapter about the data change
            productAdapter.notifyItemChanged(position);
        } else {
            // Quantity is 1, show AlertDialog to confirm removal
            showRemoveItemConfirmationDialog(position);
        }

        // Update the overall total
        updateOverallTotal();
    }

    private void showRemoveItemConfirmationDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Remove Item");
        builder.setMessage("Are you sure you want to remove this item?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Remove the item from the list
                productList.remove(position);
                // Notify the adapter about the removal
                productAdapter.notifyItemRemoved(position);
                // Update the overall total
                updateOverallTotal();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // User clicked "No," do nothing or handle accordingly
            }
        });

        builder.create().show();
    }

    public static class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

        private List<QRScannerFragment.Product> productList;
        private POSActivity posActivity;

        public ProductAdapter(List<QRScannerFragment.Product> productList, POSActivity posActivity) {
            this.productList = productList;
            this.posActivity = posActivity;
        }

        @NonNull
        @Override
        public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
            return new ProductViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
            QRScannerFragment.Product product = productList.get(position);
            holder.bind(product);
        }

        @Override
        public int getItemCount() {
            return productList.size();
        }

        class ProductViewHolder extends RecyclerView.ViewHolder {

            private TextView productNameTextView;
            private TextView productDescriptionTextView;
            private TextView priceTextView;
            private TextView categoryTextView;
            private TextView quantityTextView;
            ImageButton removeItemButton;

            public ProductViewHolder(@NonNull View itemView) {
                super(itemView);
                productNameTextView = itemView.findViewById(R.id.productNameTextView);
                productDescriptionTextView = itemView.findViewById(R.id.productDescriptionTextView);
                priceTextView = itemView.findViewById(R.id.priceTextView);
                quantityTextView = itemView.findViewById(R.id.quantityTextView);
                categoryTextView = itemView.findViewById(R.id.categoryTextView);

                // Initialize other views...
                removeItemButton = itemView.findViewById(R.id.removeItemButton);

                // Set a click listener for the remove item button
                removeItemButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        posActivity.handleRemoveItemClick(getAdapterPosition());
                    }
                });
            }


            public void bind(QRScannerFragment.Product product) {
                productNameTextView.setText(product.getProductName());
                productDescriptionTextView.setText(product.getProductDescription());
                categoryTextView.setText(product.getCategory());
                quantityTextView.setText(String.valueOf(product.getQuantity()));
                priceTextView.setText(String.format("₱%.2f", product.getPrice()));
            }
        }
    }

}