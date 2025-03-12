package org.frizzlenpop.frizzlenRanks.model;

import java.util.*;

/**
 * Represents a permission group in the permissions system.
 */
public class Group {
    private final String name;
    private final Map<String, String> meta;
    private final Set<String> permissions;
    private final Set<String> inheritance;
    private int priority;
    
    /**
     * Creates a new Group with the given name.
     * 
     * @param name the name of the group
     */
    public Group(String name) {
        this.name = name.toLowerCase();
        this.meta = new HashMap<>();
        this.permissions = new HashSet<>();
        this.inheritance = new HashSet<>();
        this.priority = 0;
    }
    
    /**
     * Gets the name of the group.
     * 
     * @return the group's name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Gets the group's metadata.
     * 
     * @return a map of metadata key-value pairs
     */
    public Map<String, String> getMeta() {
        return Collections.unmodifiableMap(meta);
    }
    
    /**
     * Sets a metadata value for the group.
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
     * Gets a metadata value for the group.
     * 
     * @param key the metadata key
     * @return the metadata value, or null if not found
     */
    public String getMeta(String key) {
        return meta.get(key);
    }
    
    /**
     * Gets the group's permissions.
     * 
     * @return a set of permissions
     */
    public Set<String> getPermissions() {
        return Collections.unmodifiableSet(permissions);
    }
    
    /**
     * Adds a permission to the group.
     * 
     * @param permission the permission to add
     */
    public void addPermission(String permission) {
        permissions.add(permission.toLowerCase());
    }
    
    /**
     * Removes a permission from the group.
     * 
     * @param permission the permission to remove
     */
    public void removePermission(String permission) {
        permissions.remove(permission.toLowerCase());
    }
    
    /**
     * Checks if the group has the specified permission.
     * 
     * @param permission the permission to check
     * @return true if the group has the permission
     */
    public boolean hasPermission(String permission) {
        return permissions.contains(permission.toLowerCase());
    }
    
    /**
     * Gets the groups this group inherits from.
     * 
     * @return a set of group names
     */
    public Set<String> getInheritance() {
        return Collections.unmodifiableSet(inheritance);
    }
    
    /**
     * Adds a group to inherit from.
     * 
     * @param group the group to inherit from
     */
    public void addInheritance(String group) {
        if (!group.equalsIgnoreCase(name)) { // Prevent circular inheritance
            inheritance.add(group.toLowerCase());
        }
    }
    
    /**
     * Removes a group from inheritance.
     * 
     * @param group the group to remove from inheritance
     */
    public void removeInheritance(String group) {
        inheritance.remove(group.toLowerCase());
    }
    
    /**
     * Checks if this group inherits from the specified group.
     * 
     * @param group the group to check
     * @return true if this group inherits from the specified group
     */
    public boolean inherits(String group) {
        return inheritance.contains(group.toLowerCase());
    }
    
    /**
     * Gets the priority of this group.
     * Higher priority groups override lower priority groups.
     * 
     * @return the group's priority
     */
    public int getPriority() {
        return priority;
    }
    
    /**
     * Sets the priority of this group.
     * 
     * @param priority the new priority
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }
} 