package fr.xpdustry.router;

import arc.util.*;
import fr.xpdustry.distributor.exception.*;
import fr.xpdustry.distributor.plugin.*;
import fr.xpdustry.distributor.script.js.*;
import fr.xpdustry.router.internal.*;
import java.io.*;
import java.nio.charset.*;
import mindustry.*;
import mindustry.core.GameState.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.net.*;
import net.mindustry_ddns.store.FileStore;
import org.jetbrains.annotations.*;

import static mindustry.Vars.netServer;

@SuppressWarnings({"unused", "NullAway.Init"})
public class Router extends AbstractPlugin {

  private static final RhinoJavaScriptEngine evalEngine = JavaScriptPlugin.createEngine();
  private static FileStore<RouterConfig> store;

  private static long hashSchematic(final @NotNull Schematic schematic) {
    long output = 0;

    for (final var tile : schematic.tiles) {
      var tile_hash = 1;
      tile_hash = 31 * tile_hash + tile.block.name.hashCode();
      tile_hash = 31 * tile_hash + tile.x;
      tile_hash = 31 * tile_hash + tile.y;

      if (tile.config != null) tile_hash = 31 * tile_hash + tile.config.hashCode();
      tile_hash = 31 * tile_hash + tile.rotation;
      output += tile_hash;
    }

    return output;
  }

  public static RouterConfig getConf() {
    return store.get();
  }

  @Override
  public void init() {
    store = getStoredConfig("config", RouterConfig.class);
  }

  @Override
  public void registerServerCommands(CommandHandler handler) {
    handler.register("reddit", "Begin hosting with the reddit gamemode.", args -> {
      final var hotLoading = Vars.state.isPlaying();
      final var reloader = new WorldReloader();

      // TODO Testing auto-reloading...

      Log.info("Generating map...");
      if(hotLoading) reloader.begin();
      Vars.logic.reset(); // Added cauz generatin'

      try (final var stream = getConf().getGeneratorScript().isBlank()
        ? getClass().getClassLoader().getResourceAsStream("generator.js")
        : JavaScriptPlugin.JAVA_SCRIPT_DIRECTORY.child(getConf().getGeneratorScript()).read()
      ) {
        if (stream == null) throw new IOException("generator.js can't be found...");
        final var reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
        final var scope = evalEngine.newScope();
        evalEngine.eval(JavaScriptPlugin.getImportsScript(), scope);
        evalEngine.eval(reader, scope);
      } catch (final ScriptException | IOException e) {
        Log.err("The unexpected happened lol...", e);
        return;
      }
      // Vars.world.loadMap(map);
      // Vars.world.loadGenerator(WORLD_WIDTH, WORLD_HEIGHT, generator);

      Vars.state.rules.modeName = "[orange]RedditDustry";

      Vars.logic.play();
      Log.info("HOTRELOADED @", hotLoading);
      if(hotLoading){
        reloader.end();
      }else{
        Vars.netServer.openServer();
      }
    });
  }
}
