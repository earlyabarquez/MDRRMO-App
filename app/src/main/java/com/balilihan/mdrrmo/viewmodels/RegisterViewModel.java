package com.balilihan.mdrrmo.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;  // ← changed from ViewModel
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.balilihan.mdrrmo.models.ApiResponse;
import com.balilihan.mdrrmo.models.RegisterRequest;
import com.balilihan.mdrrmo.network.ApiClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterViewModel extends AndroidViewModel {  // ← changed from ViewModel

    // AndroidViewModel requires this constructor
    public RegisterViewModel(@NonNull Application application) {
        super(application);
    }

    public static class RegisterState {
        public enum Status { LOADING, SUCCESS, ERROR }

        public final Status status;
        public final String message;

        private RegisterState(Status s, String m) {
            status = s; message = m;
        }

        public static RegisterState loading() {
            return new RegisterState(Status.LOADING, null);
        }
        public static RegisterState success(String message) {
            return new RegisterState(Status.SUCCESS, message);
        }
        public static RegisterState error(String message) {
            return new RegisterState(Status.ERROR, message);
        }
    }

    private final MutableLiveData<RegisterState> _registerState = new MutableLiveData<>();
    public  final LiveData<RegisterState>         registerState  = _registerState;

    public void register(String username, String email,
                         String phone, String password, String confirmPassword) {

        // Client-side validation
        if (username.isEmpty() || email.isEmpty() ||
                phone.isEmpty() || password.isEmpty()) {
            _registerState.setValue(
                    RegisterState.error("Please fill in all required fields")
            );
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _registerState.setValue(
                    RegisterState.error("Enter a valid email address")
            );
            return;
        }
        if (password.length() < 8) {
            _registerState.setValue(
                    RegisterState.error("Password must be at least 8 characters")
            );
            return;
        }
        if (!password.equals(confirmPassword)) {
            _registerState.setValue(
                    RegisterState.error("Passwords do not match")
            );
            return;
        }
        if (!android.util.Patterns.PHONE.matcher(phone).matches()) {
            _registerState.setValue(
                    RegisterState.error("Enter a valid phone number")
            );
            return;
        }

        _registerState.setValue(RegisterState.loading());

        // Pass getApplication() as context for AuthInterceptor
        ApiClient.getApiService(getApplication())
                .register(new RegisterRequest(username, email, phone, password))
                .enqueue(new Callback<ApiResponse>() {

                    @Override
                    public void onResponse(Call<ApiResponse> call,
                                           Response<ApiResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            _registerState.setValue(
                                    RegisterState.success(response.body().getMessage())
                            );
                        } else if (response.code() == 409) {
                            // 409 Conflict — email already registered
                            _registerState.setValue(
                                    RegisterState.error(
                                            "An account with this email already exists"
                                    )
                            );
                        } else {
                            _registerState.setValue(
                                    RegisterState.error("Registration failed. Try again.")
                            );
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse> call, Throwable t) {
                        // Network error — no internet or server unreachable
                        _registerState.setValue(
                                RegisterState.error(
                                        "Cannot reach server. Check your connection."
                                )
                        );
                    }
                });
    }
}