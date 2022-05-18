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

import arc.*;
import arc.util.*;
import fr.xpdustry.router.graphics.*;
import fr.xpdustry.router.map.*;
import fr.xpdustry.router.plot.*;
import fr.xpdustry.router.service.*;
import java.util.*;
import mindustry.*;
import mindustry.game.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.mod.*;
import org.jetbrains.annotations.*;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"unused", "NullAway.Init"})
public final class RouterPlugin extends Plugin {

  private static final String ROUTER_ACTIVE_KEY = "xpdustry-router:active";

  private final PlotService service = PlotService.simple();
  private final Interval timer = new Interval();

  @Override
  public void init() {
    Vars.netServer.admins.addActionFilter(action -> !isActive() || service.canModifyTile(action));

    Events.run(Trigger.update, () -> {
      if (isActive() && timer.get(Time.toSeconds)) service.renderPlots();
    });

    Events.on(PlayerLeave.class, e -> {
      service.findPlotByOwner(e.player.uuid()).ifPresent(Plot::reset);
      service.findAllPlots().forEach(plot -> plot.removeMember(e.player.uuid()));
    });
  }

  @Override
  public void registerServerCommands(final @NotNull CommandHandler handler) {
    handler.register("router", "Begin hosting with the router gamemode.", args -> {
      try (final var loader = new MapLoader()) {
        final var generator = MapGenerator.simple();
        loader.generate(generator.getMapWidth(), generator.getMapHeight(), generator);

        Gamemode.sandbox.apply(Vars.state.rules);
        Vars.state.rules.modeName = "[orange]Router";
        Vars.state.rules.tags.put(ROUTER_ACTIVE_KEY, "true");

        service.setPlotAreas(generator.getPlots());
      }
    });
  }

  @Override
  public void registerClientCommands(CommandHandler handler) {
    handler.<Player>register("router-claim", "<id>", "Claim a plot.", (args, player) -> {
      if (service.findPlotByOwner(player.uuid()).isPresent()) {
        player.sendMessage("You already own a plot, revoke it first");
        return;
      }

      service.findPlotById(Strings.parseInt(args[0], -1)).ifPresentOrElse(
        plot -> {
          if (isPlayerOnline(plot.getOwner())) {
            player.sendMessage("You can't claim this plot, it belongs someone online.");
          } else {
            plot.reset();
            plot.setOwner(player.uuid());
            player.sendMessage("You claimed the plot #" + plot.getId());
          }
        },
        () -> player.sendMessage("The id is invalid.")
      );
    });

    handler.<Player>register("router-revoke", "Revoke your current plot.", (args, player) -> {
      service.findPlotByOwner(player.uuid()).ifPresentOrElse(
        plot -> {
          plot.reset();
          player.sendMessage("You revoked the plot #" + plot.getId());
        },
        () -> player.sendMessage("You don't have a plot.")
      );
    });

    handler.<Player>register("router-members", "List the members of your plot.", (args, player) -> {
      service.findPlotByOwner(player.uuid()).ifPresentOrElse(
        plot -> {
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
        },
        () -> player.sendMessage("You don't have a plot.")
      );
    });

    handler.<Player>register("router-members-add", "<name>", "Add a an online player to your plot.", (args, player) -> {
      service.findPlotByOwner(player.uuid()).ifPresentOrElse(
        plot -> {
          findPlayerByName(args[0]).ifPresentOrElse(
            member -> {
              if (member.uuid().equals(player.uuid())) {
                player.sendMessage("You can't add yourself >:(");
              } else {
                plot.addMember(member.uuid());
                player.sendMessage("Added " + member.name());
              }
            },
            () -> player.sendMessage("Player not found.")
          );
        },
        () -> player.sendMessage("You don't have a plot.")
      );
    });

    handler.<Player>register("router-members-remove", "<name>", "Remove a member from your plot.", (args, player) -> {
      service.findPlotByOwner(player.uuid()).ifPresentOrElse(
        plot -> {
          findPlayerByName(args[0]).ifPresentOrElse(
            member -> {
              if (member.uuid().equals(player.uuid())) {
                player.sendMessage("You can't remove yourself >:(");
              } else {
                plot.removeMember(member.uuid());
                player.sendMessage("Removed " + member.name());
              }
            },
            () -> player.sendMessage("Player not found.")
          );
        },
        () -> player.sendMessage("You don't have a plot.")
      );
    });
  }

  private boolean isActive() {
    return Vars.state.isPlaying() && Vars.state.rules.tags.getBool(ROUTER_ACTIVE_KEY) && !Vars.state.gameOver;
  }

  private Optional<Player> findPlayerByUuid(final @Nullable String uuid) {
    return Optional.ofNullable(Groups.player.find(p -> p.uuid().equals(uuid)));
  }

  private boolean isPlayerOnline(final @Nullable String uuid) {
    return findPlayerByUuid(uuid).isPresent();
  }

  private Optional<Player> findPlayerByName(final @Nullable String name) {
    return Optional.ofNullable(
      Groups.player.find(p -> Strings.stripColors(p.name()).equals(name) || Strings.stripColors(Vars.netServer.admins.getInfo(p.uuid()).lastName).equals(name))
    );
  }
}
