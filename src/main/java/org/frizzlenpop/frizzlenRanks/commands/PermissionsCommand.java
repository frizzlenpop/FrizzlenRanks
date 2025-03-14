package org.frizzlenpop.frizzlenRanks.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.frizzlenpop.frizzlenRanks.FrizzlenRanks;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Command to list and export all commands and permissions on the server.
 */
public class PermissionsCommand implements CommandExecutor {
    private final FrizzlenRanks plugin;
    
    public PermissionsCommand(FrizzlenRanks plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("frizzlenranks.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }
        
        if (args.length == 0) {
            sender.sendMessage(ChatColor.GREEN + "=== FrizzlenRanks Permissions Tools ===");
            sender.sendMessage(ChatColor.YELLOW + "/perms list" + ChatColor.WHITE + " - List all permissions on the server");
            sender.sendMessage(ChatColor.YELLOW + "/perms commands" + ChatColor.WHITE + " - List all commands on the server");
            sender.sendMessage(ChatColor.YELLOW + "/perms export" + ChatColor.WHITE + " - Export all commands and permissions to files");
            sender.sendMessage(ChatColor.YELLOW + "/perms search <query>" + ChatColor.WHITE + " - Search for permissions or commands");
            sender.sendMessage(ChatColor.YELLOW + "/perms plugin <plugin>" + ChatColor.WHITE + " - List all commands and permissions for a plugin");
            return true;
        }
        
        String action = args[0].toLowerCase();
        
        switch (action) {
            case "list":
                listAllPermissions(sender);
                break;
            case "commands":
                listAllCommands(sender);
                break;
            case "export":
                exportAllData(sender);
                break;
            case "search":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /perms search <query>");
                    return true;
                }
                searchForPermissionsAndCommands(sender, args[1]);
                break;
            case "plugin":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /perms plugin <plugin>");
                    return true;
                }
                listPluginPermissionsAndCommands(sender, args[1]);
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Unknown action: " + action);
                sender.sendMessage(ChatColor.RED + "Available actions: list, commands, export, search, plugin");
        }
        
        return true;
    }
    
    /**
     * Lists all permissions registered on the server.
     * 
     * @param sender the command sender
     */
    private void listAllPermissions(CommandSender sender) {
        sender.sendMessage(ChatColor.GREEN + "=== Permissions on this server ===");
        
        // Group permissions by plugin
        Map<String, List<Permission>> permissionsByPlugin = getPermissionsByPlugin();
        
        int total = 0;
        
        // Display permissions by plugin
        for (Map.Entry<String, List<Permission>> entry : permissionsByPlugin.entrySet()) {
            String pluginName = entry.getKey();
            List<Permission> permissions = entry.getValue();
            
            sender.sendMessage(ChatColor.YELLOW + pluginName + ChatColor.WHITE + " (" + permissions.size() + " permissions):");
            
            int count = 0;
            for (Permission permission : permissions) {
                // Only show the first 10 permissions per plugin to avoid spamming chat
                if (count < 10) {
                    sender.sendMessage("  " + ChatColor.WHITE + permission.getName() + ChatColor.GRAY + 
                                    " - Default: " + permission.getDefault().name().toLowerCase());
                    count++;
                }
            }
            
            if (permissions.size() > 10) {
                sender.sendMessage(ChatColor.GRAY + "  ... and " + (permissions.size() - 10) + " more");
            }
            
            total += permissions.size();
        }
        
        sender.sendMessage(ChatColor.GREEN + "Total permissions: " + total);
        sender.sendMessage(ChatColor.GREEN + "Use '/perms export' to save a complete list to a file.");
    }
    
    /**
     * Lists all commands registered on the server.
     * 
     * @param sender the command sender
     */
    private void listAllCommands(CommandSender sender) {
        sender.sendMessage(ChatColor.GREEN + "=== Commands on this server ===");
        
        Map<String, Map<String, Command>> commandsByPlugin = getCommandsByPlugin();
        
        // Display commands by plugin
        int total = 0;
        
        for (Map.Entry<String, Map<String, Command>> entry : commandsByPlugin.entrySet()) {
            String pluginName = entry.getKey();
            Map<String, Command> commands = entry.getValue();
            
            sender.sendMessage(ChatColor.YELLOW + pluginName + ChatColor.WHITE + " (" + commands.size() + " commands):");
            
            int count = 0;
            for (Command cmd : commands.values()) {
                // Only show the first 10 commands per plugin to avoid spamming chat
                if (count < 10) {
                    String permission = cmd.getPermission() != null ? cmd.getPermission() : "none";
                    sender.sendMessage("  " + ChatColor.WHITE + "/" + cmd.getName() + ChatColor.GRAY + 
                                    " - Permission: " + permission);
                    count++;
                }
            }
            
            if (commands.size() > 10) {
                sender.sendMessage(ChatColor.GRAY + "  ... and " + (commands.size() - 10) + " more");
            }
            
            total += commands.size();
        }
        
        sender.sendMessage(ChatColor.GREEN + "Total commands: " + total);
        sender.sendMessage(ChatColor.GREEN + "Use '/perms export' to save a complete list to a file.");
    }
    
    /**
     * Searches for permissions and commands matching a query.
     * 
     * @param sender the command sender
     * @param query the search query
     */
    private void searchForPermissionsAndCommands(CommandSender sender, String query) {
        query = query.toLowerCase();
        
        sender.sendMessage(ChatColor.GREEN + "=== Search results for '" + query + "' ===");
        
        // Search permissions
        Map<String, List<Permission>> permissionsByPlugin = getPermissionsByPlugin();
        List<String> matchingPermissions = new ArrayList<>();
        
        for (Map.Entry<String, List<Permission>> entry : permissionsByPlugin.entrySet()) {
            String pluginName = entry.getKey();
            
            for (Permission permission : entry.getValue()) {
                if (permission.getName().toLowerCase().contains(query)) {
                    matchingPermissions.add(ChatColor.YELLOW + pluginName + ChatColor.WHITE + " - " + 
                                          permission.getName() + ChatColor.GRAY + " (Default: " + 
                                          permission.getDefault().name().toLowerCase() + ")");
                }
            }
        }
        
        // Search commands
        Map<String, Map<String, Command>> commandsByPlugin = getCommandsByPlugin();
        List<String> matchingCommands = new ArrayList<>();
        
        for (Map.Entry<String, Map<String, Command>> entry : commandsByPlugin.entrySet()) {
            String pluginName = entry.getKey();
            
            for (Command cmd : entry.getValue().values()) {
                if (cmd.getName().toLowerCase().contains(query) || 
                    (cmd.getDescription() != null && cmd.getDescription().toLowerCase().contains(query)) ||
                    (cmd.getPermission() != null && cmd.getPermission().toLowerCase().contains(query))) {
                    
                    String permission = cmd.getPermission() != null ? cmd.getPermission() : "none";
                    matchingCommands.add(ChatColor.YELLOW + pluginName + ChatColor.WHITE + " - /" + 
                                        cmd.getName() + ChatColor.GRAY + " (Permission: " + permission + ")");
                }
            }
        }
        
        // Display results
        sender.sendMessage(ChatColor.GREEN + "Matching permissions (" + matchingPermissions.size() + "):");
        if (matchingPermissions.isEmpty()) {
            sender.sendMessage(ChatColor.GRAY + "  No permissions found");
        } else {
            for (int i = 0; i < Math.min(matchingPermissions.size(), 20); i++) {
                sender.sendMessage("  " + matchingPermissions.get(i));
            }
            
            if (matchingPermissions.size() > 20) {
                sender.sendMessage(ChatColor.GRAY + "  ... and " + (matchingPermissions.size() - 20) + " more");
            }
        }
        
        sender.sendMessage(ChatColor.GREEN + "Matching commands (" + matchingCommands.size() + "):");
        if (matchingCommands.isEmpty()) {
            sender.sendMessage(ChatColor.GRAY + "  No commands found");
        } else {
            for (int i = 0; i < Math.min(matchingCommands.size(), 20); i++) {
                sender.sendMessage("  " + matchingCommands.get(i));
            }
            
            if (matchingCommands.size() > 20) {
                sender.sendMessage(ChatColor.GRAY + "  ... and " + (matchingCommands.size() - 20) + " more");
            }
        }
        
        sender.sendMessage(ChatColor.GREEN + "Use '/perms export' to save a complete list to a file.");
    }
    
    /**
     * Lists all permissions and commands for a specific plugin.
     * 
     * @param sender the command sender
     * @param pluginName the plugin name
     */
    private void listPluginPermissionsAndCommands(CommandSender sender, String pluginName) {
        Plugin targetPlugin = null;
        
        // Find the plugin
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            if (plugin.getName().equalsIgnoreCase(pluginName)) {
                targetPlugin = plugin;
                break;
            }
        }
        
        if (targetPlugin == null) {
            sender.sendMessage(ChatColor.RED + "Plugin '" + pluginName + "' not found.");
            return;
        }
        
        pluginName = targetPlugin.getName(); // Use proper casing
        
        sender.sendMessage(ChatColor.GREEN + "=== " + pluginName + " ===");
        
        // List permissions
        List<Permission> pluginPermissions = new ArrayList<>();
        
        // Add all registered permissions
        for (Permission permission : Bukkit.getPluginManager().getPermissions()) {
            if (permission.getName().toLowerCase().startsWith(pluginName.toLowerCase() + ".")) {
                pluginPermissions.add(permission);
            }
        }
        
        // Add permissions from plugin.yml
        for (Permission permission : targetPlugin.getDescription().getPermissions()) {
            if (!pluginPermissions.contains(permission)) {
                pluginPermissions.add(permission);
            }
        }
        
        sender.sendMessage(ChatColor.GREEN + "Permissions (" + pluginPermissions.size() + "):");
        if (pluginPermissions.isEmpty()) {
            sender.sendMessage(ChatColor.GRAY + "  No permissions found");
        } else {
            for (Permission permission : pluginPermissions) {
                sender.sendMessage("  " + ChatColor.WHITE + permission.getName() + ChatColor.GRAY + 
                                  " - Default: " + permission.getDefault().name().toLowerCase());
            }
        }
        
        // List commands
        Map<String, Command> pluginCommands = new TreeMap<>();
        
        try {
            // Use reflection to get the CommandMap
            Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            CommandMap commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());
            
            // Get all known commands
            Map<String, Command> knownCommands = new HashMap<>();
            
            // Try to get the knownCommands field
            try {
                Field knownCommandsField = commandMap.getClass().getDeclaredField("knownCommands");
                knownCommandsField.setAccessible(true);
                knownCommands = (Map<String, Command>) knownCommandsField.get(commandMap);
            } catch (NoSuchFieldException e) {
                sender.sendMessage(ChatColor.RED + "Could not access known commands: " + e.getMessage());
                return;
            }
            
            // Find commands for this plugin
            for (Command cmd : knownCommands.values()) {
                // Skip aliases (commands that have the same name as their label)
                if (!cmd.getName().equals(cmd.getLabel())) {
                    continue;
                }
                
                if (cmd instanceof PluginCommand) {
                    PluginCommand pluginCmd = (PluginCommand) cmd;
                    if (pluginCmd.getPlugin().getName().equals(pluginName)) {
                        pluginCommands.put(cmd.getName(), cmd);
                    }
                }
            }
            
            sender.sendMessage(ChatColor.GREEN + "Commands (" + pluginCommands.size() + "):");
            if (pluginCommands.isEmpty()) {
                sender.sendMessage(ChatColor.GRAY + "  No commands found");
            } else {
                for (Command cmd : pluginCommands.values()) {
                    String permission = cmd.getPermission() != null ? cmd.getPermission() : "none";
                    String description = cmd.getDescription() != null ? cmd.getDescription() : "";
                    
                    sender.sendMessage("  " + ChatColor.WHITE + "/" + cmd.getName() + ChatColor.GRAY + 
                                      " - Permission: " + permission);
                    
                    if (!description.isEmpty()) {
                        sender.sendMessage("    " + ChatColor.GRAY + "Description: " + description);
                    }
                }
            }
            
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Error getting commands: " + e.getMessage());
            e.printStackTrace();
        }
        
        sender.sendMessage(ChatColor.GREEN + "Use '/perms export' to save a complete list to a file.");
    }
    
    /**
     * Exports all permissions and commands to files.
     * 
     * @param sender the command sender
     */
    private void exportAllData(CommandSender sender) {
        sender.sendMessage(ChatColor.GREEN + "Exporting all permissions and commands...");
        
        File exportDir = new File(plugin.getDataFolder(), "exports");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String timestamp = dateFormat.format(new Date());
        
        // Export permissions
        try {
            File permissionsFile = new File(exportDir, "permissions_" + timestamp + ".txt");
            FileWriter writer = new FileWriter(permissionsFile);
            
            writer.write("=== Permissions on this server ===\n");
            writer.write("Generated on " + new Date().toString() + "\n\n");
            
            // Group permissions by plugin
            Map<String, List<Permission>> permissionsByPlugin = getPermissionsByPlugin();
            
            int total = 0;
            
            // Write permissions by plugin
            for (Map.Entry<String, List<Permission>> entry : permissionsByPlugin.entrySet()) {
                String pluginName = entry.getKey();
                List<Permission> permissions = entry.getValue();
                
                writer.write(pluginName + " (" + permissions.size() + " permissions):\n");
                writer.write("==================================================\n\n");
                
                for (Permission permission : permissions) {
                    writer.write("Permission: " + permission.getName() + "\n");
                    writer.write("  Default: " + permission.getDefault().name().toLowerCase() + "\n");
                    
                    // Write description if available
                    if (permission.getDescription() != null && !permission.getDescription().isEmpty()) {
                        writer.write("  Description: " + permission.getDescription() + "\n");
                    }
                    
                    // Write children if available
                    if (!permission.getChildren().isEmpty()) {
                        writer.write("  Children:\n");
                        for (Map.Entry<String, Boolean> child : permission.getChildren().entrySet()) {
                            writer.write("    " + child.getKey() + ": " + child.getValue() + "\n");
                        }
                    }
                    
                    writer.write("\n");
                }
                
                total += permissions.size();
            }
            
            writer.write("Total permissions: " + total + "\n");
            writer.close();
            
            sender.sendMessage(ChatColor.GREEN + "Permissions exported to " + permissionsFile.getName());
            
        } catch (IOException e) {
            sender.sendMessage(ChatColor.RED + "Error exporting permissions: " + e.getMessage());
        }
        
        // Export commands
        try {
            File commandsFile = new File(exportDir, "commands_" + timestamp + ".txt");
            FileWriter writer = new FileWriter(commandsFile);
            
            writer.write("=== Commands on this server ===\n");
            writer.write("Generated on " + new Date().toString() + "\n\n");
            
            Map<String, Map<String, Command>> commandsByPlugin = getCommandsByPlugin();
            
            // Write commands by plugin
            int total = 0;
            
            for (Map.Entry<String, Map<String, Command>> entry : commandsByPlugin.entrySet()) {
                String pluginName = entry.getKey();
                Map<String, Command> commands = entry.getValue();
                
                writer.write(pluginName + " (" + commands.size() + " commands):\n");
                writer.write("==================================================\n\n");
                
                for (Command cmd : commands.values()) {
                    writer.write("Command: /" + cmd.getName() + "\n");
                    
                    // Write permission if available
                    String permission = cmd.getPermission() != null ? cmd.getPermission() : "none";
                    writer.write("  Permission: " + permission + "\n");
                    
                    // Write permission message if available
                    if (cmd.getPermissionMessage() != null && !cmd.getPermissionMessage().isEmpty()) {
                        writer.write("  Permission message: " + cmd.getPermissionMessage() + "\n");
                    }
                    
                    // Write description if available
                    if (cmd.getDescription() != null && !cmd.getDescription().isEmpty()) {
                        writer.write("  Description: " + cmd.getDescription() + "\n");
                    }
                    
                    // Write usage if available
                    if (cmd.getUsage() != null && !cmd.getUsage().isEmpty()) {
                        writer.write("  Usage: " + cmd.getUsage() + "\n");
                    }
                    
                    // Write aliases if available
                    if (cmd.getAliases() != null && !cmd.getAliases().isEmpty()) {
                        writer.write("  Aliases: " + String.join(", ", cmd.getAliases()) + "\n");
                    }
                    
                    // Write command executor information
                    if (cmd instanceof PluginCommand) {
                        PluginCommand pluginCmd = (PluginCommand) cmd;
                        writer.write("  Plugin: " + pluginCmd.getPlugin().getName() + " v" + 
                                    pluginCmd.getPlugin().getDescription().getVersion() + "\n");
                        
                        if (pluginCmd.getExecutor() != null) {
                            writer.write("  Executor: " + pluginCmd.getExecutor().getClass().getName() + "\n");
                        }
                        
                        if (pluginCmd.getTabCompleter() != null) {
                            writer.write("  Tab Completer: " + pluginCmd.getTabCompleter().getClass().getName() + "\n");
                        }
                    }
                    
                    writer.write("\n");
                }
                
                total += commands.size();
            }
            
            writer.write("Total commands: " + total + "\n");
            writer.close();
            
            sender.sendMessage(ChatColor.GREEN + "Commands exported to " + commandsFile.getName());
            
        } catch (IOException e) {
            sender.sendMessage(ChatColor.RED + "Error exporting commands: " + e.getMessage());
        }
        
        // Export an additional HTML version for better readability
        try {
            File htmlFile = new File(exportDir, "server_commands_" + timestamp + ".html");
            FileWriter writer = new FileWriter(htmlFile);
            
            writer.write("<!DOCTYPE html>\n");
            writer.write("<html>\n");
            writer.write("<head>\n");
            writer.write("  <title>Server Commands and Permissions</title>\n");
            writer.write("  <meta charset=\"UTF-8\">\n");
            writer.write("  <style>\n");
            writer.write("    body { font-family: Arial, sans-serif; margin: 20px; }\n");
            writer.write("    h1 { color: #333; }\n");
            writer.write("    h2 { color: #0066cc; margin-top: 30px; }\n");
            writer.write("    h3 { color: #009900; margin-top: 20px; }\n");
            writer.write("    .plugin { background-color: #f0f0f0; padding: 10px; margin: 10px 0; border-radius: 5px; }\n");
            writer.write("    .command { margin: 10px 0; padding-left: 20px; }\n");
            writer.write("    .permission { margin: 10px 0; padding-left: 20px; }\n");
            writer.write("    .details { color: #666; margin-left: 20px; }\n");
            writer.write("    .toc { background-color: #f8f8f8; padding: 10px; border-radius: 5px; margin-bottom: 20px; }\n");
            writer.write("    .toc a { display: block; padding: 2px 0; }\n");
            writer.write("  </style>\n");
            writer.write("</head>\n");
            writer.write("<body>\n");
            writer.write("  <h1>Server Commands and Permissions</h1>\n");
            writer.write("  <p>Generated on " + new Date().toString() + "</p>\n");
            
            writer.write("  <div class=\"toc\">\n");
            writer.write("    <h2>Table of Contents</h2>\n");
            writer.write("    <a href=\"#commands\">Commands</a>\n");
            writer.write("    <a href=\"#permissions\">Permissions</a>\n");
            writer.write("  </div>\n");
            
            // Commands section
            writer.write("  <h2 id=\"commands\">Commands</h2>\n");
            
            Map<String, Map<String, Command>> commandsByPlugin = getCommandsByPlugin();
            int totalCommands = 0;
            
            // Create plugin list for table of contents
            writer.write("  <div class=\"toc\">\n");
            writer.write("    <h3>Plugins with Commands</h3>\n");
            for (String pluginName : commandsByPlugin.keySet()) {
                writer.write("    <a href=\"#cmd-" + pluginName.toLowerCase().replace(" ", "-") + "\">" + pluginName + "</a>\n");
            }
            writer.write("  </div>\n");
            
            // Write commands by plugin
            for (Map.Entry<String, Map<String, Command>> entry : commandsByPlugin.entrySet()) {
                String pluginName = entry.getKey();
                Map<String, Command> commands = entry.getValue();
                
                writer.write("  <div class=\"plugin\">\n");
                writer.write("    <h3 id=\"cmd-" + pluginName.toLowerCase().replace(" ", "-") + "\">" + pluginName + " (" + commands.size() + " commands)</h3>\n");
                
                for (Command cmd : commands.values()) {
                    writer.write("    <div class=\"command\">\n");
                    writer.write("      <h4>/" + cmd.getName() + "</h4>\n");
                    
                    // Write permission if available
                    String permission = cmd.getPermission() != null ? cmd.getPermission() : "none";
                    writer.write("      <p class=\"details\">Permission: " + permission + "</p>\n");
                    
                    // Write description if available
                    if (cmd.getDescription() != null && !cmd.getDescription().isEmpty()) {
                        writer.write("      <p class=\"details\">Description: " + escapeHtml(cmd.getDescription()) + "</p>\n");
                    }
                    
                    // Write usage if available
                    if (cmd.getUsage() != null && !cmd.getUsage().isEmpty()) {
                        writer.write("      <p class=\"details\">Usage: " + escapeHtml(cmd.getUsage()) + "</p>\n");
                    }
                    
                    // Write aliases if available
                    if (cmd.getAliases() != null && !cmd.getAliases().isEmpty()) {
                        writer.write("      <p class=\"details\">Aliases: " + String.join(", ", cmd.getAliases()) + "</p>\n");
                    }
                    
                    writer.write("    </div>\n");
                }
                
                writer.write("  </div>\n");
                totalCommands += commands.size();
            }
            
            writer.write("  <p>Total commands: " + totalCommands + "</p>\n");
            
            // Permissions section
            writer.write("  <h2 id=\"permissions\">Permissions</h2>\n");
            
            Map<String, List<Permission>> permissionsByPlugin = getPermissionsByPlugin();
            int totalPermissions = 0;
            
            // Create plugin list for table of contents
            writer.write("  <div class=\"toc\">\n");
            writer.write("    <h3>Plugins with Permissions</h3>\n");
            for (String pluginName : permissionsByPlugin.keySet()) {
                writer.write("    <a href=\"#perm-" + pluginName.toLowerCase().replace(" ", "-") + "\">" + pluginName + "</a>\n");
            }
            writer.write("  </div>\n");
            
            // Write permissions by plugin
            for (Map.Entry<String, List<Permission>> entry : permissionsByPlugin.entrySet()) {
                String pluginName = entry.getKey();
                List<Permission> permissions = entry.getValue();
                
                writer.write("  <div class=\"plugin\">\n");
                writer.write("    <h3 id=\"perm-" + pluginName.toLowerCase().replace(" ", "-") + "\">" + pluginName + " (" + permissions.size() + " permissions)</h3>\n");
                
                for (Permission permission : permissions) {
                    writer.write("    <div class=\"permission\">\n");
                    writer.write("      <h4>" + permission.getName() + "</h4>\n");
                    
                    // Write default value
                    writer.write("      <p class=\"details\">Default: " + permission.getDefault().name().toLowerCase() + "</p>\n");
                    
                    // Write description if available
                    if (permission.getDescription() != null && !permission.getDescription().isEmpty()) {
                        writer.write("      <p class=\"details\">Description: " + escapeHtml(permission.getDescription()) + "</p>\n");
                    }
                    
                    // Write children if available
                    if (!permission.getChildren().isEmpty()) {
                        writer.write("      <p class=\"details\">Children:</p>\n");
                        writer.write("      <ul class=\"details\">\n");
                        for (Map.Entry<String, Boolean> child : permission.getChildren().entrySet()) {
                            writer.write("        <li>" + child.getKey() + ": " + child.getValue() + "</li>\n");
                        }
                        writer.write("      </ul>\n");
                    }
                    
                    writer.write("    </div>\n");
                }
                
                writer.write("  </div>\n");
                totalPermissions += permissions.size();
            }
            
            writer.write("  <p>Total permissions: " + totalPermissions + "</p>\n");
            
            writer.write("</body>\n");
            writer.write("</html>\n");
            
            writer.close();
            
            sender.sendMessage(ChatColor.GREEN + "HTML report exported to " + htmlFile.getName());
            
        } catch (IOException e) {
            sender.sendMessage(ChatColor.RED + "Error exporting HTML report: " + e.getMessage());
        }
    }
    
    /**
     * Gets all permissions grouped by plugin.
     * 
     * @return a map of plugin names to permissions
     */
    private Map<String, List<Permission>> getPermissionsByPlugin() {
        Map<String, List<Permission>> permissionsByPlugin = new TreeMap<>();
        Set<String> processedPermissions = new TreeSet<>();
        
        // Add all registered permissions
        for (Permission permission : Bukkit.getPluginManager().getPermissions()) {
            String pluginName = getPluginForPermission(permission);
            permissionsByPlugin.computeIfAbsent(pluginName, k -> new ArrayList<>()).add(permission);
            processedPermissions.add(permission.getName().toLowerCase());
        }
        
        // Add permissions from plugin.yml files
        for (Plugin bukkitPlugin : Bukkit.getPluginManager().getPlugins()) {
            String pluginName = bukkitPlugin.getName();
            List<Permission> pluginPermissions = permissionsByPlugin.computeIfAbsent(pluginName, k -> new ArrayList<>());
            
            // Add permissions from the plugin description
            for (Permission permission : bukkitPlugin.getDescription().getPermissions()) {
                if (!processedPermissions.contains(permission.getName().toLowerCase())) {
                    pluginPermissions.add(permission);
                    processedPermissions.add(permission.getName().toLowerCase());
                }
            }
        }
        
        return permissionsByPlugin;
    }
    
    /**
     * Gets all commands grouped by plugin.
     * 
     * @return a map of plugin names to commands
     */
    private Map<String, Map<String, Command>> getCommandsByPlugin() {
        Map<String, Map<String, Command>> commandsByPlugin = new TreeMap<>();
        
        try {
            // Method 1: Try using server command map reflection
            try {
                // Use reflection to get the CommandMap
                Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
                commandMapField.setAccessible(true);
                CommandMap commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());
                
                // Get all known commands
                Map<String, Command> knownCommands = new HashMap<>();
                
                // Try to get the knownCommands field
                try {
                    Field knownCommandsField = commandMap.getClass().getDeclaredField("knownCommands");
                    knownCommandsField.setAccessible(true);
                    knownCommands = (Map<String, Command>) knownCommandsField.get(commandMap);
                    
                    // Group commands by plugin
                    for (Command cmd : knownCommands.values()) {
                        // Skip aliases (commands that have the same name as their label)
                        if (!cmd.getName().equals(cmd.getLabel())) {
                            continue;
                        }
                        
                        String pluginName = getPluginForCommand(cmd);
                        commandsByPlugin.computeIfAbsent(pluginName, k -> new TreeMap<>()).put(cmd.getName(), cmd);
                    }
                    
                    plugin.getLogger().info("Successfully retrieved " + knownCommands.size() + 
                                          " commands via reflection (Method 1)");
                    
                } catch (NoSuchFieldException e) {
                    plugin.getLogger().warning("Could not access knownCommands field: " + e.getMessage());
                    // Continue to alternate methods
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Error accessing command map via reflection: " + e.getMessage());
                // Continue to alternate methods
            }
            
            // Method 2: If first method failed, use plugin.getDescription().getCommands()
            if (commandsByPlugin.isEmpty()) {
                plugin.getLogger().info("Trying alternate command collection method (Method 2)");
                
                for (Plugin bukkitPlugin : Bukkit.getPluginManager().getPlugins()) {
                    String pluginName = bukkitPlugin.getName();
                    Map<String, Map<String, Object>> pluginCommands = bukkitPlugin.getDescription().getCommands();
                    
                    if (pluginCommands != null && !pluginCommands.isEmpty()) {
                        Map<String, Command> commands = commandsByPlugin.computeIfAbsent(pluginName, k -> new TreeMap<>());
                        
                        // For each command in plugin.yml
                        for (String cmdName : pluginCommands.keySet()) {
                            // Try to get the command from Bukkit
                            PluginCommand cmd = Bukkit.getPluginCommand(cmdName);
                            
                            if (cmd != null) {
                                commands.put(cmdName, cmd);
                            } else {
                                // Create a dummy command with data from plugin.yml
                                Map<String, Object> cmdData = pluginCommands.get(cmdName);
                                DummyCommand dummyCmd = new DummyCommand(cmdName);
                                
                                if (cmdData.containsKey("description")) {
                                    dummyCmd.setDescription(cmdData.get("description").toString());
                                }
                                
                                if (cmdData.containsKey("usage")) {
                                    dummyCmd.setUsage(cmdData.get("usage").toString());
                                }
                                
                                if (cmdData.containsKey("permission")) {
                                    dummyCmd.setPermission(cmdData.get("permission").toString());
                                }
                                
                                commands.put(cmdName, dummyCmd);
                            }
                        }
                    }
                }
                
                // Count the total commands found
                int totalCmds = 0;
                for (Map<String, Command> cmds : commandsByPlugin.values()) {
                    totalCmds += cmds.size();
                }
                
                plugin.getLogger().info("Retrieved " + totalCmds + " commands from plugin.yml files (Method 2)");
            }
            
            // Method 3: If all else failed, get plugin commands directly from Bukkit
            if (commandsByPlugin.isEmpty()) {
                plugin.getLogger().info("Trying last resort command collection method (Method 3)");
                
                for (Plugin bukkitPlugin : Bukkit.getPluginManager().getPlugins()) {
                    String pluginName = bukkitPlugin.getName();
                    Map<String, Command> commands = commandsByPlugin.computeIfAbsent(pluginName, k -> new TreeMap<>());
                    
                    // Try to get commands registered by this plugin
                    try {
                        JavaPlugin javaPlugin = (JavaPlugin) bukkitPlugin;
                        for (String cmdName : javaPlugin.getDescription().getCommands().keySet()) {
                            PluginCommand cmd = javaPlugin.getCommand(cmdName);
                            if (cmd != null) {
                                commands.put(cmdName, cmd);
                            }
                        }
                    } catch (Exception e) {
                        // Skip plugins that aren't JavaPlugins or have other issues
                    }
                }
                
                // Add Bukkit/Minecraft builtin commands
                Map<String, Command> builtinCmds = commandsByPlugin.computeIfAbsent("Bukkit/Minecraft", k -> new TreeMap<>());
                
                // Add some known vanilla commands
                String[] vanillaCmds = {"help", "plugins", "version", "list", "kill", "me", 
                                       "tell", "say", "msg", "kick", "ban", "time", "gamemode"};
                
                for (String cmdName : vanillaCmds) {
                    Command cmd = Bukkit.getPluginCommand(cmdName);
                    if (cmd != null) {
                        builtinCmds.put(cmdName, cmd);
                    }
                }
                
                // Count the total commands found
                int totalCmds = 0;
                for (Map<String, Command> cmds : commandsByPlugin.values()) {
                    totalCmds += cmds.size();
                }
                
                plugin.getLogger().info("Retrieved " + totalCmds + " commands directly from plugins (Method 3)");
            }
            
        } catch (Exception e) {
            plugin.getLogger().severe("Error getting commands: " + e.getMessage());
            e.printStackTrace();
        }
        
        return commandsByPlugin;
    }
    
    /**
     * Gets the plugin name for a permission.
     * 
     * @param permission the permission
     * @return the plugin name
     */
    private String getPluginForPermission(Permission permission) {
        // Try to get the plugin name from the permission name (common pattern)
        String[] parts = permission.getName().split("\\.");
        if (parts.length > 1) {
            String possiblePlugin = parts[0].toLowerCase();
            
            // Check if this is a valid plugin name
            for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
                if (plugin.getName().toLowerCase().equals(possiblePlugin)) {
                    return plugin.getName();
                }
            }
        }
        
        // Also try the permission metadata
        if (permission.getDescription() != null && !permission.getDescription().isEmpty()) {
            for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
                if (permission.getDescription().contains(plugin.getName())) {
                    return plugin.getName();
                }
            }
        }
        
        return "Unknown";
    }
    
    /**
     * Gets the plugin name for a command.
     * 
     * @param cmd the command
     * @return the plugin name
     */
    private String getPluginForCommand(Command cmd) {
        if (cmd instanceof PluginCommand) {
            return ((PluginCommand) cmd).getPlugin().getName();
        } else if (cmd instanceof BukkitCommand) {
            return "Bukkit/Minecraft";
        } else if (cmd.getDescription() != null && !cmd.getDescription().isEmpty()) {
            for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
                if (cmd.getDescription().contains(plugin.getName())) {
                    return plugin.getName();
                }
            }
        }
        
        return "Unknown";
    }
    
    /**
     * Escapes HTML special characters.
     * 
     * @param s the string to escape
     * @return the escaped string
     */
    private String escapeHtml(String s) {
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
    
    /**
     * Dummy command class for when we can't get the actual command instance.
     */
    private static class DummyCommand extends Command {
        public DummyCommand(String name) {
            super(name);
        }
        
        @Override
        public boolean execute(CommandSender sender, String commandLabel, String[] args) {
            return false;
        }
    }
} 