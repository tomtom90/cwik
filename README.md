# cwik

cwik is an open, interactive and exptensable Clojure (https://www.clojure.org/) framework  for automating QUIC interop tests
and performance measurements, based on the Java-based Kwik Library (https://bitbucket.org/pjtr/kwik/). It can be used for defininig test
campaigns programmatically and can enable reproducible experiments, facilitate automatic evaluation etc.

To get started open the project root directory, go to "src/cwik" and start a Clojure REPL:

```bash
~/cwik/src/cwik: lein repl
```

When REPL is started, you are able to use the commands like:

```clojure
(testSingleRequestSingleHost "quic.aiortc.org" 443 "/")
```
Which will return either Clojure vector containing the target content from quic.aiortc.org:443/, the time it took to send the request as well as the total packets send and the amount of packets lost or en error string if the request failed, or an error object if the connection failed.

## Dependencies

- Java (>= java 11.0.5)
- Gradle (>= 4.4.1)
- Clojure (>= ncurses 6.1.20181013)
- Leiningen (>= 2.9.0) 
- Kwik (commit 5a80a6b or newer)

## Example with response
Opening three connections to host: quic.aiortc.org on port 443, send HTTP Get to path "/", "/3" and "/56".

```clojure
(testMultiRequestMultipleHosts [["quic.aiortc.org" 443]["quic.aiortc.org" 443]["quic.aiortc.org" 443]] ["/" "/3" "/56"])   
(
    ["
        <!DOCTYPE html>
        <html>
            <head>
                <meta charset=\"utf-8\"/>
                <title>aioquic</title>
                <link rel=\"stylesheet\" href=\"/style.css\"/>
            </head>
            <body>
                <h1>Welcome to aioquic</h1>
                <p>
                    This is a test page for
                    <a href=\"https://github.com/aiortc/aioquic/\">aioquic</a>
                    ,a QUIC and HTTP/3 implementation written in Python.
                </p>
                <h2>Available endpoints</h2>
                <ul>
                    <li><strong>GET /</strong>returns the homepage</li>
                    <li><strong>GET /NNNNN</strong> returns NNNNN bytes of plain text</li>
                    <li><strong>POST /echo</strong> returns the request data</li>
                    <li>
                        <strong>CONNECT /ws</strong> runs a WebSocket echo service.
                        You must set the <em>:protocol</em> pseudo-header to
                        <em>\"websocket\"</em>.
                    </li>
                    <li>There is also an <a href=\"/httpbin/\">httpbin instance</a>.</li>
                </ul>
            </body>
        </html>"
        39665801
        9
        0
    ]
    ["ZZZ" 22362560 8 0]
    ["ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ" 32750340 8 0]
)
```

## API

Function to create a connection to a host:

```clojure
(connectHost <String:hostname> <Number:port> [<Number:timeout>])
  returns <QuicConnection> or in case of failure a <Vector:<Boolean> <ErrorMessage> <GenericMessage>>

(connectHost <Vector:<String:hostname><Number:port>[<Number:time out>]>)
  returns <QuicConnection> or in case of failure a <Vector:<Boolean:hasFailed><String:ErrorMessage><String:GenericMessage>>
```

Function to send an HTTP request:

```clojure
(httpRequest <Vector:<String:path><net.luminis.quic.QuicConnection>>)
  returns <Vector:<String:responseText><Number:requestTimeInNs>> or in case of failure a <String:ErrorMessage>
```

Function to send a number of HTTP request:

```clojure
(repeatedRequest <String:path> <QuicConnection:con> <Number:repeats>)
  returns <Vector:<Vector:<String:responseText><Number:requestTimeInNs>>...>
```
 Function to determine the average time of all responses:
 
```clojure
(getAverageTime <Vector:response>)
  returns <Number:averageTime>
```
 
 Function to determine the median time of all responses:
 
```clojure
(getMedianTime <Vector:response>)
  returns <Number:medianTime>
```
 
 Function to determine minimum and maximum time of all responses:
 
```clojure
(getMinMax <Vector:response>)
  returns <Vector:<Number:minTime><Number:maxTime>>
```
 
 Function to determine minimum, median, average and maximum time of all responses:
 
```clojure
(getStatistics <Vector:response>)
  returns <Vector:<Number:minTime><Number:medianTime><Number:averageTime><Number:maxTime>>
```
  
 Function to open multiple connections
 
```clojure
(multiConnection <Vector:<String:hostname><Number:port>[<time out>]>)
  returns <lazy-sequenz:connections>
```
  
 Function to open a single connection and send a request.
 
```clojure
(testSingleRequestSingleHost <String:hostname> <Number:Port> <String:path> [<Number:timeout>])
  returns <lazy-sequnez:responses>
```
    
 Function to open a single connection and send a request multiple times.
 
```clojure
(testRepeatedRequestSingleHost <String:hostname> <Number:port> <String:path> <Number:repeatCount> [<Number:timeout>])
  returns <lazy-sequnez:responses>
```
    
 Function to open a single connection and send multiple requests in parallel.
 
```clojure
(testMultipleRequestSingleHost <String:hostname> <Number:port> <Vector:<String:path>...> [<Number:timeout>])
  returns <Vector:responses>
```
    
 Function to open multiple connections and send the same request on each connection.
 
```clojure
(testSingleRequestMultipleHosts <Vector:<Vector:<String:hostname><Number:port>[<Number:timeout>]>...> <String:path>)
  returns <Vector:responses>
```
    
 Function to open multiple connections and send a request each.
 
```clojure
(testSingleRequestMultipleHosts <Vector:<Vector:<String:hostname><Number:port>[<Number:timeout>]>...> <Vector:<String:path>...>)
  returns <Vector:responses>
```

## Limitations

Tested to open up to 100 streams may fail for greater numbers.

**Version Negotiation**
Versions supported as of the included version of kwik: 20, 22, 23. Currently the version negotiation does not work, the Version used is the most resent version implemented in the kwik library.
If another version is to be used, the version would need to be changed manually.

**Error Handling**
Severel errors are yet to be catched and are therefore thrown.

Connections are currently not closed in case of an error and/or\
manually due to missing support in the Kwik implementations.

## How-To Update the Kwik Library
A working Kwik library is included in the project files. However if you wish to change to another version (update / rollback)
go to the cwik project root and start ./KwikUpdater.sh\
__In order to use the updater git needs to be installed!__

Usage:

__**./KwikUpdater.sh**__\
Get the default repository https://bitbucket.org/pjtr/kwik.git

__**./KwikUpdater.sh <Kwik_root_folder>**__\
Changes the source files in a given Kwik project.

__**./KwikUpdater.sh git <git_repository_link>**__\
clones a user speciefied git repository.

__**./KwikUpdater.sh <?|help>**__\
Print the help dialog.

## License

This program is open source and licensed under LGPL (see the LICENSE.txt and LICENSE-LESSER.txt files in the distribution). This means that you can use this program for anything you like, and that you can embed it as a library in other applications, even commercial ones. If you do so, the author would appreciate if you include a reference to the original.
As of the LGPL license, all modifications and additions to the source code must be published as (L)GPL as well.
cwik makes use of the Kwik library written by Peter Doornbosch https://bitbucket.org/pjtr/kwik/
