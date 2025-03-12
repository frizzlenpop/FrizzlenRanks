package org.frizzlenpop.frizzlenRanks.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a promotion/demotion track for groups.
 */
public class Track {
    private final String name;
    private final List<String> groups;
    
    /**
     * Creates a new Track with the given name.
     * 
     * @param name the name of the track
     */
    public Track(String name) {
        this.name = name.toLowerCase();
        this.groups = new ArrayList<>();
    }
    
    /**
     * Gets the name of the track.
     * 
     * @return the track's name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Gets the groups in this track, in order.
     * 
     * @return an unmodifiable list of group names
     */
    public List<String> getGroups() {
        return Collections.unmodifiableList(groups);
    }
    
    /**
     * Adds a group to the end of the track.
     * 
     * @param group the group to add
     */
    public void addGroup(String group) {
        groups.add(group.toLowerCase());
    }
    
    /**
     * Inserts a group at the specified position in the track.
     * 
     * @param index the position to insert the group
     * @param group the group to insert
     */
    public void insertGroup(int index, String group) {
        groups.add(index, group.toLowerCase());
    }
    
    /**
     * Removes a group from the track.
     * 
     * @param group the group to remove
     * @return true if the group was found and removed
     */
    public boolean removeGroup(String group) {
        return groups.remove(group.toLowerCase());
    }
    
    /**
     * Checks if the track contains the specified group.
     * 
     * @param group the group to check
     * @return true if the track contains the group
     */
    public boolean containsGroup(String group) {
        return groups.contains(group.toLowerCase());
    }
    
    /**
     * Gets the next group in the track after the specified group.
     * 
     * @param group the current group
     * @return the next group, or null if the group is not found or is the last group
     */
    public String getNextGroup(String group) {
        int index = groups.indexOf(group.toLowerCase());
        if (index == -1 || index == groups.size() - 1) {
            return null;
        }
        return groups.get(index + 1);
    }
    
    /**
     * Gets the previous group in the track before the specified group.
     * 
     * @param group the current group
     * @return the previous group, or null if the group is not found or is the first group
     */
    public String getPreviousGroup(String group) {
        int index = groups.indexOf(group.toLowerCase());
        if (index <= 0) {
            return null;
        }
        return groups.get(index - 1);
    }
    
    /**
     * Checks if the specified group is the highest group in the track.
     * 
     * @param group the group to check
     * @return true if the group is the highest in the track
     */
    public boolean isHighestGroup(String group) {
        if (groups.isEmpty()) {
            return false;
        }
        return group.equalsIgnoreCase(groups.get(groups.size() - 1));
    }
    
    /**
     * Checks if the specified group is the lowest group in the track.
     * 
     * @param group the group to check
     * @return true if the group is the lowest in the track
     */
    public boolean isLowestGroup(String group) {
        if (groups.isEmpty()) {
            return false;
        }
        return group.equalsIgnoreCase(groups.get(0));
    }
} 