# FrizzlenRanks

FrizzlenRanks is a powerful permissions management plugin for Minecraft servers, designed to provide fine-grained control over user permissions, ranks, and chat formatting. It serves as a complete permissions solution with Vault integration, world-specific permissions, and an intuitive command interface.

![FrizzlenRanks](https://img.shields.io/badge/FrizzlenRanks-v1.0-blue)
![Minecraft](https://img.shields.io/badge/Minecraft-1.8+-green)
![Spigot](https://img.shields.io/badge/Spigot-✓-yellow)
![Paper](https://img.shields.io/badge/Paper-✓-yellow)
![Vault](https://img.shields.io/badge/Vault-Required-red)

## Features

- **Advanced Permission Management**: Assign permissions to groups or individual users
- **Rank System**: Create hierarchical ranks with inheritance
- **Multi-world Support**: Configure different permissions per world
- **Chat Formatting**: Customize chat with prefixes and suffixes
- **Tab Display**: Sort players in tab list by rank priority with robust persistence
- **Vault Integration**: Full compatibility with all Vault-dependent plugins
- **Global or Per-world Users**: Configure whether users have the same groups across all worlds
- **Command Interface**: Intuitive commands for managing all aspects of the plugin
- **Temporary Permissions & Groups**: Assign time-limited permissions and group memberships
- **Persistent Tab Sorting**: Enhanced system to ensure tab list sorting persists even when other plugins modify it

## Installation

1. Download the latest version of FrizzlenRanks
2. Place the JAR file in your server's `plugins` folder
3. Ensure you have Vault installed
4. Start or restart your server
5. Configure the plugin using the commands below

## Quick Start

1. Default files are created on first run
2. Use `/fr helpme` to create default configuration files
3. Add permissions to groups with `/group <groupname> addperm <permission>`
4. Assign players to groups with `/setgroup <player> <groupname>`
5. Customize prefixes with `/group <groupname> prefix <prefix>`

## Commands

FrizzlenRanks offers a comprehensive set of commands for managing all aspects of the plugin:

### Main Commands

- `/frizzlenranks` or `/fr` - Main plugin command
  - `reload` - Reload configuration
  - `save` - Save data
  - `backup` - Create a backup
  - `helpme` - Create default files
  - `info` - Display plugin info
  - `version` - Show version
  - `fix <username>` - Fix a user's permissions
  - `forceglobal` - Force global settings
  - `refreshgroups` - Reload groups from files
  - `checkperms <player> [permission]` - Check a player's permissions
  - `refreshpermissions [player]` - Refresh permissions
  - `testperm <player> <permission>` - Test specific permission

### User Management

- `/user <username> [action] [arguments]` - Manage users
  - `addperm <permission>` - Add a permission
  - `removeperm <permission>` - Remove a permission
  - `listperms` - List permissions
  - `addgroup <group>` - Add to a group
  - `removegroup <group>` - Remove from a group
  - `setgroup <group>` - Set user's group
  - `listgroups` - List user's groups
  - `meta <key> [value]` - Manage metadata
  - `info` - Show user info
  - `addtempgroup <group> <duration>` - Add user to a group temporarily
  - `removetempgroup <group>` - Remove a temporary group
  - `listtempgroups` - List all temporary groups
  - `addtempperm <permission> <duration>` - Add a temporary permission
  - `removetempperm <permission>` - Remove a temporary permission
  - `listtempperm` - List all temporary permissions

### Group Management

- `/group <groupname> [action] [arguments]` - Manage groups
  - `addperm <permission>` - Add a permission
  - `removeperm <permission>` - Remove a permission
  - `listperms` - List permissions
  - `addgroup <group>` - Add inheritance
  - `removegroup <group>` - Remove inheritance
  - `listgroups` - List inherited groups
  - `prefix <prefix>` - Set prefix
  - `suffix <suffix>` - Set suffix
  - `priority <number>` - Set group priority
  - `meta <key> [value]` - Manage metadata
  - `info` - Show group info

### Other Commands

- `/world <worldname>` - Select a world
- `/promote <username> [track]` - Promote a user
- `/demote <username> [track]` - Demote a user
- `/setgroup <username> <groupname>` - Set a user's group

## Configuration

FrizzlenRanks stores its configuration in the `plugins/FrizzlenRanks` folder:

- `config.yml` - Main plugin settings
- `worlds/` - World-specific data
  - `<worldname>/groups.yml` - Groups for a specific world
  - `<worldname>/users.yml` - Users for a specific world
- `worlds/global/` - Global data (when global settings enabled)

### Basic config.yml options

```yaml
use-global-files: true  # Use global files for all worlds
use-global-users: true  # Users have same groups across worlds
auto-save: true         # Save automatically after changes
track-type: default     # Promotion track type
chat-format: '{prefix}{name}{suffix}: {message}'  # Chat format
```

## Detailed Documentation

For more detailed information about each feature, check out these specific guides:

- [Permissions System](docs/permissions.md) - Detailed explanation of the permissions system
- [Group Management](docs/groups.md) - How to create and manage groups/ranks
- [User Management](docs/users.md) - Managing users and their permissions
- [World Configuration](docs/worlds.md) - Working with multiple worlds
- [Chat Formatting](docs/chat.md) - Customizing chat appearance
- [Tab Display](docs/tab.md) - Tab list display and sorting
- [Commands Reference](docs/commands.md) - Complete command documentation
- [Vault Integration](docs/vault.md) - Working with Vault and other plugins
- [Troubleshooting](docs/troubleshooting.md) - Common issues and solutions

## Support

If you encounter any issues or have questions, please:

1. Check the [Troubleshooting Guide](docs/troubleshooting.md)
2. Use the `/fr checkperms` and `/fr testperm` commands to diagnose permission issues
3. Contact us through the support channels for further assistance

## License

FrizzlenRanks is licensed under the MIT License. See the LICENSE file for details. 