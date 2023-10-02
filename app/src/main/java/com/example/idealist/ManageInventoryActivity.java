package com.example.idealist;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.idealist.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ManageInventoryActivity extends AppCompatActivity {

    private EditText editTextAddProductId, editTextAddProductName, editTextAddSuppName, editTextAddProductDesc,
            editTextAddQuantity, editTextAddPrice;
    private ProgressBar progressBarAddProduct;
    private Spinner spinnerAddCategory;
    private static final String TAG= "ManageInventoryActivity";
    private ImageButton imageButtonAddProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_inventory);

        getSupportActionBar().setTitle("Manage Inventory");

        Toast.makeText(ManageInventoryActivity.this, "You can Manage Product now", Toast.LENGTH_LONG).show();

        ImageButton imageButtonAddProduct = findViewById(R.id.imageButtonAddProduct);
        ImageButton imageButtonUpdateProduct = findViewById(R.id.imageButtonUpdateProduct);
        ImageButton imageButtonDeleteProduct = findViewById(R.id.imageButtonDeleteProduct);
        ImageButton imageButtonViewProduct = findViewById(R.id.imageButtonViewProduct);
        imageButtonAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManageInventoryActivity.this, AddProductActivity.class);
                startActivity(intent);
                finish();
            }
        });

        imageButtonUpdateProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManageInventoryActivity.this, UpdateProductActivity.class);
                startActivity(intent);
                finish();
            }
        });

        imageButtonDeleteProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManageInventoryActivity.this, DeleteProductActivity.class);
                startActivity(intent);
                finish();
            }
        });

        imageButtonViewProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManageInventoryActivity.this, ViewProductActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
