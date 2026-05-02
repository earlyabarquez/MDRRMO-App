// utils/SessionManager.java
// Updated to use username instead of fullName

package com.balilihan.mdrrmo.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class SessionManager {

    private static final String PREF_NAME    = "mdrrmo_session";
    private static final String KEY_TOKEN    = "jwt_token";
    private static final String KEY_USER_ID  = "user_id";
    private static final String KEY_USERNAME = "username";   // was fullName
    private static final String KEY_EMAIL    = "email";
    private static final String KEY_ROLE_ID  = "role_id";
    private static final String KEY_ROLE     = "role";
    private static final String KEY_STATUS   = "account_status";

    private static SessionManager instance;
    private SharedPreferences     prefs;

    private SessionManager(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            prefs = EncryptedSharedPreferences.create(
                    context,
                    PREF_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            prefs = context.getSharedPreferences(
                    PREF_NAME, Context.MODE_PRIVATE
            );
        }
    }

    public static SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context.getApplicationContext());
        }
        return instance;
    }

    // ── Save session after login ─────────────────────────────────
    public void saveSession(String token, long userId, String username,
                            String email, int roleId,
                            String role, String status) {
        prefs.edit()
                .putString(KEY_TOKEN,    token)
                .putLong(KEY_USER_ID,    userId)
                .putString(KEY_USERNAME, username)
                .putString(KEY_EMAIL,    email)
                .putInt(KEY_ROLE_ID,     roleId)
                .putString(KEY_ROLE,     role)
                .putString(KEY_STATUS,   status)
                .apply();
    }

    // ── Getters ──────────────────────────────────────────────────
    public String  getToken()    { return prefs.getString(KEY_TOKEN, null); }
    public long    getUserId()   { return prefs.getLong(KEY_USER_ID, -1); }
    public String  getUsername() { return prefs.getString(KEY_USERNAME, ""); }
    public String  getEmail()    { return prefs.getString(KEY_EMAIL, ""); }
    public int     getRoleId()   { return prefs.getInt(KEY_ROLE_ID, -1); }
    public String  getRole()     { return prefs.getString(KEY_ROLE, ""); }
    public String  getStatus()   { return prefs.getString(KEY_STATUS, ""); }

    // ── Session checks ───────────────────────────────────────────
    public boolean isLoggedIn()      { return getToken() != null; }
    public boolean isAccountActive() { return "ACTIVE".equals(getStatus()); }

    // ── Clear on sign out ────────────────────────────────────────
    public void clearSession() { prefs.edit().clear().apply(); }
}