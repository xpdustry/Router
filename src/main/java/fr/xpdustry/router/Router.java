package fr.xpdustry.router;

import arc.util.CommandHandler;
import arc.util.Log;
import fr.xpdustry.distributor.plugin.*;
import mindustry.mod.Plugin;
import org.jetbrains.annotations.NotNull;


/** Template plugin. */
@SuppressWarnings("unused")
public class Router extends AbstractPlugin {
    /** This method is called when game initializes. */
    @Override public void init() {
        Log.info("Bonjour !");
    }

    /** This method is called when the game register the server-side commands. */
    @Override public void registerServerCommands(@NotNull CommandHandler handler) {
    }

    /** This method is called when the game register the client-side commands. */
    @Override public void registerClientCommands(@NotNull CommandHandler handler) {
    }
}
