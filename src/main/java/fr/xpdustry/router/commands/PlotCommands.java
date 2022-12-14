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
import mindustry.Vars;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;

@CommandMethod("plot")
public final class PlotCommands {

    private static final long MAX_OWNED_PLOTS = 2;
    private final RouterPlugin router;

    public PlotCommands(final RouterPlugin router) {
        this.router = router;
    }

    @CommandMethod("help")
    @CommandDescription("Show help command for plots.")
    public void onPlotHelp(final CommandSender sender) {
        // TODO Workaround until I figure out something good with Distributor
        final var builder = new StringBuilder();
        builder.append("[orange]Plot commands:[]");
        router.getClientCommands().createCommandHelpHandler().getAllCommands().stream()
                .filter(entry -> entry.getSyntaxString().startsWith("plot"))
                .forEach(entry -> builder.append("\n[cyan]")
                        .append(entry.getSyntaxString())
                        .append("[]\n > ")
                        .append(entry.getDescription()));
        sender.sendMessage(builder.toString());
    }

    @CommandMethod("claim <plot>")
    @CommandDescription("Claim a plot for editing.")
    public void onPlotClaim(final CommandSender sender, final @Argument("plot") Plot plot) {
        if (router.getPlotManager().countPlotsByOwner(sender.getPlayer().uuid()) == MAX_OWNED_PLOTS) {
            sender.sendMessage("The maximum number of owned plots is " + MAX_OWNED_PLOTS
                    + ", revoke one if you want to claim a new one.");
            return;
        }
        if (plot.isOwner(sender.getPlayer().uuid())) {
            sender.sendMessage("You have already claimed this plot.");
        } else if (plot.getOwner() != null && isPlayerOnline(plot.getOwner())) {
            sender.sendMessage("You can't claim this plot, it belongs someone online.");
        } else {
            plot.setOwner(sender.getPlayer().uuid());
            Call.sendMessage("[accent]Plot " + plot.getId() + " has been claimed by " + sender.getPlayer().name + ".");
        }
    }

    @CommandMethod("unclaim <plot>")
    @CommandDescription("Unclaim a plot you own.")
    public void onPlotRevoke(final CommandSender sender, final @RequireOwnership @Argument("plot") Plot plot) {
        plot.setOwner(null);
        Call.sendMessage("[accent]Plot " + plot.getId() + " has been unclaimed by " + sender.getPlayer().name + ".");
    }

    @CommandMethod("clear <plot>")
    @CommandDescription("Clear a plot")
    public void onPlotClear(final CommandSender sender, final @RequireOwnership @Argument("plot") Plot plot) {
        plot.getArea().clear();
        Call.sendMessage("[accent]Plot " + plot.getId() + " has been cleared by " + sender.getPlayer().name + ".");
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
            player.sendMessage("You have been trusted on the plot #" + plot.getId() + " by "
                    + sender.getPlayer().name() + ".");
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
            player.sendMessage("You have been untrusted from the plot #" + plot.getId() + " by "
                    + sender.getPlayer().name() + ".");
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

    @CommandMethod("share <plot>")
    @CommandDescription("Share the plot as a schematic with a link.")
    public void onPlotShare(final CommandSender sender, final @RequireOwnership @Argument("plot") Plot plot) {
        final var schematic = plot.getArea().getSchematic();
        if (schematic == null) {
            sender.sendWarning("Your plot is empty.");
            return;
        }

        schematic.tags.put("name", sender.getPlayer().plainName() + "'s schematic");
        router.getSharing().upload(schematic).whenComplete((uri, throwable) -> {
            if (throwable != null) {
                sender.sendWarning("Failed to generate a link for the schematic. Please notify the server owners.");
            } else {
                Call.openURI(sender.getPlayer().con(), uri.toString());
            }
        });
    }

    private boolean isPlayerOnline(final String uuid) {
        return Groups.player.find(p -> p.uuid().equals(uuid)) != null;
    }
}
