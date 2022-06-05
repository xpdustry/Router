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
import java.util.*;
import mindustry.*;
import mindustry.gen.*;
import org.jetbrains.annotations.*;

public final class Plot {

  private final PlotArea area;
  private final Set<String> members = new HashSet<>();
  private @Nullable String owner = null;
  private @Nullable String lastOwner = null;
  private @Nullable PlotSchematic placeHolder = null;

  Plot(final @NotNull PlotArea area) {
    this.area = area;
  }

  public static @NotNull Plot of(final @NotNull PlotArea area) {
    return new Plot(area);
  }

  public int getId() {
    return Point2.pack(area.getTileX(), area.getTileY());
  }

  public @Nullable String getOwner() {
    return owner;
  }

  public void setOwner(final @Nullable String owner) {
    this.lastOwner = this.owner;
    this.owner = owner;
  }

  public @Nullable String getLastOwner() {
    return lastOwner;
  }

  public void setLastOwner(final @Nullable String lastOwner) {
    this.lastOwner = lastOwner;
  }

  public boolean isOwner(final @NotNull Player player) {
    return player.uuid().equals(owner);
  }

  public @NotNull PlotArea getArea() {
    return area;
  }

  public @NotNull Collection<String> getMembers() {
    return Collections.unmodifiableCollection(members);
  }

  public void addMember(final @NotNull String member) {
    members.add(member);
  }

  public boolean hasMember(final @NotNull String member) {
    return members.contains(member);
  }

  public void removeMember(final @NotNull String member) {
    members.remove(member);
  }

  public void clearData() {
    setOwner(null);
    setPlaceHolder(null);
    members.clear();
  }

  public void clearArea() {
    Vars.world.tiles.forEach(t -> {
      if (area.contains(t) && t.build != null) t.build.kill();
    });
  }

  public @Nullable PlotSchematic getPlaceHolder() {
    return placeHolder;
  }

  public void setPlaceHolder(final @Nullable PlotSchematic placeHolder) {
    this.placeHolder = placeHolder;
  }
}
