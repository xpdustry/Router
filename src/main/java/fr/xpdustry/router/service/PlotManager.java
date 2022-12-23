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
import fr.xpdustry.router.model.PlotArea;
import java.util.List;
import java.util.Optional;

public interface PlotManager {

    Optional<Plot> findPlotById(final int id);

    List<Plot> findPlotsByOwner(final String owner);

    List<Plot> findPlotsByTrusted(final String trusted);

    List<Plot> findAllPlots();

    long countPlots();

    long countPlotsByOwner(final String owner);

    void createPlots(final List<PlotArea> areas);
}
