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
package fr.xpdustry.router.service;

import fr.xpdustry.router.model.Plot;
import fr.xpdustry.router.model.PlotSchematic;
import fr.xpdustry.router.repository.SchematicRepository;
import java.util.List;

public interface SchematicService {

    static SchematicService simple(final SchematicRepository repository) {
        return new SimpleSchematicService(repository);
    }

    void publishSchematic(final Plot plot) throws InvalidPlotException;

    List<PlotSchematic> getLatestSchematics(final long number);
}
