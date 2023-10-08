package com.example.idealist;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class QRCodeScannerActivity extends AppCompatActivity {
    private static final String TAG = "QRCodeScannerActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize the QR code scanner
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("Scan a QR Code");
        integrator.setOrientationLocked(false); // Allow both portrait and landscape scanning
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                // Handle the scanned QR code content
                String scannedContent = result.getContents();

                // Get the current user's UID
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {
                    String userUid = currentUser.getUid();

                    // Send the userUid and scanned content to the UpdateProductActivity
                    Intent intent = new Intent(this, UpdateProductActivity.class);
                    intent.putExtra("userUid", userUid);
                    intent.putExtra("scannedContent", scannedContent);
                    startActivity(intent);
                    finish(); // Close the QRCodeScannerActivity to avoid returning to it
                } else {
                    // User is not logged in
                    Log.e(TAG, "User is not logged in.");
                    // You can display an error message or take appropriate action here
                }
            } else {
                // Handle case where no content was found in the QR code
                Log.e(TAG, "No QR code content found.");
                // You can display an error message or take appropriate action here
            }
        }
    }


    // Implement your logic to retrieve the product's user ID from Firebase
    private String getProductUserId(String content) {
        // Replace this with your actual logic
        return "";
    }

    // Implement your logic to extract product ID from the scanned content
    // Update your existing extractProductIdFromContent method
    private String extractProductIdFromContent(String content) {
        // Define the prefix text that precedes the product ID in the content
        String prefix = "Product ID:";

        // Check if the content contains the prefix text
        if (content.contains(prefix)) {
            // Find the index of the prefix text
            int startIndex = content.indexOf(prefix);

            // Extract the product ID starting from the end of the prefix text
            String productId = content.substring(startIndex + prefix.length());

            // Remove any leading or trailing whitespace
            productId = productId.trim();

            return productId;
        } else {
            // Handle the case where the content does not contain the prefix
            Log.e(TAG, "Product ID not found in the content.");
            return "";
        }
    }
}

