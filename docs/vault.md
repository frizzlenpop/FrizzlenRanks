# FrizzlenRanks Vault Integration

This document explains how FrizzlenRanks integrates with the Vault API to provide permissions and chat services to other plugins.

[Return to Main Documentation](../README.md)

## Overview

FrizzlenRanks uses Vault as its primary integration method with other plugins. Vault is a permissions and economy API that allows plugins to interact with each other without direct dependencies. FrizzlenRanks provides:

- A Vault Permissions provider
- A Vault Chat provider
- Seamless integration with Vault-compatible plugins

## Vault Requirements

For FrizzlenRanks to function properly:

1. Vault plugin must be installed on your server
2. Vault must load before FrizzlenRanks
3. FrizzlenRanks should be the only active permissions provider

## Vault Service Registration

When FrizzlenRanks starts:

1. It checks if Vault is present on the server
2. It registers its permission service with Vault at the HIGHEST priority
3. It registers its chat service with Vault at the HIGHEST priority
4. It refreshes these hooks when data is reloaded

## Vault Permissions API

FrizzlenRanks implements these Vault permission methods:

### Group Methods

```java
public boolean groupHas(String world, String name, String permission)
public boolean groupAdd(String world, String name, String permission)
public boolean groupRemove(String world, String name, String permission)
public boolean playerInGroup(String world, String player, String group)
public boolean playerAddGroup(String world, String player, String group)
public boolean playerRemoveGroup(String world, String player, String group)
public String[] getGroups()
public String getPrimaryGroup(String world, String playerName)
```

### Player Methods

```java
public boolean has(String world, String player, String permission)
public boolean playerHas(String world, String player, String permission)
public boolean playerAdd(String world, String player, String permission)
public boolean playerRemove(String world, String player, String permission)
```

### System Methods

```java
public boolean hasGroupSupport()
public String getName()
public String[] getPlayerGroups(String world, String player)
public boolean isEnabled()
```

## Vault Chat API

FrizzlenRanks implements these Vault chat methods:

### Prefix/Suffix Methods

```java
public String getPlayerPrefix(String world, String player)
public String getPlayerSuffix(String world, String player)
public void setPlayerPrefix(String world, String player, String prefix)
public void setPlayerSuffix(String world, String player, String suffix)
public String getGroupPrefix(String world, String group)
public String getGroupSuffix(String world, String group)
public void setGroupPrefix(String world, String group, String prefix)
public void setGroupSuffix(String world, String group, String suffix)
```

### Meta Methods

```java
public String getPlayerInfoString(String world, String player, String node, String def)
public void setPlayerInfoString(String world, String player, String node, String value)
public String getGroupInfoString(String world, String group, String node, String def)
public void setGroupInfoString(String world, String group, String node, String value)
```

## How Other Plugins Use FrizzlenRanks

When another plugin wants to check permissions or get chat information:

1. The plugin makes a call to Vault's API
2. Vault forwards the call to FrizzlenRanks
3. FrizzlenRanks processes the request and returns the result
4. Vault passes the result back to the requesting plugin

This allows plugins to work with FrizzlenRanks without knowing anything about its internal implementation.

## Common Vault-Compatible Plugins

FrizzlenRanks is designed to work with many popular Vault-compatible plugins, including:

- **Chat Plugins**: EssentialsChat, VentureChat, HeroChat
- **Economy Plugins**: EssentialsEco, CMI, GemsEconomy
- **Permission GUIs**: LuckPerms GUI, PermissionsEx GUI
- **World Protection**: WorldGuard, GriefPrevention
- **Admin Tools**: Essentials, CMI, CoreProtect

## Technical Implementation Details

### Permission Resolution

When a Vault permission check occurs:

1. FrizzlenRanks determines the correct world context
2. It collects the user's group memberships
3. It checks for direct user permissions first
4. If no match, it checks group permissions in priority order
5. It follows the inheritance chain for each group
6. It responds with true/false based on the permission match

### Chat/Prefix Processing

When Vault requests chat information:

1. FrizzlenRanks looks up the requested user or group
2. It determines the appropriate world context
3. It retrieves metadata for prefix/suffix or other values
4. It handles inheritance for groups if needed
5. It returns the formatted result

## Refreshing Vault Hooks

In some cases, Vault hooks may need to be refreshed:

1. After plugin reload
2. After significant data changes
3. When switching to or from global mode

To manually refresh Vault hooks:
```
/fr reload
```

This will:
1. Unregister all services
2. Re-register the permission service
3. Re-register the chat service

## Debugging Vault Integration

If you encounter issues with Vault integration:

1. **Verify Vault registration:**
   ```
   /vault-info
   ```
   This should show FrizzlenRanks as the permission and chat provider.

2. **Test a specific permission:**
   ```
   /fr testperm <player> <permission>
   ```

3. **Check the player's prefix/suffix:**
   ```
   /fr checkperms <player>
   ```
   This includes chat information.

4. **Enable debug mode:**
   ```
   /fr debug
   ```
   This will show detailed information about permission checks.

## Common Vault Integration Issues

### Multiple Permission Plugins

**Symptom:** Inconsistent permission behavior

**Solution:** Ensure FrizzlenRanks is the only permission plugin registered with Vault. Disable other permission plugins.

### Plugin Load Order

**Symptom:** Vault uses a different permission provider

**Solution:** Make sure Vault loads before FrizzlenRanks, and FrizzlenRanks loads before other plugins that use permissions.

### Prefix/Suffix Not Applied

**Symptom:** Chat formats don't include prefixes/suffixes

**Solution:** 
1. Check that the chat plugin is using Vault for prefixes/suffixes
2. Verify prefixes/suffixes are set correctly in FrizzlenRanks
3. Check if another plugin is overriding chat format

## Best Practices for Vault Integration

1. **Limit plugin interactions:** Too many plugins accessing permissions simultaneously can cause performance issues
2. **Use permission caching:** Many plugins cache permission results, which can cause delays in updates
3. **Test plugin compatibility:** Some plugins may have specific requirements for Vault integration
4. **Monitor performance:** Watch for excessive permission checks that might impact server performance

## Related Documentation

- [Permissions System](permissions.md) - How the permissions system works
- [Chat Formatting](chat.md) - Chat formatting details
- [Troubleshooting](troubleshooting.md) - Common issues and solutions 