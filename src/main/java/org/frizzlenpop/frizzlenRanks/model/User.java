package org.frizzlenpop.frizzlenRanks.model;

import java.util.*;

/**
 * Represents a user in the permissions system.
 */
public class User {
    private final String name;
    private final Map<String, String> meta;
    private final Set<String> permissions;
    private final Set<String> groups;
    
    /**
     * Creates a new User with the given name.
     * 
     * @param name the name of the user
     */
    public User(String name) {
        this.name = name.toLowerCase();
        this.meta = new HashMap<>();
        this.permissions = new HashSet<>();
        this.groups = new HashSet<>();
    }
    
    /**
     * Gets the name of the user.
     * 
     * @return the user's name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Gets the user's metadata.
     * 
     * @return a map of metadata key-value pairs
     */
    public Map<String, String> getMeta() {
        return Collections.unmodifiableMap(meta);
    }
    
    /**
     * Sets a metadata value for the user.
     * 
     * @param key the metadata key
     * @param value the metadata value
     */
    public void setMeta(String key, String value) {
        if (value == null || value.isEmpty()) {
            meta.remove(key);
        } else {
            meta.put(key, value);
        }
    }
    
    /**
     * Gets a metadata value for the user.
     * 
     * @param key the metadata key
     * @return the metadata value, or null if not found
     */
    public String getMeta(String key) {
        return meta.get(key);
    }
    
    /**
     * Gets the user's permissions.
     * 
     * @return a set of permissions
     */
    public Set<String> getPermissions() {
        return Collections.unmodifiableSet(permissions);
    }
    
    /**
     * Adds a permission to the user.
     * 
     * @param permission the permission to add
     */
    public void addPermission(String permission) {
        permissions.add(permission.toLowerCase());
    }
    
    /**
     * Removes a permission from the user.
     * 
     * @param permission the permission to remove
     */
    public void removePermission(String permission) {
        permissions.remove(permission.toLowerCase());
    }
    
    /**
     * Checks if the user has the specified permission.
     * 
     * @param permission the permission to check
     * @return true if the user has the permission
     */
    public boolean hasPermission(String permission) {
        return permissions.contains(permission.toLowerCase());
    }
    
    /**
     * Gets the groups the user belongs to.
     * 
     * @return a set of group names
     */
    public Set<String> getGroups() {
        return Collections.unmodifiableSet(groups);
    }
    
    /**
     * Adds the user to a group.
     * 
     * @param group the group to add the user to
     */
    public void addGroup(String group) {
        groups.add(group.toLowerCase());
    }
    
    /**
     * Removes the user from a group.
     * 
     * @param group the group to remove the user from
     */
    public void removeGroup(String group) {
        groups.remove(group.toLowerCase());
    }
    
    /**
     * Checks if the user belongs to the specified group.
     * 
     * @param group the group to check
     * @return true if the user belongs to the group
     */
    public boolean inGroup(String group) {
        return groups.contains(group.toLowerCase());
    }
    
    /**
     * Sets the user's groups, replacing any existing groups.
     * 
     * @param groups the new groups
     */
    public void setGroups(Collection<String> groups) {
        this.groups.clear();
        for (String group : groups) {
            this.groups.add(group.toLowerCase());
        }
    }
    
    /**
     * Clears all groups from the user.
     */
    public void clearGroups() {
        groups.clear();
    }
    
    /**
     * Clears all permissions from the user.
     */
    public void clearPermissions() {
        permissions.clear();
    }
    
    /**
     * Clears all metadata from the user.
     */
    public void clearMeta() {
        meta.clear();
    }
} 