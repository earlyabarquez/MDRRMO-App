// viewmodels/ProfileViewModel.java

package com.balilihan.mdrrmo.viewmodels;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.balilihan.mdrrmo.models.User;
import com.balilihan.mdrrmo.utils.SessionManager;

public class ProfileViewModel extends AndroidViewModel {

    // SharedPreferences key for profile photo path
    private static final String PREF_NAME       = "profile_prefs";
    private static final String KEY_PHOTO_PATH  = "photo_path";

    private final MutableLiveData<User>    _user     =
            new MutableLiveData<>();
    public  final LiveData<User>            user      = _user;

    private final MutableLiveData<Boolean> _isEditing =
            new MutableLiveData<>(false);
    public  final LiveData<Boolean>         isEditing  = _isEditing;

    private final MutableLiveData<String>  _updateResult =
            new MutableLiveData<>();
    public  final LiveData<String>          updateResult  = _updateResult;

    private final MutableLiveData<Boolean> _isLoading =
            new MutableLiveData<>(false);
    public  final LiveData<Boolean>         isLoading  = _isLoading;

    private final SessionManager      session;
    private final SharedPreferences   prefs;

    public ProfileViewModel(@NonNull Application app) {
        super(app);
        session = SessionManager.getInstance(app);
        prefs   = app.getSharedPreferences(PREF_NAME,
                android.content.Context.MODE_PRIVATE);
    }

    // Load user profile from session + mock stats
    // TODO Step 9: Fetch real stats from API
    // GET /api/users/profile
    public void loadProfile() {
        _isLoading.setValue(true);

        // ── MOCK DATA — replace with real API call in Step 9 ────
        new android.os.Handler(android.os.Looper.getMainLooper())
                .postDelayed(() -> {
                    User user = new User();
                    user.setUserId(session.getUserId());
                    user.setUsername(session.getUsername());
                    user.setEmail(session.getEmail());
                    user.setPhoneNumber("09123456789"); // mock phone
                    user.setRole(session.getRole());
                    user.setAccountStatus(session.getStatus());

                    // Mock report stats
                    user.setTotalReports(24);
                    user.setVerifiedReports(18);
                    user.setPendingReports(4);
                    user.setRejectedReports(2);

                    // Load saved photo path from SharedPreferences
                    String photoPath = prefs.getString(KEY_PHOTO_PATH, null);
                    user.setPhotoPath(photoPath);

                    _user.setValue(user);
                    _isLoading.setValue(false);
                }, 500);
        // ── END MOCK DATA ────────────────────────────────────────

        // ── REAL API CALL — uncomment in Step 9 ─────────────────
        // TODO: Uncomment when Spring Boot backend is ready
//        ApiClient.getApiService(getApplication())
//                .getUserProfile()
//                .enqueue(new Callback<User>() {
//                    @Override
//                    public void onResponse(Call<User> call,
//                                           Response<User> response) {
//                        _isLoading.postValue(false);
//                        if (response.isSuccessful() && response.body() != null) {
//                            User user = response.body();
//                            String photoPath = prefs.getString(
//                                    KEY_PHOTO_PATH, null);
//                            user.setPhotoPath(photoPath);
//                            _user.postValue(user);
//                        }
//                    }
//                    @Override
//                    public void onFailure(Call<User> call, Throwable t) {
//                        _isLoading.postValue(false);
//                    }
//                });
        // ── END REAL API CALL ────────────────────────────────────
    }

    // Toggle edit mode on/off
    public void toggleEditMode() {
        Boolean current = _isEditing.getValue();
        _isEditing.setValue(current == null || !current);
    }

    // Save updated profile — username and phone only
    // TODO Step 9: PUT /api/users/profile
    public void saveProfile(String username, String phone) {
        if (username.isEmpty()) {
            _updateResult.setValue("error:Username cannot be empty");
            return;
        }
        if (phone.isEmpty()) {
            _updateResult.setValue("error:Phone number cannot be empty");
            return;
        }

        _isLoading.setValue(true);

        // ── MOCK SAVE — replace with real API call in Step 9 ────
        new android.os.Handler(android.os.Looper.getMainLooper())
                .postDelayed(() -> {
                    // Update session with new username
                    session.saveSession(
                            session.getToken(),
                            session.getUserId(),
                            username,
                            session.getEmail(),
                            session.getRoleId(),
                            session.getRole(),
                            session.getStatus()
                    );

                    // Update user LiveData
                    User current = _user.getValue();
                    if (current != null) {
                        current.setUsername(username);
                        current.setPhoneNumber(phone);
                        _user.setValue(current);
                    }

                    _isLoading.setValue(false);
                    _isEditing.setValue(false);
                    _updateResult.setValue("success:Profile updated successfully");
                }, 800);
        // ── END MOCK SAVE ────────────────────────────────────────
    }

    // Save profile photo path locally
    public void savePhotoPath(String path) {
        prefs.edit().putString(KEY_PHOTO_PATH, path).apply();

        User current = _user.getValue();
        if (current != null) {
            current.setPhotoPath(path);
            _user.setValue(current);
        }
    }

    // Sign out — clear session and return to login
    public void signOut() {
        session.clearSession();
    }
}