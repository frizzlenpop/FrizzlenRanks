package org.frizzlenpop.frizzlenRanks.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.frizzlenpop.frizzlenRanks.FrizzlenRanks;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles the /world command.
 */
public class WorldCommand implements CommandExecutor, TabCompleter {
    private final FrizzlenRanks plugin;
    
    /**
     * Creates a new WorldCommand.
     * 
     * @param plugin the plugin instance
     */
    public WorldCommand(FrizzlenRanks plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("frizzlenranks.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }
        
        if (args.length < 1) {
            // Display current world
            String currentWorld = plugin.getDataManager().getSelectedWorld();
            sender.sendMessage(ChatColor.GREEN + "Current world: " + ChatColor.WHITE + currentWorld);
            sender.sendMessage(ChatColor.GREEN + "Use /world <worldname> to select a different world for permissions");
            
            // Show if global users is enabled
            boolean globalUsers = plugin.getConfigManager().useGlobalUsers();
            if (globalUsers) {
                sender.sendMessage(ChatColor.GREEN + "Global users: " + ChatColor.WHITE + "Enabled (user permissions sync across worlds)");
            } else {
                sender.sendMessage(ChatColor.GREEN + "Global users: " + ChatColor.WHITE + "Disabled (each world has separate users)");
            }
            
            return true;
        }
        
        String worldName = args[0];
        
        // Check if the world exists
        if (!worldName.equalsIgnoreCase("global") && Bukkit.getWorld(worldName) == null) {
            sender.sendMessage(ChatColor.RED + "World '" + worldName + "' does not exist.");
            sender.sendMessage(ChatColor.RED + "Available worlds: " + getAvailableWorlds());
            return true;
        }
        
        // Get the previous world for reference
        String previousWorld = plugin.getDataManager().getSelectedWorld();
        
        // Set the selected world
        plugin.getDataManager().setSelectedWorld(worldName);
        sender.sendMessage(ChatColor.GREEN + "Selected world: " + ChatColor.WHITE + worldName);
        
        // Inform about global users
        if (plugin.getConfigManager().useGlobalUsers()) {
            sender.sendMessage(ChatColor.GREEN + "Note: Global users is enabled, so user changes will apply to all worlds.");
        } else {
            sender.sendMessage(ChatColor.GREEN + "Note: Global users is disabled, so user changes only apply to this world.");
        }
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("frizzlenranks.admin")) {
            return new ArrayList<>();
        }
        
        if (args.length == 1) {
            // Return available worlds
            List<String> worlds = new ArrayList<>();
            worlds.add("global");
            
            for (World world : Bukkit.getWorlds()) {
                worlds.add(world.getName());
            }
            
            return worlds.stream()
                    .filter(world -> world.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        return new ArrayList<>();
    }
    
    /**
     * Gets a comma-separated list of available worlds.
     * 
     * @return a string of available worlds
     */
    private String getAvailableWorlds() {
        List<String> worlds = new ArrayList<>();
        worlds.add("global");
        
        for (World world : Bukkit.getWorlds()) {
            worlds.add(world.getName());
        }
        
        return String.join(", ", worlds);
    }
} 