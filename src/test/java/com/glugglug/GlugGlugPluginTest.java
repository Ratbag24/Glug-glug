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
		assertEquals("Zezima: glug glug", GlugGlugPlugin.formatChatMessage("$name: glug glug", "Zezima"));
	}

	@Test
	public void leavesTemplateAloneWithoutToken()
	{
		assertEquals("glug glug", GlugGlugPlugin.formatChatMessage("glug glug", "Zezima"));
	}

	@Test
	public void substitutesEveryOccurrence()
	{
		assertEquals("Bob glugs, Bob", GlugGlugPlugin.formatChatMessage("$name glugs, $name", "Bob"));
	}

	@Test
	public void toleratesMissingName()
	{
		assertEquals(": glug glug", GlugGlugPlugin.formatChatMessage("$name: glug glug", null));
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
