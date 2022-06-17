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
import fr.xpdustry.router.repository.*;
import fr.xpdustry.router.service.*;
import java.io.*;
import mindustry.*;
import mindustry.game.EventType.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.mod.*;
import org.jetbrains.annotations.*;

@SuppressWarnings("unused")
public final class RouterPlugin extends Plugin {

  public static final String ROUTER_ACTIVE_KEY = "xpdustry-router:active";

  private final PlotService plots = PlotService.simple();
  private final Interval timer = new Interval();
  private final SchematicService schematics = SchematicService.simple(
    SchematicRepository.of(new File("./schematics.sqlite"))
  );
  private final RouterCommand command = new RouterCommand(plots, schematics);

  public static boolean isActive() {
    return Vars.state.isPlaying() && Vars.state.rules.tags.getBool(ROUTER_ACTIVE_KEY);
  }

  @Override
  public void init() {
    Vars.netServer.admins.addActionFilter(new RouterFilter(plots));

    Events.run(Trigger.update, () -> {
      if (isActive() && timer.get(Time.toSeconds)) {
        for (final var plot : plots.findAllPlots()) {
          Call.label(
            "Plot [cyan]#[]" + plot.getId(),
            1F,
            plot.getArea().getX() + (plot.getArea().getW() / 2F),
            plot.getArea().getY() + (plot.getArea().getH() / 2F)
          );

          final String title;
          if (plot.getOwner() != null) {
            title = "[red]" + getPlayerLastName(plot.getOwner()) + "'s plot";
          } else if (plot.getPlaceholder() != null){
            title = "[yellow]" + getPlayerLastName(plot.getPlaceholder().getAuthor()) + "'s schematic";
          } else {
            title = "[green]Empty plot";
          }

          Call.label(
            title,
            1F,
            plot.getArea().getX() + (plot.getArea().getW() / 2F),
            plot.getArea().getY() + plot.getArea().getH()
          );
        }
      }
    });

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
      plots.findPlotsByOwner(e.player.uuid()).forEach(p -> p.setOwner(null));
      plots.findAllPlots().forEach(plot -> plot.removeMember(e.player.uuid()));
    });

    // Keeps units from rebuilding
    Events.on(BlockDestroyEvent.class, e -> {
      e.tile.team().data().blocks.clear();
    });
  }

  @Override
  public void registerServerCommands(final @NotNull CommandHandler handler) {
    command.registerServerCommands(handler);
  }

  @Override
  public void registerClientCommands(final @NotNull CommandHandler handler) {
    command.registerClientCommands(handler);
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

  private @NotNull String getPlayerLastName(final @NotNull String uuid) {
    return Strings.stripColors(Vars.netServer.admins.getInfo(uuid).lastName);
  }
}
