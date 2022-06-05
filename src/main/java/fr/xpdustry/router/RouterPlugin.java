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
import arc.struct.*;
import arc.util.*;
import fr.xpdustry.router.command.*;
import fr.xpdustry.router.map.*;
import fr.xpdustry.router.model.*;
import fr.xpdustry.router.repository.*;
import fr.xpdustry.router.service.*;
import java.io.*;
import java.util.*;
import mindustry.*;
import mindustry.game.EventType.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.mod.*;
import mindustry.net.Administration.*;
import mindustry.world.*;
import org.jetbrains.annotations.*;

@SuppressWarnings("unused")
public final class RouterPlugin extends Plugin {

  private static final String ROUTER_ACTIVE_KEY = "xpdustry-router:active";

  private final PlotService plots = PlotService.simple();
  private final Interval timer = new Interval();
  private final SchematicService schematics = SchematicService.simple(
    SchematicRepository.of(new File("./schematics.sqlite"))
  );
  private final RouterCommand command = new VanillaRouterCommand(plots, schematics);

  @Override
  public void init() {
    Vars.netServer.admins.addActionFilter(action -> {
      if (isActive() && action.tile != null) {
        final var tiles = new ArrayList<Tile>();
        if (action.type == ActionType.placeBlock && action.block.isMultiblock()) {
          int size = action.block.size;
          int offset = -(size - 1) / 2;
          for (int dx = 0; dx < size; dx++) {
            for (int dy = 0; dy < size; dy++) {
              final var other = Vars.world.tile(action.tile.x + dx + offset, action.tile.y + dy + offset);
              if (other != null) tiles.add(other);
            }
          }
        } else {
          tiles.add(action.tile);
        }

        return plots.findAllPlots().stream()
          .filter(p -> p.getOwner() != null && (p.isOwner(action.player) || p.hasMember(action.player.uuid())))
          .anyMatch(p -> tiles.stream().allMatch(t -> p.getArea().contains(t)));
      }
      return true;
    });

    Events.run(Trigger.update, () -> {
      if (isActive() && timer.get(Time.toSeconds)) {
        for (final var plot : plots.findAllPlots()) {
          Call.label(
            "Plot #" + plot.getId(),
            1F,
            plot.getArea().getX() + (plot.getArea().getW() / 2F),
            plot.getArea().getY() + (plot.getArea().getH() / 2F)
          );

          if (plot.getPlaceHolder() != null) {
            Call.label(
              "[yellow]" + Strings.stripColors(Vars.netServer.admins.getInfo(plot.getPlaceHolder().getAuthor()).lastName) + "'s schematic",
              1F,
              plot.getArea().getX() + (plot.getArea().getW() / 2F),
              plot.getArea().getY()
            );
          } else if (plot.getOwner() != null) {
            Call.label(
              "[green]" + Strings.stripColors(Vars.netServer.admins.getInfo(plot.getOwner()).lastName) + "'s plot",
              1F,
              plot.getArea().getX() + (plot.getArea().getW() / 2F),
              plot.getArea().getY() + plot.getArea().getH()
            );
          } else if (plot.getLastOwner() != null) {
            Call.label(
              "[gray]" + Strings.stripColors(Vars.netServer.admins.getInfo(plot.getLastOwner()).lastName) + "'s build",
              1F,
              plot.getArea().getX() + (plot.getArea().getW() / 2F),
              plot.getArea().getY() + plot.getArea().getH()
            );
          }
        }
      }
    });

    // TODO Remove Xpdustry branding when schematics management is finished
    Events.on(PlayerJoin.class, e -> {
      if (isActive()) {
        Call.infoMessage(e.player.con(), """
          Welcome to [cyan]Xpdustry Router[],
          A dedicated server for building and sharing [cyan]schematics[].
          Check out the available commands with [cyan]/help[].
                                 
          [gray]> The plugin is still in beta, you can suggest new features in the Xpdustry discord server at [blue]https://discord.xpdustry.fr[].[]
          """
        );
      }
    });

    Events.on(PlayerLeave.class, e -> {
      plots.findPlotsByOwner(e.player.uuid()).forEach(Plot::clearData);
      plots.findAllPlots().forEach(plot -> plot.removeMember(e.player.uuid()));
    });
  }

  @Override
  public void registerServerCommands(final @NotNull CommandHandler handler) {
    handler.register("router", "Begin hosting with the router gamemode.", args -> {
      try (final var loader = new MapLoader()) {
        final var generator = MapGenerator.simple();
        loader.generate(generator.getMapWidth(), generator.getMapHeight(), generator);
        Vars.state.rules = createRouterRules();
        plots.setPlotAreas(generator.getPlots());

        final var placeHolders = schematics.getLatestSchematics(plots.countPlots()).iterator();
        final var availablePlots = Seq.with(plots.findAllPlots());

        while (placeHolders.hasNext() && !availablePlots.isEmpty()) {
          final var placeHolder = placeHolders.next();
          final var plot = availablePlots.random();
          availablePlots.remove(plot, true);

          if (placeHolder.getSchematic().width <= plot.getArea().getTileW() && placeHolder.getSchematic().height <= plot.getArea().getTileH()) {
            plot.setPlaceHolder(placeHolder);
            final var area = plot.getArea();
            final var x = (area.getTileW() - placeHolder.getSchematic().width) / 2 + area.getTileX();
            final var y = (area.getTileH() - placeHolder.getSchematic().height) / 2 + area.getTileY();
            placeHolder.getSchematic().tiles.forEach(stile -> {
              final var tile = Vars.world.tile(stile.x + x, stile.y + y);
              Call.setTile(tile, stile.block, Vars.state.rules.defaultTeam, stile.rotation);
              tile.build.configure(stile.config);
            });
          }
        }
      }
    });
  }

  @Override
  public void registerClientCommands(final @NotNull CommandHandler handler) {
    command.registerCommands(handler);
  }

  private @NotNull Rules createRouterRules() {
    final var rules = new Rules();
    Gamemode.sandbox.apply(rules);
    rules.modeName = "[orange]Router";
    rules.tags.put(ROUTER_ACTIVE_KEY, "true");
    rules.unitBuildSpeedMultiplier = Float.MIN_VALUE;
    rules.damageExplosions = false;
    rules.reactorExplosions = false;
    return rules;
  }

  private boolean isActive() {
    return Vars.state.isPlaying() && Vars.state.rules.tags.getBool(ROUTER_ACTIVE_KEY);
  }
}
