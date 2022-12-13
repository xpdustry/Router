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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import mindustry.gen.Player;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class Plot {

    private final Set<String> members = new HashSet<>();
    private final PlotArea area;
    private final int id;
    private @Nullable String owner = null;

    Plot(final PlotArea area, int id) {
        this.area = area;
        this.id = id;
    }

    public static Plot of(final PlotArea area, final int id) {
        return new Plot(area, id);
    }

    public @Nullable String getOwner() {
        return owner;
    }

    public void setOwner(final @Nullable String owner) {
        this.owner = owner;
        if (owner != null) {
            clearMembers();
        }
    }

    public boolean isOwner(final String player) {
        return player.equals(owner);
    }

    public boolean isOwner(final Player player) {
        return player.uuid().equals(owner);
    }

    public boolean isTrusted(final String player) {
        return isOwner(player) || hasMember(player);
    }

    public boolean isTrusted(final Player player) {
        return isTrusted(player.uuid());
    }

    public PlotArea getArea() {
        return area;
    }

    public Collection<String> getMembers() {
        return Collections.unmodifiableCollection(members);
    }

    public void addMember(final String member) {
        members.add(member);
    }

    public boolean hasMember(final String member) {
        return members.contains(member);
    }

    public void removeMember(final String member) {
        members.remove(member);
    }

    public void clearMembers() {
        members.clear();
    }

    public int getId() {
        return id;
    }
}
