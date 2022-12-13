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
package fr.xpdustry.router;

import arc.util.Interval;
import arc.util.Strings;
import arc.util.Time;
import fr.xpdustry.distributor.api.plugin.PluginListener;
import fr.xpdustry.router.service.PlotManager;
import mindustry.Vars;
import mindustry.gen.Call;

public final class RouterRenderer implements PluginListener {

    private final Interval timer = new Interval();
    private final PlotManager plots;

    public RouterRenderer(final PlotManager plots) {
        this.plots = plots;
    }

    @Override
    public void onPluginUpdate() {
        if (RouterPlugin.isActive() && timer.get(Time.toSeconds)) {
            for (final var plot : this.plots.findAllPlots()) {
                Call.label(
                        "Plot [cyan]#[]" + plot.getId(),
                        1F,
                        plot.getArea().getX() + (plot.getArea().getW() / 2F),
                        plot.getArea().getY() + (plot.getArea().getH() / 2F));

                final String title;
                if (plot.getOwner() != null) {
                    title = "[red]" + getPlayerLastName(plot.getOwner()) + "'s plot";
                } else {
                    title = "[green]Empty plot";
                }

                Call.label(
                        title,
                        1F,
                        plot.getArea().getX() + (plot.getArea().getW() / 2F),
                        plot.getArea().getY() + plot.getArea().getH());
            }
        }
    }

    private String getPlayerLastName(final String uuid) {
        return Strings.stripColors(Vars.netServer.admins.getInfo(uuid).lastName);
    }
}
