package fr.xpdustry.router.command;

import arc.util.*;
import fr.xpdustry.router.service.*;
import java.util.*;
import java.util.stream.*;
import mindustry.*;
import mindustry.gen.*;
import org.jetbrains.annotations.*;
import org.jetbrains.annotations.ApiStatus.*;
import org.jetbrains.annotations.Nullable;

@Internal
public final class VanillaRouterCommand implements RouterCommand {

  private static final long MAX_OWNED_PLOTS = 1;
  private final PlotService service;

  public VanillaRouterCommand(final @NotNull PlotService service) {
    this.service = service;
  }

  @Override
  public void registerCommands(@NotNull CommandHandler handler) {
    handler.<Player>register("router-claim", "<id>", "Claim a plot.", (args, player) -> {
      if (service.countPlotsByOwner(player.uuid()) == MAX_OWNED_PLOTS) {
        player.sendMessage("The maximum number of owned plots is " + MAX_OWNED_PLOTS + ", revoke one if you want to claim a new one.");
        return;
      }

      final var plot = service.findPlotById(Strings.parseInt(args[0], -1)).orElse(null);
      if (plot == null) {
        player.sendMessage("The id is invalid.");
      } else if (plot.isOwner(player)) {
        player.sendMessage("You have already claimed this plot.");
      } else if (isPlayerOnline(plot.getOwner())) {
        player.sendMessage("You can't claim this plot, it belongs someone online.");
      } else {
        plot.clearData();
        plot.clearArea();
        plot.setOwner(player.uuid());
        player.sendMessage("You claimed the plot #" + plot.getId());
      }
    });

    handler.<Player>register("router-revoke", "<id>", "Revoke a plot.", (args, player) -> {
      final var plot = service.findPlotById(Strings.parseInt(args[0], -1)).orElse(null);
      if (plot == null) {
        player.sendMessage("The id is invalid.");
      } else if (!plot.isOwner(player)) {
        player.sendMessage("You don't own the plot #" + args[0] + ".");
      } else {
        plot.clearData();
        player.sendMessage("You revoked the plot #" + plot.getId() + ".");
      }
    });

    handler.<Player>register("router-members", "<id>", "List the members of one of your plot.", (args, player) -> {
      final var plot = service.findPlotById(Strings.parseInt(args[0], -1)).orElse(null);
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

    handler.<Player>register("router-members-add", "<id> <name...>", "Add a an online player to your plot.", (args, player) -> {
      final var plot = service.findPlotById(Strings.parseInt(args[0], -1)).orElse(null);
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

    handler.<Player>register("router-members-remove", "<name>", "Remove a member from your plot.", (args, player) -> {
      final var plot = service.findPlotById(Strings.parseInt(args[0], -1)).orElse(null);
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

  private Optional<Player> findPlayerByUuid(final @Nullable String uuid) {
    return Optional.ofNullable(Groups.player.find(p -> p.uuid().equals(uuid)));
  }

  private boolean isPlayerOnline(final @Nullable String uuid) {
    return findPlayerByUuid(uuid).isPresent();
  }

  /** El famoso findp. */
  private List<Player> findPlayersByName(final @NotNull String name) {
    return StreamSupport.stream(Groups.player.spliterator(), false)
      .filter(p -> Strings.stripColors(p.name()).contains(name))
      .toList();
  }
}
