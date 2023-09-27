package com.example.idealist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainSOActivity extends AppCompatActivity {

    private FirebaseUser firebaseUserSO;
    private TextView textViewSO;
    private FirebaseAuth authProfileSO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_soactivity);

        authProfileSO = FirebaseAuth.getInstance();
        textViewSO = findViewById(R.id.textViewSO);
        firebaseUserSO = authProfileSO.getCurrentUser();
        if (firebaseUserSO == null){
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        }
        else {
            textViewSO.setText(firebaseUserSO.getEmail());
        }

    }

    //Creating ActionBar Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate menu items
        getMenuInflater().inflate(R.menu.common_menu_home_so, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //When any menu item is selected
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menuRefreshHomeSO) {
            //Refresh Activity
            startActivity(getIntent());
            finish();
            overridePendingTransition(0,0);
        } else if (id == R.id.menuManageProfileSO) {
            Intent intent = new Intent(MainSOActivity.this, StoreOwnerProfileActivity.class);
            startActivity(intent);
        } else if (id == R.id.menuLogoutHomeSO) {
            authProfileSO.signOut();
            Toast.makeText(MainSOActivity.this, "Logged Out", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(MainSOActivity.this, LoginAsStoreOwner.class);

            //Clear stack to prevent user coming back to UserProfileActivity on pressing back button after Logging out
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(MainSOActivity.this, "Something Went Wrong!", Toast.LENGTH_LONG).show();
        }
        return super.onOptionsItemSelected(item);
    }
}