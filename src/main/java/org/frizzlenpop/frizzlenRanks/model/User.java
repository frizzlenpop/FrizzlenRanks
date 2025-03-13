package org.frizzlenpop.frizzlenRanks.model;

import java.util.*;

/**
 * Represents a user in the system.
 */
public class User {
    private final String name;
    private final List<String> groups;
    private final List<String> permissions;
    private final Map<String, String> meta;
    
    // Maps for temporary permissions and groups with expiration timestamps
    private final Map<String, Long> temporaryPermissions;
    private final Map<String, Long> temporaryGroups;
    
    /**
     * Creates a new user.
     * 
     * @param name the name of the user
     */
    public User(String name) {
        this.name = name;
        this.groups = new ArrayList<>();
        this.permissions = new ArrayList<>();
        this.meta = new HashMap<>();
        this.temporaryPermissions = new HashMap<>();
        this.temporaryGroups = new HashMap<>();
    }
    
    /**
     * Gets the name of the user.
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Gets the groups the user is in.
     * 
     * @return the groups
     */
    public List<String> getGroups() {
        List<String> effectiveGroups = new ArrayList<>(groups);
        
        // Add active temporary groups
        long currentTime = System.currentTimeMillis();
        for (Map.Entry<String, Long> entry : temporaryGroups.entrySet()) {
            if (entry.getValue() > currentTime && !effectiveGroups.contains(entry.getKey())) {
                effectiveGroups.add(entry.getKey());
            }
        }
        
        return Collections.unmodifiableList(effectiveGroups);
    }
    
    /**
     * Gets the raw permanent groups without temporary ones.
     * 
     * @return the permanent groups
     */
    public List<String> getPermanentGroups() {
        return Collections.unmodifiableList(groups);
    }
    
    /**
     * Gets the temporary groups with their expiration timestamps.
     * 
     * @return the temporary groups map
     */
    public Map<String, Long> getTemporaryGroups() {
        cleanExpiredGroupEntries();
        return Collections.unmodifiableMap(temporaryGroups);
    }
    
    /**
     * Adds the user to a group.
     * 
     * @param group the group
     */
    public void addGroup(String group) {
        if (!groups.contains(group)) {
            groups.add(group);
        }
    }
    
    /**
     * Adds the user to a group temporarily.
     * 
     * @param group the group
     * @param expirationTime the expiration timestamp in milliseconds
     */
    public void addTemporaryGroup(String group, long expirationTime) {
        // If the group is already permanent, don't add a temporary entry
        if (groups.contains(group)) {
            return;
        }
        
        // Add or update the temporary group
        temporaryGroups.put(group, expirationTime);
    }
    
    /**
     * Removes the user from a group.
     * 
     * @param group the group
     */
    public void removeGroup(String group) {
        groups.remove(group);
        temporaryGroups.remove(group); // Also remove any temporary entry
    }
    
    /**
     * Removes a temporary group assignment.
     * 
     * @param group the group to remove
     */
    public void removeTemporaryGroup(String group) {
        temporaryGroups.remove(group);
    }
    
    /**
     * Sets the user's groups.
     * 
     * @param groups the groups
     */
    public void setGroups(List<String> groups) {
        this.groups.clear();
        this.groups.addAll(groups);
    }
    
    /**
     * Gets the user's permissions.
     * 
     * @return the permissions
     */
    public List<String> getPermissions() {
        List<String> effectivePermissions = new ArrayList<>(permissions);
        
        // Add active temporary permissions
        long currentTime = System.currentTimeMillis();
        for (Map.Entry<String, Long> entry : temporaryPermissions.entrySet()) {
            if (entry.getValue() > currentTime && !effectivePermissions.contains(entry.getKey())) {
                effectivePermissions.add(entry.getKey());
            }
        }
        
        return Collections.unmodifiableList(effectivePermissions);
    }
    
    /**
     * Gets the permanent permissions without temporary ones.
     * 
     * @return the permanent permissions
     */
    public List<String> getPermanentPermissions() {
        return Collections.unmodifiableList(permissions);
    }
    
    /**
     * Gets the temporary permissions with their expiration timestamps.
     * 
     * @return the temporary permissions map
     */
    public Map<String, Long> getTemporaryPermissions() {
        cleanExpiredPermissionEntries();
        return Collections.unmodifiableMap(temporaryPermissions);
    }
    
    /**
     * Adds a permission to the user.
     * 
     * @param permission the permission
     */
    public void addPermission(String permission) {
        if (!permissions.contains(permission)) {
            permissions.add(permission);
        }
    }
    
    /**
     * Adds a temporary permission to the user.
     * 
     * @param permission the permission
     * @param expirationTime the expiration timestamp in milliseconds
     */
    public void addTemporaryPermission(String permission, long expirationTime) {
        // If the permission is already permanent, don't add a temporary entry
        if (permissions.contains(permission)) {
            return;
        }
        
        // Add or update the temporary permission
        temporaryPermissions.put(permission, expirationTime);
    }
    
    /**
     * Removes a permission from the user.
     * 
     * @param permission the permission
     */
    public void removePermission(String permission) {
        permissions.remove(permission);
        temporaryPermissions.remove(permission); // Also remove any temporary entry
    }
    
    /**
     * Removes a temporary permission.
     * 
     * @param permission the permission to remove
     */
    public void removeTemporaryPermission(String permission) {
        temporaryPermissions.remove(permission);
    }
    
    /**
     * Checks if the temporary entries are expired and removes them if needed.
     * This is called internally to keep the maps clean.
     */
    private void cleanExpiredEntries() {
        cleanExpiredPermissionEntries();
        cleanExpiredGroupEntries();
    }
    
    /**
     * Cleans expired permission entries.
     */
    private void cleanExpiredPermissionEntries() {
        long currentTime = System.currentTimeMillis();
        temporaryPermissions.entrySet().removeIf(entry -> entry.getValue() <= currentTime);
    }
    
    /**
     * Cleans expired group entries.
     */
    private void cleanExpiredGroupEntries() {
        long currentTime = System.currentTimeMillis();
        temporaryGroups.entrySet().removeIf(entry -> entry.getValue() <= currentTime);
    }
    
    /**
     * Gets the user's meta.
     * 
     * @return the meta
     */
    public Map<String, String> getMeta() {
        return Collections.unmodifiableMap(meta);
    }
    
    /**
     * Gets a meta value.
     * 
     * @param key the key
     * @return the value, or null if not found
     */
    public String getMeta(String key) {
        return meta.get(key);
    }
    
    /**
     * Sets a meta value.
     * 
     * @param key the key
     * @param value the value
     */
    public void setMeta(String key, String value) {
        if (value == null) {
            meta.remove(key);
        } else {
            meta.put(key, value);
        }
    }
    
    /**
     * Checks if a user has a specific permission, including temporary ones.
     * 
     * @param permission the permission to check
     * @return true if the user has the permission, false otherwise
     */
    public boolean hasPermission(String permission) {
        cleanExpiredEntries();
        
        // Check permanent permissions
        if (permissions.contains(permission)) {
            return true;
        }
        
        // Check temporary permissions
        long currentTime = System.currentTimeMillis();
        Long expiry = temporaryPermissions.get(permission);
        return expiry != null && expiry > currentTime;
    }
    
    /**
     * Checks if a user belongs to a specific group, including temporary ones.
     * 
     * @param group the group to check
     * @return true if the user is in the group, false otherwise
     */
    public boolean inGroup(String group) {
        cleanExpiredEntries();
        
        // Check permanent groups
        if (groups.contains(group)) {
            return true;
        }
        
        // Check temporary groups
        long currentTime = System.currentTimeMillis();
        Long expiry = temporaryGroups.get(group);
        return expiry != null && expiry > currentTime;
    }
    
    /**
     * Clears all permissions from the user.
     */
    public void clearPermissions() {
        permissions.clear();
        temporaryPermissions.clear();
    }
    
    /**
     * Clears all metadata from the user.
     */
    public void clearMeta() {
        meta.clear();
    }
    
    /**
     * Clears all groups from the user.
     */
    public void clearGroups() {
        groups.clear();
        temporaryGroups.clear();
    }
} 