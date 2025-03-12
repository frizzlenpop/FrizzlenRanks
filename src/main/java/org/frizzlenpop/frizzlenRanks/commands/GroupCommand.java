package org.frizzlenpop.frizzlenRanks.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenRanks.FrizzlenRanks;
import org.frizzlenpop.frizzlenRanks.listeners.PlayerListener;
import org.frizzlenpop.frizzlenRanks.model.Group;
import org.frizzlenpop.frizzlenRanks.model.User;
import org.frizzlenpop.frizzlenRanks.model.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles the /group command.
 */
public class GroupCommand implements CommandExecutor, TabCompleter {
    private final FrizzlenRanks plugin;
    private final List<String> subCommands = Arrays.asList(
            "addperm", "removeperm", "listperms", "addgroup", "removegroup", "listgroups", "meta", "info", "priority", "prefix", "suffix"
    );
    
    /**
     * Creates a new GroupCommand.
     * 
     * @param plugin the plugin instance
     */
    public GroupCommand(FrizzlenRanks plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("frizzlenranks.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }
        
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /group <groupname> [action] [arguments]");
            return true;
        }
        
        String groupName = args[0];
        World world = plugin.getDataManager().getSelectedWorldObj();
        Group group = world.getGroup(groupName);
        
        if (args.length == 1) {
            // Display group info
            sender.sendMessage(ChatColor.GREEN + "Group: " + ChatColor.WHITE + group.getName());
            sender.sendMessage(ChatColor.GREEN + "World: " + ChatColor.WHITE + world.getName());
            sender.sendMessage(ChatColor.GREEN + "Priority: " + ChatColor.WHITE + group.getPriority());
            sender.sendMessage(ChatColor.GREEN + "Inheritance: " + ChatColor.WHITE + String.join(", ", group.getInheritance()));
            sender.sendMessage(ChatColor.GREEN + "Permissions: " + ChatColor.WHITE + group.getPermissions().size() + " permissions");
            sender.sendMessage(ChatColor.GREEN + "Metadata: " + ChatColor.WHITE + group.getMeta().size() + " entries");
            return true;
        }
        
        String action = args[1].toLowerCase();
        
        switch (action) {
            case "addperm":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /group " + groupName + " addperm <permission>");
                    return true;
                }
                
                String permission = args[2];
                group.addPermission(permission);
                sender.sendMessage(ChatColor.GREEN + "Added permission " + permission + " to group " + groupName);
                break;
                
            case "removeperm":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /group " + groupName + " removeperm <permission>");
                    return true;
                }
                
                permission = args[2];
                group.removePermission(permission);
                sender.sendMessage(ChatColor.GREEN + "Removed permission " + permission + " from group " + groupName);
                break;
                
            case "listperms":
                sender.sendMessage(ChatColor.GREEN + "Permissions for group " + groupName + ":");
                for (String perm : group.getPermissions()) {
                    sender.sendMessage(ChatColor.WHITE + "- " + perm);
                }
                break;
                
            case "addgroup":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /group " + groupName + " addgroup <group>");
                    return true;
                }
                
                String inheritedGroup = args[2];
                
                if (inheritedGroup.equalsIgnoreCase(groupName)) {
                    sender.sendMessage(ChatColor.RED + "A group cannot inherit from itself.");
                    return true;
                }
                
                group.addInheritance(inheritedGroup);
                sender.sendMessage(ChatColor.GREEN + "Added group " + inheritedGroup + " to inheritance of " + groupName);
                break;
                
            case "removegroup":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /group " + groupName + " removegroup <group>");
                    return true;
                }
                
                inheritedGroup = args[2];
                group.removeInheritance(inheritedGroup);
                sender.sendMessage(ChatColor.GREEN + "Removed group " + inheritedGroup + " from inheritance of " + groupName);
                break;
                
            case "listgroups":
                sender.sendMessage(ChatColor.GREEN + "Inherited groups for group " + groupName + ":");
                for (String inherited : group.getInheritance()) {
                    sender.sendMessage(ChatColor.WHITE + "- " + inherited);
                }
                break;
                
            case "meta":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /group " + groupName + " meta <key> [value]");
                    return true;
                }
                
                String key = args[2];
                
                if (args.length == 3) {
                    // Display meta value
                    String value = group.getMeta(key);
                    if (value != null) {
                        sender.sendMessage(ChatColor.GREEN + "Meta " + key + " for group " + groupName + ": " + ChatColor.WHITE + value);
                    } else {
                        sender.sendMessage(ChatColor.RED + "Group " + groupName + " does not have meta " + key);
                    }
                } else {
                    // Set meta value
                    String value = args[3];
                    group.setMeta(key, value);
                    sender.sendMessage(ChatColor.GREEN + "Set meta " + key + " to " + value + " for group " + groupName);
                }
                break;
                
            case "priority":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.GREEN + "Priority for group " + groupName + ": " + ChatColor.WHITE + group.getPriority());
                    return true;
                }
                
                try {
                    int priority = Integer.parseInt(args[2]);
                    group.setPriority(priority);
                    sender.sendMessage(ChatColor.GREEN + "Set priority of group " + groupName + " to " + priority);
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Invalid priority: " + args[2]);
                    return true;
                }
                break;
                
            case "info":
                sender.sendMessage(ChatColor.GREEN + "Group: " + ChatColor.WHITE + group.getName());
                sender.sendMessage(ChatColor.GREEN + "World: " + ChatColor.WHITE + world.getName());
                sender.sendMessage(ChatColor.GREEN + "Priority: " + ChatColor.WHITE + group.getPriority());
                sender.sendMessage(ChatColor.GREEN + "Inheritance: " + ChatColor.WHITE + String.join(", ", group.getInheritance()));
                sender.sendMessage(ChatColor.GREEN + "Permissions: " + ChatColor.WHITE + group.getPermissions().size() + " permissions");
                sender.sendMessage(ChatColor.GREEN + "Metadata: " + ChatColor.WHITE + group.getMeta().size() + " entries");
                break;
                
            case "prefix":
                if (args.length < 3) {
                    // Display current prefix
                    String currentPrefix = plugin.getChatHook().getGroupPrefix(world.getName(), groupName);
                    sender.sendMessage(ChatColor.GREEN + "Current prefix for group " + groupName + ": " + ChatColor.WHITE + currentPrefix);
                    sender.sendMessage(ChatColor.GREEN + "Usage: /group " + groupName + " prefix <new prefix>");
                    return true;
                }
                
                // Set new prefix
                String prefix = args[2];
                plugin.getChatHook().setGroupPrefix(world.getName(), groupName, prefix);
                sender.sendMessage(ChatColor.GREEN + "Set prefix for group " + groupName + " to " + ChatColor.translateAlternateColorCodes('&', prefix));
                
                // Update tab display for all players in this group
                updatePlayersInGroup(world, groupName);
                break;
                
            case "suffix":
                if (args.length < 3) {
                    // Display current suffix
                    String currentSuffix = plugin.getChatHook().getGroupSuffix(world.getName(), groupName);
                    sender.sendMessage(ChatColor.GREEN + "Current suffix for group " + groupName + ": " + ChatColor.WHITE + currentSuffix);
                    sender.sendMessage(ChatColor.GREEN + "Usage: /group " + groupName + " suffix <new suffix>");
                    return true;
                }
                
                // Set new suffix
                String suffix = args[2];
                plugin.getChatHook().setGroupSuffix(world.getName(), groupName, suffix);
                sender.sendMessage(ChatColor.GREEN + "Set suffix for group " + groupName + " to " + ChatColor.translateAlternateColorCodes('&', suffix));
                
                // Update tab display for all players in this group
                updatePlayersInGroup(world, groupName);
                break;
                
            default:
                sender.sendMessage(ChatColor.RED + "Unknown action: " + action);
                sender.sendMessage(ChatColor.RED + "Available actions: " + String.join(", ", subCommands));
                return true;
        }
        
        // Save if auto-save is enabled
        if (plugin.getConfigManager().autoSave()) {
            plugin.getDataManager().saveWorld(world);
        }
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("frizzlenranks.admin")) {
            return new ArrayList<>();
        }
        
        if (args.length == 1) {
            // Return available groups
            World world = plugin.getDataManager().getSelectedWorldObj();
            return world.getGroups().stream()
                    .map(Group::getName)
                    .filter(group -> group.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            // Return subcommands
            return subCommands.stream()
                    .filter(subCmd -> subCmd.startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 3) {
            String action = args[1].toLowerCase();
            World world = plugin.getDataManager().getSelectedWorldObj();
            Group group = world.getGroup(args[0]);
            
            switch (action) {
                case "removeperm":
                case "listperms":
                    // Return group's permissions
                    return group.getPermissions().stream()
                            .filter(perm -> perm.startsWith(args[2].toLowerCase()))
                            .collect(Collectors.toList());
                    
                case "removegroup":
                case "listgroups":
                    // Return group's inheritance
                    return group.getInheritance().stream()
                            .filter(inherited -> inherited.startsWith(args[2].toLowerCase()))
                            .collect(Collectors.toList());
                    
                case "addgroup":
                    // Return available groups except the current one
                    return world.getGroups().stream()
                            .map(Group::getName)
                            .filter(name -> !name.equalsIgnoreCase(args[0]))
                            .filter(name -> name.startsWith(args[2].toLowerCase()))
                            .collect(Collectors.toList());
                    
                case "meta":
                    // Return group's meta keys
                    return group.getMeta().keySet().stream()
                            .filter(key -> key.startsWith(args[2].toLowerCase()))
                            .collect(Collectors.toList());
            }
        }
        
        return new ArrayList<>();
    }
    
    /**
     * Updates tab display for all online players in a specific group.
     * 
     * @param world the world where the group exists
     * @param groupName the name of the group
     */
    private void updatePlayersInGroup(World world, String groupName) {
        // Update all online players who are in this group
        for (Player player : Bukkit.getOnlinePlayers()) {
            User user = world.getUser(player.getName());
            if (user.inGroup(groupName)) {
                PlayerListener playerListener = new PlayerListener(plugin);
                playerListener.updatePlayerTabDisplay(player);
            }
        }
    }
} 