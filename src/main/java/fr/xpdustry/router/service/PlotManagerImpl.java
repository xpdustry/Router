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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

final class PlotManagerImpl implements PlotManager {

    private final Map<Integer, Plot> plots = new HashMap<>();

    @Override
    public Optional<Plot> findPlotById(final int id) {
        return Optional.ofNullable(plots.get(id));
    }

    @Override
    public List<Plot> findPlotsByOwner(final String owner) {
        return plots.values().stream().filter(p -> owner.equals(p.getOwner())).toList();
    }

    @Override
    public List<Plot> findPlotsByTrusted(final String trusted) {
        return plots.values().stream().filter(p -> p.isTrusted(trusted)).toList();
    }

    @Override
    public List<Plot> findAllPlots() {
        return List.copyOf(plots.values());
    }

    @Override
    public long countPlots() {
        return plots.size();
    }

    @Override
    public long countPlotsByOwner(final String owner) {
        return plots.values().stream().filter(p -> owner.equals(p.getOwner())).count();
    }

    @Override
    public void createPlots(final List<PlotArea> areas) {
        this.plots.clear();
        for (int i = 0; i < areas.size(); i++) {
            final int id = 100 + i;
            plots.put(id, Plot.of(areas.get(i), id));
        }
    }
}
