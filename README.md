# Tracker MCP

See also
- [eighttrigrams/clojure-claude-and-mcp-knowledge](https://github.com/eighttrigrams/clojure-claude-and-mcp-knowledge).
- [eighttrigrams/vesicapiscis](https://github.com/eighttrigrams/vesicapiscis)

## Run

Use

```sh
$ ./run.sh
```

and then feed it lines like

```json
{"id":1,"method":"initialize","params":{}}
```

or

```json
{"id":3,"method":"tools/call","params":{"name":"get_related_items","arguments":{"selected-context-item-id":"10935", "q":"", "secondary-contexts-items-ids":["11041"]}}}
```

and it will respond to that on pressing **enter**. 

That is, the server reads JSON-RPC requests from stdin and writes responses to stdout.

Or with `echo`:

```sh
$ echo '{"id":2,"method":"tools/list","params":{}}' | ./run.sh
$ echo '{"id":3,"method":"tools/call","params":{"name":"get_issues","arguments":{"q":"Paris"}}}' | ./run.sh
$ echo '{"id":1,"method":"initialize","params":{}}' | ./run.sh
```
