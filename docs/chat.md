# FrizzlenRanks Chat Formatting

This document explains how to customize chat appearance in the FrizzlenRanks plugin.

[Return to Main Documentation](../README.md)

## Overview

FrizzlenRanks includes a powerful chat formatting system that allows you to:

- Customize how player names appear in chat
- Apply group-based prefixes and suffixes
- Use color codes and formatting
- Create a consistent visual hierarchy
- Configure different formats per world

## Basic Chat Format

The chat format is specified in the `config.yml` file:

```yaml
chat-format: '{prefix}{name}{suffix}: {message}'
```

### Available Placeholders

The following placeholders can be used in the chat format:

- `{prefix}` - The player's group prefix
- `{suffix}` - The player's group suffix
- `{name}` - The player's name
- `{world}` - The world the player is in
- `{message}` - The player's message (always required)

## Prefixes and Suffixes

### Setting Group Prefixes/Suffixes

Prefixes and suffixes are set per group:

```
/group <groupname> prefix "<prefix>"
/group <groupname> suffix "<suffix>"
```

Example:
```
/group admin prefix "&c[Admin] "
/group vip suffix " &6⭐"
```

### Setting User-Specific Prefixes/Suffixes

Individual users can have custom prefixes/suffixes that override their group's:

```
/user <username> meta prefix "<prefix>"
/user <username> meta suffix "<suffix>"
```

### Color Codes

All prefixes and suffixes support Minecraft color codes using the `&` symbol:

- `&0` to `&9`, `&a` to `&f` - Colors
- `&k` - Obfuscated
- `&l` - Bold
- `&m` - Strikethrough
- `&n` - Underline
- `&o` - Italic
- `&r` - Reset

Example: `&c[&lAdmin&c]` produces a red, bold "Admin" tag with red brackets

## Technical Implementation

FrizzlenRanks implements chat formatting by:

1. Listening for `AsyncPlayerChatEvent` with `HIGHEST` priority
2. Retrieving the player's prefix and suffix using Vault
3. Replacing placeholders in the format
4. Translating all color codes
5. Setting the new format on the event

This implementation ensures compatibility with most chat plugins while maintaining control over formatting.

## World-Specific Chat Formats

When using per-world configurations:

1. Players can have different prefixes/suffixes in different worlds
2. The current world name is shown in the `{world}` placeholder
3. The current world's prefix/suffix settings are used

## Advanced Chat Configuration

### Extended Format Examples

Here are some example formats you can use in your `config.yml`:

```yaml
# Simple format with prefix, name and message
chat-format: '{prefix}{name}: {message}'

# Format with world name
chat-format: '[{world}] {prefix}{name}: {message}'

# Format with custom colors
chat-format: '&8[&7{world}&8] {prefix}{name}{suffix}&8: &f{message}'

# Complex format with multiple sections
chat-format: '&8[&7{world}&8] {prefix}{name} &8» &f{message}'
```

### Chat Format Best Practices

1. **Keep it readable**: Don't overuse colors or formatting
2. **Be consistent**: Use similar color schemes across ranks
3. **Test thoroughly**: Some formats might look different on various clients
4. **Mind the length**: Long prefixes can make chat unwieldy
5. **Consider mobile players**: Some players may be on devices with smaller screens

## Troubleshooting Chat Issues

### Common Issues and Solutions

1. **Colors not working**:
   - Ensure color codes use `&` not `§`
   - Check if another plugin is overriding the chat format
   - Verify the correct format is set in config.yml

2. **Prefixes/suffixes not appearing**:
   - Verify they're set correctly with `/group <group> prefix`
   - Check if Vault integration is working
   - Make sure the chat format includes the placeholders

3. **Format conflicts with other plugins**:
   - Adjust other chat plugins to have lower priority
   - Disable chat formatting in other plugins if possible
   - Consider using a chat management plugin designed to work with Vault

## Plugin Compatibility

FrizzlenRanks chat formatting is designed to work with:

- Most chat management plugins that respect Vault
- Plugins that use standard chat events
- Other permission plugins that provide Vault hooks

Some plugins may require additional configuration to avoid conflicts.

## Related Documentation

- [Group Management](groups.md) - Setting group prefixes and suffixes
- [User Management](users.md) - Setting user-specific prefixes and suffixes
- [Permissions System](permissions.md) - How permissions affect chat
- [World Configuration](worlds.md) - World-specific chat settings 