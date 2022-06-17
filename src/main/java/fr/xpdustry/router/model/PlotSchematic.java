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
package fr.xpdustry.router.model;

import com.j256.ormlite.field.*;
import com.j256.ormlite.table.*;
import java.io.*;
import java.time.*;
import java.util.*;
import mindustry.game.*;
import org.jetbrains.annotations.*;

@DatabaseTable(tableName = "plot_schematic")
public final class PlotSchematic {

  @DatabaseField(canBeNull = false, generatedId = true)
  private final long id;
  @DatabaseField(canBeNull = false, index = true)
  private final String author;
  @DatabaseField(canBeNull = false, dataType = DataType.BYTE_ARRAY, columnName = "schematic")
  private final byte[] rawSchematic;
  @DatabaseField(canBeNull = false)
  private final Date creationDate;

  private transient @Nullable Schematic schematic;

  PlotSchematic(final long id, final @NotNull String author, final byte[] rawSchematic, final @NotNull Date creationDate) {
    this.id = id;
    this.author = author;
    this.rawSchematic = rawSchematic;
    this.creationDate = creationDate;
  }

  // Constructor for ORMLIte, DO NOT USE
  PlotSchematic() {
    this(0, "", new byte[] {}, Date.from(Instant.now()));
  }

  public static @NotNull PlotSchematic of(final long id, final @NotNull String author, final byte[] rawSchematic, final @NotNull Date creationDate) {
    return new PlotSchematic(id, author, rawSchematic, creationDate);
  }

  public @NotNull String getAuthor() {
    return author;
  }

  public long getId() {
    return id;
  }

  public byte[] getRawSchematic() {
    return rawSchematic;
  }

  public synchronized @NotNull Schematic getSchematic() {
    if (schematic == null) {
      try (final var stream = new ByteArrayInputStream(rawSchematic)) {
        this.schematic = Schematics.read(stream);
      } catch (final IOException e) {
        throw new RuntimeException(e);
      }
    }
    return schematic;
  }

  public @NotNull String getName() {
    return getSchematic().name();
  }

  public @NotNull String getDescription() {
    return getSchematic().description();
  }

  public Date getCreationDate() {
    return creationDate;
  }
}
