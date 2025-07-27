# Tracker MCP

For minimal Clojure MCP examples see [eighttrigrams/clojure-claude-and-mcp-knowledge](https://github.com/eighttrigrams/clojure-claude-and-mcp-knowledge).

## Run continuously

Either

```
$ /Users/daniel/Workspace/eighttrigrams/tracker-mcp/run.sh
```

or

```
$ cd /Users/daniel/Workspace/eighttrigrams/tracker-mcp
$ clj -M:run
```

and then feed it lines like

```json
{"id":1,"method":"initialize","params":{}}
```

and it will respond to that on pressing **enter**. 

That is, the server reads JSON-RPC requests from stdin and writes responses to stdout.

## One-shot usage examples

```sh
$ echo '{"id":2,"method":"tools/list","params":{}}' | /Users/daniel/Workspace/eighttrigrams/tracker-mcp/run.sh
$ echo '{"id":3,"method":"tools/call","params":{"name":"get_issues","arguments":{"q":"Paris"}}}' | /Users/daniel/Workspace/eighttrigrams/tracker-mcp/run.sh
$ echo '{"id":1,"method":"initialize","params":{}}' | /Users/daniel/Workspace/eighttrigrams/tracker-mcp/run.sh


{"id":3,"method":"tools/call","params":{"name":"get_related_items","arguments":{"selected-context-item-id":"10935", "q":"", "secondary-contexts-items-ids":["11041"]}}}
```

## Implementation note

Make sure to re-route all log events from the underlying lib to stderr or
to a logfile.
