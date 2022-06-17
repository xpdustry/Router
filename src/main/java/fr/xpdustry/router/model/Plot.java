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
  private @Nullable PlotSchematic placeholder = null;

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
    this.owner = owner;
    if (owner != null) {
      setPlaceholder(null);
      clearMembers();
    }
  }

  public boolean isOwner(final @NotNull String player) {
    return player.equals(owner);
  }

  public boolean isOwner(final @NotNull Player player) {
    return player.uuid().equals(owner);
  }

  public boolean isTrusted(final @NotNull String player) {
    return isOwner(player) || hasMember(player);
  }

  public boolean isTrusted(final @NotNull Player player) {
    return isTrusted(player.uuid());
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

  public void clearMembers() {
    members.clear();
  }

  public @Nullable PlotSchematic getPlaceholder() {
    return placeholder;
  }

  public void setPlaceholder(final @Nullable PlotSchematic placeholder) {
    this.placeholder = placeholder;
    if (placeholder != null) {
      setOwner(null);
      final var x = (area.getTileW() - placeholder.getSchematic().width) / 2 + area.getTileX();
      final var y = (area.getTileH() - placeholder.getSchematic().height) / 2 + area.getTileY();
      placeholder.getSchematic().tiles.forEach(stile -> {
        final var tile = Vars.world.tile(stile.x + x, stile.y + y);
        Call.setTile(tile, stile.block, Vars.state.rules.defaultTeam, stile.rotation);
        tile.build.configure(stile.config);
      });
    }
  }
}
