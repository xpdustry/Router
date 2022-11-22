/*
 * Router, a plugin for sharing schematics.
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

import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.logger.Level;
import com.j256.ormlite.table.TableUtils;
import fr.xpdustry.router.model.PlotSchematic;
import java.sql.SQLException;
import java.util.Optional;
import org.checkerframework.checker.nullness.qual.Nullable;

final class SimpleSchematicRepository implements SchematicRepository {

    static {
        com.j256.ormlite.logger.Logger.setGlobalLogLevel(Level.ERROR);
    }

    private final RuntimeExceptionDao<PlotSchematic, Long> dao;

    SimpleSchematicRepository(final String url, final @Nullable String username, final @Nullable String password) {
        try {
            final var source = new JdbcConnectionSource(url, username, password);
            this.dao = RuntimeExceptionDao.createDao(source, PlotSchematic.class);
            TableUtils.createTableIfNotExists(source, PlotSchematic.class);
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }

    SimpleSchematicRepository(final String url) {
        this(url, null, null);
    }

    @Override
    public void saveSchematic(final PlotSchematic schematic) {
        dao.createOrUpdate(schematic);
    }

    @Override
    public Optional<PlotSchematic> findSchematicById(final long id) {
        return Optional.ofNullable(dao.queryForId(id));
    }

    @Override
    public Iterable<PlotSchematic> findAllSchematics() {
        return dao;
    }

    @Override
    public boolean existsSchematicById(final long id) {
        return findSchematicById(id).isPresent();
    }

    @Override
    public long countSchematics() {
        return dao.countOf();
    }

    @Override
    public void deleteSchematicById(final long id) {
        dao.deleteById(id);
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
