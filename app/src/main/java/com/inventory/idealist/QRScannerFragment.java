package com.inventory.idealist;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CompoundBarcodeView;

import java.util.ArrayList;
import java.util.List;

// QRScannerFragment.java
public class QRScannerFragment extends Fragment {

    private CompoundBarcodeView barcodeView;
    // List to store scanned products
    private List<Product> scannedProducts = new ArrayList<>();

    // Adapter for the RecyclerView
    private ScannedProductAdapter scannedProductAdapter;
    private boolean canScan = true;

    private static final String KEY_PRODUCT_ID = "Product ID";
    private static final String KEY_PRODUCT_NAME = "Product Name";
    private static final String KEY_PRODUCT_DESCRIPTION = "Product Description";
    private static final String KEY_CATEGORY = "Category";
    private static final String KEY_PRICE = "Price";
    private EditText editTextProductId;
    private ImageButton imageButtonSearch;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_qr_scanner, container, false);
        barcodeView = view.findViewById(R.id.barcodeScannerView);
        // Initialize RecyclerView and its adapter
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewScannedProducts);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        scannedProductAdapter = new ScannedProductAdapter(scannedProducts);
        recyclerView.setAdapter(scannedProductAdapter);

        editTextProductId = view.findViewById(R.id.editTextProductId);
        imageButtonSearch = view.findViewById(R.id.imageButtonSearch);

        imageButtonSearch.setOnClickListener(v -> {
            String productId = editTextProductId.getText().toString().trim();
            if (!productId.isEmpty()) {
                searchAndAddProduct(productId);
            } else {
                // Handle empty input, show a Toast or error message
                Toast.makeText(requireContext(), "Please enter a product ID", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    // Define an interface for communication with the activity
    public interface OnProductScannedListener {
        void onProductScanned(QRScannerFragment.Product product);
    }

    private OnProductScannedListener onProductScannedListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnProductScannedListener) {
            onProductScannedListener = (OnProductScannedListener) context;
        } else {
            throw new ClassCastException(context.toString() + " must implement OnProductScannedListener");
        }
    }

    private void searchAndAddProduct(String productId) {
        DatabaseReference productRef = FirebaseDatabase.getInstance()
                .getReference()
                .child("ProductInventory")
                .child(getCurrentUserUid()); // Assuming getCurrentUserUid() returns the current user's UID

        Query query = productRef.orderByChild("productId").equalTo(productId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Product found
                    DataSnapshot productSnapshot = dataSnapshot.getChildren().iterator().next();

                    // Extract product details from the snapshot
                    String productName = productSnapshot.child("productName").getValue(String.class);
                    String productDescription = productSnapshot.child("productDescription").getValue(String.class);
                    String category = productSnapshot.child("category").getValue(String.class);
                    double price = Double.parseDouble(productSnapshot.child("price").getValue(String.class));

                    // Set quantity to 1 directly
                    int quantity = 1;

                    // Create a Product object
                    Product foundProduct = new Product(productId, productName, productDescription, price, category, quantity);

                    // Notify the activity that a product is scanned
                    onProductScannedListener.onProductScanned(foundProduct);

                    // Optionally, clear the input field after adding the product
                    editTextProductId.setText("");
                } else {
                    // Product not found, show a Toast or error message
                    Toast.makeText(requireContext(), "Product not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle any errors that occur during the search
                Log.e(TAG, "Failed to search for product: " + databaseError.getMessage());
            }
        });
    }

    private String getCurrentUserUid() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            return currentUser.getUid();
        } else {
            return "";
        }
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        startBarcodeScanning();
    }

    private void startBarcodeScanning() {
        barcodeView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (canScan) {
                    canScan = false;
                    handleScannedData(result.getText());

                    // Set a delay of 2 seconds before allowing the next scan
                    new Handler().postDelayed(() -> canScan = true, 2000);
                }
            }

            @Override
            public void possibleResultPoints(List<ResultPoint> resultPoints) {
                // Handle possible result points
            }
        });
    }
    private void handleScannedData(String qrData) {
        try {
            // Print the received QR data for debugging
            Log.d("QRScannerFragment", "Scanned QR data: " + qrData);

            // Split the data based on line breaks
            String[] lines = qrData.split("\n");

            // Create a Product object to store the parsed data
            Product product = new Product("", "", "", 0.0, "");

            // Process each line
            for (String line : lines) {
                // Print each line for debugging
                Log.d("QRScannerFragment", "Line: " + line);

                // Split each line based on colon (":") to separate key and value
                String[] parts = line.split(":");

                // Check if the line has the expected number of parts
                if (parts.length == 2) {
                    String key = parts[0].trim();
                    String value = parts[1].trim();

                    // Set the corresponding field in the Product object
                    switch (key) {
                        case KEY_PRODUCT_ID:
                            product.setProductId(value);
                            break;
                        case KEY_PRODUCT_NAME:
                            product.setProductName(value);
                            break;
                        case KEY_PRODUCT_DESCRIPTION:
                            product.setProductDescription(value);
                            break;
                        case KEY_CATEGORY:
                            product.setCategory(value);
                            break;
                        case KEY_PRICE:
                            try {
                                product.setPrice(Double.parseDouble(value));
                            } catch (NumberFormatException e) {
                                Log.e("QRScannerFragment", "Error parsing price: " + e.getMessage());
                                Toast.makeText(requireContext(), "Error parsing price", Toast.LENGTH_SHORT).show();
                            }
                            break;
                        default:
                            // Handle unknown key (if needed)
                            break;
                    }
                } else {
                    // Handle invalid line format
                    Log.e("QRScannerFragment", "Invalid line format: " + line);
                    Toast.makeText(requireContext(), "Invalid line format: " + line, Toast.LENGTH_SHORT).show();
                }
            }

            // Notify the POSActivity about the scanned product
            if (getActivity() instanceof POSActivity) {
                POSActivity posActivity = (POSActivity) getActivity();

                // Check if the product is already in the list
                boolean isProductInList = false;
                for (Product existingProduct : posActivity.getProductList()) {
                    if (existingProduct.getProductId().equals(product.getProductId())) {
                        // Product is already in the list, update the quantity
                        existingProduct.setQuantity(existingProduct.getQuantity() + 1);
                        isProductInList = true;
                        break;
                    }
                }

                if (!isProductInList) {
                    // Product is not in the list, add it with default quantity
                    posActivity.onProductScanned(product);
                }

                // Notify the adapter about the data change
                posActivity.getProductAdapter().notifyDataSetChanged();

                // Calculate and update the overall total
                posActivity.updateOverallTotal();
            }
        } catch (Exception e) {
            // Log any exceptions that occur during scanning
            Log.e("QRScannerFragment", "Exception during scanning: " + e.getMessage());
            Toast.makeText(requireContext(), "Exception during scanning", Toast.LENGTH_SHORT).show();
        }
    }

    // ScannedProductAdapter class
    public static class ScannedProductAdapter extends RecyclerView.Adapter<ScannedProductAdapter.ScannedProductViewHolder> {

        private List<Product> scannedProducts;

        public ScannedProductAdapter(List<Product> scannedProducts) {
            this.scannedProducts = scannedProducts;
        }

        @NonNull
        @Override
        public ScannedProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
            return new ScannedProductViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ScannedProductViewHolder holder, int position) {
            Product product = scannedProducts.get(position);
            holder.bind(product);
        }

        @Override
        public int getItemCount() {
            return scannedProducts.size();
        }

        static class ScannedProductViewHolder extends RecyclerView.ViewHolder {

            private TextView productNameTextView;
            private TextView productDescriptionTextView;
            private TextView priceTextView;
            private TextView categoryTextView;
            private TextView quantityTextView;

            public ScannedProductViewHolder(@NonNull View itemView) {
                super(itemView);
                productNameTextView = itemView.findViewById(R.id.productNameTextView);
                productDescriptionTextView = itemView.findViewById(R.id.productDescriptionTextView);
                priceTextView = itemView.findViewById(R.id.priceTextView);
                categoryTextView = itemView.findViewById(R.id.categoryTextView);
                quantityTextView = itemView.findViewById(R.id.quantityTextView);
            }

            public void bind(Product product) {
                productNameTextView.setText(product.getProductName());
                productDescriptionTextView.setText(product.getProductDescription());
                priceTextView.setText(String.format("₱%.2f", product.getPrice()));
                categoryTextView.setText(product.getCategory());
                quantityTextView.setText(String.valueOf(product.getQuantity()));
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        barcodeView.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        barcodeView.pause();
    }

    // Product.java
    public static class Product {
        private String productId;
        private String productName;
        private String productDescription;
        private double price;
        private String category;
        private int quantity;

        // No-argument constructor required for Firebase
        public Product() {
            // Default constructor required for Firebase
        }

        // Constructor without quantity to handle Firebase conversion
        public Product(String productId, String productName, String productDescription, double price, String category) {
            this(productId, productName, productDescription, price, category, 1); // Default quantity is 1
        }

        // Constructor with quantity
        public Product(String productId, String productName, String productDescription, double price, String category, int quantity) {
            this.productId = productId;
            this.productName = productName;
            this.productDescription = productDescription;
            this.price = price;
            this.category = category;
            this.quantity = quantity;
        }

        // Getter methods
        public String getProductId() {
            return productId;
        }

        public String getProductName() {
            return productName;
        }

        public String getProductDescription() {
            return productDescription;
        }

        public double getPrice() {
            return price;
        }

        public String getCategory() {
            return category;
        }
        public int getQuantity() {
            return quantity;
        }

        // Setter methods
        public void setProductId(String productId) {
            this.productId = productId;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public void setProductDescription(String productDescription) {
            this.productDescription = productDescription;
        }

        public void setPrice(double price) {
            this.price = price;
        }

        public void setCategory(String category) {
            this.category = category;
        }
        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }
}
