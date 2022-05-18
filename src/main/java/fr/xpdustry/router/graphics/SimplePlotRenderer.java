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
package fr.xpdustry.router.graphics;

import arc.util.*;
import fr.xpdustry.router.plot.*;
import mindustry.gen.*;
import org.jetbrains.annotations.*;

final class SimplePlotRenderer implements PlotRenderer {

  static final SimplePlotRenderer INSTANCE = new SimplePlotRenderer();

  @Override
  public void renderPlot(final @NotNull Plot plot) {
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
