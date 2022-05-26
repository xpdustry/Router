package fr.xpdustry.router.command;

import arc.util.*;
import org.jetbrains.annotations.*;

/**
 * Hides the commands behind an interface because I plan to move to Distributor 3.
 */
public interface RouterCommand {

  void registerCommands(final @NotNull CommandHandler handler);
}
