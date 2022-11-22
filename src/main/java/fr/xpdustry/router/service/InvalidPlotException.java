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
import java.io.Serial;

public class InvalidPlotException extends Exception {

    @Serial
    private static final long serialVersionUID = -1671640589672425282L;

    private final Plot plot;

    public InvalidPlotException(final String message, final Plot plot) {
        super(message);
        this.plot = plot;
    }

    public InvalidPlotException(final String message, final Plot plot, final Throwable cause) {
        super(message, cause);
        this.plot = plot;
    }

    public Plot getPlot() {
        return plot;
    }
}
