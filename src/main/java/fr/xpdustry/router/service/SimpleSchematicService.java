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

import fr.xpdustry.router.exception.*;
import fr.xpdustry.router.model.*;
import fr.xpdustry.router.repository.*;
import java.io.*;
import java.time.*;
import java.util.*;
import java.util.stream.*;
import mindustry.game.*;
import org.jetbrains.annotations.*;

final class SimpleSchematicService implements SchematicService {

  private final SchematicRepository repository;

  SimpleSchematicService(final @NotNull SchematicRepository repository) {
    this.repository = repository;
  }

  @Override
  public void publishSchematic(final @NotNull Plot plot) throws InvalidPlotException {
    if (plot.getOwner() == null) {
      throw new InvalidPlotException("This plot has no owner.", plot);
    }

    final var schematic = plot.getArea().toSchematic();
    if (schematic == null) {
      throw new InvalidPlotException("The plot is empty...", plot);
    }

    final byte[] bytes;
    try (final var stream = new ByteArrayOutputStream(512)) {
      Schematics.write(schematic, stream);
      bytes = stream.toByteArray();
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }

    repository.saveSchematic(
      PlotSchematic.of(0, plot.getOwner(), bytes, Date.from(Instant.now(Clock.systemUTC())))
    );
  }

  @Override
  public @NotNull List<PlotSchematic> getLatestSchematics(final long number) {
    return StreamSupport.stream(repository.findAllSchematics().spliterator(), false)
      .sorted(Comparator.comparing(PlotSchematic::getCreationDate))
      .limit(number)
      .toList();
  }
}
