package org.frizzlenpop.frizzlenRanks.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.frizzlenpop.frizzlenRanks.FrizzlenRanks;
import org.frizzlenpop.frizzlenRanks.model.User;
import org.frizzlenpop.frizzlenRanks.model.World;

/**
 * Listens for player events.
 */
public class PlayerListener implements Listener {
    private final FrizzlenRanks plugin;
    private static FrizzlenRanks staticPlugin;
    
    /**
     * Creates a new PlayerListener.
     * 
     * @param plugin the plugin instance
     */
    public PlayerListener(FrizzlenRanks plugin) {
        this.plugin = plugin;
        staticPlugin = plugin;
    }
    
    /**
     * Handles player join events.
     * 
     * @param event the event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String worldName = player.getWorld().getName();
        String playerName = player.getName();
        
        // Log player join for debugging
        plugin.getLogger().info("Player joined: " + playerName + " (world: " + worldName + ")");
        
        // Ensure the player exists in the data
        World world = plugin.getDataManager().getWorld(worldName);
        User user = world.getUser(playerName);
        
        // If the user has no groups, add them to the default group
        if (user.getGroups().isEmpty()) {
            plugin.getLogger().info("Player " + playerName + " has no groups, adding to default group");
            user.addGroup("default");
        } else {
            plugin.getLogger().info("Player " + playerName + " has groups: " + String.join(", ", user.getGroups()));
        }
        
        // Always save on player join to ensure data integrity
        plugin.getDataManager().saveWorld(world);
        
        // Always sync user across worlds on join
        plugin.getDataManager().syncUserAcrossWorlds(world, playerName);
        
        // Reset Vault's permission cache for this player with a small initial delay
        // This helps avoid timing issues with other plugins
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            plugin.getLogger().info("Performing initial permission setup for: " + playerName);
            plugin.resetPlayerPermissionCache(playerName);
            updatePlayerTabDisplay(player);
        }, 5L); // 0.25 second delay
        
        // Apply permissions again after a short delay
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            plugin.getLogger().info("Refreshing permissions for " + playerName + " after delay (1)");
            plugin.resetPlayerPermissionCache(playerName);
            updatePlayerTabDisplay(player);
            
            // Log the player's groups and permissions for debugging
            World w = plugin.getDataManager().getWorld(player.getWorld().getName());
            User u = w.getUser(playerName);
            plugin.getLogger().info("Player " + playerName + " groups: " + String.join(", ", u.getGroups()));
            
            // Log a few permissions for confirmation
            plugin.getLogger().info("Checking permissions for player: " + playerName);
            int permCount = 0;
            for (org.bukkit.permissions.PermissionAttachmentInfo perm : player.getEffectivePermissions()) {
                if (perm.getValue()) {
                    permCount++;
                    if (permCount <= 5) {
                        plugin.getLogger().info("  - Has perm: " + perm.getPermission());
                    }
                }
            }
            plugin.getLogger().info("Total permissions for " + playerName + ": " + permCount);
            
            // If no permissions are found, try one more time with a longer delay
            if (permCount == 0) {
                plugin.getLogger().warning("No permissions found for " + playerName + " - trying again with longer delay");
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    plugin.getLogger().info("Refreshing permissions for " + playerName + " after delay (2)");
                    plugin.resetPlayerPermissionCache(playerName);
                    updatePlayerTabDisplay(player);
                    
                    // Final check
                    int finalPermCount = 0;
                    for (org.bukkit.permissions.PermissionAttachmentInfo perm : player.getEffectivePermissions()) {
                        if (perm.getValue()) finalPermCount++;
                    }
                    plugin.getLogger().info("Final permission count for " + playerName + ": " + finalPermCount);
                }, 60L); // 3 second delay - final attempt
            }
        }, 40L); // 2 second delay - longer delay for more reliable permission application
        
        // Add extra delayed updates for tab display sorting specifically
        // This helps handle cases where other plugins might override our changes
        Bukkit.getScheduler().runTaskLater(plugin, () -> ensurePlayerTabSorting(player), 80L);  // 4 seconds
        Bukkit.getScheduler().runTaskLater(plugin, () -> ensurePlayerTabSorting(player), 120L); // 6 seconds
        Bukkit.getScheduler().runTaskLater(plugin, () -> ensurePlayerTabSorting(player), 200L); // 10 seconds
    }
    
    /**
     * Handles player chat events.
     * 
     * @param event the event
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String playerWorld = player.getWorld().getName();
        
        // Make sure we're using the player's current world
        plugin.getDataManager().setSelectedWorld(playerWorld);
        
        // Get prefix and suffix
        String prefix = plugin.getChatHook().getPlayerPrefix(playerWorld, player);
        String suffix = plugin.getChatHook().getPlayerSuffix(playerWorld, player);
        
        /* Debug information - Disabled as requested
        if (player.hasPermission("frizzlenranks.admin")) {
            player.sendMessage("§7[Debug] Your prefix: '" + prefix + "'");
            player.sendMessage("§7[Debug] Your suffix: '" + suffix + "'");
            player.sendMessage("§7[Debug] Your world: '" + playerWorld + "'");
            
            // Show groups the player is in
            World world = plugin.getDataManager().getWorld(playerWorld);
            User user = world.getUser(player.getName());
            player.sendMessage("§7[Debug] Your groups: " + String.join(", ", user.getGroups()));
            player.sendMessage("§7[Debug] Selected world: " + plugin.getDataManager().getSelectedWorld());
        }
        */
        
        // Make sure prefix and suffix are never null
        if (prefix == null) prefix = "";
        if (suffix == null) suffix = "";
        
        // Translate color codes in prefix and suffix
        prefix = prefix.replace("&", "§");
        suffix = suffix.replace("&", "§");
        
        // Get the chat format from config
        String format = plugin.getConfigManager().getChatFormat();
        
        // Replace placeholders
        format = format.replace("{prefix}", prefix);
        format = format.replace("{suffix}", suffix);
        format = format.replace("{world}", playerWorld);
        format = format.replace("{name}", player.getName());
        format = format.replace("{message}", "%2$s"); // Use %2$s for the message to preserve formatting
        
        // Apply color codes to the entire format
        format = format.replace("&", "§");
        
        // Set the new format
        event.setFormat(format);
    }
    
    /**
     * Ensures the player's tab sorting without changing display names.
     * This method only focuses on maintaining the team assignments for proper sorting.
     * 
     * @param player the player to ensure sorting for
     */
    public void ensurePlayerTabSorting(Player player) {
        if (player == null || !player.isOnline()) return;
        
        try {
            // Get the player's world
            String worldName = player.getWorld().getName();
            
            // Get the player's groups and find the highest priority group
            org.frizzlenpop.frizzlenRanks.model.World pluginWorld = plugin.getDataManager().getWorld(worldName);
            User user = pluginWorld.getUser(player.getName());
            
            // Initialize max priority to lowest possible value
            int maxPriority = Integer.MIN_VALUE;
            String highestGroup = "default"; // Default fallback
            
            // Find the highest priority group
            for (String groupName : user.getGroups()) {
                if (pluginWorld.hasGroup(groupName)) {
                    org.frizzlenpop.frizzlenRanks.model.Group group = pluginWorld.getGroup(groupName);
                    int priority = group.getPriority();
                    
                    if (priority > maxPriority) {
                        maxPriority = priority;
                        highestGroup = groupName;
                    }
                }
            }
            
            // Format the priority as a string with leading zeros for correct sorting
            // Higher priority numbers will be at the top of the tablist
            String formattedPriority = String.format("%04d", 9999 - maxPriority); // Invert for descending order
            
            // Get prefix and suffix (we'll need these for the team)
            String prefix = plugin.getChatHook().getPlayerPrefix(worldName, player);
            String suffix = plugin.getChatHook().getPlayerSuffix(worldName, player);
            
            // Make sure prefix and suffix are never null
            if (prefix == null) prefix = "";
            if (suffix == null) suffix = "";
            
            // Translate color codes
            prefix = prefix.replace("&", "§");
            suffix = suffix.replace("&", "§");
            
            // Get or create a team for this player based on their priority
            org.bukkit.scoreboard.Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
            String teamName = "FR_" + formattedPriority + "_" + highestGroup; // Add prefix to avoid conflicts
            
            // Ensure name is not longer than 16 characters (Minecraft limit)
            if (teamName.length() > 16) {
                teamName = teamName.substring(0, 16);
            }
            
            // Check if player is already in the correct team
            boolean isInCorrectTeam = false;
            for (org.bukkit.scoreboard.Team team : scoreboard.getTeams()) {
                if (team.getName().equals(teamName) && team.hasEntry(player.getName())) {
                    isInCorrectTeam = true;
                    break;
                }
            }
            
            // If already in correct team, no need to change anything
            if (isInCorrectTeam) {
                return;
            }
            
            // Remove player from any existing teams
            for (org.bukkit.scoreboard.Team team : scoreboard.getTeams()) {
                if (team.hasEntry(player.getName())) {
                    team.removeEntry(player.getName());
                }
            }
            
            // Get or create the team
            org.bukkit.scoreboard.Team team = scoreboard.getTeam(teamName);
            if (team == null) {
                // Try to unregister any existing team with the same name (just to be safe)
                try {
                    org.bukkit.scoreboard.Team existingTeam = scoreboard.getTeam(teamName);
                    if (existingTeam != null) {
                        existingTeam.unregister();
                    }
                } catch (Exception e) {
                    // Ignore - just being cautious
                }
                
                // Create new team
                team = scoreboard.registerNewTeam(teamName);
                team.setPrefix(prefix);
                team.setSuffix(suffix);
                
                // Set team options
                try {
                    // Use reflection to check if these methods exist (1.12+ feature)
                    team.getClass().getMethod("setColor", org.bukkit.ChatColor.class);
                    // If we get here, the methods exist
                    try {
                        // Get the first color code from the prefix if available
                        org.bukkit.ChatColor color = org.bukkit.ChatColor.WHITE; // Default
                        if (!prefix.isEmpty()) {
                            char colorChar = prefix.charAt(1); // Get color after §
                            org.bukkit.ChatColor possibleColor = org.bukkit.ChatColor.getByChar(colorChar);
                            if (possibleColor != null && possibleColor.isColor()) {
                                color = possibleColor;
                            }
                        }
                        team.setColor(color);
                    } catch (Exception e) {
                        // Ignore reflection errors
                    }
                } catch (NoSuchMethodException e) {
                    // Method doesn't exist in this version, ignore
                }
            } else {
                // Update team settings in case they changed
                team.setPrefix(prefix);
                team.setSuffix(suffix);
            }
            
            // Add the player to the team
            team.addEntry(player.getName());
            
        } catch (Exception e) {
            // Catch any errors to prevent tab display issues from breaking the plugin
            plugin.getLogger().warning("Error ensuring tab sorting for " + player.getName() + ": " + e.getMessage());
        }
    }
    
    /**
     * Updates a player's tab display name based on their prefix and suffix.
     * Also assigns players to teams to sort them by rank priority in the tablist.
     * 
     * @param player the player to update
     */
    public void updatePlayerTabDisplay(Player player) {
        if (player == null) return;
        
        try {
            // Get the player's world
            String worldName = player.getWorld().getName();
            
            // Get prefix and suffix
            String prefix = plugin.getChatHook().getPlayerPrefix(worldName, player);
            String suffix = plugin.getChatHook().getPlayerSuffix(worldName, player);
            
            // Make sure prefix and suffix are never null
            if (prefix == null) prefix = "";
            if (suffix == null) suffix = "";
            
            // Translate color codes
            prefix = prefix.replace("&", "§");
            suffix = suffix.replace("&", "§");
            
            // Create the formatted display name for tab
            String displayName = prefix + player.getName() + suffix;
            
            // Set the player's tab display name
            player.setPlayerListName(displayName);
            player.setDisplayName(displayName);
            
            // Update the tab sorting (now delegated to separate method)
            ensurePlayerTabSorting(player);
            
        } catch (Exception e) {
            // Catch any errors to prevent tab display issues from breaking the plugin
            plugin.getLogger().warning("Error updating tab display for " + player.getName() + ": " + e.getMessage());
        }
    }
    
    /**
     * Handles player quit events.
     * 
     * @param event the event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        String worldName = player.getWorld().getName();
        String playerName = player.getName();
        
        // Save the player's data to ensure ranks and permissions are retained
        World world = plugin.getDataManager().getWorld(worldName);
        User user = world.getUser(playerName);
        
        // Always save on player quit, regardless of auto-save setting
        plugin.getDataManager().saveWorld(world);
        
        // Sync user across worlds
        plugin.getDataManager().syncUserAcrossWorlds(world, playerName);
        
        // Also save all worlds to be extra safe
        plugin.getDataManager().saveAll();
        
        // Clean up permission attachments to prevent memory leaks
        plugin.cleanupPlayerPermissions(playerName);
        
        plugin.getLogger().info("Saved data for player " + playerName + " on disconnect");
    }
    
    /**
     * Handles player world change events to update their tab display.
     * 
     * @param event the event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        String newWorldName = player.getWorld().getName();
        String playerName = player.getName();
        
        // Log world change for debugging
        plugin.getLogger().info("Player " + playerName + " changed worlds to: " + newWorldName);
        
        // Update the player's tab display for the new world
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            plugin.resetPlayerPermissionCache(playerName);
            updatePlayerTabDisplay(player);
            plugin.getLogger().info("Updated tab display for " + playerName + " after world change");
        }, 5L); // Short delay for stability
    }
} 