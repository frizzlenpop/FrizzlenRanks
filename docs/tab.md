# FrizzlenRanks Tab Display System

This document explains the tab list display and sorting functionality in FrizzlenRanks.

[Return to Main Documentation](../README.md)

## Overview

The Tab Display System in FrizzlenRanks provides:

- Customized player tab list display names with prefixes and suffixes
- Sorting players by group priority in the tab list
- Consistent display across server restarts and world changes
- Compatibility with other plugins through strategic update timing

## Tab Display Features

### Player Display Names

FrizzlenRanks customizes how players appear in the tab list:

1. **Formatted Display Names**: Combines group prefix + player name + group suffix
2. **Color Codes**: Full support for Minecraft color and formatting codes using `&` symbol
3. **Per-World Configuration**: Display can change based on the player's current world
4. **Dynamic Updates**: Changes when permissions, prefixes, or suffixes are modified

### Priority-Based Sorting

Players are automatically sorted in the tab list based on their group's priority:

1. **Higher Priority First**: Players in groups with higher priority appear at the top
2. **Alphabetical Sub-sorting**: Players within the same priority are sorted alphabetically
3. **Visual Organization**: Creates a clear hierarchy of player ranks
4. **Automatic Updates**: Sorting is maintained when players join, leave, or change worlds

## Technical Implementation

### Scoreboard Teams

FrizzlenRanks uses Minecraft's scoreboard team system for tab list sorting:

1. **Team Creation**: Creates teams with names that include priority values for sorting
2. **Team Naming Convention**: Teams are named `FR_<formattedPriority>_<groupName>`
3. **Team Prefixes**: Team prefixes are set to group prefixes for display
4. **Team Assignment**: Players are assigned to teams based on their highest priority group

### Sorting Algorithm

The tab sorting follows this process:

1. Determine player's groups when they join or change worlds
2. Find the highest priority group the player belongs to
3. Format the priority number with leading zeros for proper sorting
4. Invert the priority (9999 - priority) so higher values sort first
5. Create or get the scoreboard team with this formatted priority
6. Assign the player to this team

### Robust Update System

FrizzlenRanks ensures tab display remains consistent through:

1. **Initial Application**: Tab display is set when a player joins
2. **Staggered Updates**: Multiple delayed updates (4s, 6s, 10s after join)
3. **World Change Updates**: Updates when players change worlds
4. **Recurring Task**: A global task that runs every 10 seconds to refresh all player tab sorting
5. **Manual Refresh**: Can be triggered with permission commands

## Configuration

Tab display behavior is determined by:

1. **Group Priorities**: Set with `/group <groupname> priority <number>`
2. **Group Prefixes**: Set with `/group <groupname> prefix "<prefix>"`
3. **Group Suffixes**: Set with `/group <groupname> suffix "<suffix>"`

## Troubleshooting Tab Display Issues

### Common Issues and Solutions

1. **Tab sorting not working or reverting**:
   - Check for conflicts with other tab/scoreboard plugins
   - Run `/fr refreshpermissions <player>` to reset display
   - Verify the player has correct group assignments
   - Check for timing issues with world changes

2. **Formatting problems**:
   - Ensure prefixes/suffixes use `&` for color codes, not `§`
   - Verify total length (prefix + name + suffix) is not excessive
   - Check for invalid color code combinations

3. **Players missing from tab**:
   - This may indicate permission or group assignment issues
   - Verify the player is in at least one group
   - Check if they're properly loaded into the permission system

### Compatibility with Other Plugins

FrizzlenRanks strives for maximum compatibility, but some plugins may conflict:

1. **Other Tab Plugins**: Disable their tab formatting features if using FrizzlenRanks
2. **Scoreboard Plugins**: May need configuration to avoid conflict
3. **Chat Plugins**: Ensure they're configured to respect Vault prefixes/suffixes

## Advanced Tab Configuration

### Display Name Format

The tab display format follows this pattern:
```
[Prefix][PlayerName][Suffix]
```

Examples:
- `&c[Admin] &fPlayerName` - Red "Admin" prefix with white player name
- `&6[VIP] &ePlayerName &7#1` - Gold "VIP" prefix with yellow name and gray suffix

### Team Priority Format

Team names are formatted to ensure proper sorting:
```
FR_<formattedPriority>_<groupName>
```

Example:
- `FR_9099_admin` - For admin group with priority 900
- `FR_9499_owner` - For owner group with priority 500

The number is inverted (9999 - priority) so higher priorities appear first.

## Technical Workflow for Tab Updates

When a player joins or when tab sorting needs to be updated:

1. **Determine Player Status**:
   - Check if player is online
   - Get current world
   - Retrieve groups from this world

2. **Find Highest Priority Group**:
   - Iterate through all player's groups
   - Compare priorities to find highest
   - Use default group if no groups found

3. **Format Team Name**:
   - Create formatted priority string
   - Combine with group name
   - Ensure name is valid and not too long

4. **Check Current Teams**:
   - Check if player is already in correct team
   - Remove from incorrect teams

5. **Create or Update Team**:
   - Get or create team with proper name
   - Set prefix/suffix from group
   - Set color if supported by server version

6. **Assign Player to Team**:
   - Add player to new team
   - Log completion for troubleshooting

## Example Commands

```
# Set group priorities for sorting
/group owner priority 1000
/group admin priority 900
/group moderator priority 800
/group vip priority 500
/group default priority 0

# Set prefixes for display
/group owner prefix "&4[Owner] &c"
/group admin prefix "&c[Admin] &f"
/group moderator prefix "&9[Mod] &f"
/group vip prefix "&6[VIP] &e"
/group default prefix "&8"

# Refresh a player's tab display
/fr refreshpermissions PlayerName
```

## Related Documentation

- [Group Management](groups.md) - How to manage groups and priorities
- [Chat Formatting](chat.md) - Customizing chat appearance
- [Troubleshooting](troubleshooting.md) - Common issues and solutions 