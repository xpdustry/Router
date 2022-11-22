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
package fr.xpdustry.router.model;

import arc.math.geom.Position;
import arc.struct.IntSet;
import arc.struct.Seq;
import arc.struct.StringMap;
import java.util.ArrayList;
import java.util.Objects;
import mindustry.Vars;
import mindustry.game.Schematic;
import mindustry.game.Schematic.Stile;
import mindustry.world.blocks.ConstructBlock.ConstructBuild;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class PlotArea implements Position {

    private final int x;
    private final int y;
    private final int w;
    private final int h;

    private PlotArea(final int x, final int y, final int w, final int h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public static PlotArea of(final int x, final int y, final int w, final int h) {
        return new PlotArea(x, y, w, h);
    }

    public int getTileX() {
        return x;
    }

    @Override
    public float getX() {
        return x * Vars.tilesize;
    }

    public int getTileY() {
        return y;
    }

    @Override
    public float getY() {
        return y * Vars.tilesize;
    }

    public int getTileW() {
        return w;
    }

    public float getW() {
        return w * Vars.tilesize;
    }

    public int getTileH() {
        return h;
    }

    public float getH() {
        return h * Vars.tilesize;
    }

    public boolean contains(final int x, final int y) {
        return getTileX() <= x && getTileX() + getTileW() > x && getTileY() <= y && getTileY() + getTileH() > y;
    }

    public boolean contains(final Position position) {
        return getX() <= position.getX()
                && getX() + getW() > position.getX()
                && getY() <= position.getY()
                && getY() + getH() > position.getY();
    }

    public @Nullable Schematic toSchematic() {
        int x1 = Integer.MAX_VALUE;
        int y1 = Integer.MAX_VALUE;
        int x2 = Integer.MIN_VALUE;
        int y2 = Integer.MIN_VALUE;
        var empty = true;

        for (int tx = getTileX(); tx < getTileX() + getTileW(); tx++) {
            for (int ty = getTileY(); ty < getTileY() + getTileH(); ty++) {
                final var build = Vars.world.build(tx, ty);

                if (build != null) {
                    final var block = build instanceof ConstructBuild cons ? cons.current : build.block;
                    final int top = block.size / 2;
                    final int bot = block.size % 2 == 1 ? -block.size / 2 : -(block.size - 1) / 2;

                    x1 = Math.min(build.tileX() + bot, x1);
                    y1 = Math.min(build.tileY() + bot, y1);
                    x2 = Math.max(build.tileX() + top, x2);
                    y2 = Math.max(build.tileY() + top, y2);
                    empty = false;
                }
            }
        }

        if (empty) {
            return null;
        } else {
            final var counted = new IntSet();
            final var tiles = new ArrayList<Stile>();

            for (int x = getTileX(); x < getTileX() + getTileW(); x++) {
                for (int y = getTileY(); y < getTileY() + getTileH(); y++) {
                    final var build = Vars.world.build(x, y);

                    if (build != null && !counted.contains(build.pos())) {
                        final var block = build instanceof ConstructBuild cons ? cons.current : build.block;
                        final var config = build instanceof ConstructBuild cons ? cons.lastConfig : build.config();
                        tiles.add(new Stile(
                                block, build.tileX() - x1, build.tileY() - y1, config, (byte) build.rotation));
                        counted.add(build.pos());
                    }
                }
            }

            return new Schematic(Seq.with(tiles), new StringMap(), x2 - x1 + 1, y2 - y1 + 1);
        }
    }

    public void clear() {
        Vars.world.tiles.forEach(t -> {
            if (contains(t) && t.build != null) {
                t.build.kill();
            }
        });
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) {
            return true;
        } else {
            return (o instanceof PlotArea area) && (x == area.x) && (y == area.y) && (w == area.w) && (h == area.h);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, w, h);
    }
}
