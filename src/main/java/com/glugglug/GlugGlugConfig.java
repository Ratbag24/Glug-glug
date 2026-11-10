package com.glugglug;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;
import net.runelite.client.config.Units;

@ConfigGroup(GlugGlugConfig.GROUP)
public interface GlugGlugConfig extends Config
{
	String GROUP = "glugglug";

	@ConfigSection(
		name = "Overhead text",
		description = "The text that floats above the drinker's head.",
		position = 0
	)
	String overheadSection = "overheadSection";

	@ConfigSection(
		name = "Chat message",
		description = "The message printed into your chatbox.",
		position = 10
	)
	String chatSection = "chatSection";

	@ConfigSection(
		name = "What counts as a chug",
		description = "Which drinking actions trigger the glub.",
		position = 20
	)
	String triggerSection = "triggerSection";

	// ------------------------------------------------------------------
	// Overhead
	// ------------------------------------------------------------------

	@ConfigItem(
		keyName = "showOverhead",
		name = "Show overhead text",
		description = "Float the text above the drinker's head, like normal public chat.",
		position = 1,
		section = overheadSection
	)
	default boolean showOverhead()
	{
		return true;
	}

	@ConfigItem(
		keyName = "overheadText",
		name = "Overhead text",
		description = "What appears above their head.",
		position = 2,
		section = overheadSection
	)
	default String overheadText()
	{
		return "glub glub";
	}

	@Range(min = 1, max = 20)
	@Units(Units.TICKS)
	@ConfigItem(
		keyName = "overheadDuration",
		name = "Duration",
		description = "How long the overhead text stays up, in game ticks (1 tick = 0.6s).",
		position = 3,
		section = overheadSection
	)
	default int overheadDurationTicks()
	{
		return 5;
	}

	@ConfigItem(
		keyName = "keepExistingOverhead",
		name = "Don't interrupt real chat",
		description = "If the player is already saying something overhead, leave their message alone.",
		position = 4,
		section = overheadSection
	)
	default boolean keepExistingOverhead()
	{
		return true;
	}

	// ------------------------------------------------------------------
	// Chat
	// ------------------------------------------------------------------

	@ConfigItem(
		keyName = "showChat",
		name = "Show chat message",
		description = "Print a message in your chatbox when someone chugs.",
		position = 11,
		section = chatSection
	)
	default boolean showChat()
	{
		return true;
	}

	@ConfigItem(
		keyName = "chatStyle",
		name = "Chat style",
		description = "Game message prints a line from the game, using the chat text below. "
			+ "Public chat prints \"Name: glub glub\" in your public chat tab, exactly as it "
			+ "would look if they had typed it - the chat text below is then ignored and the "
			+ "overhead text is used as the words.",
		position = 12,
		section = chatSection
	)
	default GlugChatStyle chatStyle()
	{
		return GlugChatStyle.GAME_MESSAGE;
	}

	@ConfigItem(
		keyName = "chatText",
		name = "Chat text",
		description = "The message itself, for the Game message style. Use $name for the player's name.",
		position = 13,
		section = chatSection
	)
	default String chatText()
	{
		return "$name: glub glub";
	}

	@Range(min = 0, max = 100)
	@Units(Units.TICKS)
	@ConfigItem(
		keyName = "chatCooldown",
		name = "Chat cooldown",
		description = "Minimum ticks between chat messages for the same player, so a 5-dose chug doesn't spam. 0 disables the cooldown.",
		position = 14,
		section = chatSection
	)
	default int chatCooldownTicks()
	{
		return 8;
	}

	// ------------------------------------------------------------------
	// Triggers
	// ------------------------------------------------------------------

	@ConfigItem(
		keyName = "includeOtherPlayers",
		name = "Include other players",
		description = "Glub for everyone you can see, not just yourself.",
		position = 21,
		section = triggerSection
	)
	default boolean includeOtherPlayers()
	{
		return true;
	}

	@ConfigItem(
		keyName = "includeNormalPotions",
		name = "Also normal potions and food",
		description = "Trigger on the ordinary eat/drink animation too, not only the chugging barrel. Noisy.",
		position = 22,
		section = triggerSection
	)
	default boolean includeNormalPotions()
	{
		return false;
	}
}
