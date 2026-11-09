# Glub Glub

A [RuneLite](https://runelite.net) plugin for Old School RuneScape. When someone drinks
from the **chugging barrel**, it says `glub glub` in your chatbox and floats `glub glub`
above their head.

## How it works

The chugging barrel has its own drinking animation (`PREPOT_DEVICE_DRINK_FULL`, id `11645` —
the barrel was called the "pre-pot device" until it was renamed in October 2024, and the game
data still uses the old name). Animations are broadcast to every nearby client, so the plugin
can spot *anyone* chugging, not just you.

The overhead text uses the same mechanism as normal public chat, so it looks exactly like a
player saying "glub glub" out loud.

## Important: everyone needs to install it

RuneLite plugins run on your own client. This plugin **cannot** make text appear on your
friends' screens — it only draws on yours.

So: each friend installs it themselves. Once they have, everyone sees `glub glub` over anyone
who chugs, which is as close to server-wide as a client-side plugin can get. Nothing is sent
to other players, and players without the plugin see nothing.

## Installing

Not on the Plugin Hub yet, so side-load it:

```bash
git clone https://github.com/ratbag24/glug-glug.git
cd glug-glug
./gradlew build
```

Then either run `GlugGlugPluginTest.main()` from your IDE to launch a client with the plugin
loaded, or drop `build/libs/glug-glug-1.0.0.jar` into `~/.runelite/sideloaded-plugins/` and
restart RuneLite.

## Settings

| Setting | Default | What it does |
| --- | --- | --- |
| **Show overhead text** | on | Float the text above the drinker's head |
| **Overhead text** | `glub glub` | What appears above their head |
| **Duration** | 5 ticks | How long it stays up (1 tick = 0.6s) |
| **Don't interrupt real chat** | on | If they're already saying something, leave it alone |
| **Show chat message** | on | Print a message in your chatbox |
| **Chat text** | `$name: glub glub` | The message; `$name` becomes the player's name |
| **Chat cooldown** | 8 ticks | Minimum gap between messages for the same player |
| **Include other players** | on | Glub for everyone you can see, not just yourself |
| **Also normal potions and food** | off | Trigger on the ordinary eat/drink animation too (noisy) |

The chat cooldown matters because a barrel holds up to five potions and fires the animation
per dose — without it, one click would print five lines.

## Notes

- The barrel only works at a bank, so this fires at bank chests.
- Built against RuneLite 1.13.6, Java 11 target.
- If a real public chat message takes over someone's overhead text mid-glub, the plugin backs
  off and restores whatever was there before.
