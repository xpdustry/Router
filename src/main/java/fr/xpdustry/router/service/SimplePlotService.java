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

import fr.xpdustry.router.model.*;
import java.util.*;
import org.jetbrains.annotations.*;

final class SimplePlotService implements PlotService {

  private final Collection<Plot> plots = new HashSet<>();

  @Override
  public @NotNull Optional<Plot> findPlotById(final int id) {
    return plots.stream().filter(p -> p.getId() == id).findAny();
  }

  @Override
  public @NotNull Iterable<Plot> findPlotsByOwner(@NotNull String owner) {
    return plots.stream().filter(p -> owner.equals(p.getOwner())).toList();
  }

  @Override
  public @NotNull Iterable<Plot> findAllPlots() {
    return Collections.unmodifiableCollection(plots);
  }

  @Override
  public long countPlotsByOwner(@NotNull String owner) {
    return plots.stream().filter(p -> owner.equals(p.getOwner())).count();
  }

  @Override
  public void setPlotAreas(final @NotNull Collection<PlotArea> areas) {
    plots.clear();
    areas.forEach(area -> plots.add(Plot.of(area)));
    if (areas.size() != plots.size()) {
      throw new IllegalStateException("This ain't supposed to happen mate.");
    }
  }
}
