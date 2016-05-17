# Problems to Solve

## Metadata stream semantics

Metadata streams are named the same as their main stream with a dot postfix... for example "my_output.state" would be the state metadata for the "my_output" stream. Currently, metadata streams don't show up in the streams list and get deleted automatically if you truncate the main stream to 0. This makes them somewhat of an invisible mess, but at the same time - including them in the general list would also clutter things... maybe the best solution is to make them visible, but at the same time provide much better tooling for managing streams?

## Service Configuration

Currently you create a service by posting its configuration to the /service/ endpoint. You may update it by sending a put to that same endpoint. However... when you get, we return much more information (in a slightly different format)... which makes it very hard to get inorder to edit something and send an update back. How should we solve this? by not adding information to the get? and maybe have a different endpoint for service statuses? or by simply stripping un needed fields when posted back to us? or maybe have a sub endpoint that returns just the config?

