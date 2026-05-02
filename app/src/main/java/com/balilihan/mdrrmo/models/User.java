// models/User.java
// Represents the logged-in reporter's profile
// Fetched from API and cached locally

package com.balilihan.mdrrmo.models;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class User {

    // Matches users table in DB
    private long   userId;
    private String username;
    private String email;
    private String phoneNumber;
    private int    roleId;
    private String role;
    private String accountStatus;

    // Profile photo — stored locally on device
    // Uploaded to server in Step 9
    private String photoPath;

    // Report statistics — computed from reports table
    private int totalReports;
    private int verifiedReports;
    private int pendingReports;
    private int rejectedReports;
}