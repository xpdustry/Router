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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import mindustry.game.Team;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.Tiles;
import mindustry.world.blocks.environment.Floor;

public class SimpleMapContext implements MapContext {

    private final List<Function<Tiles, Tiles>> actions = new ArrayList<>();

    @Override
    public void reset(final int width, final int height) {
        addFunction(tiles -> {
            final var newTiles = new Tiles(width, height);
            newTiles.fill();
            return newTiles;
        });
    }

    @Override
    public void forEachTile(final Consumer<Tile> action) {
        addConsumer(tiles -> tiles.forEach(action));
    }

    @Override
    public void setBlock(final int x, final int y, final Block block, final Team team) {
        addConsumer(tiles -> tiles.get(x, y).setBlock(block, team));
    }

    @Override
    public void setFloor(final int x, final int y, final Floor floor) {
        addConsumer(tiles -> tiles.get(x, y).setFloor(floor));
    }

    @Override
    public void setFloor(final int x, final int y, final int w, final int h, Floor floor) {
        addConsumer(tiles -> {
            for (int i = x; i < x + w; i++) {
                for (int j = y; j < y + h; j++) {
                    tiles.get(i, j).setFloor(floor);
                }
            }
        });
    }

    @Override
    public List<Function<Tiles, Tiles>> getActions() {
        return Collections.unmodifiableList(actions);
    }

    protected final void addFunction(final Function<Tiles, Tiles> function) {
        actions.add(function);
    }

    protected final void addConsumer(final Consumer<Tiles> consumer) {
        actions.add(tiles -> {
            consumer.accept(tiles);
            return tiles;
        });
    }
}
