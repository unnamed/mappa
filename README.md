<h1 align="center">Mappa</h1>
<p align="center">
Originally called "Map Live Editor".<br>
Has the purpose to create and edit map configurations for any kind of minigame.<br>
Provides various useful tools to define properties like a number, a text, a vector (for locations)<br>
and much more with In-game solutions.<br>
</p>
<h5 align="center">The project still on alpha. Anything can be changed in any time.</h5>

*Features*:
- Scheme of Minigame's map configuration in Yaml without depends on it or exposing any plugin.
- Configuration properties mapping into commands! (Examples below)
- Basic types support (Integer, Double, String, Vector, Cuboid and more)
- Simple command to setup everything step-by-step (`/mappa setup <session id> <value>`)
- Support save map configuration into a specific path
- Support 

## Example: MyScheme (With SnakeYaml)
```yaml
MyScheme:
# ▼ Any node that starts with '$' is a parse configuration for the parent node (MyScheme).
  $parent: # ◄ Parent parse configuration. contains crucial configuration of the entire scheme.
    aliases: [my-scheme, mys] # ◄ Aliases for command parse.

      # ▼ All of these nodes that define his own types by tag '!' would be considered properties.
      # ▼ Metadata tag defines properties that can be used for Mappa on his commands.
  name: !metadata name
  world: !metadata world
  version: !metadata version
  author: !metadata author
  my-integer: !int + # ◄ Also the nodes can receive flags by their type.
                     #   In this case, it is an integer with the positive flag to deny any negative int number.

  my-string: !string
  my-list: !list of string # ◄ Define list verbally with 'of' and the type of the list.

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
- [X] Mirror tool (with a defined map center)
- [X] Create Map scheme standards to identify properties by object (like a block) and use it to automatically create properties with a Scanner tool
- [X] Region radius tool
- [ ] Implement visuals for each property in world
- [ ] Copy & Paste tool (Get all vector/region/chunk properties near)
- [ ] Make Map scheme file parse independent of yaml (Maybe replace them with scripts?)
