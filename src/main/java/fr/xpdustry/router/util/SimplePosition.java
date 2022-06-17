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
package fr.xpdustry.router.util;

import arc.math.geom.*;
import mindustry.*;
import org.jetbrains.annotations.*;

public final class SimplePosition implements Position {

  private final float x;
  private final float y;

  public static @NotNull SimplePosition of(final float x, final float y) {
    return new SimplePosition(x, y);
  }

  public static @NotNull SimplePosition of(final int pos) {
    return of(Point2.unpack(pos));
  }

  public static @NotNull SimplePosition of(final @NotNull Point2 point) {
    return new SimplePosition(point.x * Vars.tilesize, point.y * Vars.tilesize);
  }

  private SimplePosition(final float x, final float y) {
    this.x = x;
    this.y = y;
  }

  @Override
  public float getX() {
    return x;
  }

  @Override
  public float getY() {
    return y;
  }
}
