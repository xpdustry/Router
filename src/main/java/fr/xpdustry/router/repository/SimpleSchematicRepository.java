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
package fr.xpdustry.router.repository;

import com.j256.ormlite.dao.*;
import com.j256.ormlite.jdbc.*;
import com.j256.ormlite.logger.*;
import com.j256.ormlite.table.*;
import fr.xpdustry.router.model.*;
import java.sql.*;
import java.util.*;
import org.jetbrains.annotations.*;

final class SimpleSchematicRepository implements SchematicRepository {

  static {
    com.j256.ormlite.logger.Logger.setGlobalLogLevel(Level.ERROR);
  }

  private final Dao<PlotSchematic, Long> dao;

  SimpleSchematicRepository(final @NotNull String url, final @Nullable String username, final @Nullable String password) {
    try {
      final var source = new JdbcConnectionSource(url, username, password);
      this.dao = DaoManager.createDao(source, PlotSchematic.class);
      TableUtils.createTableIfNotExists(source, PlotSchematic.class);
    } catch (final SQLException e) {
      throw new RuntimeException(e);
    }
  }

  SimpleSchematicRepository(final @NotNull String url) {
    this(url, null, null);
  }

  @Override
  public void saveSchematic(final @NotNull PlotSchematic schematic) {
    try {
      dao.createOrUpdate(schematic);
    } catch (final SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public @NotNull Optional<PlotSchematic> findSchematicById(final long id) {
    try {
      return Optional.ofNullable(dao.queryForId(id));
    } catch (final SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public @NotNull Iterable<PlotSchematic> findAllSchematics() {
    return dao;
  }

  @Override
  public boolean existsSchematicById(final long id) {
    return findSchematicById(id).isPresent();
  }

  @Override
  public long countSchematics() {
    try {
      return dao.countOf();
    } catch (final SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void deleteSchematicById(final long id) {
    try {
      dao.deleteById(id);
    } catch (final SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void deleteAllSchematics() {
    try {
      dao.deleteBuilder().delete();
    } catch (final SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
