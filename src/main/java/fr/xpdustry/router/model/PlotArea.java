/*
 * Router, a Reddit-like Mindustry plugin for sharing schematics.
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

import arc.math.geom.*;
import mindustry.*;
import org.jetbrains.annotations.*;

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

  public boolean contains(final float x, final float y) {
    return getX() <= x && getX() + getW() > x && getY() <= y && getY() + getH() > y;
  }

  public boolean contains(final @NotNull Position position) {
    return contains(position.getX(), position.getY());
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
}
