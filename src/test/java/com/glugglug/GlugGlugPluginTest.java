package com.glugglug;

import static org.junit.Assert.assertEquals;
import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;
import org.junit.Test;

public class GlugGlugPluginTest
{
	/**
	 * Launches a RuneLite client with this plugin side-loaded, for manual testing.
	 */
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(GlugGlugPlugin.class);
		RuneLite.main(args);
	}

	@Test
	public void substitutesPlayerName()
	{
		assertEquals("Zezima: glub glub", GlugGlugPlugin.formatChatMessage("$name: glub glub", "Zezima"));
	}

	@Test
	public void leavesTemplateAloneWithoutToken()
	{
		assertEquals("glub glub", GlugGlugPlugin.formatChatMessage("glub glub", "Zezima"));
	}

	@Test
	public void substitutesEveryOccurrence()
	{
		assertEquals("Bob glubs, Bob", GlugGlugPlugin.formatChatMessage("$name glubs, $name", "Bob"));
	}

	@Test
	public void toleratesMissingName()
	{
		assertEquals(": glub glub", GlugGlugPlugin.formatChatMessage("$name: glub glub", null));
	}

	@Test
	public void replacesNonBreakingSpacesInNames()
	{
		assertEquals("Some Guy", GlugGlugPlugin.cleanName("Some Guy"));
	}

	@Test
	public void toleratesNullName()
	{
		assertEquals("", GlugGlugPlugin.cleanName(null));
	}
}
