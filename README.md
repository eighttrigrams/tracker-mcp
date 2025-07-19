# Weather MCP Server

A minimal MCP (Model Context Protocol) server that provides fake weather information.

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
$ echo '{"id":3,"method":"tools/call","params":{"name":"get_weather","arguments":{"location":"Paris"}}}' | /Users/daniel/Workspace/eighttrigrams/tracker-mcp/run.sh
$ echo '{"id":1,"method":"initialize","params":{}}' | /Users/daniel/Workspace/eighttrigrams/tracker-mcp/run.sh
```

## Features

- Implements MCP protocol version 2024-11-05
- Provides a `get_weather` tool that returns one of 5 predefined weather descriptions

## Adding to Claude Code

Run

```sh
$ claude mcp add weather3 -- sh /Users/daniel/Workspace/eighttrigrams/tracker-mcp/run.sh
```
