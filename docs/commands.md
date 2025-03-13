# FrizzlenRanks Commands Reference

This document provides a complete reference for all commands available in the FrizzlenRanks plugin.

[Return to Main Documentation](../README.md)

## Command Overview

FrizzlenRanks organizes its commands in a logical structure:

- `/frizzlenranks` or `/fr` - Main plugin commands
- `/user` - User management
- `/group` - Group management
- `/world` - World selection
- `/promote` & `/demote` - Quick promotion/demotion
- `/setgroup` - Quick group assignment

## Main Commands

### `/frizzlenranks` or `/fr`

The primary command for plugin administration.

| Command | Permission | Description |
|---------|------------|-------------|
| `/fr reload` | `frizzlenranks.admin` | Reload configuration and data from disk |
| `/fr save` | `frizzlenranks.admin` | Save all data to disk |
| `/fr backup` | `frizzlenranks.admin` | Create a backup of all data |
| `/fr helpme` | `frizzlenranks.admin` | Generate default configuration files |
| `/fr info` | `frizzlenranks.admin` | Display plugin information |
| `/fr version` | `frizzlenranks.admin` | Show plugin version |
| `/fr fix <username>` | `frizzlenranks.admin` | Fix a user's permissions |
| `/fr forceglobal` | `frizzlenranks.admin` | Force global settings |
| `/fr refreshgroups` | `frizzlenranks.admin` | Reload groups from files |
| `/fr checkperms <player> [permission]` | `frizzlenranks.admin` | Check a player's permissions |
| `/fr refreshpermissions [player]` | `frizzlenranks.admin` | Refresh permissions |
| `/fr testperm <player> <permission>` | `frizzlenranks.admin` | Test specific permission |
| `/fr debug` | `frizzlenranks.admin` | Toggle debug mode |

## User Management Commands

### `/user <username> [action] [arguments]`

Commands for managing users and their permissions.

| Command | Permission | Description |
|---------|------------|-------------|
| `/user <username> info` | `frizzlenranks.command.user` | Display user information |
| `/user <username> addperm <permission>` | `frizzlenranks.command.user` | Add a permission |
| `/user <username> removeperm <permission>` | `frizzlenranks.command.user` | Remove a permission |
| `/user <username> listperms` | `frizzlenranks.command.user` | List permissions |
| `/user <username> addgroup <group>` | `frizzlenranks.command.user` | Add to a group |
| `/user <username> removegroup <group>` | `frizzlenranks.command.user` | Remove from a group |
| `/user <username> setgroup <group>` | `frizzlenranks.command.user` | Set user's group |
| `/user <username> listgroups` | `frizzlenranks.command.user` | List user's groups |
| `/user <username> meta <key> [value]` | `frizzlenranks.command.user` | Manage metadata |
| `/user <username> syncworlds` | `frizzlenranks.command.user` | Sync user across worlds |

## Group Management Commands

### `/group <groupname> [action] [arguments]`

Commands for managing groups and their permissions.

| Command | Permission | Description |
|---------|------------|-------------|
| `/group <groupname> create` | `frizzlenranks.command.group` | Create a new group |
| `/group <groupname> delete` | `frizzlenranks.command.group` | Delete a group |
| `/group <groupname> info` | `frizzlenranks.command.group` | Show group info |
| `/group <groupname> addperm <permission>` | `frizzlenranks.command.group` | Add a permission |
| `/group <groupname> removeperm <permission>` | `frizzlenranks.command.group` | Remove a permission |
| `/group <groupname> listperms` | `frizzlenranks.command.group` | List permissions |
| `/group <groupname> addgroup <group>` | `frizzlenranks.command.group` | Add inheritance |
| `/group <groupname> removegroup <group>` | `frizzlenranks.command.group` | Remove inheritance |
| `/group <groupname> listgroups` | `frizzlenranks.command.group` | List inherited groups |
| `/group <groupname> prefix <prefix>` | `frizzlenranks.command.group` | Set prefix |
| `/group <groupname> suffix <suffix>` | `frizzlenranks.command.group` | Set suffix |
| `/group <groupname> priority <number>` | `frizzlenranks.command.group` | Set group priority |
| `/group <groupname> meta <key> [value]` | `frizzlenranks.command.group` | Manage metadata |

## World Selection Command

### `/world <worldname>`

Command to switch world context for subsequent commands.

| Command | Permission | Description |
|---------|------------|-------------|
| `/world <worldname>` | `frizzlenranks.command.world` | Select a world |
| `/world` | `frizzlenranks.command.world` | Show current world |

## Promotion and Demotion Commands

### `/promote` and `/demote`

Quick commands for rank changes.

| Command | Permission | Description |
|---------|------------|-------------|
| `/promote <username> [track]` | `frizzlenranks.command.promote` | Promote a user |
| `/demote <username> [track]` | `frizzlenranks.command.demote` | Demote a user |

## Quick Group Assignment

### `/setgroup`

Quick command for assigning users to groups.

| Command | Permission | Description |
|---------|------------|-------------|
| `/setgroup <username> <groupname>` | `frizzlenranks.command.setgroup` | Set a user's group |

## Command Examples

### User Management Examples

```
# Add a player to the VIP group
/user JohnDoe addgroup vip

# Give a player a specific permission
/user JohnDoe addperm essentials.fly

# Set custom prefix for a player
/user JohnDoe meta prefix "&b[Cool] "

# List all permissions a player has directly assigned
/user JohnDoe listperms

# Remove a player from a group
/user JohnDoe removegroup moderator
```

### Group Management Examples

```
# Create a new group
/group vipplus create

# Set group priority (for tab sorting and permission inheritance)
/group vipplus priority 600

# Add permission to a group
/group vipplus addperm essentials.kits.vipplus

# Set group prefix
/group vipplus prefix "&6[VIP+] "

# Make one group inherit another
/group vipplus addgroup vip
```

### Administrative Examples

```
# Check if a player has a specific permission
/fr testperm JohnDoe essentials.fly

# View all permissions a player has
/fr checkperms JohnDoe

# Fix a player's permissions after changes
/fr fix JohnDoe

# Save all data to disk
/fr save

# Reload the plugin
/fr reload
```

## Command Best Practices

1. **Switch World Context First**: Always use `/world <worldname>` before making world-specific changes.
2. **Save After Important Changes**: Use `/fr save` after important changes if auto-save is disabled.
3. **Fix After Group Changes**: Use `/fr fix <player>` after changing groups to apply changes immediately.
4. **Use Tab Completion**: Most commands support tab completion for easier use.
5. **Check Command Output**: Always read command output for confirmation or error messages.

## Permission Nodes for Commands

### Administrative Permissions

- `frizzlenranks.admin` - Full admin access to all commands
- `frizzlenranks.reload` - Permission to reload the plugin

### Command Category Permissions

- `frizzlenranks.command.user` - Access to user commands
- `frizzlenranks.command.group` - Access to group commands
- `frizzlenranks.command.world` - Access to world selection
- `frizzlenranks.command.promote` - Access to promotion command
- `frizzlenranks.command.demote` - Access to demotion command
- `frizzlenranks.command.setgroup` - Access to setgroup command

## Related Documentation

- [Permissions System](permissions.md) - How permissions work
- [Group Management](groups.md) - Detailed group management
- [User Management](users.md) - Detailed user management
- [World Configuration](worlds.md) - Working with multiple worlds 