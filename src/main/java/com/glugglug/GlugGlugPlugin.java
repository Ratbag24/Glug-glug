package com.glugglug;

import com.google.inject.Provides;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.PlayerDespawned;
import net.runelite.api.gameval.AnimationID;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
	name = "Glub Glub",
	description = "Says \"glub glub\" in chat and above your head when you drink from the chugging barrel",
	tags = {"glub", "glug", "chug", "chugging", "barrel", "potion", "drink", "mixology", "overhead"}
)
public class GlugGlugPlugin extends Plugin
{
	/**
	 * The chugging barrel's own drink animation. The barrel was called the "pre-pot device"
	 * before it was renamed in October 2024, which is the name the game data still uses.
	 * Animations are sent to every nearby client, so this fires for other players too.
	 */
	static final int CHUGGING_BARREL_ANIMATION = AnimationID.PREPOT_DEVICE_DRINK_FULL;

	/** The ordinary eat/drink animation, used for normal potions. Off by default. */
	static final int EAT_DRINK_ANIMATION = AnimationID.HUMAN_EAT;

	/**
	 * Overhead text is hidden once the actor's overhead cycle counter runs out. We re-arm it
	 * every game tick with a value comfortably larger than one tick (600ms), so the text never
	 * lapses between ticks and we keep full control of how long it stays up.
	 */
	private static final int OVERHEAD_CYCLES = 100;

	private static final String NAME_TOKEN = "$name";

	@Inject
	private Client client;

	@Inject
	private GlugGlugConfig config;

	@Inject
	private ChatMessageManager chatMessageManager;

	/** Players currently showing our overhead text, and how many ticks they have left. */
	private final Map<Player, ActiveGlug> active = new HashMap<>();

	/** Last tick we printed a chat message for a given player, for the cooldown. */
	private final Map<String, Integer> lastChatTick = new HashMap<>();

	private int tick;

	@Provides
	GlugGlugConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(GlugGlugConfig.class);
	}

	@Override
	protected void shutDown()
	{
		// Put everyone's overhead text back the way we found it before we go.
		for (Map.Entry<Player, ActiveGlug> entry : active.entrySet())
		{
			clearOverhead(entry.getKey(), entry.getValue());
		}
		active.clear();
		lastChatTick.clear();
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() != GameState.LOGGED_IN)
		{
			// The actors we were tracking no longer exist, so there is nothing to restore.
			active.clear();
			lastChatTick.clear();
		}
	}

	@Subscribe
	public void onPlayerDespawned(PlayerDespawned event)
	{
		active.remove(event.getPlayer());
	}

	@Subscribe
	public void onAnimationChanged(AnimationChanged event)
	{
		final Actor actor = event.getActor();
		if (!(actor instanceof Player))
		{
			return;
		}

		final Player player = (Player) actor;
		if (!isChugAnimation(player.getAnimation()))
		{
			return;
		}

		final Player local = client.getLocalPlayer();
		final boolean isLocalPlayer = local != null && player == local;
		if (!isLocalPlayer && !config.includeOtherPlayers())
		{
			return;
		}

		if (config.showOverhead())
		{
			showOverhead(player);
		}

		if (config.showChat())
		{
			sendChatMessage(player);
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		tick++;

		final Iterator<Map.Entry<Player, ActiveGlug>> it = active.entrySet().iterator();
		while (it.hasNext())
		{
			final Map.Entry<Player, ActiveGlug> entry = it.next();
			final Player player = entry.getKey();
			final ActiveGlug glug = entry.getValue();

			// If something else has taken over their overhead text (real public chat, say),
			// stop fighting it and let go.
			if (!glug.text.equals(player.getOverheadText()))
			{
				it.remove();
				continue;
			}

			if (--glug.ticksRemaining <= 0)
			{
				clearOverhead(player, glug);
				it.remove();
				continue;
			}

			// Re-arm so the text survives until we decide to drop it.
			player.setOverheadCycle(OVERHEAD_CYCLES);
		}
	}

	boolean isChugAnimation(int animation)
	{
		if (animation == CHUGGING_BARREL_ANIMATION)
		{
			return true;
		}
		return config.includeNormalPotions() && animation == EAT_DRINK_ANIMATION;
	}

	private void showOverhead(Player player)
	{
		final String text = config.overheadText();
		if (text == null || text.trim().isEmpty())
		{
			return;
		}

		final ActiveGlug existing = active.get(player);
		if (existing == null && config.keepExistingOverhead())
		{
			// They are mid-sentence; don't talk over them.
			final String current = player.getOverheadText();
			if (current != null && !current.isEmpty())
			{
				return;
			}
		}

		final ActiveGlug glug = existing != null
			? existing
			: new ActiveGlug(text, player.getOverheadText());

		// A fresh gulp refreshes the timer, and picks up any config edit to the text.
		glug.text = text;
		glug.ticksRemaining = Math.max(1, config.overheadDurationTicks());
		active.put(player, glug);

		player.setOverheadText(text);
		player.setOverheadCycle(OVERHEAD_CYCLES);
	}

	private void clearOverhead(Player player, ActiveGlug glug)
	{
		// Only clear text that is still ours.
		if (glug.text.equals(player.getOverheadText()))
		{
			player.setOverheadText(glug.previousText);
			player.setOverheadCycle(0);
		}
	}

	private void sendChatMessage(Player player)
	{
		final String template = config.chatText();
		if (template == null || template.trim().isEmpty())
		{
			return;
		}

		final String name = cleanName(player.getName());
		final int cooldown = config.chatCooldownTicks();
		if (cooldown > 0)
		{
			final Integer last = lastChatTick.get(name);
			if (last != null && tick - last < cooldown)
			{
				return;
			}
		}
		lastChatTick.put(name, tick);

		final String message = new ChatMessageBuilder()
			.append(ChatColorType.HIGHLIGHT)
			.append(formatChatMessage(template, name))
			.build();

		chatMessageManager.queue(QueuedMessage.builder()
			.type(ChatMessageType.GAMEMESSAGE)
			.runeLiteFormattedMessage(message)
			.build());
	}

	/**
	 * Substitutes the player's name into the configured template. Visible for testing.
	 */
	static String formatChatMessage(String template, String playerName)
	{
		return template.replace(NAME_TOKEN, playerName == null ? "" : playerName);
	}

	/**
	 * Player names arrive with non-breaking spaces in them. Visible for testing.
	 */
	static String cleanName(String name)
	{
		return name == null ? "" : name.replace(' ', ' ');
	}

	private static final class ActiveGlug
	{
		private String text;
		private final String previousText;
		private int ticksRemaining;

		private ActiveGlug(String text, String previousText)
		{
			this.text = text;
			this.previousText = previousText;
		}
	}
}
