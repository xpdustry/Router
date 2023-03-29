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
package fr.xpdustry.router.command;

import cloud.commandframework.arguments.parser.ParserParameters;
import fr.xpdustry.distributor.api.command.ArcCommandManager;
import fr.xpdustry.distributor.api.command.sender.CommandSender;
import fr.xpdustry.router.RouterPlugin;
import fr.xpdustry.router.model.Plot;
import io.leangen.geantyref.TypeToken;
import java.util.function.Function;

// Temporary class to fix a dumb bug in Distributor
public final class RouterCommandManager extends ArcCommandManager<CommandSender> {

    public RouterCommandManager(final RouterPlugin plugin) {
        super(plugin, Function.identity(), Function.identity(), false);

        this.parserRegistry()
                .registerAnnotationMapper(
                        RequireOwnership.class,
                        (command, annotation) ->
                                ParserParameters.single(RouterParserParameters.REQUIRE_PLOT_OWNERSHIP, true));

        this.parserRegistry()
                .registerParserSupplier(
                        TypeToken.get(Plot.class),
                        params -> new PlotParser<>(
                                params.get(RouterParserParameters.REQUIRE_PLOT_OWNERSHIP, false),
                                ((RouterPlugin) this.getPlugin()).getPlotManager(),
                                getBackwardsCommandSenderMapper()));
    }
}
