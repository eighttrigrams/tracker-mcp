# Weather MCP Server

echo '{"id":2,"method":"tools/list","params":{}}' | bb -cp /Users/daniel/Workspace/sandbox/claude/weather-mcp/src -m server

echo '{"id":3,"method":"tools/call","params":{"name":"get_weather","arguments":{"location":"Paris"}}}' | bb -cp /Users/daniel/Workspace/sandbox/claude/weather-mcp/src -m server

echo '{"id":1,"method":"initialize","params":{}}' | bb -cp /Users/daniel/Workspace/sandbox/claude/weather-mcp/src -m server



A minimal MCP (Model Context Protocol) server built with Babashka that provides fake weather information.

## Features

- Implements MCP protocol version 2024-11-05
- Provides a `get_weather` tool that returns one of 5 predefined weather descriptions
- Built with Babashka for fast startup and easy deployment

## Usage

Start the server:
```bash
bb server
```

The server reads JSON-RPC requests from stdin and writes responses to stdout.

## Testing

You can test the server manually by sending JSON-RPC requests:

```bash
echo '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{}}' | bb server
echo '{"jsonrpc":"2.0","id":2,"method":"tools/list","params":{}}' | bb server  
echo '{"jsonrpc":"2.0","id":3,"method":"tools/call","params":{"name":"get_weather","arguments":{"location":"San Francisco"}}}' | bb server
```

## Adding to Claude Code

This works

```bash
$ claude mcp add weather -- bb -cp /Users/daniel/Workspace/sandbox/claude/weather-mcp/src -m server
```
