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
package fr.xpdustry.router;

import arc.math.geom.Point2;
import arc.math.geom.Position;
import fr.xpdustry.router.model.Plot;
import fr.xpdustry.router.service.PlotManager;
import fr.xpdustry.router.util.SimplePosition;
import java.util.ArrayList;
import java.util.List;
import mindustry.net.Administration.ActionFilter;
import mindustry.net.Administration.PlayerAction;
import mindustry.world.Block;
import mindustry.world.blocks.distribution.ItemBridge;
import mindustry.world.blocks.distribution.MassDriver;
import mindustry.world.blocks.logic.LogicBlock;
import mindustry.world.blocks.payloads.PayloadMassDriver;
import mindustry.world.blocks.power.PowerNode;

public final class RouterFilter implements ActionFilter {

    private final PlotManager plots;

    public RouterFilter(final PlotManager plots) {
        this.plots = plots;
    }

    @Override
    public boolean allow(final PlayerAction action) {
        if (RouterPlugin.isActive()) {
            final List<Position> positions = new ArrayList<>();

            switch (action.type) {
                case placeBlock -> action.tile.getLinkedTilesAs(action.block, positions::add);
                case breakBlock, rotate, withdrawItem, depositItem -> positions.add(action.tile);
                case configure -> {
                    if (isLinkableBlock(action.tile.block())) {
                        if (action.config instanceof Integer pos) {
                            positions.add(SimplePosition.of(pos));
                        } else if (action.config instanceof Point2 point) {
                            positions.add(SimplePosition.of(point));
                        } else if (action.config instanceof Point2[] points) {
                            for (final var point : points) {
                                positions.add(SimplePosition.of(point));
                            }
                        }
                    } else {
                        positions.add(action.tile);
                    }
                }
                default -> {
                    return true;
                }
            }

            return plots.findAllPlots().stream()
                    .filter(p -> p.isTrusted(action.player.uuid()))
                    .map(Plot::getArea)
                    .anyMatch(a -> positions.stream().allMatch(a::contains));
        }
        return true;
    }

    private boolean isLinkableBlock(final Block block) {
        return block instanceof LogicBlock
                || block instanceof PowerNode
                || block instanceof ItemBridge
                || block instanceof MassDriver
                || block instanceof PayloadMassDriver;
    }
}
