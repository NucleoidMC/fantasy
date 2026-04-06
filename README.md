# Fantasy
Fantasy is a library that allows for dimensions to be created and destroyed at runtime on the server.
It supports both temporary dimensions which do not get saved, as well as persistent dimensions which can be safely used across server restarts.

## Using

### Adding to Gradle
To add Fantasy to your Gradle project, add the Nucleoid Maven repository and Fantasy dependency.
`FANTASY_VERSION` should be replaced with the latest version from [Maven](https://maven.nucleoid.xyz/xyz/nucleoid/fantasy).
```gradle
repositories {
  maven { url = 'https://maven.nucleoid.xyz/' }
}

dependencies {
  // ...
  modImplementation 'xyz.nucleoid:fantasy:FANTASY_VERSION'
}
```

### Creating Runtime Dimensions
All access to Fantasy's APIs goes through the `Fantasy` object, which can be acquired given a `MinecraftServer` instance.

```java
Fantasy fantasy = Fantasy.get(server);
// ...
```

All dimensions created with Fantasy must be set up through a `RuntimeLevelConfig`.
This specifies how the dimension should be created, involving a dimension type, seed, chunk generator, and so on.

For example, we could create a config like such:
```java
RuntimeLevelConfig levelConfig = new RuntimeLevelConfig()
        .setDimensionType(DimensionTypes.OVERWORLD)
        .setDifficulty(Difficulty.HARD)
        .setGameRule(GameRules.DO_DAYLIGHT_CYCLE, false)
        .setGenerator(server.getOverworld().getChunkManager().getChunkGenerator())
        .setSeed(1234L);
```

World-wide values such as difficulty and game rules can be configured per-level. 

#### Creating a temporary dimension
Once we have a runtime level config, creating a temporary dimension is simple:
```java
RuntimeLevelHandle levelHandle = fantasy.openTemporaryLevel(levelConfig);

// set a block in our created temporary level!
ServerLevel level = levelHandle.asLevel();
level.setBlock(BlockPos.ZERO, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

// we don't need the level anymore, delete it!
levelHandle.delete();
```
Explicit deletion is not strictly required for temporary levels: they will be automatically cleaned up when the server exits.
However, it is generally a good idea to delete old levels if they're not in use anymore.

#### Creating a persistent dimension 
Persistent dimensions work along very similar lines to temporary dimensions:

```java
RuntimeLevelHandle levelHandle = fantasy.getOrOpenPersistentLevel(new Identifier("foo", "bar"), config);

// set a block in our created persistent level!
ServerLevel level = levelHandle.asLevel();
level.setBlockState(BlockPos.ORIGIN, Blocks.STONE.getDefaultState());
```

The main difference involves the addition of an `Identifier` parameter which much be specified to name your dimension uniquely.

Another **very important note** with persistent dimensions is that `getOrOpenPersistentLevel` must be called to re-initialize
the dimension after a game restart! Fantasy will not restore the dimension by itself- it only makes sure that the level data
sticks around. This means, if you have a custom persistent dimension, you need to keep track of it and all its needed
data such that it can be reconstructed by calling `getOrOpenPersistentLevel` again with the same identifier.
