package org.frizzlenpop.frizzlenRanks.tasks;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.frizzlenpop.frizzlenRanks.FrizzlenRanks;
import org.frizzlenpop.frizzlenRanks.model.User;
import org.frizzlenpop.frizzlenRanks.model.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Task that periodically checks for expired temporary permissions and groups
 * and removes them.
 */
public class TemporaryPermissionCleanupTask extends BukkitRunnable {
    private final FrizzlenRanks plugin;
    private final Logger logger;
    
    /**
     * Creates a new cleanup task.
     * 
     * @param plugin the plugin instance
     */
    public TemporaryPermissionCleanupTask(FrizzlenRanks plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }
    
    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        logger.fine("Starting temporary permission cleanup task");
        
        // Process all online players to ensure their permissions are up to date
        List<String> updatedPlayers = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            String playerName = player.getName();
            String worldName = player.getWorld().getName();
            
            World pluginWorld = plugin.getDataManager().getWorld(worldName);
            User user = pluginWorld.getUser(playerName);
            
            boolean permissionsUpdated = checkAndCleanTempPermissions(user, playerName);
            boolean groupsUpdated = checkAndCleanTempGroups(user, playerName);
            
            // If any temp permissions or groups were removed, refresh the player's permissions
            if (permissionsUpdated || groupsUpdated) {
                plugin.resetPlayerPermissionCache(playerName);
                updatedPlayers.add(playerName);
            }
        }
        
        // Process offline players too (to keep data clean)
        for (org.frizzlenpop.frizzlenRanks.model.World world : plugin.getDataManager().getWorlds()) {
            for (User user : world.getUsers()) {
                String playerName = user.getName();
                
                // Skip online players as we've already processed them
                if (Bukkit.getPlayerExact(playerName) != null) {
                    continue;
                }
                
                // Just clean the expired entries without resetting permissions since they're offline
                checkAndCleanTempPermissions(user, playerName);
                checkAndCleanTempGroups(user, playerName);
            }
        }
        
        // Log completion info
        long duration = System.currentTimeMillis() - startTime;
        if (!updatedPlayers.isEmpty()) {
            logger.info("Cleaned expired temporary permissions/groups for " + updatedPlayers.size() + 
                       " players: " + String.join(", ", updatedPlayers) + " (took " + duration + "ms)");
        } else {
            logger.fine("Temporary permission cleanup complete, no changes needed (took " + duration + "ms)");
        }
    }
    
    /**
     * Checks and cleans temporary permissions for a user.
     * 
     * @param user the user to check
     * @param playerName the player's name for logging
     * @return true if any permissions were removed, false otherwise
     */
    private boolean checkAndCleanTempPermissions(User user, String playerName) {
        Map<String, Long> tempPerms = user.getTemporaryPermissions();
        long currentTime = System.currentTimeMillis();
        boolean updated = false;
        
        List<String> expiredPermissions = new ArrayList<>();
        for (Map.Entry<String, Long> entry : tempPerms.entrySet()) {
            if (entry.getValue() <= currentTime) {
                expiredPermissions.add(entry.getKey());
            }
        }
        
        for (String permission : expiredPermissions) {
            user.removeTemporaryPermission(permission);
            logger.info("Removed expired permission " + permission + " from " + playerName);
            updated = true;
        }
        
        return updated;
    }
    
    /**
     * Checks and cleans temporary groups for a user.
     * 
     * @param user the user to check
     * @param playerName the player's name for logging
     * @return true if any groups were removed, false otherwise
     */
    private boolean checkAndCleanTempGroups(User user, String playerName) {
        Map<String, Long> tempGroups = user.getTemporaryGroups();
        long currentTime = System.currentTimeMillis();
        boolean updated = false;
        
        List<String> expiredGroups = new ArrayList<>();
        for (Map.Entry<String, Long> entry : tempGroups.entrySet()) {
            if (entry.getValue() <= currentTime) {
                expiredGroups.add(entry.getKey());
            }
        }
        
        for (String group : expiredGroups) {
            user.removeTemporaryGroup(group);
            logger.info("Removed " + playerName + " from expired group " + group);
            updated = true;
        }
        
        return updated;
    }
} 