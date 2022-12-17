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

import fr.xpdustry.router.model.PlotArea;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import mindustry.content.Blocks;
import mindustry.game.Team;
import mindustry.world.Tiles;
import mindustry.world.blocks.environment.Floor;

public final class SimplePlotMapGenerator implements MapGenerator<PlotMapGeneratorResult> {

    private static final int PLOT_QUARTER_X = 2;
    private static final int PLOT_QUARTER_Y = 2;
    private static final int PLOT_SIZE_X = 32;
    private static final int PLOT_SIZE_Y = 32;

    private static final int MAIN_ROAD_SIZE = Blocks.coreNucleus.size + 2;
    private static final int ROAD_SIZE = 4;
    private static final int BORDER_SIZE = 1;

    private static final int PLOT_TOTAL_SIZE_X = PLOT_SIZE_X + (BORDER_SIZE * 2);
    private static final int PLOT_QUARTER_SIZE_X = (ROAD_SIZE + PLOT_TOTAL_SIZE_X) * PLOT_QUARTER_X;
    private static final int PLOT_TOTAL_SIZE_Y = PLOT_SIZE_Y + (BORDER_SIZE * 2);
    private static final int PLOT_QUARTER_SIZE_Y = (ROAD_SIZE + PLOT_TOTAL_SIZE_Y) * PLOT_QUARTER_Y;

    private static final Floor BORDER_FLOOR = Blocks.darkPanel1.asFloor();
    private static final Floor PLOT_FLOOR = Blocks.metalFloor3.asFloor();
    private static final Floor ROAD_FLOOR = Blocks.dacite.asFloor();

    @Override
    public PlotMapGeneratorResult generate() {
        final var tiles = new Tiles(PLOT_QUARTER_SIZE_X * 2 + MAIN_ROAD_SIZE, PLOT_QUARTER_SIZE_Y * 2 + MAIN_ROAD_SIZE);
        final var areas = new ArrayList<PlotArea>();

        tiles.fill();
        tiles.forEach(t -> t.setFloor(ROAD_FLOOR));

        final var coreX = PLOT_QUARTER_SIZE_X + Math.floorDiv(MAIN_ROAD_SIZE, 2);
        final var coreY = PLOT_QUARTER_SIZE_Y + Math.floorDiv(MAIN_ROAD_SIZE, 2);
        tiles.get(coreX, coreY).setBlock(Blocks.coreNucleus, Team.sharded);

        for (int i = 0; i < 2; i++) { // QUARTER_X
            for (int j = 0; j < 2; j++) { // QUARTER_Y
                for (int k = 0; k < PLOT_QUARTER_X; k++) {
                    for (int l = 0; l < PLOT_QUARTER_Y; l++) {
                        // coords = QUARTER + ROAD + PLOT
                        final var x = ((PLOT_QUARTER_SIZE_X + MAIN_ROAD_SIZE) * i)
                                + (ROAD_SIZE * (k + 1 - i))
                                + (PLOT_TOTAL_SIZE_X * k);
                        final var y = ((PLOT_QUARTER_SIZE_Y + MAIN_ROAD_SIZE) * j)
                                + (ROAD_SIZE * (l + 1 - j))
                                + (PLOT_TOTAL_SIZE_Y * l);

                        areas.add(PlotArea.of(x + 1, y + 1, PLOT_SIZE_X, PLOT_SIZE_Y));
                        setFloors(tiles, x, y, PLOT_TOTAL_SIZE_X, PLOT_TOTAL_SIZE_Y, BORDER_FLOOR); // Outline
                        setFloors(tiles, x + 1, y + 1, PLOT_SIZE_X, PLOT_SIZE_Y, PLOT_FLOOR); // Internal
                    }
                }
            }
        }

        return new SimplePlotMapGeneratorResult(tiles, Collections.unmodifiableList(areas));
    }

    private void setFloors(final Tiles tiles, int x, int y, int width, int height, final Floor floor) {
        for (int i = x; i < x + width; i++) {
            for (int j = y; j < y + height; j++) {
                tiles.get(i, j).setFloor(floor);
            }
        }
    }

    private static final class SimplePlotMapGeneratorResult implements PlotMapGeneratorResult {

        private final Tiles tiles;
        private final List<PlotArea> areas;

        private SimplePlotMapGeneratorResult(final Tiles tiles, final List<PlotArea> areas) {
            this.tiles = tiles;
            this.areas = areas;
        }

        @Override
        public Tiles getTiles() {
            return tiles;
        }

        @Override
        public List<PlotArea> getAreas() {
            return areas;
        }
    }
}
