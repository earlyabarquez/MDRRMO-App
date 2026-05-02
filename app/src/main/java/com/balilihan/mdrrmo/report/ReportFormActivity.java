// report/ReportFormActivity.java

package com.balilihan.mdrrmo.report;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.balilihan.mdrrmo.R;
import com.balilihan.mdrrmo.databinding.ActivityReportFormBinding;
import com.balilihan.mdrrmo.models.HazardType;
import com.balilihan.mdrrmo.utils.DateUtils;
import com.balilihan.mdrrmo.utils.SessionManager;
import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ReportFormActivity extends AppCompatActivity {

    private ActivityReportFormBinding binding;
    private ReportViewModel           viewModel;
    private SessionManager            session;

    // Selected form values
    private String photoPath        = null;
    private int    selectedTypeId   = -1;
    private String selectedTypeName = "";
    private int    selectedSeverity = -1;   // tracks which button is active
    private String selectedRiskLevel = "";  // "Low", "Moderate", "High", "Critical"
    private double latitude         = 0;
    private double longitude        = 0;
    private String address          = "";

    // Hazard type list from Room
    private List<HazardType> hazardTypeList = new ArrayList<>();

    public static final String EXTRA_REPORT_ID = "report_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding   = ActivityReportFormBinding.inflate(getLayoutInflater());
        viewModel = new ViewModelProvider(this).get(ReportViewModel.class);
        session   = SessionManager.getInstance(this);
        setContentView(binding.getRoot());

        // Get photo path passed from ReportCameraActivity
        photoPath = getIntent().getStringExtra(
                ReportCameraActivity.EXTRA_PHOTO_PATH
        );

        setupUI();
        loadPhoto();
        loadHazardTypes();
        fetchCurrentLocation();
        observeSubmitState();
    }

    private void setupUI() {
        // Reporter name + timestamp — auto filled from session
        binding.tvReporter.setText(session.getUsername());
        binding.tvTimestamp.setText(
                DateUtils.formatForDisplay(DateUtils.getCurrentTimestamp())
        );

        // Severity buttons
        setupSeverityButtons();

        // Retake photo — goes back to camera
        binding.tvRetake.setOnClickListener(v -> finish());

        // Submit button
        binding.btnSubmit.setOnClickListener(v -> submitReport());
    }

    private void loadPhoto() {
        if (photoPath != null) {
            Glide.with(this)
                    .load(new File(photoPath))
                    .centerCrop()
                    .into(binding.ivPhotoPreview);
        }
    }

    private void loadHazardTypes() {
        // Observe hazard types from Room cache
        viewModel.getHazardTypesFromDb().observe(this, types -> {
            if (types != null && !types.isEmpty()) {
                hazardTypeList = types;
                setupHazardTypeDropdown(types);
            } else {
                // Room is empty — load defaults so form still works offline
                loadDefaultHazardTypes();
            }
        });
    }

    private void setupHazardTypeDropdown(List<HazardType> types) {
        List<String> names = new ArrayList<>();
        for (HazardType t : types) names.add(t.getHazardName());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                names
        );

        binding.actvHazardType.setAdapter(adapter);
        binding.actvHazardType.setOnItemClickListener((parent, view, pos, id) -> {
            HazardType selected = hazardTypeList.get(pos);
            selectedTypeId   = selected.getHazardId();
            selectedTypeName = selected.getHazardName();
        });
    }

    private void loadDefaultHazardTypes() {
        // Fallback list — used when Room cache is empty (first offline launch)
        // Mirrors the 19 types defined in the system
        String[][] defaults = {
                {"1",  "Flood"                    },
                {"2",  "Flash Flood"              },
                {"3",  "Landslide"                },
                {"4",  "Soil Erosion"             },
                {"5",  "Typhoon Damage"           },
                {"6",  "Earthquake Damage"        },
                {"7",  "Ground Fissure"           },
                {"8",  "Rockfall"                 },
                {"9",  "Structural Fire"          },
                {"10", "Grass / Forest Fire"      },
                {"11", "Road Damage"              },
                {"12", "Bridge Damage"            },
                {"13", "Fallen Tree"              },
                {"14", "Downed Power Line"        },
                {"15", "Collapsed Structure"      },
                {"16", "Crop Damage"              },
                {"17", "Animal Intrusion on Road" },
                {"18", "Dengue Hotspot"           },
                {"19", "Contaminated Water Source"}
        };

        hazardTypeList = new ArrayList<>();
        for (String[] d : defaults) {
            hazardTypeList.add(
                    new HazardType(
                            Integer.parseInt(d[0]), d[1]  // only 2 args — id and name
                    )
            );
        }
        setupHazardTypeDropdown(hazardTypeList);
    }

    private void setupSeverityButtons() {
        // Map of button → severity level + risk label + active color
        setupSeverityButton(
                binding.btnSeverityLow,      1, "Low",      R.color.severity_low
        );
        setupSeverityButton(
                binding.btnSeverityModerate, 2, "Moderate", R.color.severity_moderate
        );
        setupSeverityButton(
                binding.btnSeverityHigh,     3, "High",     R.color.severity_high
        );
        setupSeverityButton(
                binding.btnSeverityCritical, 4, "Critical", R.color.severity_critical
        );
    }

    private void setupSeverityButton(MaterialButton btn,
                                     int level,
                                     String label,
                                     int colorRes) {
        btn.setOnClickListener(v -> {
            // Deselect all buttons first
            resetSeverityButtons();

            // Select this button — fill with severity color
            selectedSeverity  = level;
            selectedRiskLevel = label; // matches riskLevel field in DB
            btn.setBackgroundColor(getColor(colorRes));
            btn.setTextColor(getColor(R.color.surface));
            btn.setStrokeWidth(0);
        });
    }

    private void resetSeverityButtons() {
        MaterialButton[] buttons = {
                binding.btnSeverityLow,
                binding.btnSeverityModerate,
                binding.btnSeverityHigh,
                binding.btnSeverityCritical
        };
        for (MaterialButton btn : buttons) {
            btn.setBackgroundColor(getColor(R.color.surface));
            btn.setTextColor(getColor(R.color.primary));
            btn.setStrokeWidth(2);
        }
    }

    private void fetchCurrentLocation() {
        FusedLocationProviderClient client =
                LocationServices.getFusedLocationProviderClient(this);

        if (androidx.core.app.ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            binding.tvLocationAddress.setText("Location permission required");
            return;
        }

        client.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                latitude  = location.getLatitude();
                longitude = location.getLongitude();

                binding.tvLocationCoords.setText(
                        String.format(Locale.getDefault(),
                                "%.6f°N, %.6f°E", latitude, longitude)
                );

                // Reverse geocode on background thread
                new Thread(() -> {
                    try {
                        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                        List<Address> addresses = geocoder.getFromLocation(
                                latitude, longitude, 1
                        );
                        if (addresses != null && !addresses.isEmpty()) {
                            Address addr = addresses.get(0);
                            address = addr.getSubLocality() != null
                                    ? addr.getSubLocality() + ", " + addr.getLocality()
                                    : addr.getLocality();
                            runOnUiThread(() ->
                                    binding.tvLocationAddress.setText(address)
                            );
                        }
                    } catch (Exception e) {
                        address = "Balilihan, Bohol";
                        runOnUiThread(() ->
                                binding.tvLocationAddress.setText(address)
                        );
                    }
                }).start();
            } else {
                binding.tvLocationAddress.setText("Acquiring GPS...");
            }
        });
    }

    private void submitReport() {
        String description = binding.etDescription.getText() != null
                ? binding.etDescription.getText().toString().trim()
                : "";

        viewModel.submitReport(
                selectedTypeId,    // hazardId — FK → hazard.hazard_id
                selectedTypeName,  // hazardName — denormalized for display
                0,                 // brgyId — 0 for now, auto-detect in Step 9
                address,           // brgyName — using address as display for now
                selectedRiskLevel, // riskLevel — "Low", "Moderate", "High", "Critical"
                description,       // optional notes
                latitude,          // GPS latitude
                longitude,         // GPS longitude
                photoPath          // imageUrl — local path before upload
        );
    }

    private void observeSubmitState() {
        viewModel.submitState.observe(this, state -> {
            switch (state.status) {

                case LOADING:
                    binding.progressBar.setVisibility(View.VISIBLE);
                    binding.btnSubmit.setEnabled(false);
                    break;

                case SUCCESS:
                    binding.progressBar.setVisibility(View.GONE);
                    // Navigate to preview screen
                    Intent intent = new Intent(
                            this, ReportPreviewActivity.class
                    );
                    intent.putExtra(EXTRA_REPORT_ID, state.report.getLocalId());
                    intent.putExtra("is_offline", false);
                    startActivity(intent);
                    finish();
                    break;

                case SAVED_OFFLINE:
                    binding.progressBar.setVisibility(View.GONE);
                    // Navigate to preview with offline indicator
                    Intent offlineIntent = new Intent(
                            this, ReportPreviewActivity.class
                    );
                    offlineIntent.putExtra(
                            EXTRA_REPORT_ID, state.report.getLocalId()
                    );
                    offlineIntent.putExtra("is_offline", true);
                    startActivity(offlineIntent);
                    finish();
                    break;

                case ERROR:
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnSubmit.setEnabled(true);
                    Snackbar.make(
                            binding.getRoot(),
                            state.message,
                            Snackbar.LENGTH_LONG
                    ).show();
                    break;
            }
        });
    }
}