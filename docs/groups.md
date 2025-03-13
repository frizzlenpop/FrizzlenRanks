# FrizzlenRanks Group Management

This document provides a detailed overview of the group/rank system in the FrizzlenRanks plugin.

[Return to Main Documentation](../README.md)

## Overview

Groups in FrizzlenRanks form the foundation of the permission system, providing a way to organize players into ranks with specific permissions, prefixes, suffixes, and priorities. Key features include:

- Hierarchical group structure
- Group inheritance
- Priority-based sorting
- Custom prefixes and suffixes
- Tab list sorting by group priority
- World-specific group configurations

## Creating and Managing Groups

### Basic Group Management

Groups can be created and managed using the `/group` command:

```
/group <groupname> create          - Create a new group
/group <groupname> delete          - Delete a group
/group <groupname> info            - View group information
/group <groupname> listperms       - List all permissions for a group
/group <groupname> listgroups      - List all groups this group inherits from
```

### Group Inheritance

Groups can inherit permissions from other groups, creating a hierarchy:

```
/group <groupname> addgroup <parentgroup>    - Add inheritance from parent group
/group <groupname> removegroup <parentgroup> - Remove inheritance
```

When a group inherits from another group:
1. All permissions from the parent group are applied
2. Inheritance can be nested (grandparent permissions are applied too)
3. Circular inheritance is automatically prevented

### Group Permissions

Permissions can be added to or removed from groups:

```
/group <groupname> addperm <permission>      - Add a permission to a group
/group <groupname> removeperm <permission>   - Remove a permission
```

### Group Customization

Groups can be customized with prefixes, suffixes, and other metadata:

```
/group <groupname> prefix "<prefix>"         - Set group chat prefix
/group <groupname> suffix "<suffix>"         - Set group chat suffix
/group <groupname> priority <number>         - Set group priority
/group <groupname> meta <key> <value>        - Set custom metadata
```

## Group Priority System

Group priorities determine:
1. Which group's permissions take precedence when a user belongs to multiple groups
2. The order of players in the tab list
3. The promotion/demotion track order

Higher priority numbers indicate more important groups:
- Default priority is 0
- Negative values are allowed
- Groups with higher priority values override lower priority groups for permissions

Example priorities:
- Owner: 1000
- Admin: 900
- Moderator: 800
- VIP: 500
- Member: 100
- Default: 0

## Default Groups

FrizzlenRanks has a concept of a default group, which is assigned to new players:

1. By default, the group named "default" is used
2. Players with no explicit group assignments are automatically added to the default group
3. The default group should always exist and have basic permissions

## Tab List Sorting

FrizzlenRanks sorts players in the tab list based on group priorities:

1. Players are assigned to scoreboard teams named after their highest priority group
2. The team names include the priority number for sorting
3. Teams are prefixed with "FR_" to avoid conflicts with other plugins
4. Players in higher priority groups appear first in the tab list

## Group Prefixes and Suffixes

Prefixes and suffixes provide visual identification in chat and tab list:

1. Set with `/group <groupname> prefix "<prefix>"` and `/group <groupname> suffix "<suffix>"`
2. Support color codes (e.g., `&c` for red)
3. Applied in chat messages and the tab list
4. Maximum recommended length: 16 characters (including color codes)

Example:
```
/group admin prefix "&c[Admin] "
/group vip prefix "&a[VIP] "
```

## Group Permissions

Groups should have permissions that define what their members can do:

1. Plugin-specific permissions (e.g., `essentials.home`, `worldedit.wand`)
2. Permission nodes with wildcards (e.g., `essentials.*`)
3. Negated permissions to explicitly deny access (e.g., `-worldedit.tool.tree`)

## Promotion Tracks

FrizzlenRanks supports promotion and demotion tracks:

1. Players can move up or down between groups with `/promote` and `/demote`
2. Promotion follows the priority order of groups
3. Custom promotion tracks can be configured

Commands:
```
/promote <username> [track]   - Promote a player
/demote <username> [track]    - Demote a player
```

## World-Specific Groups

When not using global files, groups can be configured differently per world:

1. Each world has its own set of groups
2. Groups with the same name may have different permissions, prefixes, etc.
3. Use `/world <worldname>` to switch contexts before managing groups

## Technical Details

### Group Storage

Groups are stored in YAML files:
- When using global files: `plugins/FrizzlenRanks/worlds/global/groups.yml`
- Per-world files: `plugins/FrizzlenRanks/worlds/<worldname>/groups.yml`

### Group Data Structure

Each group stores:
- Name
- Permissions list
- Inheritance list (parent groups)
- Priority value
- Prefix and suffix
- Custom metadata map

### Performance Considerations

Group data is:
1. Loaded at server start
2. Cached in memory for performance
3. Saved to disk when modified (if auto-save is enabled)
4. Reloaded when the plugin is reloaded

## Common Issues and Solutions

1. **Permissions not applying from parent groups**:
   - Check that inheritance is set up correctly
   - Verify parent groups exist
   - Use `/fr refreshgroups` to reload group data

2. **Prefix/suffix not appearing**:
   - Ensure color codes use `&` not `§`
   - Check for character limit issues
   - Verify Vault integration is working

3. **Tab sorting issues**:
   - Check group priorities are set correctly
   - Look for conflicts with other tab/scoreboard plugins
   - Try `/fr refreshpermissions` to reset tab display

## Related Documentation

- [Permissions System](permissions.md) - How permissions work
- [User Management](users.md) - Managing users and their groups
- [World Configuration](worlds.md) - World-specific configurations
- [Tab Display](tab.md) - How tab display and sorting works 