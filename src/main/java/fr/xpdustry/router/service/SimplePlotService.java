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
package fr.xpdustry.router.service;

import arc.util.*;
import fr.xpdustry.router.plot.*;
import java.util.*;
import mindustry.*;
import mindustry.gen.*;
import mindustry.net.*;
import mindustry.net.Administration.*;
import mindustry.world.*;
import org.jetbrains.annotations.*;

final class SimplePlotService implements PlotService {

  private final Collection<Plot> plots = new HashSet<>();

  @Override
  public @NotNull Optional<Plot> findPlotById(final int id) {
    return plots.stream().filter(p -> p.getId() == id).findAny();
  }

  @Override
  public @NotNull Optional<Plot> findPlotByOwner(@NotNull String owner) {
    return plots.stream().filter(p -> owner.equals(p.getOwner())).findAny();
  }

  @Override
  public @NotNull Iterable<Plot> findAllPlots() {
    return Collections.unmodifiableCollection(plots);
  }

  @Override
  public void setPlotAreas(final @NotNull Collection<PlotArea> areas) {
    plots.clear();
    areas.forEach(area -> plots.add(Plot.of(area)));
    if (areas.size() != plots.size()) {
      throw new IllegalStateException("This ain't supposed to happen mate.");
    }
  }

  @Override
  public boolean canModifyTile(final @NotNull Administration.PlayerAction action) {
    if (action.tile != null) {
      final var tiles = new ArrayList<Tile>();
      if (action.type == ActionType.placeBlock && action.block.isMultiblock()) {
        int size = action.block.size;
        int offsetX = -(size - 1) / 2;
        int offsetY = -(size - 1) / 2;
        for (int dx = 0; dx < size; dx++) {
          for (int dy = 0; dy < size; dy++) {
            final var other = Vars.world.tile(action.tile.x + dx + offsetX, action.tile.y + dy + offsetY);
            if (other != null) tiles.add(other);
          }
        }
      } else {
        tiles.add(action.tile);
      }

      return plots.stream()
        .filter(p -> p.getOwner() != null && (p.getOwner().equals(action.player.uuid()) || p.hasMember(action.player.uuid())))
        .anyMatch(p -> tiles.stream().allMatch(t -> p.getArea().contains(t)));
    }
    return true;
  }

  @Override
  public void renderPlots() {
    for (final var plot : plots) {
      Call.label(
        "Plot #" + plot.getId(),
        1F,
        plot.getArea().getX() + (plot.getArea().getW() / 2F),
        plot.getArea().getY() + (plot.getArea().getH() / 2F)
      );

      final var owner = Groups.player.find(p -> p.uuid().equals(plot.getOwner()));
      if (owner != null) {
        Call.label(
          "[green]" + Strings.stripColors(owner.name()) + "'s plot",
          1F,
          plot.getArea().getX() + (plot.getArea().getW() / 2F),
          plot.getArea().getY() + plot.getArea().getH()
        );
      }
    }
  }
}
