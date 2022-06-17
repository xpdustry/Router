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
package fr.xpdustry.router;

import arc.struct.*;
import arc.util.*;
import fr.xpdustry.router.exception.*;
import fr.xpdustry.router.map.*;
import fr.xpdustry.router.model.*;
import fr.xpdustry.router.service.*;
import java.util.*;
import java.util.stream.*;
import mindustry.*;
import mindustry.game.*;
import mindustry.gen.*;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.*;

/**
 * Hides the commands behind a class because I plan to move to Distributor 3.
 */
public final class RouterCommand {

  private static final long MAX_OWNED_PLOTS = 2;
  private final PlotService plots;
  private final SchematicService schematics;

  public RouterCommand(final @NotNull PlotService plots, final @NotNull SchematicService schematics) {
    this.plots = plots;
    this.schematics = schematics;
  }

  public void registerClientCommands(final @NotNull CommandHandler handler) {
    handler.<Player>register("plot-claim", "<id>", "Claim a plot.", (args, player) -> {
      if (plots.countPlotsByOwner(player.uuid()) == MAX_OWNED_PLOTS) {
        player.sendMessage("The maximum number of owned plots is " + MAX_OWNED_PLOTS + ", revoke one if you want to claim a new one.");
        return;
      }

      final var plot = plots.findPlotById(Strings.parseInt(args[0], -1)).orElse(null);
      if (plot == null) {
        player.sendMessage("The id is invalid.");
      } else if (plot.isOwner(player)) {
        player.sendMessage("You have already claimed this plot.");
      } else if (isPlayerOnline(plot.getOwner())) {
        player.sendMessage("You can't claim this plot, it belongs someone online.");
      } else {
        plot.setOwner(player.uuid());
        player.sendMessage("You claimed the plot #" + plot.getId());
      }
    });

    handler.<Player>register("plot-revoke", "<id>", "Revoke a plot.", (args, player) -> {
      final var plot = plots.findPlotById(Strings.parseInt(args[0], -1)).orElse(null);
      if (plot == null) {
        player.sendMessage("The id is invalid.");
      } else if (!plot.isOwner(player)) {
        player.sendMessage("You don't own the plot #" + args[0] + ".");
      } else {
        plot.setOwner(null);
        player.sendMessage("You revoked the plot #" + plot.getId() + ".");
      }
    });

    handler.<Player>register("plot-clear", "<id>", "Clear a plot.", (args, player) -> {
      final var plot = plots.findPlotById(Strings.parseInt(args[0], -1)).orElse(null);
      if (plot == null) {
        player.sendMessage("The id is invalid.");
      } else if (!plot.isOwner(player)) {
        player.sendMessage("You don't own the plot #" + args[0] + ".");
      } else {
        plot.getArea().clear();
        player.sendMessage("You cleared the plot #" + plot.getId());
      }
    });

    handler.<Player>register("plot-publish", "<id>", "Publish a schematic.", (args, player) -> {
      final Plot plot = plots.findPlotById(Strings.parseInt(args[0], -1)).orElse(null);
      if (plot == null) {
        player.sendMessage("The id is invalid.");
      } else {
        try {
          schematics.publishSchematic(plot);
          player.sendMessage("The schematic has been published.");
        } catch (final InvalidPlotException e) {
          player.sendMessage(e.getMessage());
        }
      }
    });

    handler.<Player>register("plot-members", "<id>", "List the members of one of your plot.", (args, player) -> {
      final var plot = plots.findPlotById(Strings.parseInt(args[0], -1)).orElse(null);
      if (plot == null) {
        player.sendMessage("The id is invalid.");
      } else if (!plot.isOwner(player)) {
        player.sendMessage("You don't own the plot #" + args[0] + ".");
      } else {
        final var builder = new StringBuilder();
        builder.append("Plot Members :");
        if (plot.getMembers().isEmpty()) {
          builder.append("\n[orange]No bitches ?");
        } else {
          plot.getMembers().forEach(member -> {
            builder.append("\n[orange] - [white]").append(Vars.netServer.admins.getInfo(member).lastName);
          });
        }
        player.sendMessage(builder.toString());
      }
    });

    handler.<Player>register("plot-members-add", "<id> <name...>", "Add a an online player to your plot.", (args, player) -> {
      final var plot = plots.findPlotById(Strings.parseInt(args[0], -1)).orElse(null);
      if (plot == null) {
        player.sendMessage("The id is invalid.");
      } else if (!plot.isOwner(player)) {
        player.sendMessage("You don't own the plot #" + args[0] + ".");
      } else {
        final var players = findPlayersByName(args[1]);
        if (players.isEmpty()) {
          player.sendMessage("Player not found.");
        } else if (players.size() > 1) {
          player.sendMessage("Too many matches, be more precise please.");
        } else if (players.get(0) == player) {
          player.sendMessage("You can't add yourself >:(");
        } else {
          plot.addMember(players.get(0).uuid());
          player.sendMessage("Added " + players.get(0).name());
        }
      }
    });

    handler.<Player>register("plot-members-remove", "<name>", "Remove a member from your plot.", (args, player) -> {
      final var plot = plots.findPlotById(Strings.parseInt(args[0], -1)).orElse(null);
      if (plot == null) {
        player.sendMessage("The id is invalid.");
      } else if (!plot.isOwner(player)) {
        player.sendMessage("You don't own the plot #" + args[1] + ".");
      } else {
        final var players = findPlayersByName(args[0]);
        if (players.isEmpty()) {
          player.sendMessage("Player not found.");
        } else if (players.size() > 1) {
          player.sendMessage("Too many matches, be more precise please.");
        } else if (players.get(0) == player) {
          player.sendMessage("You can't add yourself >:(");
        } else {
          plot.removeMember(players.get(0).uuid());
          player.sendMessage("Added " + players.get(0).name());
        }
      }
    });
  }

  public void registerServerCommands(final @NotNull CommandHandler handler) {
    handler.register("router", "Begin hosting with the router gamemode.", args -> {
      try (final var loader = new MapLoader()) {
        final var generator = PlotMapGenerator.simple();
        loader.load(generator);
        Vars.state.rules = createRouterRules();
        plots.setPlotAreas(generator.getAreas());

        final var placeholders = schematics.getLatestSchematics(plots.countPlots()).iterator();
        final var available = Seq.with(plots.findAllPlots());

        while (placeholders.hasNext() && !available.isEmpty()) {
          final var placeholder = placeholders.next();
          final var plot = available.random();
          available.remove(plot, true);

          if (placeholder.getSchematic().width <= plot.getArea().getTileW() && placeholder.getSchematic().height <= plot.getArea().getTileH()) {
            plot.setPlaceholder(placeholder);
          }
        }
      }
    });
  }

  private Optional<Player> findPlayerByUuid(final @Nullable String uuid) {
    return Optional.ofNullable(Groups.player.find(p -> p.uuid().equals(uuid)));
  }

  private boolean isPlayerOnline(final @Nullable String uuid) {
    return findPlayerByUuid(uuid).isPresent();
  }

  /**
   * El famoso findp.
   */
  private List<Player> findPlayersByName(final String name) {
    return StreamSupport.stream(Groups.player.spliterator(), false)
      .filter(p -> Strings.stripColors(p.name()).contains(name))
      .toList();
  }

  private Rules createRouterRules() {
    final var rules = new Rules();
    Gamemode.sandbox.apply(rules);
    rules.modeName = "[orange]Router";
    rules.tags.put(RouterPlugin.ROUTER_ACTIVE_KEY, "true");
    rules.unitBuildSpeedMultiplier = Float.MIN_VALUE;
    rules.damageExplosions = false;
    rules.reactorExplosions = false;
    return rules;
  }
}
