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
import fr.xpdustry.distributor.api.util.MoreEvents;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.gen.Call;
import mindustry.gen.WorldLabel;

public final class RouterRenderer implements PluginListener {

    private static final String WELCOME_MESSAGE =
            """
    Welcome to [cyan]Xpdustry Router[],
    A dedicated server for building and sharing [cyan]schematics[].
    Check out the available plot commands with [cyan]/plot help[].""";

    private final Interval timer = new Interval();
    private final RouterPlugin router;

    public RouterRenderer(final RouterPlugin router) {
        this.router = router;
    }

    @Override
    public void onPluginInit() {
        MoreEvents.subscribe(EventType.PlayEvent.class, event -> {
            for (final var core : Vars.state.rules.defaultTeam.cores()) {
                final var tutorial = WorldLabel.create();
                tutorial.text(WELCOME_MESSAGE);
                tutorial.x(core.tile().getX());
                tutorial.y(core.tile().getY() + ((core.block().size / 3F) * Vars.tilesize));
                tutorial.flags(WorldLabel.flagBackground);
                tutorial.fontSize(1.4F);
                tutorial.add();
            }
        });
    }

    @Override
    public void onPluginUpdate() {
        if (router.isActive() && timer.get(Time.toSeconds)) {
            for (final var plot : router.getPlotManager().findAllPlots()) {
                Call.label(
                        "Plot [cyan]#[]" + plot.getId(),
                        1F,
                        plot.getArea().getX() + (plot.getArea().getW() / 2F),
                        plot.getArea().getY() + (plot.getArea().getH() / 2F));

                final String title;
                if (plot.getOwner() != null) {
                    title = "[orange]" + getPlayerLastName(plot.getOwner()) + "'s plot";
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
