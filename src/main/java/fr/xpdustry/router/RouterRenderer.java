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

import arc.math.geom.Position;
import arc.math.geom.Vec2;
import arc.util.CommandHandler;
import arc.util.Interval;
import arc.util.Strings;
import arc.util.Time;
import fr.xpdustry.distributor.api.event.EventHandler;
import fr.xpdustry.distributor.api.plugin.PluginListener;
import fr.xpdustry.router.model.Plot;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.WorldLabel;

public final class RouterRenderer implements PluginListener {

    private static final String WELCOME_MESSAGE =
            """
            Welcome to [cyan]Xpdustry Router[],
            A dedicated server for building and sharing [cyan]schematics[].
            Check out the available plot commands with [cyan]/plot help[].""";

    private final Set<String> debuggers = new HashSet<>();
    private final Interval timer = new Interval();
    private final RouterPlugin router;

    public RouterRenderer(final RouterPlugin router) {
        this.router = router;
    }

    @EventHandler
    public void onPlayEvent(final EventType.PlayEvent event) {
        for (final var core : Vars.state.rules.defaultTeam.cores()) {
            final var tutorial = WorldLabel.create();
            tutorial.text(WELCOME_MESSAGE);
            tutorial.x(core.tile().getX());
            tutorial.y(core.tile().getY() + ((core.block().size / 3F) * Vars.tilesize));
            tutorial.flags(WorldLabel.flagBackground);
            tutorial.fontSize(1.4F);
            tutorial.add();
        }
    }

    @EventHandler
    public void onPlayerLeave(final EventType.PlayerLeave event) {
        this.debuggers.remove(event.player.uuid());
    }

    @Override
    public void onPluginClientCommandsRegistration(final CommandHandler handler) {
        final var manager = router.getClientCommands();
        // TODO Maybe move the debug command elsewhere
        manager.command(manager.commandBuilder("router").literal("debug").handler(context -> {
            final var uuid = context.getSender().getPlayer().uuid();
            if (!debuggers.add(uuid)) {
                debuggers.remove(uuid);
                context.getSender().sendMessage("Debug mode disabled.");
            } else {
                context.getSender().sendMessage("Debug mode enabled.");
            }
        }));
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

                Groups.player.each(player -> debuggers.contains(player.uuid()), player -> getBuildBoundaries(plot)
                        .forEach(boundary ->
                                Call.label("[gray]<" + plot.getId() + ">", 1F, boundary.getX(), boundary.getY())));
            }
        }
    }

    private String getPlayerLastName(final String uuid) {
        return Strings.stripColors(Vars.netServer.admins.getInfo(uuid).lastName);
    }

    private List<Position> getBuildBoundaries(final Plot plot) {
        return List.of(
                new Vec2(plot.getArea().getX(), plot.getArea().getY()),
                new Vec2(
                        plot.getArea().getX() + plot.getArea().getW() - Vars.tilesize,
                        plot.getArea().getY()),
                new Vec2(
                        plot.getArea().getX(),
                        plot.getArea().getY() + plot.getArea().getH() - Vars.tilesize),
                new Vec2(
                        plot.getArea().getX() + plot.getArea().getW() - Vars.tilesize,
                        plot.getArea().getY() + plot.getArea().getH() - Vars.tilesize));
    }
}
