# Mappa
Originally called by his purpose, _"Map Live Editor"_

Has the objective to be a Mini-game Map Editor In-game
with the capability to read a scheme of the Mini-game map configuration
and interpret them as a Map scheme.

With the Map scheme you can create a Map session, which works to create
all the required properties by the scheme.

When all the required properties are completed the session can be saved
into a generic file _(like map-MySchemeName.yml)_ and ready to be used by the Mini-game.

There are various features to work with:
- A yaml scheme to define a map configuration
- Define each property by various in-game tools for vectors and chunks (or cuboids and chunk cuboids) and commands
- Automatically parse Map scheme nodes into commands (see the examples)
- Map scheme configurations to modify the parse phase (define command aliases, define multi-nodes, etc) (see the examples)
- Supports collection and optional nodes
- Simple command to setup everything step-by-step (`/mappa setup <session id> <value>`)
- When a Map session doesn't satisfy all the needed properties will be serialized into sessions.yml to be possible to resume.
- Supports load already created map configurations to transform into map session.

**The project still on alpha. Anything can be changed in any time.**
## Example: MyScheme (With SnakeYaml)
```yaml
MyScheme:
# ▼ Any node that starts with '$' is a parse configuration for the parent node (MyScheme).
  $parent: # ◄ Parent parse configuration. contains crucial configuration of the entire scheme.
    aliases: [my-scheme, mys] # ◄ Aliases for command parse.

      # ▼ All of these nodes that define his own types by tag '!' would be considered properties.
      # ▼ The nodes with 'property' literal tag are special properties to be considers when the session is saved.
  name: !property name
  world: !property world
  version: !property version
  author: !property author
  my-integer: !int + # ◄ Also the nodes can receive flags by their type.
                     #   In this case, it is an integer with the positive flag to deny any negative int number.

  my-string: !string
  my-list: !list of string # ◄ Define list verbally with 'of' and the type of the list as flags.

  # Supports deep sub nodes
  my-node:
    my-deep-node:
      my-deeper-node: !float
      my-deeper-optional-node?: !float
                           # ▲ Question mark at the final character defines an optional node.
    my-repetitive-node:
      # ▼ A parse configuration to duplicate the parent node (my-repetitive-node) for each node of the array.
      # ▼ Useful for cases like mini-game teams and more.
      $multi-node: ['node-1', "node-2", "node-3"]

      first-node: !string
      second-node: !string
      third-node: !string

# The multi-node result should be:
#   node-1:
#     first-node: !string
#     second-node: !string
#     third-node: !string
#   node-2:
#     first-node: !string
#     second-node: !string
#     third-node: !string
#   node-3:
#     first-node: !string
#     second-node: !string
#     third-node: !string

```

### Map scheme commands result
* Root command: `/myscheme, /my-scheme, /mys`
* Sub nodes: `/myscheme name, /myscheme world, etc`
* Deep sub nodes: `/myscheme my-node my-deep-node my-deeper-node`

## Contributing
Any kind of contribution is accepted!
Mappa uses JDK 8 with Minecraft version target 1.8.8 to be compatible with old versions.

We use the MIT License, be free to clone and fork the repository always giving credits.

## TODO
- [ ] Make Map scheme file parse independent of yaml (Maybe replace them with scripts?)
- [ ] Migrate yaml to MongoDB for storage (Making save to yaml optional)
