// activities/LoginActivity.java

package com.balilihan.mdrrmo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.balilihan.mdrrmo.databinding.ActivityLoginBinding;
import com.balilihan.mdrrmo.utils.SessionManager;
import com.balilihan.mdrrmo.viewmodels.LoginViewModel;
import com.google.android.material.snackbar.Snackbar;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private LoginViewModel        viewModel;
    private SessionManager        session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ViewBinding — no more findViewById()
        binding   = ActivityLoginBinding.inflate(getLayoutInflater());
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        session   = SessionManager.getInstance(this);

        setContentView(binding.getRoot());

        setupClickListeners();
        observeLoginState();
    }

    private void setupClickListeners() {

        // Login button
        binding.btnLogin.setOnClickListener(v -> {
            String email    = binding.etEmail.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();
            viewModel.login(email, password);
        });

        // Navigate to Register screen
        binding.tvGoToRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );
    }

    private void observeLoginState() {
        viewModel.loginState.observe(this, state -> {
            switch (state.status) {

                case LOADING:
                    // Show spinner, disable button so user can't double-tap
                    binding.progressBar.setVisibility(View.VISIBLE);
                    binding.btnLogin.setEnabled(false);
                    break;

                case SUCCESS:
                    binding.progressBar.setVisibility(View.GONE);

                    // Save JWT + user info to EncryptedSharedPreferences
                    session.saveSession(
                            state.data.getToken(),
                            state.data.getUserId(),
                            state.data.getUsername(),   // was getFullName()
                            state.data.getEmail(),
                            state.data.getRoleId(),
                            state.data.getRole(),
                            state.data.getAccountStatus()
                    );

                    // Check if admin has approved this account
                    if ("ACTIVE".equals(state.data.getAccountStatus())) {
                        // Go to main app
                        Intent intent = new Intent(this, MainActivity.class);
                        // Clear back stack — user can't press back to get to Login
                        intent.setFlags(
                                Intent.FLAG_ACTIVITY_NEW_TASK |
                                        Intent.FLAG_ACTIVITY_CLEAR_TASK
                        );
                        startActivity(intent);
                    } else {
                        // Account exists but is still PENDING
                        // Show a dedicated pending screen
                        binding.progressBar.setVisibility(View.GONE);
                        binding.btnLogin.setEnabled(true);
                        showPendingDialog();
                    }
                    break;

                case ERROR:
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnLogin.setEnabled(true);
                    // Show error message at bottom of screen
                    Snackbar.make(
                            binding.getRoot(),
                            state.message,
                            Snackbar.LENGTH_LONG
                    ).show();
                    break;
            }
        });
    }

    private void showPendingDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Account Pending Approval")
                .setMessage(
                        "Your account has been submitted and is awaiting approval " +
                                "from the MDRRMO admin. You will be notified once approved."
                )
                .setPositiveButton("OK", null)
                .setCancelable(false)
                .show();
    }
}