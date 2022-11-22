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
package fr.xpdustry.router.commands;

import arc.struct.Seq;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import fr.xpdustry.distributor.api.command.sender.CommandSender;
import fr.xpdustry.distributor.api.plugin.PluginListener;
import fr.xpdustry.router.RouterPlugin;
import fr.xpdustry.router.map.MapLoader;
import fr.xpdustry.router.map.PlotMapGenerator;
import mindustry.Vars;
import mindustry.game.Gamemode;
import mindustry.game.Rules;

public final class StartCommand implements PluginListener {

    private final RouterPlugin router;

    public StartCommand(final RouterPlugin router) {
        this.router = router;
    }

    @CommandMethod("router start")
    @CommandDescription("Start hosting a router server.")
    public void onRouterStart(final CommandSender sender) {
        sender.sendMessage("Starting router server...");
        try (final var loader = new MapLoader()) {
            final var result = loader.load(PlotMapGenerator.simple());

            // Apply rules
            final var rules = new Rules();
            Gamemode.sandbox.apply(rules);
            rules.modeName = "[orange]Router";
            rules.tags.put(RouterPlugin.ROUTER_ACTIVE_KEY, "true");
            rules.unitBuildSpeedMultiplier = Float.MIN_VALUE;
            rules.damageExplosions = false;
            rules.reactorExplosions = false;
            rules.ghostBlocks = false;
            Vars.state.rules = rules;

            final var plots = router.getPlotManager();
            plots.createPlots(result.getAreas());
            final var placeholders = router.getSchematicService()
                    .getLatestSchematics(plots.countPlots())
                    .iterator();
            final var available = Seq.with(plots.findAllPlots());

            while (placeholders.hasNext() && !available.isEmpty()) {
                final var placeholder = placeholders.next();
                final var plot = available.random();
                available.remove(plot, true);

                if (placeholder.getSchematic().width <= plot.getArea().getTileW()
                        && placeholder.getSchematic().height <= plot.getArea().getTileH()) {
                    plot.setPlaceholder(placeholder);
                }
            }
        }
        sender.sendMessage("Router server started.");
    }
}
