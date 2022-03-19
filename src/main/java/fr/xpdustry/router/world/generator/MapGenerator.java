package fr.xpdustry.router.world.generator;

import arc.func.*;
import fr.xpdustry.router.world.plot.*;
import mindustry.world.*;
import org.jetbrains.annotations.*;

public interface MapGenerator extends Cons<Tiles> {

  static MapGenerator simple() {
    return SimpleMapGenerator.INSTANCE;
  }

  int getMapWidth();

  int getMapHeight();

  @NotNull Iterable<Plot> getPlots();
}
