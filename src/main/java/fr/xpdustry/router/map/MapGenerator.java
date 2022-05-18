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
import fr.xpdustry.router.plot.*;
import java.util.*;
import mindustry.world.*;
import org.jetbrains.annotations.*;

public interface MapGenerator extends Cons<Tiles> {

  static MapGenerator simple() {
    return new SimpleMapGenerator();
  }

  int getMapWidth();

  int getMapHeight();

  @NotNull Collection<PlotArea> getPlots();
}
