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
package fr.xpdustry.router.map;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import mindustry.game.Team;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.Tiles;
import mindustry.world.blocks.environment.Floor;

public interface MapContext {

    void reset(final int width, final int height);

    void setBlock(final int x, final int y, final Block block, final Team team);

    void setFloor(final int x, final int y, final Floor floor);

    void setFloor(final int x, final int y, final int w, final int h, final Floor floor);

    void forEachTile(final Consumer<Tile> action);

    List<Function<Tiles, Tiles>> getActions();
}
