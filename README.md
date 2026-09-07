# JScriptor

**Pre-Script / Post-Script for Burp Suite — like Postman, but on every request Burp sends.**

JScriptor is a Burp Suite extension that lets you run arbitrary JavaScript at seven points in
Burp's HTTP and WebSocket pipelines. Your script receives the request, response or WebSocket
message as a plain JavaScript object, mutates it, and hands it back — Burp then continues with
the modified message.

Typical uses: re-signing requests (HMAC, custom signature headers), refreshing bearer tokens,
encrypting/decrypting proprietary body formats, injecting anti-CSRF nonces, rewriting responses
on the fly so the browser stays usable while you test, or transparently decrypting an encrypted
WebSocket channel so you can read and tamper with it in plaintext.

---

## Table of contents

- [Features](#features)
- [Requirements](#requirements)
- [Build](#build)
- [Install](#install)
- [Concepts](#concepts)
  - [The seven hook points](#the-seven-hook-points)
  - [Script contract](#script-contract)
- [Scripting API](#scripting-api)
  - [jsrequest](#jsrequest)
  - [jsresponse](#jsresponse)
  - [jswebsocket](#jswebsocket)
  - [jsresult](#jsresult)
  - [jsvariables](#jsvariables)
  - [jslogger](#jslogger)
- [WebSocket](#websocket)
  - [WebSocket hook points](#websocket-hook-points)
  - [WebSocket message editor](#websocket-message-editor)
- [UI reference](#ui-reference)
- [Engines: Javet vs GraalJS](#engines-javet-vs-graaljs)
- [Examples](#examples)
- [How it works internally](#how-it-works-internally)
- [Project layout](#project-layout)
- [Known limitations](#known-limitations)

---

## Features

- Four independent HTTP script slots — proxy request in, request out, response in, proxy response out.
- Three WebSocket script slots — proxy message in, proxy message out, and all-tools messages.
- A **JScriptor** tab inside Burp's WebSocket message viewer that decodes payloads for reading and
  re-encodes whatever you type before it goes on the wire.
- Text and binary WebSocket frames (binary is handed to the script base64 encoded).
- Two JavaScript engines: **Javet** (real Node.js / V8) and **GraalJS** (GraalVM polyglot).
- Per-slot filters: Burp scope, regex match on the raw message, and "skip messages coming from Proxy".
- Scripts can drop a WebSocket message outright.
- Persistent key/value store shared between scripts and across Burp restarts.
- Load pure `.js` library files, or point at a `node_modules` directory and `require()` from it.
- Syntax-highlighted editors (RSyntaxTextArea) that follow Burp's light/dark theme.
- Optional log table with side-by-side **Original** vs **Modified** request/response viewers.

## Requirements

| | |
|---|---|
| Burp Suite | Any edition supporting Montoya API `2026.4` |
| JDK | 17 to 20 to build. The bundled Gradle 8.2 cannot run on JDK 21+ (`Unsupported class file major version`). |
| Gradle | Wrapper included (Gradle 8.2) |

## Build

```bash
./gradlew shadowJar          # Linux / macOS
gradlew.bat shadowJar        # Windows
```

The fat JAR lands in `build/libs/JScriptor-3.0.0.jar`.

> **Build on the platform you will run on.** [build.gradle](build.gradle) picks the Javet native
> binary from the *build* machine's OS/arch: `javet-macos` on macOS, `javet-linux-arm64` on ARM
> Linux, and the default `javet` artifact (Windows/Linux x64) everywhere else. A JAR built on
> Windows carries no macOS V8 native, so the Javet engine will fail to start there.

## Install

1. Burp Suite → **Extensions** → **Installed** → **Add**
2. Extension type: **Java**
3. Select `build/libs/JScriptor-3.0.0.jar`
4. A new top-level **JScriptor** tab appears.

---

## Concepts

### The seven hook points

JScriptor registers five Montoya handlers, giving seven script slots. Each slot is a separate
tab with its own editor, its own filters, and its own enable checkbox. They live under two
top-level tabs, **HTTP** and **WebSocket**.

#### HTTP

| Tab | Fires when | Montoya callback | Applies to |
|---|---|---|---|
| **Request Received** | Proxy just received the request from the browser | `ProxyRequestHandler.handleRequestReceived` | Proxy only |
| **Request to be Sent** | Burp is about to put the request on the wire | `HttpHandler.handleHttpRequestToBeSent` | All tools (Proxy, Repeater, Intruder, Scanner…) |
| **Response Received** | Burp just read the response off the wire | `HttpHandler.handleHttpResponseReceived` | All tools |
| **Response to be Sent** | Proxy is about to return the response to the browser | `ProxyResponseHandler.handleResponseToBeSent` | Proxy only |

Ordering for a browser request through Proxy:

```
browser ──► Request Received ──► Request to be Sent ──► server
browser ◄── Response to be Sent ◄── Response Received ◄── server
```

#### WebSocket

| Tab | Fires when | Montoya callback | Applies to |
|---|---|---|---|
| **Message Received** | Proxy received a frame, from either side | `ProxyMessageHandler.handleTextMessageReceived` / `handleBinaryMessageReceived` | Proxy only |
| **Message to be Sent** | Proxy is about to forward a frame, to either side | `ProxyMessageHandler.handleTextMessageToBeSent` / `handleBinaryMessageToBeSent` | Proxy only |
| **Message (All Tools)** | Any Burp tool sends or receives a frame | `MessageHandler.handleTextMessage` / `handleBinaryMessage` | All tools (Proxy, Repeater…) |

Both directions hit the same slot; the script tells them apart with `jswebsocket.direction`.

Because *Request to be Sent* and *Message (All Tools)* also see Proxy traffic, those tabs offer a
**"Don't modify … from Proxy"** checkbox so the two layers don't stack on top of each other.

### Script contract

Every script runs in a fresh JavaScript context with these globals already populated:

| Global | Request-side slots | Response-side slots | WebSocket slots |
|---|---|---|---|
| `jsrequest` | yes | Javet only — see [limitations](#known-limitations) | — |
| `jsresponse` | — | yes | — |
| `jswebsocket` | — | — | yes |
| `jsresult` | `{ request: null }` | `{ response: null }` | `{ websocket: null }` |
| `jsvariables` | yes | yes | yes |
| `jslogger` | yes | yes | yes |

**You must assign `jsresult`.** JScriptor reads `jsresult.request` / `jsresult.response` /
`jsresult.websocket` after your script finishes and rebuilds the Burp message from it. If you
leave it `null`, the deserialisation fails, the error goes to the extension's Errors tab, and the
**original, unmodified message is passed through**.

Minimal skeleton:

```javascript
// request-side slot
jsrequest.headers["X-Trace"] = "jscriptor";
jsresult.request = jsrequest;
```

```javascript
// response-side slot
jsresponse.body = jsresponse.body.replace("disabled", "");
jsresult.response = jsresponse;
```

```javascript
// websocket slot
jswebsocket.payload = jswebsocket.payload.toUpperCase();
jsresult.websocket = jswebsocket;
```

---

## Scripting API

### jsrequest

Serialised from [JSHttpRequest.java](src/main/java/org/JScriptor/model/JSHttpRequest.java).

```jsonc
{
  "body": "user=admin&pass=1234",
  "headers": { "Host": "example.com", "Content-Type": "application/x-www-form-urlencoded" },
  "httpService": { "host": "example.com", "port": 443, "secure": true },
  "httpVersion": "HTTP/1.1",
  "method": "POST",
  "parameters": { "user": "admin", "pass": "1234" },   // BODY params only
  "queries":    { "id": "7" },                          // URL params only
  "path": "/login?id=7",
  "pathWithoutQuery": "/login",
  "url": "https://example.com/login?id=7"
}
```

What actually gets written back to the Burp request (`getHttpRequest`):

| Field | Effect |
|---|---|
| `headers` | Each entry is applied with `withHeader` — **adds or updates**. Deleting a key from the map does *not* remove the header. |
| `path` | Applied only if it differs from the original. |
| `parameters` | Written back as `BODY` parameters. |
| `queries` | Written back as `URL` parameters. |
| `method` | Applied only if it differs. |
| `body` | Applied only if it differs — **applied after `parameters`**, so setting both means `body` wins. |
| `httpService` | Always re-applied — change `host`/`port`/`secure` to redirect the request. |
| `url`, `pathWithoutQuery`, `httpVersion` | Read-only; ignored on write. |

> Edit **either** `parameters`/`queries` **or** `body` — not both in the same script.

### jsresponse

Serialised from [JSHttpResponse.java](src/main/java/org/JScriptor/model/JSHttpResponse.java).

```jsonc
{
  "body": "{\"ok\":true}",
  "headers": { "Content-Type": "application/json" },
  "httpVersion": "HTTP/1.1",
  "mimeType": "JSON",
  "reasonPhrase": "OK",
  "statusCode": 200
}
```

Written back: `headers` (updated if present, added if not), `body`, `httpVersion`,
`reasonPhrase`, `statusCode`. `mimeType` is read-only.

### jswebsocket

Serialised from [JSWebSocketMessage.java](src/main/java/org/JScriptor/model/JSWebSocketMessage.java).

```jsonc
{
  "payload": "{\"type\":\"ping\"}",   // text payload, or base64 when binary is true
  "binary": false,
  "direction": "CLIENT_TO_SERVER",     // or "SERVER_TO_CLIENT"
  "drop": false,
  "httpService": { "host": "example.com", "port": 443, "secure": true },
  "path": "/socket",
  "url": "https://example.com/socket"
}
```

| Field | Effect |
|---|---|
| `payload` | The new frame payload. Written back verbatim for text frames, base64 *decoded* first when `binary` is true. |
| `binary` | How JScriptor should read `payload` back. Flip it to switch a frame between text and base64 handling. |
| `direction` | `CLIENT_TO_SERVER` or `SERVER_TO_CLIENT`. Read-only; changing it does not redirect the frame. |
| `drop` | Set to `true` to drop the frame instead of forwarding it. |
| `httpService`, `path`, `url` | Taken from the HTTP upgrade request that opened the socket. Read-only; useful for telling sockets apart. |

Binary frames arrive base64 encoded with `binary: true`, so one script can handle both:

```javascript
if (jswebsocket.binary) {
  const raw = Buffer.from(jswebsocket.payload, "base64");   // Javet
  jswebsocket.payload = raw.toString("base64");
} else {
  jswebsocket.payload = jswebsocket.payload.trim();
}
jsresult.websocket = jswebsocket;
```

### jsresult

The output slot. Assign the (possibly mutated) object you want Burp to use:

```javascript
jsresult.request   = jsrequest;    // request-side slots
jsresult.response  = jsresponse;   // response-side slots
jsresult.websocket = jswebsocket;  // websocket slots
```

You can also build a fresh object literal with the same shape instead of mutating the input.

### jsvariables

A persistent string→string store, backed by Burp's extension data. It survives Burp restarts
and is shared by all four slots — this is how you pass a token from a response script to the
next request script.

```javascript
// read
const token = jsvariables.variables["access_token"];

// write (persisted automatically after the script returns)
jsvariables.variables["access_token"] = newToken;
```

Everything in `jsvariables.variables` is written back to Burp's persistence when your script
finishes. The same values are visible and editable in the **Variable Setting** tab.

> The key `node_modules` is reserved (it stores the Nodejs library path) and is filtered out
> of `jsvariables`.

### jslogger

Push strings here to get them printed to Burp's **Output** tab after the script finishes.

```javascript
jslogger.logs.push("signature = " + sig);
jslogger.logs.push(JSON.stringify(jsrequest.headers));
```

`console.log` is *not* forwarded to Burp — use `jslogger`.

---

## WebSocket

### WebSocket hook points

Enable a slot from the **WebSocket** column of the Running tab, then write the script in the
matching tab under **WebSocket**. All three slots share the filters described in
[Script tabs](#script-tabs), matching the regex against the frame payload and the scope against
the URL of the socket's HTTP upgrade request.

Pick the slot that matches what you are doing:

- **Message Received** — the earliest point in the Proxy. Decrypt here and the plaintext shows up
  in Burp's WebSocket history and interception window.
- **Message to be Sent** — the last point in the Proxy. Re-encrypt here, after you have tampered
  with the plaintext.
- **Message (All Tools)** — the only slot that sees Repeater's WebSocket tab. Tick
  *Don't modify message from Proxy* if a Proxy slot is already handling proxied traffic, otherwise
  both will run.

Dropping a frame:

```javascript
if (jswebsocket.payload.indexOf("heartbeat") !== -1) {
  jswebsocket.drop = true;
}
jsresult.websocket = jswebsocket;
```

### WebSocket message editor

Burp's WebSocket message viewer gets an extra **JScriptor** tab, the same idea as PyCript's
WebSocket editor. It uses two scripts of its own, written in **WebSocket → Message Editor**:

| Script | Runs when |
|---|---|
| **Decode** | A message is opened in the viewer. Its output is what the tab displays. |
| **Encode** | The message you edited is about to be sent. Its output goes on the wire. |

Both receive and return the same `jswebsocket` object as the traffic slots. Direction and
upgrade-request fields are filled in; `drop` is ignored here.

Turn it on with **Enable JScriptor WebSocket message editor** in the Running tab. The tab only
appears when that box is ticked and the Decode script is non-empty. Tick
**Editor payload is binary (base64)** for sockets carrying binary frames so the payload reaches
your script base64 encoded.

A decode / encode pair for a base64-wrapped JSON channel:

```javascript
// Decode
jswebsocket.payload = Buffer.from(jswebsocket.payload, "base64").toString("utf8");
jsresult.websocket = jswebsocket;
```

```javascript
// Encode
jswebsocket.payload = Buffer.from(jswebsocket.payload, "utf8").toString("base64");
jsresult.websocket = jswebsocket;
```

If a script fails or is empty, the editor falls back to showing and sending the raw payload
unchanged, so a broken script never blocks the message.

---

## UI reference

The JScriptor tab is split into three regions: the log table (top-left), the control + editor
panel (top-right), and the Original/Modified message viewers (bottom).

### Running tab

The controls are grouped into three columns — **Log**, **HTTP** and **WebSocket**.

| Control | Meaning |
|---|---|
| **Remove Log** | Clears the log table, the entry cache, and the four message viewers. |
| **Save Log** | Record traffic into the log table. Only effective while *Request to be Sent* or *Response Received* is enabled. |
| **Run Script with Request Received** | Enables the *Request Received* slot. |
| **Run Script with Request to be Sent** | Enables the *Request to be Sent* slot. |
| **Run Script with Response Received** | Enables the *Response Received* slot. |
| **Run Script with Response to be Sent** | Enables the *Response to be Sent* slot. |
| **Run Script with WS Message Received** | Enables the WebSocket *Message Received* slot. |
| **Run Script with WS Message to be Sent** | Enables the WebSocket *Message to be Sent* slot. |
| **Run Script with WS Message (All Tools)** | Enables the WebSocket *Message (All Tools)* slot. |
| **Enable JScriptor WebSocket message editor** | Shows the **JScriptor** tab in Burp's WebSocket message viewer. |

Nothing runs unless the matching checkbox is ticked **and** the slot's editor is non-empty.

### Configuration tab

Radio buttons selecting the engine — **Run with Javet** (default) or **Run with Graaljs**.
The choice is global: all four slots use the selected engine.

### Script tabs

The script tabs live under **HTTP** and **WebSocket**. Each has the editor on the left and the
filters on the right:

| Filter | Available in | Behaviour |
|---|---|---|
| **In Scope Item** | Request Received, Request to be Sent, all three WebSocket slots | Skip the script unless the URL is in Burp's scope. For WebSocket, the URL of the HTTP upgrade request. |
| **Match regex** | all slots | Skip unless the regex matches. HTTP matches the **entire raw message** (`request.toString()` / `response.toString()`); WebSocket matches the **frame payload**. Java regex syntax; entered in the small editor below the checkbox. |
| **Don't modified request from Proxy** | Request to be Sent | Skip when `ToolType.PROXY` is the source. |
| **Don't modified response from Proxy** | Response Received | Skip when `ToolType.PROXY` is the source. |
| **Don't modify message from Proxy** | WS Message (All Tools) | Skip when the socket was created by `ToolType.PROXY`. |

Filters are evaluated in order; the first one that fails passes the message through untouched.

WebSocket scripts are saved to Burp's extension data as you type them. HTTP scripts are still
saved from inside the traffic handlers — see [limitations](#known-limitations).

### Variable Setting

Table of the persisted key/value pairs, with **Add Value** (key + multi-line value dialog),
**Remove Selected** and **Clear All**. Adding an existing key overwrites its value.

### Pure JavaScript Library

Add `.js` files that are evaluated into the context **before** your script runs — use it for
bundled libraries (e.g. a standalone `crypto-js.js`) or your own helper functions. The file
list is persisted under the `pure_libraries` key.

> This applies to the **GraalJS** engine only.

### Nodejs Library

Point at a `node_modules` directory to enable `require()`:

- **Javet** — sets it as the Node require root directory.
- **GraalJS** — enables `js.commonjs-require` with that directory as the CWD.

Only one directory is stored (persisted under the `node_modules` key); **Clear All** first if
you need to change it.

### Log table

Columns: `#`, `METHOD`, `URL`, `STATUS`, `ORIGINAL LENGTH`. A row is added once the response
arrives. Selecting a row loads the **Original** and **Modified** request/response into the
editors at the bottom, so you can diff exactly what your script changed.

---

## Engines: Javet vs GraalJS

|  | **Javet** (default) | **GraalJS** |
|---|---|---|
| Runtime | Real Node.js / V8, fresh `NodeRuntime` per message | GraalVM polyglot `Context` per message |
| `require()` of npm packages | yes, via Nodejs Library | yes, via `js.commonjs-require` |
| Pure JS library files | no | yes (Pure JavaScript Library tab) |
| Java class access from JS | no | yes (`HostAccess.ALL`, `allowAllAccess`) |
| `jsrequest` in response slots | yes | no |
| WebSocket slots | yes | yes |
| `window` shim predefined | no | yes (`window = {}`) |
| Heavy concurrency | handles it | may error under many concurrent requests |

The extension's own advice, shown in the Configuration tab:

> *GraalJS gives you Java classes and Node modules, but can error when many concurrent
> requests are sent. Javet runs Node.js and handles concurrency.*

---

## Examples

### 1. Add an HMAC signature to every outgoing request

*Slot: Request to be Sent · Engine: Javet*

```javascript
const crypto = require("crypto");

const secret  = jsvariables.variables["hmac_secret"];
const payload = jsrequest.method + jsrequest.pathWithoutQuery + jsrequest.body;
const sig     = crypto.createHmac("sha256", secret).update(payload).digest("hex");

jsrequest.headers["X-Signature"] = sig;
jslogger.logs.push("signed " + jsrequest.path + " -> " + sig);

jsresult.request = jsrequest;
```

### 2. Capture a token from the login response

*Slot: Response Received · filter: Match regex `access_token`*

```javascript
try {
  const data = JSON.parse(jsresponse.body);
  if (data.access_token) {
    jsvariables.variables["access_token"] = data.access_token;
    jslogger.logs.push("captured token: " + data.access_token.slice(0, 12) + "...");
  }
} catch (e) {
  jslogger.logs.push("not json: " + e.message);
}

jsresult.response = jsresponse;
```

### 3. Replay it on the next request

*Slot: Request to be Sent · filter: In Scope Item*

```javascript
const token = jsvariables.variables["access_token"];
if (token) {
  jsrequest.headers["Authorization"] = "Bearer " + token;
}
jsresult.request = jsrequest;
```

### 4. Decrypt an encrypted response body so you can read it in Burp

*Slot: Response to be Sent · Engine: GraalJS with `crypto-js.js` added as a pure library*

```javascript
const key = CryptoJS.enc.Utf8.parse(jsvariables.variables["aes_key"]);
const iv  = CryptoJS.enc.Utf8.parse(jsvariables.variables["aes_iv"]);
const dec = CryptoJS.AES.decrypt(jsresponse.body, key, { iv: iv });

jsresponse.body = dec.toString(CryptoJS.enc.Utf8);
jsresponse.headers["Content-Type"] = "application/json";

jsresult.response = jsresponse;
```

### 5. Decrypt a WebSocket channel end to end

*Engine: Javet. Two slots, so the plaintext is what you see and tamper with in Burp.*

```javascript
// WebSocket -> Message Received   (decrypt everything entering Burp)
const crypto = require("crypto");
const key = Buffer.from(jsvariables.variables["ws_key"], "hex");

const raw = Buffer.from(jswebsocket.payload, "base64");
const iv = raw.subarray(0, 16);
const decipher = crypto.createDecipheriv("aes-256-cbc", key, iv);

jswebsocket.payload = Buffer.concat([decipher.update(raw.subarray(16)), decipher.final()]).toString("utf8");
jswebsocket.binary = false;

jsresult.websocket = jswebsocket;
```

```javascript
// WebSocket -> Message to be Sent   (re-encrypt on the way out)
const crypto = require("crypto");
const key = Buffer.from(jsvariables.variables["ws_key"], "hex");

const iv = crypto.randomBytes(16);
const cipher = crypto.createCipheriv("aes-256-cbc", key, iv);
const enc = Buffer.concat([cipher.update(jswebsocket.payload, "utf8"), cipher.final()]);

jswebsocket.payload = Buffer.concat([iv, enc]).toString("base64");
jswebsocket.binary = true;

jsresult.websocket = jswebsocket;
```

### 6. Drop WebSocket keepalive noise

*Slot: WS Message Received*

```javascript
if (jswebsocket.direction === "SERVER_TO_CLIENT" && jswebsocket.payload === "pong") {
  jswebsocket.drop = true;
}
jsresult.websocket = jswebsocket;
```

### 7. Redirect traffic to another host

```javascript
jsrequest.httpService.host   = "staging.internal";
jsrequest.httpService.port   = 8443;
jsrequest.httpService.secure = true;
jsrequest.headers["Host"]    = "staging.internal";

jsresult.request = jsrequest;
```

---

## How it works internally

For every intercepted message the selected engine does the same round trip:

```
Burp HttpRequest / HttpResponse
        |   JSHttpRequest.loadValue()
        v
   Java POJO  --Jackson-->  JSON string
        |
        |   bound as a global, then JSON.parse()'d inside the JS context
        v
   your script runs   (jsrequest / jsresponse / jsvariables / jslogger / jsresult)
        |
        |   JSON.stringify()  --Jackson-->  POJO
        v
  JSHttpRequest.getHttpRequest()  -->  modified Burp message
```

Side effects are flushed after the script returns, in order: `jsvariables` → Burp persistence,
`jslogger` → Burp Output, then `jsresult` → the modified message. Any exception at any stage is
logged to Burp's Errors tab and the **original message is returned unchanged** — a broken script
degrades to a no-op rather than breaking your traffic.

Each message gets a brand-new context/runtime, so **no state leaks between requests**. Use
`jsvariables` for anything that must persist.

## Project layout

```
src/main/java/org/JScriptor/
├── JScriptor.java                             BurpExtension entry point; registers the tab + 5 handlers
├── HttpHandlerWithScript.java                 Request to be Sent / Response Received (all tools)
├── ProxyRequestHandlerWithScript.java         Request Received (proxy)
├── ProxyResponseHandlerWithScript.java        Response to be Sent (proxy)
├── ProxyWebSocketCreationHandlerWithScript.java   attaches the proxy WS handler per socket
├── ProxyWebSocketMessageHandlerWithScript.java    WS Message Received / to be Sent (proxy)
├── WebSocketCreatedHandlerWithScript.java     attaches the all-tools WS handler per socket
├── WebSocketMessageHandlerWithScript.java     WS Message (All Tools)
├── Helper/
│   ├── JavetEngine.java                       Node.js / V8 execution
│   ├── GraalJSEngine.java                     GraalVM polyglot execution
│   └── WebSocketScriptRunner.java             shared filter + engine dispatch for WS slots
├── model/
│   ├── JSHttpRequest.java                     Burp request  <-> JS object
│   ├── JSHttpResponse.java                    Burp response <-> JS object
│   ├── JSWebSocketMessage.java                WS frame      <-> JS object (base64 for binary)
│   ├── JSHttpService.java                     host / port / secure
│   ├── JSVariableList.java                    persistent key-value store
│   └── JSLogger.java                          log buffer flushed to Burp Output
├── Logs/                                      LogEntry, LogTable, LogTableModel
└── UI/
    ├── JScriptorPanel.java                    the whole Swing UI + persistence wiring
    ├── ScriptSlot.java                        one hook point: editor + filters + enable checkbox
    ├── WebSocketEditorProvider.java           supplies the WS message editor to Burp
    └── JScriptorWebSocketEditor.java          the "JScriptor" tab in the WS message viewer

src/main/resources/                      RSyntaxTextArea themes (dark.xml is applied in Burp's dark theme)
```

## Known limitations

Behaviours worth knowing before you file a bug:

- **`jsrequest` is not available in the GraalJS response slots.** `GraalJSEngine.handleResponse`
  and `handleProxyResponse` bind only `jsresponse`; the Javet equivalents also bind `jsrequest`
  (from `responseReceived.initiatingRequest()`). Switch to Javet if you need the request in a
  response script.
- **Headers can only be added or updated, never removed** — for both requests and responses.
  Deleting a key from the `headers` map has no effect on the outgoing message.
- **Pure JavaScript Library files are ignored by Javet.** Only GraalJS evaluates them.
- **Log rows are only produced by the `Request to be Sent` / `Response Received` path.** Proxy
  traffic handled purely by the *Request Received* / *Response to be Sent* slots is not logged.
- **WebSocket traffic is not logged in the JScriptor log table.** The table is built around
  `HttpRequestEditor` / `HttpResponseEditor`; use `jslogger` or Burp's own WebSocket history.
- **A WebSocket frame carries no text/binary marker in the message editor.** Burp hands the editor
  a raw `ByteArray`, so the *Editor payload is binary (base64)* checkbox tells JScriptor how to
  present it. The traffic slots do know, and set `jswebsocket.binary` correctly on their own.
- **Script persistence keys overlap.** `preburp_code` and `postburp_code` are both written from
  the *Request to be Sent* editor's text, so the *Request Received* and *Response to be Sent*
  editors do not reliably restore their content across restarts. Keep a copy of long scripts
  outside Burp.
- **The Variable Setting table refresh is bound to tab index 2** (*Response Received*) rather than
  the Variable tab. If the table looks stale, re-open the JScriptor tab.
- **`ProxyResponseHandler.handleResponseReceived` returns `null`.** All response-side proxy work
  happens in `handleResponseToBeSent`.
- Scripts run **synchronously** on Burp's HTTP and WebSocket threads. A slow script slows down
  your traffic; there is no execution timeout. This bites harder on WebSocket, where a chatty
  application can push hundreds of frames a second through your script.

## More

Additional usage notes and screenshots live in the
[project Wiki](https://github.com/ngduyquockhanh/JScriptor/wiki).
