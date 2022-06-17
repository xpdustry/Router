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
package fr.xpdustry.router;

import arc.math.geom.*;
import fr.xpdustry.router.model.*;
import fr.xpdustry.router.service.*;
import fr.xpdustry.router.util.*;
import java.util.*;
import mindustry.net.Administration.*;
import mindustry.world.*;
import mindustry.world.blocks.distribution.*;
import mindustry.world.blocks.logic.*;
import mindustry.world.blocks.power.*;
import org.jetbrains.annotations.*;

public final class RouterFilter implements ActionFilter {

  private final PlotService plots;

  public RouterFilter(final @NotNull PlotService plots) {
    this.plots = plots;
  }

  @Override
  public boolean allow(final @NotNull PlayerAction action) {
    if (RouterPlugin.isActive()) {
      final List<Position> positions = new ArrayList<>();

      switch (action.type) {
        case placeBlock -> action.tile.getLinkedTilesAs(action.block, positions::add);
        case breakBlock, rotate, withdrawItem, depositItem -> positions.add(action.tile);
        case configure -> positions.addAll(getConfigPositions(action.tile, action.config));
        default -> {
          return true;
        }
      }

      return plots.findAllPlots().stream()
        .filter(p -> p.isTrusted(action.player.uuid()))
        .map(Plot::getArea)
        .anyMatch(a -> positions.stream().allMatch(a::contains));
    }
    return true;
  }

  private @NotNull Collection<Position> getConfigPositions(final @NotNull Tile tile, final @Nullable Object config) {
    final List<Position> positions = new ArrayList<>();

    if (tile.block() instanceof LogicBlock && config instanceof Integer pos) {
      positions.add(SimplePosition.of(pos));
    } else if (tile.block() instanceof PowerNode) {
      if (config instanceof Integer pos) {
        positions.add(SimplePosition.of(pos));
      } else if (config instanceof Point2[] points) {
        for (final var point : points) {
          positions.add(SimplePosition.of(point));
        }
      }
    } else if (tile.block() instanceof ItemBridge) {
      if (config instanceof Integer pos) {
        positions.add(SimplePosition.of(pos));
      } else if (config instanceof Point2 point) {
        positions.add(SimplePosition.of(point));
      }
    } else {
      positions.add(tile);
    }

    return positions;
  }
}
