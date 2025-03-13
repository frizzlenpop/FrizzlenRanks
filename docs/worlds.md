# FrizzlenRanks World Configuration

This document explains how to work with per-world and global configurations in FrizzlenRanks.

[Return to Main Documentation](../README.md)

## Overview

FrizzlenRanks provides flexible world configuration options that allow you to:

- Set up different permissions per world
- Configure world-specific groups and ranks
- Choose between global and per-world user assignments
- Synchronize user data across worlds when needed

## World Configuration Modes

FrizzlenRanks offers two main configuration modes that can be set in `config.yml`:

### 1. Global Files Mode

```yaml
use-global-files: true
```

In this mode:
- All worlds share the same groups and their permissions
- Group prefixes, suffixes, and priorities are the same across all worlds
- Changes to a group affect all worlds

### 2. Per-World Files Mode

```yaml
use-global-files: false
```

In this mode:
- Each world has its own set of groups with their own permissions
- Groups with the same name can have different settings in different worlds
- Changes to a group only affect the current world

## User Assignment Modes

Independent from the files mode, you can configure how users are assigned to groups:

### 1. Global Users Mode

```yaml
use-global-users: true
```

In this mode:
- Users have the same groups in all worlds
- When a user's groups change, the change applies to all worlds
- User permissions remain consistent across worlds

### 2. Per-World Users Mode

```yaml
use-global-users: false
```

In this mode:
- Users can have different groups in each world
- Changes to a user's groups only affect the current world
- User permissions can vary between worlds

## World Context

When managing groups and users, you work within a "world context":

1. The default context is the first world detected on the server
2. You can switch context using the `/world <worldname>` command
3. The current context affects all group and user commands
4. The context is shown in the `/fr info` command output

Example:
```
/world world_nether
Group and user commands will now apply to world: world_nether
/group vip addperm essentials.warp
Permission added to group vip in world world_nether
```

## File Structure

FrizzlenRanks stores world data in this directory structure:

```
plugins/FrizzlenRanks/
├── config.yml
├── worlds/
│   ├── global/
│   │   ├── groups.yml
│   │   └── users.yml
│   ├── world/
│   │   ├── groups.yml
│   │   └── users.yml
│   ├── world_nether/
│   │   ├── groups.yml
│   │   └── users.yml
│   └── world_the_end/
│       ├── groups.yml
│       └── users.yml
```

When using global files:
- Only the `worlds/global/` directory is used
- Per-world directories are ignored

When using per-world files:
- Each world has its own directory
- The `worlds/global/` directory is only used when explicitly selected

## Managing World-Specific Configurations

### Selecting a World

Before performing group or user management, select the world context:

```
/world <worldname>
```

If the world doesn't exist in the configuration yet, it will be created.

### Viewing Current World

To see which world you're currently working with:

```
/fr info
```

This will show the selected world along with other plugin information.

### Copying Groups Between Worlds

While there's no built-in command for this, you can:

1. Use `/world <source-world>` to select the source world
2. Note the configuration of groups you want to copy
3. Use `/world <target-world>` to select the target world
4. Recreate the groups with the same settings

### Synchronizing a User Across Worlds

To make a user's groups the same in all worlds:

```
/user <username> syncworlds
```

This copies the user's groups from the current world to all other worlds.

## Common World Configuration Scenarios

### Server with Different Game Modes

For a server with survival, creative, and minigames worlds:

```yaml
# config.yml
use-global-files: false
use-global-users: false
```

This allows:
- Different permissions in each game mode
- Different rank structures per world
- Players can have different ranks in different game modes

### Survival Server with Multiple Worlds

For a standard survival server with overworld, nether, and end:

```yaml
# config.yml
use-global-files: true
use-global-users: true
```

This ensures:
- Consistent permissions across all worlds
- Same rank structure everywhere
- Players maintain the same rank throughout the server

### Mixed Approach

For more complex setups:

```yaml
# config.yml
use-global-files: true
use-global-users: false
```

This provides:
- Consistent group definitions across worlds
- Flexibility to assign users differently per world

## Migrating Between Configurations

### From Per-World to Global

To convert from per-world to global configuration:

1. Set `use-global-files: true` in config.yml
2. Choose which world's groups to use as global
3. Run `/world <chosen-world>` to select that world
4. Use `/fr forceglobal` to copy that world's settings to global

### From Global to Per-World

To convert from global to per-world configuration:

1. Set `use-global-files: false` in config.yml
2. Restart the server
3. Each world will automatically get a copy of the global settings
4. You can then customize each world individually

## Technical Details

### Data Loading Order

When FrizzlenRanks starts:

1. It loads the config.yml settings
2. Based on the settings, it determines which files to load
3. For each world it loads groups first, then users
4. It synchronizes users across worlds if global users is enabled

### World Creation

Worlds are automatically detected and added to the configuration:

1. When a player joins a world that doesn't exist in the config
2. When the `/world <worldname>` command is used with a new world
3. When the server creates a new world dynamically

### Default World Handling

If no worlds are configured yet:

1. FrizzlenRanks uses the server's first world as the default
2. It creates a default configuration for this world
3. If global files are enabled, it also sets up the global configuration

## Common Issues and Solutions

### Players Have Wrong Permissions in Some Worlds

**Possible causes:**
- Using per-world users mode but expecting global behavior
- World context was incorrect when setting permissions
- User data not synchronized across worlds

**Solutions:**
1. Check `use-global-users` setting in config.yml
2. Use `/user <username> syncworlds` to synchronize
3. Verify current world with `/fr info` before making changes

### Changes to Groups Not Affecting All Worlds

**Possible causes:**
- Using per-world files mode but expecting global behavior
- World context was incorrect when making changes

**Solutions:**
1. Check `use-global-files` setting in config.yml
2. Use `/world global` to make truly global changes
3. If needed, switch to each world and make the same changes

### New Worlds Not Getting Default Groups

**Possible causes:**
- Default world templates not set up
- Plugin conflicts preventing world detection

**Solutions:**
1. Set up groups in the "global" world context
2. Make sure `use-global-files` is set to true
3. Manually create world configuration with `/world <newworld>`

## Related Documentation

- [Permissions System](permissions.md) - How permissions work across worlds
- [Group Management](groups.md) - Managing groups within world contexts
- [User Management](users.md) - Managing users across worlds
- [Troubleshooting](troubleshooting.md) - Common issues and solutions 