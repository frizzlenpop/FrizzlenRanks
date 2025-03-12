package org.frizzlenpop.frizzlenRanks.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a world and its permission settings.
 */
public class World {
    private final String name;
    private final Map<String, User> users;
    private final Map<String, Group> groups;
    
    /**
     * Creates a new World with the given name.
     * 
     * @param name the name of the world
     */
    public World(String name) {
        this.name = name.toLowerCase();
        this.users = new HashMap<>();
        this.groups = new HashMap<>();
    }
    
    /**
     * Gets the name of the world.
     * 
     * @return the world's name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Gets a user by name, creating one if it doesn't exist.
     * 
     * @param name the name of the user
     * @return the user object
     */
    public User getUser(String name) {
        String lowerName = name.toLowerCase();
        if (!users.containsKey(lowerName)) {
            users.put(lowerName, new User(lowerName));
        }
        return users.get(lowerName);
    }
    
    /**
     * Checks if a user exists in this world.
     * 
     * @param name the name of the user
     * @return true if the user exists
     */
    public boolean hasUser(String name) {
        return users.containsKey(name.toLowerCase());
    }
    
    /**
     * Gets all users in this world.
     * 
     * @return a collection of all users
     */
    public Collection<User> getUsers() {
        return users.values();
    }
    
    /**
     * Removes a user from this world.
     * 
     * @param name the name of the user to remove
     * @return true if the user was found and removed
     */
    public boolean removeUser(String name) {
        return users.remove(name.toLowerCase()) != null;
    }
    
    /**
     * Gets a group by name, creating one if it doesn't exist.
     * 
     * @param name the name of the group
     * @return the group object
     */
    public Group getGroup(String name) {
        String lowerName = name.toLowerCase();
        if (!groups.containsKey(lowerName)) {
            groups.put(lowerName, new Group(lowerName));
        }
        return groups.get(lowerName);
    }
    
    /**
     * Checks if a group exists in this world.
     * 
     * @param name the name of the group
     * @return true if the group exists
     */
    public boolean hasGroup(String name) {
        return groups.containsKey(name.toLowerCase());
    }
    
    /**
     * Gets all groups in this world.
     * 
     * @return a collection of all groups
     */
    public Collection<Group> getGroups() {
        return groups.values();
    }
    
    /**
     * Removes a group from this world.
     * 
     * @param name the name of the group to remove
     * @return true if the group was found and removed
     */
    public boolean removeGroup(String name) {
        return groups.remove(name.toLowerCase()) != null;
    }
    
    /**
     * Checks if a user has a permission in this world, including group permissions.
     * 
     * @param userName the name of the user
     * @param permission the permission to check
     * @return true if the user has the permission
     */
    public boolean hasPermission(String userName, String permission) {
        String lowerName = userName.toLowerCase();
        if (!users.containsKey(lowerName)) {
            return false;
        }
        
        User user = users.get(lowerName);
        
        // Check direct user permissions
        if (user.hasPermission(permission)) {
            return true;
        }
        
        // Check negated permission
        if (user.hasPermission("-" + permission)) {
            return false;
        }
        
        // Check group permissions
        for (String groupName : user.getGroups()) {
            if (hasGroupPermission(groupName, permission, new HashMap<>())) {
                return true;
            }
            if (hasGroupPermission(groupName, "-" + permission, new HashMap<>())) {
                return false;
            }
        }
        
        return false;
    }
    
    /**
     * Checks if a group has a permission, including inherited permissions.
     * 
     * @param groupName the name of the group
     * @param permission the permission to check
     * @param checked a map of already checked groups to prevent infinite recursion
     * @return true if the group has the permission
     */
    private boolean hasGroupPermission(String groupName, String permission, Map<String, Boolean> checked) {
        String lowerName = groupName.toLowerCase();
        
        // Prevent infinite recursion from circular inheritance
        if (checked.containsKey(lowerName)) {
            return checked.get(lowerName);
        }
        
        // Mark as being checked
        checked.put(lowerName, false);
        
        if (!groups.containsKey(lowerName)) {
            return false;
        }
        
        Group group = groups.get(lowerName);
        
        // Check direct group permission
        if (group.hasPermission(permission)) {
            checked.put(lowerName, true);
            return true;
        }
        
        // Check inherited groups
        for (String inheritedGroup : group.getInheritance()) {
            if (hasGroupPermission(inheritedGroup, permission, checked)) {
                checked.put(lowerName, true);
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Clears all groups in this world.
     * This is useful for reloading groups from files.
     */
    public void clearGroups() {
        groups.clear();
    }
} 