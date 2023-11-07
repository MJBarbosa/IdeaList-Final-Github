package com.inventory.idealist;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.print.PrintHelper;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.inventory.idealist.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class QrGeneratorActivity extends AppCompatActivity {

    private AutoCompleteTextView autoCompleteTextViewQRSearch;
    private ProgressBar progressBarViewQRProduct;
    private List<QrGeneratorActivity.Product> productList;
    private ListView listViewQRProducts;
    private ArrayAdapter<String> searchAdapter;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_generator);

        getSupportActionBar().setTitle("Generate QR");

        // Initialize UI elements
        progressBarViewQRProduct = findViewById(R.id.progressBarViewQRProduct);
        autoCompleteTextViewQRSearch = findViewById(R.id.autoCompleteTextViewQRSearch);
        listViewQRProducts = findViewById(R.id.listViewQRProducts);

        // Initialize the productList and set up the search functionality
        productList = new ArrayList<>();
        searchAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line);
        autoCompleteTextViewQRSearch.setAdapter(searchAdapter);
        setupSearch();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.menu_qr_gene);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_home_so) {
                // Handle the Home menu item click here
                Intent intent = new Intent(QrGeneratorActivity.this, MainSOActivity.class);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.menu_inventory) {
                // You are already in the Inventory tab, no action needed
                Intent intent = new Intent(QrGeneratorActivity.this, ManageInventoryActivity.class);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.menu_pos) {
                Intent intent = new Intent(QrGeneratorActivity.this, PointOfSaleActivity.class);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.menu_qr_gene) {
                // You are already in the QR Gene tab, no action needed
                return true;
            } else if (itemId == R.id.menu_sales_report) {
                Intent intent = new Intent(QrGeneratorActivity.this, SalesReportActivity.class);
                startActivity(intent);
                finish();
                return true;
            }
            return false;
        });

        Button buttonGenerateQR = findViewById(R.id.buttonGenerateQR);
        buttonGenerateQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the selected product from the TextViews
                String productId = ((TextView) findViewById(R.id.textViewQRProductId)).getText().toString();
                String productName = ((TextView) findViewById(R.id.textViewQRProductName)).getText().toString();
                String productDescription = ((TextView) findViewById(R.id.textViewQRProductDescription)).getText().toString();
                String price = ((TextView) findViewById(R.id.textViewQRPrice)).getText().toString();
                String category = ((TextView) findViewById(R.id.textViewQRCategory)).getText().toString();

                // Combine the product details into a single text
                String productDetails = productId + "\n"
                        + productName + "\n"
                        + productDescription + "\n"
                        + price + "\n"
                        + category;

                // Generate the QR code
                generateQRCode(productDetails);
            }
        });

        Button buttonSearch = findViewById(R.id.buttonQRSearch);
        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the search logic here
                String searchQuery = autoCompleteTextViewQRSearch.getText().toString();
                searchProduct(searchQuery);
            }
        });
    }

    private String getAppPackageName() {
        return getApplicationContext().getPackageName();
    }


    private void generateQRCode(String data) {
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(data, BarcodeFormat.QR_CODE, 500, 500);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);

            // Save the QR code image to a file
            String fileName = "QRCode.png";
            File file = new File(getExternalCacheDir(), fileName);

            try (FileOutputStream fos = new FileOutputStream(file)) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.flush();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error saving QR code image", Toast.LENGTH_SHORT).show();
                return;
            }

            // Inflate the alert dialog layout
            View dialogView = LayoutInflater.from(this).inflate(R.layout.qr_code_alert, null);

            // Find views in the dialog layout
            ImageView qrCodeImageView = dialogView.findViewById(R.id.qrCodeImageView);
            Button printButton = dialogView.findViewById(R.id.printQRButton);
            Button downloadButton = dialogView.findViewById(R.id.downloadQRButton);
            Button cancelButton = dialogView.findViewById(R.id.cancelQRButton);

            // Set the generated QR code image to the ImageView
            qrCodeImageView.setImageBitmap(bitmap);

            // Create the alert dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(dialogView);

            // Create and show the dialog
            final AlertDialog alertDialog = builder.create();
            alertDialog.show();

            // Handle print button click (implement printing logic here)

            // Handle download button click
            downloadButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Create a content URI using FileProvider
                    Uri contentUri = FileProvider.getUriForFile(
                            QrGeneratorActivity.this,
                            getAppPackageName() + ".fileprovider",
                            file
                    );

                    // Open a share intent to allow the user to download the QR code
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("image/*");
                    intent.putExtra(Intent.EXTRA_STREAM, contentUri);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // Grant read permission
                    startActivity(Intent.createChooser(intent, "Download QR Code"));
                }
            });



            // Handle print button click
            printButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Print the QR code
                    printQRCode(bitmap);

                    // Dismiss the dialog
                    alertDialog.dismiss();
                }
            });


            // Handle cancel button click
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Dismiss the dialog
                    alertDialog.dismiss();
                }
            });

        } catch (WriterException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error generating QR code", Toast.LENGTH_SHORT).show();
        }
    }



    private void setupSearch() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("ProductInventory").child(getCurrentUserUid());

        // Attach a listener to populate the productList when data changes in the database
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                productList.clear();

                for (DataSnapshot productSnapshot : dataSnapshot.getChildren()) {
                    // Check if the data is a HashMap or a Product object
                    if (productSnapshot.getValue() instanceof HashMap) {
                        // Handle data stored as HashMap (if needed)
                        // You can extract data from the HashMap and create a Product object
                        // Example:
                        HashMap<String, Object> productData = (HashMap<String, Object>) productSnapshot.getValue();
                        QrGeneratorActivity.Product product = new QrGeneratorActivity.Product();
                        product.setProductId((String) productData.get("productId"));
                        product.setProductName((String) productData.get("productName"));
                        product.setSupplierName((String) productData.get("supplierName"));
                        product.setCategory((String) productData.get("category"));
                        product.setProductDescription((String) productData.get("productDescription"));
                        product.setPrice((String) productData.get("price"));
                        product.setQuantity((String) productData.get("quantity"));
                        // Populate other fields similarly

                        // Add the product to the list
                        productList.add(product);
                    } else if (productSnapshot.getValue() instanceof QrGeneratorActivity.Product) {
                        // Handle data stored as Product object
                        QrGeneratorActivity.Product product = productSnapshot.getValue(QrGeneratorActivity.Product.class);
                        if (product != null) {
                            productList.add(product);
                        }
                    } else {
                        Log.e(TAG, "Unexpected data type: " + productSnapshot.getValue().getClass().getSimpleName());
                    }
                }

                // Clear the searchAdapter before adding new items
                searchAdapter.clear();

                // Create an array of product names for the search suggestions
                List<String> productNames = new ArrayList<>();
                for (QrGeneratorActivity.Product product : productList) {
                    productNames.add(product.getProductName());
                }

                // Add product names to the searchAdapter
                searchAdapter.addAll(productNames);

                // Notify the adapter that the data has changed
                searchAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
                Toast.makeText(QrGeneratorActivity.this, "Error loading product data.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error loading product data: " + databaseError.getMessage());
            }
        });

        // Handle item selection from the search suggestions
        autoCompleteTextViewQRSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the selected product name
                String selectedProductName = parent.getItemAtPosition(position).toString();

                // Find the corresponding product in the productList
                QrGeneratorActivity.Product selectedProduct = findProductByName(selectedProductName);

                if (selectedProduct != null) {
                    // Populate TextViews with the selected product's data
                    TextView textViewQRProductId = findViewById(R.id.textViewQRProductId);
                    TextView textViewQRProductName = findViewById(R.id.textViewQRProductName);
                    TextView textViewQRProductDescription = findViewById(R.id.textViewQRProductDescription);
                    TextView textViewQRPrice = findViewById(R.id.textViewQRPrice);
                    TextView textViewQRCategory = findViewById(R.id.textViewQRCategory);

                    textViewQRProductId.setText("Product ID:" + selectedProduct.getProductId());
                    textViewQRProductName.setText("Product Name:" + selectedProduct.getProductName());
                    textViewQRProductDescription.setText("Product Description:" + selectedProduct.getProductDescription());
                    textViewQRPrice.setText("Price:" + selectedProduct.getPrice());
                    textViewQRCategory.setText("Category:" + selectedProduct.getCategory());

                    // Update other TextViews with additional product details as needed
                } else {
                    Log.e(TAG, "Selected product is null.");
                }
            }
        });

        // Add an OnItemClickListener to the ListView to handle item clicks
        listViewQRProducts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the selected product name from the adapter
                String selectedProductName = adapter.getItem(position);

                // Find the corresponding product in the productList
                QrGeneratorActivity.Product selectedProduct = findProductByName(selectedProductName);

                if (selectedProduct != null) {
                    // Populate TextViews with the selected product's data
                    updateTextViewsWithProductData(selectedProduct);
                } else {
                    Log.e(TAG, "Selected product is null.");
                }
            }
        });

    }

    private void printQRCode(Bitmap qrCodeBitmap) {
        // Check if printing is supported
        if (!PrintHelper.systemSupportsPrint()) {
            Toast.makeText(this, "Printing is not supported on this device", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a PrintHelper instance
        PrintHelper printHelper = new PrintHelper(this);
        printHelper.setScaleMode(PrintHelper.SCALE_MODE_FIT);

        // Print the QR code
        printHelper.printBitmap("QR Code", qrCodeBitmap);
    }




    private void updateTextViewsWithProductData(QrGeneratorActivity.Product selectedProduct) {
        TextView textViewQRProductId = findViewById(R.id.textViewQRProductId);
        TextView textViewProductName = findViewById(R.id.textViewQRProductName);
        TextView textViewProductDescription = findViewById(R.id.textViewQRProductDescription);
        TextView textViewPrice = findViewById(R.id.textViewQRPrice);
        TextView textViewQRCategory = findViewById(R.id.textViewQRCategory);

        textViewQRProductId.setText("Product ID:" + selectedProduct.getProductId());
        textViewProductName.setText("Product Name:" + selectedProduct.getProductName());
        textViewProductDescription.setText("Product Description:" + selectedProduct.getProductDescription());
        textViewPrice.setText("Price:" + selectedProduct.getPrice());
        textViewQRCategory.setText("Category:" + selectedProduct.getCategory());
    }

    private void searchProduct(String query) {
        // Perform a search based on the entered query
        // You can use the productList to filter and display the search results
        // Example: Filter the productList to find products matching the query
        List<QrGeneratorActivity.Product> searchResults = new ArrayList<>();
        for (QrGeneratorActivity.Product product : productList) {
            if (product.getProductName().toLowerCase().contains(query.toLowerCase())) {
                searchResults.add(product);
            }
        }

        // Create an adapter to display the search results in the listViewProducts
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        listViewQRProducts.setAdapter(adapter);

        // Clear the previous search results
        adapter.clear();

        // Add the search results to the adapter
        for (QrGeneratorActivity.Product product : searchResults) {
            adapter.add(product.getProductName());
        }

        // Notify the adapter that the data has changed
        adapter.notifyDataSetChanged();
    }

    private String getCurrentUserUid() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            return currentUser.getUid();
        } else {
            return "";
        }
    }

    // Find a product by its name
    private QrGeneratorActivity.Product findProductByName(String productName) {
        for (QrGeneratorActivity.Product product : productList) {
            if (product.getProductName().equals(productName)) {
                return product;
            }
        }
        return null;
    }

    // Define a Product class to manage product data (if needed)
    private static class Product {
        private String productId;
        private String productName;
        private String supplierName;
        private String category;
        private String productDescription;
        private String quantity;
        private String price;

        // Constructors, getters, setters, and other methods go here

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