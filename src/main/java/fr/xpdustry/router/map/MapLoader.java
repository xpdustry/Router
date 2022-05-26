/*
 * Router, a Reddit-like Mindustry plugin for sharing schematics.
 *
 * Copyright (C) 2022 Xpdustry
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package fr.xpdustry.router.map;

import arc.func.*;
import mindustry.*;
import mindustry.maps.*;
import mindustry.net.*;
import mindustry.world.*;
import org.jetbrains.annotations.ApiStatus.*;
import org.jetbrains.annotations.*;

@Experimental
public final class MapLoader implements AutoCloseable {

  private final WorldReloader reloader = new WorldReloader();

  public MapLoader() {
    if (Vars.net.active()) reloader.begin();
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

    if (Vars.net.active()) {
      reloader.end();
    } else {
      Vars.netServer.openServer();
    }
  }
}
