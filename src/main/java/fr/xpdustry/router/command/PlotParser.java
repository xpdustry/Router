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

import arc.util.Strings;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.captions.Caption;
import cloud.commandframework.captions.CaptionVariable;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import cloud.commandframework.exceptions.parsing.ParserException;
import fr.xpdustry.distributor.api.command.sender.CommandSender;
import fr.xpdustry.router.model.Plot;
import fr.xpdustry.router.service.PlotManager;
import java.io.Serial;
import java.util.Queue;
import java.util.function.Function;

public final class PlotParser<C> implements ArgumentParser<C, Plot> {

    private final boolean requireOwnership;
    private final PlotManager manager;
    private final Function<C, CommandSender> backwardsSenderMapper;

    public PlotParser(
            final boolean requireOwnership,
            final PlotManager manager,
            final Function<C, CommandSender> backwardsSenderMapper) {
        this.requireOwnership = requireOwnership;
        this.manager = manager;
        this.backwardsSenderMapper = backwardsSenderMapper;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public ArgumentParseResult<Plot> parse(final CommandContext<C> ctx, final Queue<String> inputQueue) {
        final var input = inputQueue.peek();
        if (input == null) {
            return ArgumentParseResult.failure(new NoInputProvidedException(this.getClass(), ctx));
        }
        final var id = Strings.parseInt(input, -1);
        if (id == -1) {
            return ArgumentParseResult.failure(new PlotNotFoundException(this.getClass(), ctx, input));
        }
        final var result = manager.findPlotById(id);
        if (result.isEmpty()) {
            return ArgumentParseResult.failure(new PlotNotFoundException(this.getClass(), ctx, input));
        }
        if (requireOwnership
                && !result.get()
                        .isOwner(this.backwardsSenderMapper
                                .apply(ctx.getSender())
                                .getPlayer())) {
            return ArgumentParseResult.failure(new PlotNotOwnedException(this.getClass(), ctx, input));
        }
        inputQueue.remove();
        return ArgumentParseResult.success(result.get());
    }

    @Override
    public boolean isContextFree() {
        return true;
    }

    public static class PlotParseException extends ParserException {

        @Serial
        private static final long serialVersionUID = 6198962637800022691L;

        private final String input;

        public PlotParseException(
                final Class<? extends PlotParser> clazz,
                final CommandContext<?> context,
                final Caption caption,
                final String input) {
            super(clazz, context, caption, CaptionVariable.of("input", input));
            this.input = input;
        }

        public final String getInput() {
            return input;
        }
    }

    public static final class PlotNotFoundException extends PlotParseException {

        @Serial
        private static final long serialVersionUID = 4867520399130787558L;

        private static final Caption PLOT_NOT_FOUND_FAILURE_CAPTION =
                Caption.of("argument.parse.failure.plot.not_found");

        public PlotNotFoundException(
                final Class<? extends PlotParser> clazz, final CommandContext<?> context, final String input) {
            super(clazz, context, PLOT_NOT_FOUND_FAILURE_CAPTION, input);
        }
    }

    public static final class PlotNotOwnedException extends PlotParseException {

        @Serial
        private static final long serialVersionUID = -5527083978016166839L;

        private static final Caption PLOT_NOT_OWNED_FAILURE_CAPTION =
                Caption.of("argument.parse.failure.plot.not_owned");

        public PlotNotOwnedException(
                final Class<? extends PlotParser> clazz, final CommandContext<?> context, final String input) {
            super(clazz, context, PLOT_NOT_OWNED_FAILURE_CAPTION, input);
        }
    }
}
