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

import fr.xpdustry.router.model.PlotSchematic;
import java.io.File;
import java.util.Optional;

public interface SchematicRepository {

    static SchematicRepository of(final String url, final String username, final String password) {
        return new SimpleSchematicRepository(url, username, password);
    }

    static SchematicRepository of(final File file) {
        return new SimpleSchematicRepository("jdbc:sqlite:" + file.getAbsolutePath());
    }

    void saveSchematic(final PlotSchematic schematic);

    Optional<PlotSchematic> findSchematicById(final long id);

    Iterable<PlotSchematic> findAllSchematics();

    boolean existsSchematicById(final long id);

    long countSchematics();

    void deleteSchematicById(final long id);

    void deleteAllSchematics();
}
