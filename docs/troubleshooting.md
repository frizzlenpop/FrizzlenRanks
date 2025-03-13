# FrizzlenRanks Troubleshooting Guide

This document provides solutions for common issues encountered when using FrizzlenRanks.

[Return to Main Documentation](../README.md)

## Common Issues and Solutions

### Permission Issues

#### Players Not Getting Correct Permissions

**Symptoms:**
- Players can't use commands they should have access to
- Permission-based features don't work properly
- Players report "no permission" messages

**Solutions:**
1. **Reset the player's permission cache:**
   ```
   /fr fix <playername>
   ```

2. **Verify group assignments:**
   ```
   /user <playername> listgroups
   ```

3. **Check the permissions of the group:**
   ```
   /group <groupname> listperms
   ```

4. **Refresh permissions for all players:**
   ```
   /fr refreshpermissions
   ```

5. **Make sure the player is in the right world context:**
   ```
   /world <worldname>
   ```

6. **Check for negated permissions** that might be overriding positive ones (look for permissions starting with `-`).

7. **Have the player rejoin the server** to force a complete permissions refresh.

#### Inheritance Not Working

**Symptoms:**
- Group permissions aren't inheriting from parent groups
- Changes to parent groups don't affect child groups

**Solutions:**
1. **Verify inheritance is set up correctly:**
   ```
   /group <groupname> listgroups
   ```

2. **Manually refresh groups:**
   ```
   /fr refreshgroups
   ```

3. **Check for circular inheritance** - Group A inherits from Group B which inherits from Group A.

4. **Ensure parent groups exist** in the current world context.

### Tab Display Issues

#### Tab List Not Sorting Correctly

**Symptoms:**
- Players aren't sorted by rank in the tab list
- Sorting reverts after a short time
- Some players appear in the wrong order

**Solutions:**
1. **Verify group priorities are set correctly:**
   ```
   /group <groupname> info
   ```

2. **Make sure tab sorting is active** - The plugin has a built-in task that runs every 10 seconds.

3. **Check for conflicts with other tab plugins** - Some tab list plugins might override FrizzlenRanks' sorting.

4. **Test tab sorting on a player:**
   ```
   /fr refreshpermissions <playername>
   ```

5. **Look for scoreboard team conflicts** from other plugins - FrizzlenRanks uses teams prefixed with "FR_".

6. **Restart the server** to reset all scoreboards and teams.

#### Prefixes/Suffixes Not Displaying in Tab

**Symptoms:**
- Player names in tab list don't have prefixes or suffixes
- Formatting codes aren't working in tab list

**Solutions:**
1. **Check prefix/suffix format:**
   - Ensure color codes use `&` not `§`
   - Make sure prefixes/suffixes aren't too long (16 char limit)
   - Verify they're set correctly with `/group <groupname> prefix`

2. **Force refresh tab display:**
   ```
   /fr refreshpermissions <playername>
   ```

3. **Verify Vault integration** is working properly.

4. **Check for plugin conflicts** that might override tab display names.

### Chat Formatting Issues

#### Chat Format Not Working

**Symptoms:**
- Chat format doesn't match configuration
- Prefixes or suffixes don't appear in chat
- Color codes don't work in chat

**Solutions:**
1. **Check chat format in config.yml:**
   - Ensure it includes necessary placeholders: `{prefix}`, `{name}`, `{suffix}`, `{message}`
   - Verify color codes use `&` not `§`

2. **Look for plugin conflicts:**
   - Chat plugins with higher event priority
   - Other permission plugins that might override prefixes/suffixes

3. **Check group prefix/suffix settings:**
   ```
   /group <groupname> info
   ```

4. **Reload the plugin** to refresh the chat format:
   ```
   /fr reload
   ```

### World-Specific Configuration Issues

#### Permissions Different Across Worlds

**Symptoms:**
- Players have different permissions in different worlds
- Groups don't sync between worlds

**Solutions:**
1. **Check global settings in config.yml:**
   - `use-global-files: true` - Uses the same groups across all worlds
   - `use-global-users: true` - Uses the same user assignments across all worlds

2. **Sync a user manually across worlds:**
   ```
   /user <username> syncworlds
   ```

3. **Make sure you're operating in the correct world context:**
   ```
   /world <worldname>
   ```

4. **Check if the issue is world-specific** by testing in multiple worlds.

### Data Saving/Loading Issues

#### Changes Not Persisting After Restart

**Symptoms:**
- Group or user changes disappear after server restart
- New settings revert to old ones

**Solutions:**
1. **Manually save data:**
   ```
   /fr save
   ```

2. **Check auto-save setting** in config.yml.

3. **Verify file permissions** on the server:
   - The plugin needs write access to its data folders
   - Check if the user running the server has proper permissions

4. **Look for errors in the console** when the server starts or stops.

5. **Create a backup** of your current configuration:
   ```
   /fr backup
   ```

### Plugin Conflict Issues

#### Conflicts with Other Permission Plugins

**Symptoms:**
- Inconsistent permission behavior
- Error messages about Vault
- Features not working correctly

**Solutions:**
1. **Make sure FrizzlenRanks is the only permission plugin** registered with Vault.

2. **Check plugin load order** - FrizzlenRanks should load after Vault but before plugins that use permissions.

3. **Disable other permission plugins** to test if they're causing conflicts.

4. **Verify Vault is installed and up to date**.

#### Conflicts with Chat/Tab Plugins

**Symptoms:**
- Chat format changes unexpectedly
- Tab display flickers or changes
- Multiple formatting applied at once

**Solutions:**
1. **Disable chat formatting in other plugins** if possible.

2. **Check event priorities** - FrizzlenRanks uses HIGHEST for chat events.

3. **Disable tab management in other plugins** or configure them to use Vault data.

4. **Test with other plugins disabled** to pinpoint conflicts.

## Diagnostic Commands

FrizzlenRanks provides several diagnostic commands to help troubleshoot issues:

### Permission Diagnostics

```
/fr checkperms <player> [permission]    - Check all or specific permissions
/fr testperm <player> <permission>      - Test if a player has a specific permission
/fr debug                               - Toggle verbose debugging output
/fr info                                - Display plugin information and status
```

### User/Group Diagnostics

```
/user <username> info                   - Display detailed user information
/group <groupname> info                 - Display detailed group information
/world                                  - Show current world context
```

### System Diagnostics

```
/fr version                             - Show plugin version
/fr listgroups                          - List all groups
/fr listusers                           - List all users
```

## Console Errors

### Common Error Messages

#### "Failed to hook into Vault"

**Cause:** Vault plugin is missing or failed to load.

**Solution:** Install Vault and ensure it loads before FrizzlenRanks.

#### "Error loading group/user data"

**Cause:** Data files are corrupt or have permission issues.

**Solution:**
1. Check file permissions
2. Restore from backup
3. Check for YAML syntax errors in config files

#### "Error in permission attachment"

**Cause:** Problems with Bukkit permission attachments.

**Solution:**
1. Restart the server
2. Reset the player's permissions with `/fr fix <playername>`
3. Check for conflicts with other permission plugins

## Advanced Troubleshooting

### Server Startup Issues

If FrizzlenRanks fails to start or causes server startup issues:

1. **Check console for error messages**
2. **Verify plugin dependencies** (Vault)
3. **Check Java version compatibility**
4. **Try with a clean configuration** by renaming the FrizzlenRanks folder and letting it generate new files
5. **Check if other plugins depend on FrizzlenRanks** and might be causing cascading failures

### Performance Issues

If FrizzlenRanks is causing performance problems:

1. **Limit the number of groups and inheritance levels**
2. **Reduce direct user permissions** (use groups instead)
3. **Set a higher interval for the tab sorting task** in the plugin's source code
4. **Check for excessive permission checks** from other plugins

## Getting Additional Help

If you continue to experience issues after trying these solutions:

1. **Gather diagnostic information:**
   - Server software and version
   - FrizzlenRanks version
   - List of other plugins
   - Relevant error messages from the console
   - Steps to reproduce the issue

2. **Contact support through the appropriate channels**

3. **Submit detailed bug reports** with all the information gathered above 