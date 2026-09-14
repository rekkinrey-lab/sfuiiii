# CrystalPingCompensation

A Paper 1.21.x plugin that gives high-ping players fairer end-crystal PvP,
including Bedrock players connecting through Geyser/Floodgate.

## What it actually does

Every tick it records each online player's location into a short rolling
history buffer. When an end crystal someone placed explodes and damages a
victim, it looks up the attacker's ping, rewinds the victim's position back
by that (capped) amount of time, and compares it to the victim's current
position:

- If the victim was already inside "point blank" range back then too,
  nothing changes.
- If the victim had moved further away from the crystal in the time it took
  the attacker's ping to catch up, the damage (and optionally knockback
  direction) is scaled up toward what the attacker's client actually saw,
  capped by `max-damage-multiplier`.

It never reduces damage and never affects players below `min-ping-threshold-ms`,
so normal-ping players see no change in how crystals feel against them.

Bedrock players are detected via Floodgate. If Geyser-Spigot is also
installed, it tries to use Geyser's native RakNet ping (usually more accurate
for Bedrock connections) instead of the Java keep-alive ping; if that call
fails for any reason it just falls back to the normal ping automatically.

## Building

### Option A: You have a PC with Java + Maven

```bash
cd crystal-ping-compensation
mvn clean package
```

The built jar will be at `target/crystal-ping-compensation-1.0.0.jar`.

### Option B: You're on a host (LilyPad, etc.) with no local dev environment

This project includes a GitHub Actions workflow (`.github/workflows/build.yml`)
that builds the jar for you automatically, for free, with nothing installed
on your own machine:

1. Create a free GitHub account if you don't have one, and create a new
   (public or private) repository.
2. Upload every file/folder from this project into that repository (drag and
   drop works fine on github.com, or use "Add file > Upload files").
3. Go to the repo's **Actions** tab. A workflow run should start
   automatically (or click "Run workflow" if it doesn't).
4. Once it finishes (green checkmark, usually under a minute), open that run
   and download the **crystal-ping-compensation-jar** artifact - it's a zip
   containing the actual `.jar` file.
5. Unzip it to get `crystal-ping-compensation-1.0.0.jar`.

Then, on your host:

1. Open your host's file manager (or FTP/SFTP if it offers that).
2. Navigate to your server's `plugins/` folder.
3. Upload `crystal-ping-compensation-1.0.0.jar` there.
4. Restart the server (or use your host's restart button) so it loads.
5. In the server console, run `pingcrystal status` to confirm it's running.

Floodgate and Geyser-Spigot are both *optional* (soft-depend) - the plugin
works fine without them, it just won't do Bedrock-specific ping lookups. If
your host offers a plugin marketplace, you may already have both installed
for Bedrock crossplay.

## One thing to double check before you build

Geyser's API has shifted method names across versions before. In
`GeyserPingHook.java` I call:

```java
GeyserApi.api().connectionByUuid(javaUuid).ping()
```

If your installed Geyser-Spigot version renamed `ping()` (e.g. to
`getPing()`), Maven will tell you immediately at compile time - just fix that
one line. Everything else has no such risk since it uses stable Bukkit/Paper
API.

## Config (`config.yml`)

| Setting | What it does |
|---|---|
| `min-ping-threshold-ms` | Below this ping, zero compensation is applied. Default `80`. |
| `max-compensation-ms` | Hard cap on the rewind window, so no one can abuse an inflated ping. Default `250`. |
| `ping-compensation-factor` | Multiplier on reported ping before capping. `1.0` = full ping. |
| `max-damage-multiplier` | Ceiling on how much a single hit's damage can be scaled up. Default `1.5`. |
| `compensate-knockback` | Also nudges knockback direction to match the rewound position. |
| `bedrock.use-floodgate-detection` | Turn off if you don't want Bedrock-specific handling at all. |
| `bedrock.prefer-geyser-native-ping` | Turn off to always use the plain Bukkit ping even for Bedrock players. |

## Commands

- `/pingcrystal status` - show current settings
- `/pingcrystal reload` - reload config.yml
- `/pingcrystal toggle` - enable/disable without a reload

Permission: `pingcrystal.admin` (default: op). Players with
`pingcrystal.exempt` never have compensation applied against them (e.g. for
staff practicing without any adjustment).

## Tuning notes

The damage-scaling formula (`currentDist / pastDist`, capped) is a
deliberately simple approximation rather than a reconstruction of vanilla's
exact explosion damage curve — reverse-engineering that curve is possible but
fragile across versions. In practice, tightening `max-damage-multiplier` and
`max-compensation-ms` is the fastest way to keep it feeling fair if it ever
feels too strong on your server. Turn on `debug: true` to see exactly what
it's doing per-hit in console while you tune it.
