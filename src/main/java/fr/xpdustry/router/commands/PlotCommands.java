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
package fr.xpdustry.router.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import fr.xpdustry.distributor.api.command.sender.CommandSender;
import fr.xpdustry.router.RouterPlugin;
import fr.xpdustry.router.command.RequireOwnership;
import fr.xpdustry.router.model.Plot;
import fr.xpdustry.router.service.InvalidPlotException;
import mindustry.Vars;
import mindustry.gen.Groups;
import mindustry.gen.Player;

@CommandMethod("plots")
public final class PlotCommands {

    private static final long MAX_OWNED_PLOTS = 2;
    private final RouterPlugin router;

    public PlotCommands(final RouterPlugin router) {
        this.router = router;
    }

    @CommandMethod("claim <plot>")
    @CommandDescription("Claim a plot")
    public void onPlotClaim(final CommandSender sender, final @Argument("plot") Plot plot) {
        if (router.getPlotManager().countPlotsByOwner(sender.getPlayer().uuid()) == MAX_OWNED_PLOTS) {
            sender.sendMessage("The maximum number of owned plots is " + MAX_OWNED_PLOTS
                    + ", revoke one if you want to claim a new one.");
            return;
        }
        if (plot.isOwner(sender.getPlayer())) {
            sender.sendMessage("You have already claimed this plot.");
        } else if (plot.getOwner() != null && isPlayerOnline(plot.getOwner())) {
            sender.sendMessage("You can't claim this plot, it belongs someone online.");
        } else {
            plot.setOwner(sender.getPlayer().uuid());
            sender.sendMessage("You claimed the plot #" + plot.getId());
        }
    }

    @CommandMethod("revoke <plot>")
    @CommandDescription("Revoke a plot")
    public void onPlotRevoke(final CommandSender sender, final @RequireOwnership @Argument("plot") Plot plot) {
        plot.setOwner(null);
        sender.sendMessage("You revoked the plot #" + plot.getId() + ".");
    }

    @CommandMethod("clear <plot>")
    @CommandDescription("Clear a plot")
    public void onPlotClear(final CommandSender sender, final @RequireOwnership @Argument("plot") Plot plot) {
        plot.getArea().clear();
        sender.sendMessage("You cleared the plot #" + plot.getId() + ".");
    }

    @CommandMethod("publish <plot>")
    @CommandDescription("Publish a plot as a schematic.")
    public void onPlotPublish(final CommandSender sender, final @RequireOwnership @Argument("plot") Plot plot) {
        try {
            router.getSchematicService().publishSchematic(plot);
            sender.sendMessage("You published the plot #" + plot.getId() + ".");
        } catch (final InvalidPlotException e) {
            sender.sendWarning("Failed to publish the plot: " + e.getMessage());
        }
    }

    @CommandMethod("trust <plot> <player>")
    @CommandDescription("Trust a player to your plot.")
    public void onPlotTrust(
            final CommandSender sender,
            final @RequireOwnership @Argument("plot") Plot plot,
            final @Argument("player") Player player) {
        if (sender.getPlayer().uuid().equals(player.uuid())) {
            sender.sendMessage("You can't trust yourself.");
        } else if (plot.hasMember(player.uuid())) {
            sender.sendMessage("The player is already trusted on this plot.");
        } else {
            plot.addMember(player.uuid());
            sender.sendMessage("You trusted the player on the plot #" + plot.getId() + ".");
        }
    }

    @CommandMethod("untrust <plot> <player>")
    @CommandDescription("Untrust a player from your plot.")
    public void onPlotUntrust(
            final CommandSender sender,
            final @RequireOwnership @Argument("plot") Plot plot,
            final @Argument("player") Player player) {
        if (sender.getPlayer().uuid().equals(player.uuid())) {
            sender.sendMessage("You can't untrust yourself.");
        } else if (!plot.hasMember(player.uuid())) {
            sender.sendMessage("The player is not trusted on this plot.");
        } else {
            plot.removeMember(player.uuid());
            sender.sendMessage("You untrusted the player from the plot #" + plot.getId() + ".");
        }
    }

    @CommandMethod("info <plot>")
    @CommandDescription("Get information about a plot.")
    public void onPlotInfo(final CommandSender sender, final @Argument("plot") Plot plot) {
        final var builder =
                new StringBuilder(128).append("Plot #").append(plot.getId()).append(" information:\n");
        builder.append("Owner: ")
                .append(plot.getOwner() == null ? "None" : plot.getOwner())
                .append('\n');
        builder.append("Members: ");
        if (plot.getMembers().isEmpty()) {
            builder.append("\n[orange]No bitches ?");
        } else {
            for (final var member : plot.getMembers()) {
                final var info = Vars.netServer.admins.getInfo(member);
                builder.append("\n[orange] - [white]").append(info.lastName);
            }
        }
        sender.sendMessage(builder.toString());
    }

    private boolean isPlayerOnline(final String uuid) {
        return Groups.player.find(p -> p.uuid().equals(uuid)) != null;
    }
}
