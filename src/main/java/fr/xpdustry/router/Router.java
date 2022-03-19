package fr.xpdustry.router;

import arc.util.*;
import fr.xpdustry.distributor.plugin.*;
import fr.xpdustry.distributor.script.js.*;
import fr.xpdustry.router.internal.*;
import fr.xpdustry.router.util.*;
import fr.xpdustry.router.world.generator.*;
import mindustry.*;
import mindustry.net.Administration.*;
import net.mindustry_ddns.store.FileStore;
import org.jetbrains.annotations.*;

@SuppressWarnings({"unused", "NullAway.Init"})
public class Router extends AbstractPlugin {

  private static final String ROUTER_ACTIVE_KEY = "xpdustry:router";
  private static final RhinoJavaScriptEngine evalEngine = JavaScriptPlugin.createEngine();
  private static FileStore<RouterConfig> store;

  public static RouterConfig getConf() {
    return store.get();
  }

  public static boolean isActive() {
    return Vars.state.isPlaying() && Vars.state.rules.tags.getBool(ROUTER_ACTIVE_KEY) && !Vars.state.gameOver;
  }

  @Override
  public void init() {
    store = getStoredConfig("config", RouterConfig.class);

    // TODO Make the plots actually meaningful + redo the packaging...

    /*
    Vars.netServer.admins.addActionFilter(action -> {
      if (action.type == ActionType.placeBlock) {
      }
    });

     */
  }

  @Override
  public void registerServerCommands(final @NotNull CommandHandler handler) {
    handler.register("router", "Begin hosting with the reddit gamemode.", args -> {
      try (final var loader = new MapLoader()) {
        Log.info("Generating map...");
        final var generator = MapGenerator.simple();
        loader.generate(generator.getMapWidth(), generator.getMapHeight(), generator);
        Vars.state.rules.modeName = "[orange]Router";
        Vars.state.rules.tags.put(ROUTER_ACTIVE_KEY, "true");
        Log.info("PLOTS @", generator.getPlots());
      }
    });
  }
}
