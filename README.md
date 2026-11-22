# BaseScout (Minecraft 26.3, Fabric)

A passive, client-side scanner for your friend-bet. It only reads chunks the server
already sent to your game and sends nothing back. Check with your friend that his
"mods are okay" covers a client mod like this one.

## How it works
Press **B** in-game (rebind it under Options > Controls > BaseScout) to open the
window. Click **Scan**; it scores every loaded chunk by the chests, furnaces, beds,
barrels, hoppers and similar blocks in it, adds up neighbouring chunks, and lists the
top hotspots with approximate X/Z, score and distance. **Copy** puts a hotspot's
coordinates on your clipboard. **+ / -** change sensitivity, then rescan.

`/basescan` also opens the window. `/basescan 2` runs a scan in chat (1 = most
sensitive, 10 = strictest).

It can only see as far as your render distance, so move around (elytra, boat, or
minecart along a long straight line is great) and run it again every so often.
Villages, shipwrecks and bastions will false-positive; a lone chest is ignored by
the threshold.

## Build
Needs Java 25 and Gradle 9.6+ (or the Gradle wrapper).

    gradle build

The jar ends up in `build/libs/basescout-0.1.0.jar`. Drop it into `.minecraft/mods`
with Fabric Loader 0.19.5+ and Fabric API for 26.3.

## Note
Written without being able to compile it. If the build complains about a class name,
check the 26.3 names in your IDE (Minecraft is unobfuscated now) and tweak the imports.
