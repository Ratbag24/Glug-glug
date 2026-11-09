package com.glugglug;

public enum GlugChatStyle
{
	/**
	 * A game message, the kind the game itself prints ("You feel stronger"). Uses the
	 * configured chat text, so it can name the player.
	 */
	GAME_MESSAGE("Game message"),

	/**
	 * A public chat line, so the chatbox shows "Name: glub glub" exactly as it would if the
	 * player had typed it. The message itself is the overhead text.
	 */
	PUBLIC_CHAT("Public chat");

	private final String name;

	GlugChatStyle(String name)
	{
		this.name = name;
	}

	@Override
	public String toString()
	{
		return name;
	}
}
