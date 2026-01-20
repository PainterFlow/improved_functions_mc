# Public d-Functions V1.0.0 (for datapacks ofc) 1.21.11
## /dfunc namespace:function nbt (*I may or may not have done this with AI xd*)
 allows users without op to use any functions in that specific namespace, with nbt arguments.
### How to use:
- install the mod with Fabric API
- how to create a dfunctions:
mypack/
 └─ data/
    └─ namespace/
       └─ functions/
          └─ func/
             └─ anypublicfunctions.mcfunction

This is very usefull for dialogs :)
also you don't have to write the /*func*/ part into /dfunc as it redirects automatically
### Known issues:
- functions don't give any error responses, I recommend first testing with /function and then just replacing the "function" command with "dfunc"
- while selectors like @s work, using ~ ~ ~ doesn't work yet, altho ill fix that
- if you have any really bad issues dm me on discord: @painterflow11

> [!IMPORTANT]
> - [Modrinth Download](https://modrinth.com/mod/public-dfunctions/settings/description)
