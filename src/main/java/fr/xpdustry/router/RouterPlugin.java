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
import fr.xpdustry.distributor.api.DistributorProvider;
import fr.xpdustry.distributor.api.command.ArcCommandManager;
import fr.xpdustry.distributor.api.command.sender.CommandSender;
import fr.xpdustry.distributor.api.localization.LocalizationSourceRegistry;
import fr.xpdustry.distributor.api.plugin.ExtendedPlugin;
import fr.xpdustry.router.command.RouterCommandManager;
import fr.xpdustry.router.commands.PlotCommands;
import fr.xpdustry.router.commands.StartCommand;
import fr.xpdustry.router.service.PlotManager;
import java.util.Locale;
import mindustry.Vars;

@SuppressWarnings("unused")
public final class RouterPlugin extends ExtendedPlugin {

    public static final String ROUTER_ACTIVE_KEY = "xpdustry-router:active";

    private final PlotManager plots = PlotManager.simple();
    private final ArcCommandManager<CommandSender> serverCommands = new RouterCommandManager(this);
    private final ArcCommandManager<CommandSender> clientCommands = new RouterCommandManager(this);

    @Override
    public void onInit() {
        final var registry = LocalizationSourceRegistry.create(Locale.ENGLISH);
        registry.registerAll(Locale.ENGLISH, "bundles/bundle", getClass().getClassLoader());
        DistributorProvider.get().getGlobalLocalizationSource().addLocalizationSource(registry);

        this.addListener(new RouterLogic(this));
        this.addListener(new RouterRenderer(this));
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

    public ArcCommandManager<CommandSender> getClientCommands() {
        return clientCommands;
    }

    public boolean isActive() {
        return Vars.state.isPlaying() && Vars.state.rules.tags.getBool(ROUTER_ACTIVE_KEY);
    }
}
