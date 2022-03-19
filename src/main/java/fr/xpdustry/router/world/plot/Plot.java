package fr.xpdustry.router.world.plot;

public interface Plot {
  static Plot simple(final int x, final int y, final int width, final int height) {
    return new SimplePlot(x, y, width, height);
  }

  int getTileX();

  int getTileY();

  int getWidth();

  int getHeight();
}
