package com.example.idealist;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainSOActivity extends AppCompatActivity {

    private FirebaseUser firebaseUserSO;
    private TextView textViewSO;
    private FirebaseAuth authProfileSO;
    private SwipeRefreshLayout swipeContainer;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private DatabaseReference salesDataRef;
    private GraphView salesGraph;
    private LineGraphSeries<DataPoint> series;
    private long twelveMonthsInMillis;
    private int currentYear;
    private int maxDataPoints = 12;
    private double maxSalesValue = 0.0;
    // Declare the dataPoints list as a class member
    private List<DataPoint> dataPoints = new ArrayList<>();

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_soactivity);

        getSupportActionBar().setTitle("Main SO Activity");

        swipeToRefresh();

        // Initialize the GraphView
        salesGraph = findViewById(R.id.salesGraph);

        // Add a title to the graph
        salesGraph.setTitle("Sales Over Time");

        series = new LineGraphSeries<>();
        salesGraph.addSeries(series);

        // Set the OnDataPointTapListener to display details when a point is tapped
        series.setOnDataPointTapListener(new OnDataPointTapListener() {
            @Override
            public void onTap(Series series, DataPointInterface dataPoint) {
                // Handle tap event, e.g., display details of the data point
                String details = "Timestamp: " + new Date((long) dataPoint.getX()) + "\nSales: " + dataPoint.getY();
                Toast.makeText(MainSOActivity.this, details, Toast.LENGTH_SHORT).show();
            }
        });

        // Initialize Firebase database reference
        salesDataRef = FirebaseDatabase.getInstance().getReference().child("Sales");

        authProfileSO = FirebaseAuth.getInstance();
        textViewSO = findViewById(R.id.textViewSO);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                R.string.open,
                R.string.close
        );

        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.menuManageProfileSO) {
                    Toast.makeText(MainSOActivity.this, "Manage Profile Selected", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainSOActivity.this, StoreOwnerProfileActivity.class);
                    startActivity(intent);
                    finish();
                } else if (itemId == R.id.menuLogoutHomeSO) {
                    authProfileSO.signOut();
                    Toast.makeText(MainSOActivity.this, "Logged Out", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(MainSOActivity.this, LoginAsStoreOwner.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        // Calculate twelveMonthsInMillis once
        twelveMonthsInMillis = 12L * 30L * 24L * 60L * 60L * 1000L;

        // Initialize the graph
        initializeGraph();

        // Inside the onCreate method, modify the loop for loading data:
        for (int month = 1; month <= 12; month++) {
            dataPoints.clear(); // Clear the dataPoints list for each month
            loadSalesDataForYearAndMonth(currentYear, month);
        }


        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.menu_home_so);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_home_so) {
                // Handle the Home menu item click here
                return true;
            } else if (itemId == R.id.menu_inventory) {
                Toast.makeText(MainSOActivity.this, "Manage Inventory Selected", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainSOActivity.this, ManageInventoryActivity.class);
                startActivity(intent);
                finish();
            } else if (itemId == R.id.menu_pos) {
                // Handle the Profile menu item click here
                Intent intent = new Intent(MainSOActivity.this, PointOfSaleActivity.class);
                startActivity(intent);
                finish();
                return true;
            }
            else if (itemId == R.id.menu_qr_gene) {
                // Handle the QR menu item click here
                Intent intent = new Intent(MainSOActivity.this, QrGeneratorActivity.class);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.menu_sales_report) {
                Intent intent = new Intent(MainSOActivity.this, SalesReportActivity.class);
                startActivity(intent);
                finish();
                return true;
            }
            return false;
        });

        firebaseUserSO = authProfileSO.getCurrentUser();
        if (firebaseUserSO == null) {
            Intent intent = new Intent(getApplicationContext(), LoginAsStoreOwner.class);
            startActivity(intent);
            finish();
        } else {
            textViewSO.setText(firebaseUserSO.getEmail());
        }
    }

    private void initializeGraph() {
        Viewport viewport = salesGraph.getViewport();
        viewport.setYAxisBoundsManual(true);
        viewport.setXAxisBoundsManual(true);

        long currentTimestamp = System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(currentTimestamp);
        currentYear = calendar.get(Calendar.YEAR);

        // Set minX to the beginning of the current year (January 1st, current year)
        calendar.set(currentYear, 0, 1, 0, 0, 0);
        long minX = calendar.getTimeInMillis();

        // Set maxX to the end of the current year (December 31st, current year)
        calendar.set(currentYear, 11, 31, 23, 59, 59);
        long maxX = calendar.getTimeInMillis();

        // Set the Y-axis bounds to start from zero
        viewport.setMinY(0);

        // Disable scrolling
        viewport.setScalable(false);
        viewport.setScrollable(false);

        // Set the calculated bounds
        viewport.setMaxX(maxX);
        viewport.setMinX(minX);

        // Implement a custom X-axis label formatter
        GridLabelRenderer gridLabel = salesGraph.getGridLabelRenderer();
        gridLabel.setHorizontalLabelsVisible(true);
        gridLabel.setHorizontalAxisTitle("Months " + currentYear);

        gridLabel.setVerticalLabelsVisible(true);
        gridLabel.setVerticalAxisTitle("Number of Items");

        // Set the custom X-axis label formatter
        gridLabel.setLabelFormatter(new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    return formatXLabel((long) value);
                } else {
                    // Format Y-axis labels here
                    return super.formatLabel(value, isValueX);
                }
            }
        });

        // Initialize the graph with zero data points
        series.resetData(new DataPoint[]{new DataPoint(minX, 0)});
    }

    private void loadSalesDataForYearAndMonth(int year, int month) {
        Log.d(TAG, "Start of loadSalesDataForYearAndMonth");
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, 1, 0, 0, 0);
        long startTimestamp = calendar.getTimeInMillis();

        calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DATE));
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        long endTimestamp = calendar.getTimeInMillis();

        Log.d(TAG, "Database for sale: " + salesDataRef);

        Query query = salesDataRef
                .orderByChild("timestamp");

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "Number of children in dataSnapshot: " + dataSnapshot.getChildrenCount());

                Log.d(TAG, "Fetching data for year: " + year + ", month: " + month);

                List<DataPoint> dataPointsForMonth = new ArrayList<>(); // Create a list to store data points for the current month

                String currentUserUid = firebaseUserSO.getUid(); // Get the current user's UID

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String saleUserUid = snapshot.child("userUid").getValue(String.class);
                    Double saleAmount = snapshot.child("price").getValue(Double.class);
                    String saleTimestampStr = snapshot.child("timestamp").getValue(String.class);

                    if (saleUserUid != null && saleUserUid.equals(currentUserUid)) {
                        if (saleAmount != null && saleTimestampStr != null && !saleTimestampStr.isEmpty()) {
                            long saleTimestamp = parseTimestamp(saleTimestampStr);
                            if (saleTimestamp >= startTimestamp && saleTimestamp <= endTimestamp) {
                                // Add each data point to the list for the current month
                                dataPointsForMonth.add(new DataPoint(saleTimestamp, saleAmount));
                                Log.d(TAG, "Data fetched for sales: timestamp: " + saleTimestampStr + saleAmount + saleUserUid);
                            }
                        }
                    }
                }

                // Add data points for the current month to the dataPoints list
                dataPoints.addAll(dataPointsForMonth);

                // Add data points for the current month to the series
                series.resetData(dataPoints.toArray(new DataPoint[0]));


                // Update the maximum sales value if necessary
                maxSalesValue = Math.max(maxSalesValue, calculateMaxSalesValue());

                // Update the graph view
                updateGraphView();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
                Log.e(TAG, "Database error: " + databaseError.getMessage());
            }
        });
        Log.d(TAG, "End of loadSalesDataForYearAndMonth");
    }

    private String formatXLabel(long timestamp) {
        // Convert the timestamp to a readable date format with month names
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("MMM");
        return sdf.format(calendar.getTime());
    }

    private void updateGraphView() {
        // Ensure that the series starts from zero
        if (dataPoints.size() > 0) {
            // Add a data point at the beginning with a Y-value of 0
            dataPoints.add(0, new DataPoint(dataPoints.get(0).getX(), 0));
        }

        // Set the maximum Y value for the graph
        salesGraph.getViewport().setMaxY(maxSalesValue);

        // Update the graph with the series containing data points
        salesGraph.removeAllSeries();
        salesGraph.addSeries(series);

        // Set the custom X-axis label formatter (moved from initializeGraph)
        GridLabelRenderer gridLabel = salesGraph.getGridLabelRenderer();
        gridLabel.setHorizontalLabelsVisible(true);
        gridLabel.setHorizontalAxisTitle("Months " + currentYear);

        gridLabel.setVerticalLabelsVisible(true);
        gridLabel.setVerticalAxisTitle("Number of Items");
    }

    private double calculateMaxSalesValue() {
        // Initialize a variable to keep track of the maximum sales value
        double maxSalesValue = 0.0;

        // Iterate through the data points to find the maximum sales value
        for (DataPoint dataPoint : dataPoints) {
            double salesValue = dataPoint.getY();
            if (salesValue > maxSalesValue) {
                maxSalesValue = salesValue;
            }
        }

        // Add some padding to the maximum value for a better graph display
        maxSalesValue *= 1.2; // Adjust the padding factor as needed

        return maxSalesValue;
    }

    // Other methods as needed

    private long parseTimestamp(String timestampStr) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        try {
            Date date = format.parse(timestampStr);
            if (date != null) {
                return date.getTime();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0; // Return 0 if parsing fails
    }


    private void swipeToRefresh() {
        swipeContainer = findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(() -> {
            startActivity(getIntent());
            finish();
            overridePendingTransition(0, 0);
            swipeContainer.setRefreshing(false);
        });

        swipeContainer.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
        );
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

}
