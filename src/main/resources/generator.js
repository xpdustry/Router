const WORLD_WIDTH = 1000
const WORLD_HEIGHT = 150
const SCHEM_SIZE = 64
const plots = new Seq()

const generator = cons(tiles => {
  tiles.fill();
  tiles.forEach(t => t.setFloor(Blocks.metalFloor4.asFloor()));

  Blocks.coreNucleus.unitType = UnitTypes.mega; // fast travel
  Vars.world.tile(WORLD_WIDTH / 2, WORLD_HEIGHT / 2).setBlock(Blocks.coreNucleus, Team.sharded, 0);

  // plot init

  let x = WORLD_WIDTH / 2;
  while(x > 0){
    for(let y = 0; y < WORLD_HEIGHT; y++)
      Vars.world.tile(x, y).setFloor(Blocks.darkPanel3.asFloor());
    x -= 64;
    if(x < 0) continue;
    plots.add(Vars.world.tile(x + 2, (WORLD_HEIGHT / 2) + 3))
    plots.add(Vars.world.tile(x + 2, (WORLD_HEIGHT / 2) - SCHEM_SIZE - 3))
    // plots.add(new Plot(Vars.world.tile(x + 2, (WORLD_HEIGHT / 2) + 3), PlotType.NICE));
    // plots.add(new Plot(Vars.world.tile(x + 2, (WORLD_HEIGHT / 2) - SCHEM_SIZE - 3), PlotType.NICE));
  }

  x = WORLD_WIDTH / 2;
  while(x < WORLD_WIDTH){
    for(let y = 0; y < WORLD_HEIGHT; y++)
      Vars.world.tile(x, y).setFloor(Blocks.darkPanel3.asFloor());
    if((WORLD_WIDTH - x) > 64){
      plots.add(Vars.world.tile(x + 2, (WORLD_HEIGHT / 2) + 3))
      plots.add(Vars.world.tile(x + 2, (WORLD_HEIGHT / 2) - SCHEM_SIZE - 3))
      // plots.add(new Plot(Vars.world.tile(x + 2, (WORLD_HEIGHT / 2) + 3), PlotType.NICE));
      // plots.add(new Plot(Vars.world.tile(x + 2, (WORLD_HEIGHT / 2) - SCHEM_SIZE - 3), PlotType.NICE));
    }
    x += 64;
  }
  // make a fancy road for people to fly on
  for(x = 0; x < WORLD_WIDTH; x++){
    for(let y = (WORLD_HEIGHT / 2 - 1); y < (WORLD_HEIGHT / 2 + 2); y++){
      Vars.world.tile(x, y).setFloor(Blocks.metalFloor5.asFloor());
    }
  }

  Log.info("PLOTS @", plots)
})

Vars.world.loadGenerator(WORLD_WIDTH, WORLD_HEIGHT, generator);

/*

function reload() {
  const reloader = new WorldReloader()
  reloader.begin()
  Vars.logic.reset()

  Vars.world.loadGenerator(WORLD_WIDTH, WORLD_HEIGHT, generator)
  Vars.state.rules.modeName = "[red]RedditDustry :^)"

  Vars.logic.play()
  reloader.end()
}

reload()

 */