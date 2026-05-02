// activities/RegisterActivity.java

package com.balilihan.mdrrmo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.balilihan.mdrrmo.databinding.ActivityRegisterBinding;
import com.balilihan.mdrrmo.viewmodels.RegisterViewModel;
import com.google.android.material.snackbar.Snackbar;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private RegisterViewModel        viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding   = ActivityRegisterBinding.inflate(getLayoutInflater());
        viewModel = new ViewModelProvider(this).get(RegisterViewModel.class);
        setContentView(binding.getRoot());

        setupClickListeners();
        observeRegisterState();
    }

    private void setupClickListeners() {
        binding.btnRegister.setOnClickListener(v -> {
            String fullName  = binding.etFullName.getText().toString().trim();
            String email     = binding.etEmail.getText().toString().trim();
            String phone     = binding.etPhone.getText().toString().trim();
            String password  = binding.etPassword.getText().toString().trim();
            String confirm   = binding.etConfirmPassword.getText().toString().trim();
            viewModel.register(fullName, email, phone, password, confirm);
        });

        // Back to Login
        binding.tvGoToLogin.setOnClickListener(v -> finish());
    }

    private void observeRegisterState() {
        viewModel.registerState.observe(this, state -> {
            switch (state.status) {

                case LOADING:
                    binding.progressBar.setVisibility(View.VISIBLE);
                    binding.btnRegister.setEnabled(false);
                    break;

                case SUCCESS:
                    binding.progressBar.setVisibility(View.GONE);
                    // Show success dialog — tell user to wait for admin approval
                    new androidx.appcompat.app.AlertDialog.Builder(this)
                            .setTitle("Registration Submitted")
                            .setMessage(
                                    "Your account has been submitted for approval. " +
                                            "The MDRRMO admin will review and activate your " +
                                            "account. You will be notified once approved."
                            )
                            .setPositiveButton("Go to Login", (d, w) -> {
                                // Return to login screen
                                startActivity(
                                        new Intent(this, LoginActivity.class)
                                                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                );
                                finish();
                            })
                            .setCancelable(false)
                            .show();
                    break;

                case ERROR:
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnRegister.setEnabled(true);
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