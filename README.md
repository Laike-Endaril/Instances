# Instances
Allows the creation and handling of area instances.  Originally based on/forked from lumien's Simple Dimensions mod

## Changes from original:
+ Added support for any dimension type from any mod (Ex: The Nether, The End, Twilight Forest)
+ Fixed compatibility with Twilight Forest
+ Added various user feedback for commands and fixed /timed command
+ Force dimension deletion rather than wait and assume its completion (Fixes issue where dimensions never delete and command gives no error or feedback)
+ Allow deletion of a dimension with other players in it (Teleports them to thier spawn point in the overworld, or the overworlds spawnpoint)
+ Removes OpenComputers interface (Just adds bloat to the mod (can only list dimensions which really isn't very usefull anyway), It's supposed to be simple dimensions not bloated dimensions!)
