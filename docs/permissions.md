# FrizzlenRanks Permissions System

This document provides a comprehensive overview of the permissions system in the FrizzlenRanks plugin.

[Return to Main Documentation](../README.md)

## Overview

The permissions system in FrizzlenRanks is designed to be flexible and powerful, featuring:

- Group-based permission assignment
- Individual user permissions
- Permission inheritance
- World-specific permissions
- Negated permissions
- Full Vault integration

## Core Concepts

### Permission Nodes

Permissions in FrizzlenRanks follow the standard node format used by most Minecraft plugins:

- **Basic format**: `pluginname.action.subaction`
- **Wildcards**: `pluginname.*` (grants all permissions for that plugin)
- **Negated permissions**: `-pluginname.action.subaction` (specifically denies a permission)

Examples:
- `worldedit.wand` - Allows use of the WorldEdit wand
- `essentials.home` - Allows use of home commands in Essentials
- `frizzlenranks.admin` - Grants admin access to FrizzlenRanks commands

### Permission Inheritance

FrizzlenRanks implements a robust inheritance system:

1. **Group inheritance**: Groups can inherit permissions from parent groups
2. **Priority-based resolution**: When conflicts occur, permissions are resolved based on:
   - Direct user permissions override group permissions
   - Permissions from higher priority groups override lower priority groups
   - Explicit negated permissions (`-permission`) override positive permissions

## How Permissions Are Applied

When a player performs an action that requires permission checking:

1. The plugin queries FrizzlenRanks via the Vault API
2. FrizzlenRanks checks the following in order:
   - Direct user permissions
   - Permissions from groups the user belongs to (in order of priority)
   - Permissions inherited from parent groups
3. The first matching permission (positive or negative) determines the result
4. If no matching permission is found, access is denied

## Permission Caching and Synchronization

FrizzlenRanks uses an advanced permission attachment system to ensure permissions are applied correctly:

1. **Permission attachments**: Created for each player when they join
2. **Cache reset triggers**:
   - Player join
   - Player world change
   - Manual permission updates
   - Plugin reload
3. **Multiple refresh attempts**: To handle timing issues with other plugins, permissions are refreshed several times with delays

## World-Specific Permissions

FrizzlenRanks supports configuring permissions separately for each world:

1. Each world has its own groups and users configuration
2. The `use-global-files` option determines whether worlds share configurations
3. The `use-global-users` option determines whether users have the same groups across worlds
4. World selection is managed through the `/world <worldname>` command

## Admin Permissions

Administrators need the following permissions to manage the system:

- `frizzlenranks.admin` - Full admin access
- `frizzlenranks.command.user` - Manage users
- `frizzlenranks.command.group` - Manage groups
- `frizzlenranks.command.world` - Switch world contexts
- `frizzlenranks.command.reload` - Reload configuration

## Permission Management Commands

### User Permissions

```
/user <username> addperm <permission>
/user <username> removeperm <permission>
/user <username> listperms
```

### Group Permissions

```
/group <groupname> addperm <permission>
/group <groupname> removeperm <permission>
/group <groupname> listperms
```

### Debugging Permissions

```
/fr checkperms <player> [permission]
/fr testperm <player> <permission>
/fr fix <player>
/fr refreshpermissions [player]
```

## Technical Implementation

FrizzlenRanks applies permissions through the following mechanism:

1. Permission attachments are created for each player
2. When permissions change, the system:
   - Cleans up any existing attachments
   - Creates a new attachment
   - Adds all permissions from the user's groups
   - Adds direct user permissions
   - Forces a permission recalculation
3. For reliability, permissions are checked at critical times (join, world change)
4. When inheritance is involved, parent groups are processed recursively

## Common Issues and Solutions

1. **Permissions not applying**:
   - Use `/fr fix <player>` to reset permission cache
   - Try rejoining the server
   - Use `/fr checkperms <player>` to diagnose

2. **Permission conflicts**:
   - Check for negated permissions with `-` prefix
   - Verify group priorities are set correctly

3. **Plugin compatibility issues**:
   - Ensure FrizzlenRanks is loaded before other permission-based plugins
   - Check for conflicts with other permission plugins

## Related Documentation

- [Group Management](groups.md) - How groups work with permissions
- [User Management](users.md) - Managing user permissions
- [World Configuration](worlds.md) - World-specific permissions
- [Vault Integration](vault.md) - How FrizzlenRanks works with Vault 