package com.balilihan.mdrrmo.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.balilihan.mdrrmo.models.AuthRequest;
import com.balilihan.mdrrmo.models.AuthResponse;
import com.balilihan.mdrrmo.network.ApiClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginViewModel extends AndroidViewModel {

    public LoginViewModel(@NonNull Application application) {
        super(application);
    }

    public static class LoginState {
        public enum Status { LOADING, SUCCESS, ERROR }

        public final Status       status;
        public final AuthResponse data;
        public final String       message;

        private LoginState(Status s, AuthResponse d, String m) {
            status = s; data = d; message = m;
        }

        public static LoginState loading() {
            return new LoginState(Status.LOADING, null, null);
        }
        public static LoginState success(AuthResponse data) {
            return new LoginState(Status.SUCCESS, data, null);
        }
        public static LoginState error(String message) {
            return new LoginState(Status.ERROR, null, message);
        }
    }

    private final MutableLiveData<LoginState> _loginState = new MutableLiveData<>();
    public  final LiveData<LoginState>         loginState  = _loginState;

    // ── MOCK CREDENTIALS — remove when backend is connected ─────
    // TODO: Remove mock credentials when Spring Boot backend is ready
    private static final String MOCK_EMAIL    = "admin@mdrrmo.com";
    private static final String MOCK_PASSWORD = "mdrrmo2024";

    public void login(String email, String password) {

        // Basic client-side validation before hitting the API
        if (email.isEmpty() || password.isEmpty()) {
            _loginState.setValue(LoginState.error("Please fill in all fields"));
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _loginState.setValue(LoginState.error("Enter a valid email address"));
            return;
        }

        _loginState.setValue(LoginState.loading());

        // ── MOCK LOGIN — bypasses real API for UI testing ────────
        // TODO: Remove this block when Spring Boot backend is ready
        if (email.equals(MOCK_EMAIL) && password.equals(MOCK_PASSWORD)) {
            new android.os.Handler(android.os.Looper.getMainLooper())
                    .postDelayed(() -> {
                        AuthResponse mockResponse = new AuthResponse();
                        mockResponse.setToken("mock_jwt_token_12345");
                        mockResponse.setUserId(1L);
                        mockResponse.setUsername("Juan Dela Cruz");
                        mockResponse.setEmail(MOCK_EMAIL);
                        mockResponse.setRoleId(2);
                        mockResponse.setRole("REPORTER");
                        mockResponse.setAccountStatus("ACTIVE");

                        _loginState.setValue(LoginState.success(mockResponse));
                    }, 1000);
            return;
        }
        // ── END MOCK LOGIN ───────────────────────────────────────

        // ── REAL API CALL — uncomment when Spring Boot backend is ready ──
        // TODO: Uncomment this block in Step 9 when connecting to backend
//        ApiClient.getApiService(getApplication())
//                .login(new AuthRequest(email, password))
//                .enqueue(new Callback<AuthResponse>() {
//
//                    @Override
//                    public void onResponse(Call<AuthResponse> call,
//                                           Response<AuthResponse> response) {
//                        if (response.isSuccessful() && response.body() != null) {
//                            _loginState.setValue(LoginState.success(response.body()));
//                        } else if (response.code() == 401) {
//                            // Wrong email or password
//                            _loginState.setValue(
//                                    LoginState.error("Incorrect email or password")
//                            );
//                        } else if (response.code() == 403) {
//                            // Account exists but admin hasn't approved yet
//                            _loginState.setValue(
//                                    LoginState.error(
//                                            "Your account is pending approval by the MDRRMO admin"
//                                    )
//                            );
//                        } else {
//                            _loginState.setValue(
//                                    LoginState.error("Login failed. Please try again.")
//                            );
//                        }
//                    }
//
//                    @Override
//                    public void onFailure(Call<AuthResponse> call, Throwable t) {
//                        // Network error — no internet or server unreachable
//                        _loginState.setValue(
//                                LoginState.error("Cannot reach server. Check your connection.")
//                        );
//                    }
//                });
        // ── END REAL API CALL ────────────────────────────────────

        // Fallback error when mock credentials don't match
        // and real API is commented out
        _loginState.setValue(
                LoginState.error("Use mock credentials: admin@mdrrmo.com / mdrrmo2024")
        );
    }
}