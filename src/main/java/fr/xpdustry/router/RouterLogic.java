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

import arc.Core;
import arc.math.geom.Point2;
import fr.xpdustry.distributor.api.plugin.PluginListener;
import fr.xpdustry.distributor.api.util.ArcList;
import fr.xpdustry.distributor.api.util.MoreEvents;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.net.Administration;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.distribution.ItemBridge;
import mindustry.world.blocks.distribution.MassDriver;
import mindustry.world.blocks.logic.LogicBlock;
import mindustry.world.blocks.payloads.PayloadMassDriver;
import mindustry.world.blocks.power.PowerNode;

public final class RouterLogic implements PluginListener {

    private final RouterPlugin router;

    public RouterLogic(final RouterPlugin router) {
        this.router = router;
    }

    @Override
    public void onPluginInit() {
        MoreEvents.subscribe(EventType.PlayerLeave.class, event -> {
            this.router.getPlotManager().findPlotsByOwner(event.player.uuid()).forEach(p -> p.setOwner(null));
            this.router.getPlotManager().findAllPlots().forEach(plot -> plot.removeMember(event.player.uuid()));
        });

        Vars.netServer.admins.addActionFilter(this::filterAction);
    }

    private boolean filterAction(final Administration.PlayerAction action) {
        if (!router.isActive()) {
            return true;
        }
        switch (action.type) {
            case placeBlock -> {
                final List<Point2> positions = new ArrayList<>();
                action.tile.getLinkedTilesAs(action.block, tile -> positions.add(Point2.unpack(tile.pos())));
                final var tile = action.tile;
                final var uuid = action.player.uuid();
                Core.app.post(() -> revertAutoConfigure(tile, uuid));
                return isAllInTrustedPlots(uuid, positions);
            }
            case breakBlock, rotate, withdrawItem, depositItem -> {
                return isAllInTrustedPlots(action.player.uuid(), List.of(Point2.unpack(action.tile.pos())));
            }
            case configure -> {
                final List<Point2> positions = new ArrayList<>();
                if (isLinkableBlock(action.tile.block())) {
                    getLinks(action.tile, action.config, positions::add);
                }
                positions.add(Point2.unpack(action.tile.pos()));
                return isAllInTrustedPlots(action.player.uuid(), positions);
            }
            default -> {
                return true;
            }
        }
    }

    private boolean isAllInTrustedPlots(final String player, final List<Point2> points) {
        final var plots = this.router.getPlotManager().findPlotsByTrusted(player);
        for (final var point : points) {
            if (plots.stream()
                    .noneMatch(plot ->
                            (point.x == -1 || point.y == -1) || plot.getArea().contains(point))) {
                return false;
            }
        }
        return true;
    }

    private void revertAutoConfigure(final Tile tile, final String player) {
        if (tile.build == null || !isLinkableBlock(tile.block())) {
            return;
        }

        final List<Point2> filtered = new ArrayList<>();
        final var plots = this.router.getPlotManager().findPlotsByTrusted(player);
        getLinks(tile, tile.build.config(), position -> {
            if (plots.stream().anyMatch(plot -> plot.getArea().contains(position))) {
                filtered.add(position);
            }
        });

        // Reconfigure only allowed links
        if (tile.build instanceof LogicBlock.LogicBuild build) {
            new ArcList<>(build.links).forEach(link -> build.configure(Point2.pack(link.x, link.y)));
        } else if (tile.build instanceof PowerNode.PowerNodeBuild build) {
            build.configure(new Point2[0]);
        } else if (tile.build instanceof ItemBridge.ItemBridgeBuild build) {
            build.configure(-1);
        } else if (tile.build instanceof MassDriver.MassDriverBuild build) {
            build.configure(-1);
        } else if (tile.build instanceof PayloadMassDriver.PayloadDriverBuild build) {
            build.configure(-1);
        }
        for (final var position : filtered) {
            tile.build.configure(position.pack());
        }
    }

    private boolean isLinkableBlock(final Block block) {
        return block instanceof LogicBlock
                || block instanceof PowerNode
                || block instanceof ItemBridge
                || block instanceof MassDriver
                || block instanceof PayloadMassDriver;
    }

    private void getLinks(final Tile tile, final Object config, final Consumer<Point2> consumer) {
        if (config instanceof Integer position) {
            consumer.accept(Point2.unpack(position));
        } else if (config instanceof Point2 position) {
            consumer.accept(new Point2(tile.x + position.x, tile.y + position.y));
        } else if (config instanceof Point2[] positions) {
            for (final var position : positions) {
                consumer.accept(new Point2(tile.x + position.x, tile.y + position.y));
            }
        }
    }
}
