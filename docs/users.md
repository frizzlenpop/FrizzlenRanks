# FrizzlenRanks User Management

This document explains how to manage users and their permissions in the FrizzlenRanks plugin.

[Return to Main Documentation](../README.md)

## Overview

The user management system in FrizzlenRanks allows you to:

- Assign users to groups/ranks
- Grant specific permissions to individual users
- Promote and demote users through rank tracks
- Set user-specific metadata
- Configure user permissions per-world or globally
- Assign temporary permissions and group memberships with automatic expiration

## Basic User Management

### User Commands

The primary command for managing users is `/user`:

```
/user <username> info                    - Display user information
/user <username> addgroup <group>        - Add the user to a group
/user <username> removegroup <group>     - Remove the user from a group
/user <username> setgroup <group>        - Set the user's primary group
/user <username> listgroups              - List all groups the user belongs to
/user <username> addperm <permission>    - Add a permission directly to the user
/user <username> removeperm <permission> - Remove a permission from the user
/user <username> listperms               - List all direct permissions for the user
/user <username> meta <key> [value]      - Set or get user metadata
```

### Shortcut Commands

FrizzlenRanks provides these additional user management commands:

```
/setgroup <username> <group>       - Set a user's primary group
/promote <username> [track]        - Promote a user to the next rank
/demote <username> [track]         - Demote a user to the previous rank
```

## User Groups and Permissions

### Group Membership

Users in FrizzlenRanks can:
- Be members of multiple groups at once
- Inherit permissions from all groups they belong to
- Have direct permissions that override group permissions

The `/setgroup` command replaces all existing groups with a single group, while `/addgroup` adds a group without removing existing ones.

### Permission Inheritance

Permissions are resolved in this order:
1. Direct user permissions (highest priority)
2. Permissions from the user's groups (highest priority group first)
3. Permissions from inherited groups (parent groups)

### Default Group Assignment

New users are automatically:
- Created when they first join the server
- Assigned to the default group if they have no groups
- Synced across worlds if global users are enabled

## User Data Management

### Data Storage

User data is stored in:
- When using global files: `plugins/FrizzlenRanks/worlds/global/users.yml`
- Per-world files: `plugins/FrizzlenRanks/worlds/<worldname>/users.yml`

### World-Specific vs. Global Users

FrizzlenRanks offers two modes for user data:

1. **Global Users** (default):
   - Users have the same groups in all worlds
   - When a user's groups change, it affects all worlds
   - Set with `use-global-users: true` in config.yml

2. **World-Specific Users**:
   - Users can have different groups in each world
   - Changes to groups only affect the current world
   - Set with `use-global-users: false` in config.yml

### User Data Synchronization

Users are automatically synced:
- When they join the server
- When they change worlds
- When permissions are manually updated
- During plugin reload

You can manually sync user data with:
```
/fr save          - Save all data to disk
/fr reload        - Reload all data from disk
```

## Advanced User Features

### User-Specific Metadata

Users can have metadata that affects various plugin behaviors:

```
/user <username> meta <key> <value>   - Set metadata
/user <username> meta <key>           - Get metadata
```

Common metadata keys:
- `prefix` - Custom user prefix (overrides group prefix)
- `suffix` - Custom user suffix (overrides group suffix)
- `build` - Whether the user can build (used by some plugins)

### Promotion and Demotion

Users can be promoted or demoted through configured rank tracks:

```
/promote <username> [track]   - Promote a user
/demote <username> [track]    - Demote a user
```

Promotion logic:
1. Find the user's current highest priority group
2. Find the next higher priority group in the system or track
3. Replace the current group with the new one

### Temporary Permissions and Groups

FrizzlenRanks provides built-in support for temporary permissions and groups that automatically expire after a specified duration:

```
# Temporary group commands
/user <username> addtempgroup <group> <duration>   - Add user to a group temporarily
/user <username> removetempgroup <group>           - Remove a temporary group
/user <username> listtempgroups                    - List all temporary groups

# Temporary permission commands
/user <username> addtempperm <permission> <duration>  - Add a temporary permission
/user <username> removetempperm <permission>          - Remove a temporary permission
/user <username> listtempperm                         - List all temporary permissions
```

#### Duration Format

Durations can be specified using these formats:
- `30s` - 30 seconds
- `10m` - 10 minutes
- `5h` - 5 hours
- `7d` - 7 days

#### How Temporary Permissions Work

1. When a temporary permission or group is added, an expiration timestamp is stored
2. A cleanup task runs every minute to check and remove expired entries
3. Expired permissions and groups are automatically removed without requiring manual intervention
4. The permission cache is refreshed whenever a temporary permission/group expires
5. Users can see the exact expiration time and time remaining with the list commands

#### Examples

```
# Add a player to the VIP group for 7 days
/user JohnDoe addtempgroup vip 7d

# Grant a fly permission for 2 hours
/user JohnDoe addtempperm essentials.fly 2h

# Check all temporary groups a player has
/user JohnDoe listtempgroups

# Remove a temporary permission before it expires
/user JohnDoe removetempperm essentials.fly
```

## Debugging User Permissions

FrizzlenRanks provides several commands to debug user permissions:

```
/fr checkperms <player> [permission]   - Check a player's permissions
/fr testperm <player> <permission>     - Test a specific permission
/fr fix <player>                       - Fix a player's permissions
/fr refreshpermissions [player]        - Refresh a player's permissions
```

Use these commands when:
- Permissions aren't being applied correctly
- After making changes to groups or permissions
- When a player changes worlds
- If a player reports missing permissions

## User Permission Reset

If a user's permissions become corrupted or don't apply correctly:

1. Use `/fr fix <username>` to reset their permission cache
2. Have them rejoin the server
3. Use `/fr refreshpermissions <username>` to force a permission update

## Technical Implementation

### User Data Structure

Each user stores:
- Username
- List of groups they belong to
- List of direct permissions
- Metadata map
- Temporary permissions with expiration timestamps
- Temporary groups with expiration timestamps

### Runtime Permission Processing

When permissions are processed:
1. A permission attachment is created for the player
2. All permissions from all groups are added to the attachment
3. Direct user permissions are added (overriding group permissions)
4. Active temporary permissions are included based on their expiration status
5. Permissions are cached for performance
6. The cache is reset when permissions change or temporary permissions expire

### Performance Considerations

For optimal performance:
- Limit the number of direct user permissions (use groups instead)
- Keep the number of groups per user small
- Use permission wildcards when appropriate
- Be mindful of the number of temporary permissions (they require regular checks)

## Common Issues and Solutions

1. **User not in the expected group**:
   - Check current world context with `/world`
   - Verify global users setting in config
   - Use `/user <username> listgroups` to see all groups

2. **Permissions not applying after changes**:
   - Run `/fr fix <username>` to reset permission cache
   - Have the player rejoin the server
   - Check for permission conflicts or negations

3. **Changes not persisting after restart**:
   - Ensure data is saved with `/fr save`
   - Check file permissions on the server
   - Verify the auto-save setting in config

4. **Temporary permissions not working**:
   - Verify the duration format is correct (e.g., 30s, 10m, 5h, 7d)
   - Check if the permission has already expired with `/user <username> listtempperm`
   - Ensure the server's system time is accurate

## Related Documentation

- [Permissions System](permissions.md) - Detailed explanation of the permissions system
- [Group Management](groups.md) - How to create and manage groups/ranks
- [World Configuration](worlds.md) - Working with multiple worlds
- [Troubleshooting](troubleshooting.md) - Common issues and solutions 