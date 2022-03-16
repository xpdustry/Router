package fr.xpdustry.router.internal;

import org.aeonbits.owner.*;

public interface RouterConfig extends Accessible {
  @DefaultValue("")
  @Key("router.generator")
  String getGeneratorScript();
}
