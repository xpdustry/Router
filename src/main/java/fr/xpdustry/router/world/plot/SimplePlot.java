package fr.xpdustry.router.world.plot;

final class SimplePlot implements Plot {
  private final int x;
  private final int y;
  private final int width;
  private final int height;

  public SimplePlot(final int x, final int y, final int width, final int height) {
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
  }

  @Override
  public int getTileX() {
    return x;
  }

  @Override
  public int getTileY() {
    return y;
  }

  @Override
  public int getWidth() {
    return width;
  }

  @Override
  public int getHeight() {
    return height;
  }
}
