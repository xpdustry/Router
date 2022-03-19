package fr.xpdustry.router.util;

import arc.func.*;
import mindustry.*;
import mindustry.maps.*;
import mindustry.net.*;
import mindustry.world.*;
import org.jetbrains.annotations.*;

public final class MapLoader implements AutoCloseable {
  private final WorldReloader reloader = new WorldReloader();

  public MapLoader() {
    if(Vars.net.active()) reloader.begin();
  }

  public void load(final @NotNull Map map) {
    Vars.world.loadMap(map);
  }

  public void generate(final int width, final int height, final @NotNull Cons<Tiles> generator) {
    Vars.logic.reset();
    Vars.world.loadGenerator(width, height, generator);
  }

  @Override
  public void close() {
    Vars.logic.play();

    if(Vars.net.active()) {
      reloader.end();
    } else {
      Vars.netServer.openServer();
    }
  }
}
