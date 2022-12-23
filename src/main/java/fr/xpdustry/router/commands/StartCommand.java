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

import arc.util.ColorCodes;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.Flag;
import fr.xpdustry.distributor.api.command.sender.CommandSender;
import fr.xpdustry.distributor.api.plugin.PluginListener;
import fr.xpdustry.router.RouterPlugin;
import fr.xpdustry.router.map.MapLoader;
import fr.xpdustry.router.map.PlotMapContext;
import fr.xpdustry.router.map.SimplePlotMapGenerator;
import java.io.IOException;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.game.Gamemode;
import mindustry.game.Rules;

public final class StartCommand implements PluginListener {

    private final RouterPlugin router;

    public StartCommand(final RouterPlugin router) {
        this.router = router;
    }

    @CommandPermission("fr.xpdustry.router.start")
    @CommandMethod("router start")
    @CommandDescription("Start hosting a router server.")
    public void onRouterStart(final CommandSender sender, final @Flag(value = "force", aliases = "f") boolean force) {
        if (!force && Vars.state.isPlaying()) {
            final var message = "A game is already running![white] If you want to start router anyway, "
                    + "use [accent]/router start --force";
            sender.sendWarning(
                    sender.isConsole()
                            ? message.replace("[white]", ColorCodes.white).replace("[accent]", ColorCodes.yellow)
                            : message);
            return;
        }

        sender.sendMessage("Starting router server...");
        final PlotMapContext context;

        try (final var loader = new MapLoader()) {
            context = loader.load(new SimplePlotMapGenerator());

            // Apply the rules
            final var rules = new Rules();
            Gamemode.sandbox.apply(rules);
            rules.modeName = "[orange]Router";
            rules.tags.put(RouterPlugin.ROUTER_ACTIVE_KEY, "true");
            rules.unitBuildSpeedMultiplier = Float.MIN_VALUE;
            rules.damageExplosions = false;
            rules.reactorExplosions = false;
            rules.fire = false;
            rules.ghostBlocks = false;
            rules.bannedBlocks.add(Blocks.payloadSource);
            Vars.state.rules = rules;
        } catch (final IOException exception) {
            sender.sendMessage("Failed to start router server: " + exception.getMessage());
            return;
        }

        final var plots = router.getPlotManager();
        plots.createPlots(context.getAreas());
        sender.sendMessage("Router server started.");
    }
}
