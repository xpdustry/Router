package fr.xpdustry.router.world.generator;

import arc.struct.*;
import fr.xpdustry.router.world.plot.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.game.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;
import org.jetbrains.annotations.*;

final class SimpleMapGenerator implements MapGenerator {
  static final SimpleMapGenerator INSTANCE = new SimpleMapGenerator();

  private static final int PLOT_QUARTER_X = 6;
  private static final int PLOT_QUARTER_Y = 4;
  private static final int PLOT_SIZE_X = 32;
  private static final int PLOT_SIZE_Y = 32;

  private static final int MAIN_ROAD_SIZE = Blocks.coreNucleus.size + 2;
  private static final int ROAD_SIZE = 4;
  private static final int BORDER_SIZE = 1;

  private static final int PLOT_TOTAL_SIZE_X = PLOT_SIZE_X + (BORDER_SIZE * 2);
  private static final int PLOT_TOTAL_SIZE_Y = PLOT_SIZE_Y + (BORDER_SIZE * 2);

  private static final int PLOT_QUARTER_SIZE_X = (ROAD_SIZE + PLOT_TOTAL_SIZE_X) * PLOT_QUARTER_X;
  private static final int PLOT_QUARTER_SIZE_Y = (ROAD_SIZE + PLOT_TOTAL_SIZE_Y) * PLOT_QUARTER_Y;

  private static final Floor BORDER_FLOOR = Blocks.darkPanel1.asFloor();
  private static final Floor PLOT_FLOOR = Blocks.metalFloor3.asFloor();
  private static final Floor ROAD_FLOOR = Blocks.dacite.asFloor();

  private final Seq<Plot> plots = new Seq<>();

  private SimpleMapGenerator() {
  }

  @Override
  public int getMapWidth() {
    return PLOT_QUARTER_SIZE_X * 2 + MAIN_ROAD_SIZE;
  }

  @Override
  public int getMapHeight() {
    return PLOT_QUARTER_SIZE_Y * 2 + MAIN_ROAD_SIZE;
  }

  @Override
  public @NotNull Iterable<Plot> getPlots() {
    return plots;
  }

  @Override
  public void get(final @NotNull Tiles tiles) {
    tiles.fill();
    tiles.forEach(t -> t.setFloor(ROAD_FLOOR));

    final var coreX = PLOT_QUARTER_SIZE_X + Math.floorDiv(MAIN_ROAD_SIZE, 2);
    final var coreY = PLOT_QUARTER_SIZE_Y + Math.floorDiv(MAIN_ROAD_SIZE, 2);
    Vars.world.tile(coreX, coreY).setBlock(Blocks.coreNucleus, Team.sharded, 0);

    for (int i = 0; i < 2; i++) { // QUARTER_X
      for (int j = 0; j < 2; j++) { // QUARTER_Y
        for (int k = 0; k < PLOT_QUARTER_X; k++) {
          for (int l = 0; l < PLOT_QUARTER_Y; l++) {
            // coord = QUARTER + ROAD + PLOT
            final var x = ((PLOT_QUARTER_SIZE_X + MAIN_ROAD_SIZE) * i) + (ROAD_SIZE * (k + 1 - i)) + (PLOT_TOTAL_SIZE_X * k);
            final var y = ((PLOT_QUARTER_SIZE_Y + MAIN_ROAD_SIZE) * j) + (ROAD_SIZE * (l + 1 - j)) + (PLOT_TOTAL_SIZE_Y * l);

            plots.add(Plot.simple(x + 1, y + 1, PLOT_SIZE_X, PLOT_SIZE_Y));
            setFloors(x, y, PLOT_TOTAL_SIZE_X, PLOT_TOTAL_SIZE_Y, BORDER_FLOOR); // Outline
            setFloors(x + 1, y + 1, PLOT_SIZE_X, PLOT_SIZE_Y, PLOT_FLOOR);       // Internal
          }
        }
      }
    }
  }

  private static void setFloors(int x, int y, int width, int height, final @NotNull Floor floor) {
    for (int i = x; i < x + width; i++) {
      for (int j = y; j < y + height; j++) {
        Vars.world.tile(i, j).setFloor(floor);
      }
    }
  }
}
