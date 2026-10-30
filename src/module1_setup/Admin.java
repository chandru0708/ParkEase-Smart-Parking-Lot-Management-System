package module1_setup;

import java.util.Objects;

/**
 * Represents an Administrator entity with authentication capabilities.
 * Demonstrates Encapsulation and Access Control.
 */
public class Admin {
    private final String adminId;
    private final String username;
    private final String password;
    private final String name;

    public Admin(String adminId, String username, String password, String name) {
        if (adminId == null || adminId.trim().isEmpty()) {
            throw new IllegalArgumentException("Admin ID cannot be null or empty.");
        }
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty.");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty.");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty.");
        }
        this.adminId = adminId.trim();
        this.username = username.trim();
        this.password = password.trim();
        this.name = name.trim();
    }

    public String getAdminId() {
        return adminId;
    }

    public String getUsername() {
        return username;
    }

    public String getName() {
        return name;
    }

    public boolean authenticate(String inputPassword) {
        return this.password.equals(inputPassword);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Admin admin)) return false;
        return Objects.equals(adminId, admin.adminId) || Objects.equals(username, admin.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(adminId, username);
    }

    @Override
    public String toString() {
        return String.format("Admin[ID=%s, User=%s, Name=%s]", adminId, username, name);
    }
}
