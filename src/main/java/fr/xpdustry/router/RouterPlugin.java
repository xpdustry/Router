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
import fr.xpdustry.router.command.*;
import fr.xpdustry.router.map.*;
import fr.xpdustry.router.model.*;
import fr.xpdustry.router.service.*;
import java.util.*;
import java.util.stream.*;
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

  private final PlotService service = PlotService.simple();
  private final Interval timer = new Interval();
  private final RouterCommand command = new VanillaRouterCommand(service);

  @Override
  public void init() {
    Vars.netServer.admins.addActionFilter(action -> {
      if (isActive() && action.tile != null) {
        final var tiles = new ArrayList<Tile>();
        if (action.type == ActionType.placeBlock && action.block.isMultiblock()) {
          int size = action.block.size;
          int offsetX = -(size - 1) / 2;
          int offsetY = -(size - 1) / 2;
          for (int dx = 0; dx < size; dx++) {
            for (int dy = 0; dy < size; dy++) {
              final var other = Vars.world.tile(action.tile.x + dx + offsetX, action.tile.y + dy + offsetY);
              if (other != null) tiles.add(other);
            }
          }
        } else {
          tiles.add(action.tile);
        }

        return StreamSupport.stream(service.findAllPlots().spliterator(), false)
          .filter(p -> p.getOwner() != null && (p.getOwner().equals(action.player.uuid()) || p.hasMember(action.player.uuid())))
          .anyMatch(p -> tiles.stream().allMatch(t -> p.getArea().contains(t)));
      }
      return true;
    });

    Events.run(Trigger.update, () -> {
      if (isActive() && timer.get(Time.toSeconds)) {
        for (final var plot : service.findAllPlots()) {
          Call.label(
            "Plot #" + plot.getId(),
            1F,
            plot.getArea().getX() + (plot.getArea().getW() / 2F),
            plot.getArea().getY() + (plot.getArea().getH() / 2F)
          );

          final var owner = Groups.player.find(p -> p.uuid().equals(plot.getOwner()));
          if (owner != null) {
            Call.label(
              "[green]" + Strings.stripColors(owner.name()) + "'s plot",
              1F,
              plot.getArea().getX() + (plot.getArea().getW() / 2F),
              plot.getArea().getY() + plot.getArea().getH()
            );
          }
        }
      }
    });

    Events.on(PlayerLeave.class, e -> {
      service.findPlotsByOwner(e.player.uuid()).forEach(Plot::clearData);
      service.findAllPlots().forEach(plot -> plot.removeMember(e.player.uuid()));
    });
  }

  @Override
  public void registerServerCommands(final @NotNull CommandHandler handler) {
    handler.register("router", "Begin hosting with the router gamemode.", args -> {
      try (final var loader = new MapLoader()) {
        final var generator = MapGenerator.simple();
        loader.generate(generator.getMapWidth(), generator.getMapHeight(), generator);
        Vars.state.rules = createRouterRules();
        service.setPlotAreas(generator.getPlots());
      }
    });
  }

  @Override
  public void registerClientCommands(CommandHandler handler) {
    command.registerCommands(handler);
  }

  private @NotNull Rules createRouterRules() {
    final var rules = new Rules();
    Gamemode.sandbox.apply(rules);
    rules.modeName = "[orange]Router";
    rules.tags.put(ROUTER_ACTIVE_KEY, "true");
    rules.unitBuildSpeedMultiplier = Float.MAX_VALUE;
    rules.damageExplosions = false;
    rules.reactorExplosions = false;
    return rules;
  }

  private boolean isActive() {
    return Vars.state.isPlaying() && Vars.state.rules.tags.getBool(ROUTER_ACTIVE_KEY);
  }
}
