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
package fr.xpdustry.router.exception;

import fr.xpdustry.router.model.*;
import java.io.*;
import org.jetbrains.annotations.*;

public class InvalidPlotException extends Exception {

  @Serial
  private static final long serialVersionUID = -1671640589672425282L;

  private final Plot plot;

  public InvalidPlotException(final @NotNull String reason, final @NotNull Plot plot) {
    super(reason);
    this.plot = plot;
  }

  public @NotNull Plot getPlot() {
    return plot;
  }
}
