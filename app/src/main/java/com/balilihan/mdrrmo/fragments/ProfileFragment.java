// fragments/ProfileFragment.java

package com.balilihan.mdrrmo.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.balilihan.mdrrmo.R;
import com.balilihan.mdrrmo.activities.LoginActivity;
import com.balilihan.mdrrmo.databinding.FragmentProfileBinding;
import com.balilihan.mdrrmo.models.User;
import com.balilihan.mdrrmo.viewmodels.ProfileViewModel;
import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private ProfileViewModel        viewModel;

    // Gallery picker launcher
    private ActivityResultLauncher<Intent> galleryLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding   = FragmentProfileBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupGalleryLauncher();
        setupObservers();
        setupClickListeners();

        viewModel.loadProfile();
    }

    private void setupGalleryLauncher() {
        // Handles result from gallery picker
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK
                            && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            // Get real file path from URI
                            String path = getRealPathFromUri(imageUri);
                            if (path != null) {
                                viewModel.savePhotoPath(path);
                            }
                        }
                    }
                }
        );
    }

    private void setupObservers() {
        // Observe user profile
        viewModel.user.observe(getViewLifecycleOwner(), user -> {
            if (user != null) populateUI(user);
        });

        // Observe edit mode toggle
        viewModel.isEditing.observe(getViewLifecycleOwner(), isEditing -> {
            if (isEditing) {
                // Switch to edit mode
                binding.cardViewMode.setVisibility(View.GONE);
                binding.cardEditMode.setVisibility(View.VISIBLE);
                binding.tvEditSave.setText("Cancel");
                binding.tvChangePhoto.setVisibility(View.VISIBLE);

                // Pre-fill edit fields
                User user = viewModel.user.getValue();
                if (user != null) {
                    binding.etUsername.setText(user.getUsername());
                    binding.etPhone.setText(user.getPhoneNumber());
                }
            } else {
                // Switch to view mode
                binding.cardViewMode.setVisibility(View.VISIBLE);
                binding.cardEditMode.setVisibility(View.GONE);
                binding.tvEditSave.setText("Edit");
                binding.tvChangePhoto.setVisibility(View.GONE);
            }
        });

        // Observe update result
        viewModel.updateResult.observe(getViewLifecycleOwner(), result -> {
            if (result == null) return;
            if (result.startsWith("success:")) {
                Snackbar.make(
                        binding.getRoot(),
                        result.replace("success:", ""),
                        Snackbar.LENGTH_SHORT
                ).show();
            } else if (result.startsWith("error:")) {
                Snackbar.make(
                        binding.getRoot(),
                        result.replace("error:", ""),
                        Snackbar.LENGTH_LONG
                ).show();
            }
        });

        // Loading state
        viewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(
                    isLoading ? View.VISIBLE : View.GONE
            );
            binding.btnSave.setEnabled(!isLoading);
        });
    }

    private void populateUI(User user) {
        // Profile photo
        if (user.getPhotoPath() != null) {
            Glide.with(this)
                    .load(new File(user.getPhotoPath()))
                    .circleCrop()
                    .placeholder(R.drawable.ic_nav_profile)
                    .into(binding.ivProfilePhoto);
        }

        // Username on photo card
        binding.tvUsername.setText(user.getUsername());

        // Role badge
        binding.tvRole.setText(user.getRole() != null
                ? user.getRole() : "Reporter");
        binding.tvRole.getBackground().setTint(
                ContextCompat.getColor(requireContext(), R.color.primary)
        );

        // Stats
        binding.tvTotalCount.setText(
                String.valueOf(user.getTotalReports())
        );
        binding.tvVerifiedCount.setText(
                String.valueOf(user.getVerifiedReports())
        );
        binding.tvPendingCount.setText(
                String.valueOf(user.getPendingReports())
        );
        binding.tvRejectedCount.setText(
                String.valueOf(user.getRejectedReports())
        );

        // Account info — view mode
        binding.tvUsernameValue.setText(user.getUsername());
        binding.tvEmailValue.setText(user.getEmail());
        binding.tvPhoneValue.setText(user.getPhoneNumber());
    }

    private void setupClickListeners() {

        // Edit / Cancel toggle
        binding.tvEditSave.setOnClickListener(v ->
                viewModel.toggleEditMode()
        );

        // Change photo — opens gallery
        binding.tvChangePhoto.setOnClickListener(v -> openGallery());

        // Profile photo tap in edit mode — also opens gallery
        binding.ivProfilePhoto.setOnClickListener(v -> {
            Boolean isEditing = viewModel.isEditing.getValue();
            if (isEditing != null && isEditing) {
                openGallery();
            }
        });

        // Save changes
        binding.btnSave.setOnClickListener(v -> {
            String username = binding.etUsername.getText() != null
                    ? binding.etUsername.getText().toString().trim() : "";
            String phone = binding.etPhone.getText() != null
                    ? binding.etPhone.getText().toString().trim() : "";
            viewModel.saveProfile(username, phone);
        });

        // Sign out — confirm dialog
        binding.btnSignOut.setOnClickListener(v -> showSignOutDialog());
    }

    private void openGallery() {
        Intent intent = new Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        );
        galleryLauncher.launch(intent);
    }

    private void showSignOutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Sign out")
                .setMessage(
                        "Are you sure you want to sign out of your MDRRMO account?"
                )
                .setPositiveButton("Sign out", (dialog, which) -> {
                    viewModel.signOut();
                    // Navigate to login and clear back stack
                    Intent intent = new Intent(
                            requireContext(), LoginActivity.class
                    );
                    intent.setFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK |
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK
                    );
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private String getRealPathFromUri(Uri uri) {
        // Convert content URI to actual file path
        String[] projection = {
                MediaStore.Images.Media.DATA
        };
        try (android.database.Cursor cursor = requireContext()
                .getContentResolver()
                .query(uri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(
                        MediaStore.Images.Media.DATA
                );
                return cursor.getString(columnIndex);
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}