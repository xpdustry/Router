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

import arc.util.CommandHandler;
import fr.xpdustry.distributor.api.command.ArcCommandManager;
import fr.xpdustry.distributor.api.command.sender.CommandSender;
import fr.xpdustry.distributor.api.plugin.ExtendedPlugin;
import fr.xpdustry.distributor.api.util.MoreEvents;
import fr.xpdustry.router.command.RouterCommandManager;
import fr.xpdustry.router.commands.PlotCommands;
import fr.xpdustry.router.commands.StartCommand;
import fr.xpdustry.router.repository.SchematicRepository;
import fr.xpdustry.router.service.PlotManager;
import fr.xpdustry.router.service.SchematicService;
import java.io.File;
import mindustry.Vars;
import mindustry.game.EventType.PlayerJoin;
import mindustry.game.EventType.PlayerLeave;
import mindustry.gen.Call;

@SuppressWarnings("unused")
public final class RouterPlugin extends ExtendedPlugin {

    public static final String ROUTER_ACTIVE_KEY = "xpdustry-router:active";

    private final SchematicService schematics =
            SchematicService.simple(SchematicRepository.of(new File("./schematics.sqlite")));
    private final PlotManager plots = PlotManager.simple();
    private final ArcCommandManager<CommandSender> serverCommands = new RouterCommandManager(this);
    private final ArcCommandManager<CommandSender> clientCommands = new RouterCommandManager(this);

    public static boolean isActive() {
        return Vars.state.isPlaying() && Vars.state.rules.tags.getBool(ROUTER_ACTIVE_KEY);
    }

    @Override
    public void onInit() {
        this.addListener(new RouterRenderer(plots));
        Vars.netServer.admins.addActionFilter(new RouterFilter(plots));

        MoreEvents.subscribe(PlayerJoin.class, event -> {
            if (isActive()) {
                Call.infoMessage(
                        event.player.con(),
                        """
                        Welcome to [cyan]Xpdustry Router[],
                        A dedicated server for building and sharing [cyan]schematics[].
                        Check out the available commands with [cyan]/help[].

                        [gray]> The plugin is still in beta, you can suggest new features in the Xpdustry discord server at [blue]https://discord.xpdustry.fr[].[]
                        """);
            }
        });

        MoreEvents.subscribe(PlayerLeave.class, event -> {
            plots.findPlotsByOwner(event.player.uuid()).forEach(p -> p.setOwner(null));
            plots.findAllPlots().forEach(plot -> plot.removeMember(event.player.uuid()));
        });
    }

    @Override
    public void onServerCommandsRegistration(final CommandHandler handler) {
        this.serverCommands.initialize(handler);
        final var annotations = this.serverCommands.createAnnotationParser(CommandSender.class);
        annotations.parse(new StartCommand(this));
    }

    @Override
    public void onClientCommandsRegistration(final CommandHandler handler) {
        this.clientCommands.initialize(handler);
        final var annotations = this.clientCommands.createAnnotationParser(CommandSender.class);
        annotations.parse(new PlotCommands(this));
    }

    public PlotManager getPlotManager() {
        return plots;
    }

    public SchematicService getSchematicService() {
        return schematics;
    }
}
