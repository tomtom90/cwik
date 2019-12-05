(comment "
Copyright © 2019 Thomas Ripping

This file is part of cwik, a QUIC test framework clojure library
cwik is free software: you can redistribute it and/or modify it under
the terms of the GNU Lesser General Public License as published by the
Free Software Foundation, either version 3 of the License, or (at your option)
any later version.
cwik is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
more details.

You should have received a copy of the GNU Lesser General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.

cwik makes use of the Kwik library written by Peter Doornbosch <https://bitbucket.org/pjtr/kwik/>")


(ns cwik.core
  (:gen-class)
  (:import (net.luminis.quic QuicConnection SysOutLogger Version QuicStream)
           (java.io BufferedReader InputStreamReader))
  )

(def connectionTimeOut 3000)


(comment
    Creates a connection to a Host

    @param host Hostname or IP as string
    @param port Port no. as long
    @param timeOut in ms as long (optional)

    @param hostVector (host, port <?timeOut>)

    @return connectionVector or Object or a vector in case the connection fails
    )
(defn connectHost ([host port & [timeOut]]
               (try
                 (let [logger (SysOutLogger.)]
                   (let [con (QuicConnection. host port (Version/getDefault) logger)]
                     (if (nil? timeOut)
                       (.connect con connectionTimeOut)
                       (.connect con timeOut))
                     (println (.toString (.connectionState con)))
                     con))
                 (catch Exception e
                   (let [res [true (.getMessage e) (str "Connection failed for " host ":" port)]]
                     (print res)
                     res))))
  ([hostVector]
   (try
     (let [logger (SysOutLogger.)]
       (let [con (QuicConnection. (nth hostVector 0) (nth hostVector 1) (Version/getDefault) logger)]
         (if (< (count hostVector) 3)
           (.connect con connectionTimeOut)
           (.connect con (nth hostVector 2)))
         con))
     (catch Exception e
       (let [res [true (.getMessage e) (str "Connection failed for " (nth hostVector 0) ":" (nth hostVector 1))]]
         (print res)
         res))))
  )

(comment
  Sends a http request on a given connection requesting the given path

  @param conVec Vector (path domain excluded, connection)

  @return A vector in case of success (http-response, request time in ns, packages sent, packages lost)
          and a String containing the error message in case of failure.
  )
(defn httpRequest [conVec]
  (if (and (vector? (last conVec)) (boolean? (first (last conVec))))
    (last conVec)
    (try
      (let [stream (.createStream (last conVec) true)]
        (let [startTime (. System (nanoTime))]
          (.write (.getOutputStream stream) (.getBytes (str "GET " (first conVec) "\r\n")))
          (.close (.getOutputStream stream))
          (let [stopTime (. System (nanoTime))]
            (Thread/sleep 80)
            (let [resString (StringBuilder.)]
              (let [buffer (BufferedReader. (InputStreamReader. (.getInputStream stream)))]
                (while (and (complement nil?) (def line (.readLine buffer)) ((complement nil?) line))
                  (.append resString line)
                  )
                (vector (.toString resString) (- stopTime startTime) (.getSent (.getStats (last conVec))) (.getLost (.getStats (last conVec)))))))))
      (catch Exception e
        (.getMessage e))))
    )

(comment
  Sends a given amount of parallel requests to a given path on a given connection.

  @param path String for the file path, domain or ip excluded.
  @param con connection object
  @param times Amount of repeats

  @return A vector of httpRequest-vectors
  )
(defn repeatedRequest [path con times]
  (def conVec [])
  (if (and (vector? con) (boolean? (first con)))
    (println (last con))
  (dotimes [i times] (def conVec (conj conVec [path con]))))
  (pmap httpRequest conVec))

(comment
  Calculates the average request time from a vector of httpRequest-vectors,

  @param A vector of httpRequest-vectors

  @return Result as long or es fraction
  )
(defn getAverageTime [resVector]
  (let [sizeVector (count resVector)]
    (def devider sizeVector)
    (def sum 0)
    (dotimes [i sizeVector]
      (if (> (count (nth resVector i)) 1)
        (def sum (+ sum ((nth resVector i) 1)))
        (def devider (dec devider))))
    (if (> devider 0)
      (/ sum devider)
      (let [res 0]
        res))))

(comment
  Calculates the median request time from a vector of httpRequest-vectors without trimming.

  @param A vector of httpRequest-vectors

  @return Result as long or es fraction
  )
(defn getMedianTime [resVector]
  (let [sizeVector (count resVector)]
    (def timings (vector))
    (def devider sizeVector)
    (dotimes [i sizeVector]
      (if (> (count (nth resVector i)) 1)
        (def timings (conj timings ((nth resVector i) 1)))
        (def devider (dec devider))
        ))
    (def timings (sort timings))

    (if (> devider 0)
     (if (odd? devider)
      (let [medTime (nth timings (/ (dec devider) 2))] medTime)
      (/ (+ (nth timings (/ devider 2)) (nth timings (inc (/ devider 2)))) 2))
     (let [res 0]
       res))))

(comment
  Calculates the average request time from a vector of httpRequest-vectors

  @param A vector of httpRequest-vectors

  @return Vector (min request time, max request time)
  )
(defn getMinMax [resVector]
  (let [sizeVector (count resVector)]
    (def timings [])
    (dotimes [i sizeVector]
      (if (> (count (nth resVector i)) 1)
        (def timings (conj timings ((nth resVector i) 1)))))
    (def timings (sort timings))
    (vector (first timings) (last timings))
    ))

(comment
  Calculates the average, median, min and max request time from a vector of httpRequest-vectors

  @param A vector of httpRequest-vectors

  @return Vector (min , median, average, max)
  )
(defn getStatistics [resVector]
  (let [med (getMedianTime resVector)]
    (let [minmax (getMinMax resVector)]
      (let [averageTime (getAverageTime resVector)]
        (vector (first minmax) med averageTime (last minmax))))))

(comment
  Open a single connection and send a request.

  @param host Hostname or IP as string
  @param port Port no. as long
  @param path String for the file path, domain or ip excluded.
  @param timeOut in ms as long (optional)

  @return vector of response objects
  )
(defn testSingleRequestSingleHost [host port path & [timeOut]]
  (if (nil? timeOut)
    (httpRequest [path (connectHost host port)])
    (httpRequest [path (connectHost host port timeOut)])))

(comment
  Open a single connection and send a request multiple times.

  @param host Hostname or IP as string
  @param port Port no. as long
  @param path String for the file path, domain or ip excluded.
  @param requestCount Amount of requests send
  @param timeOut in ms as long (optional)

  @return vector of response objects
  )
(defn testRepeatedRequestSingleHost [host port path requestCount & [timeOut]]
  (if (nil? timeOut)
    (repeatedRequest path (connectHost host port) requestCount)
    (repeatedRequest path (connectHost host port timeOut) requestCount)))

(comment
  Open a single connection and send multiple requests in parallel.

  @param host Hostname or IP as string
  @param port Port no. as long
  @param path Vector of strings for the file path, domain or ip excluded.
  @param timeOut in ms as long (optional)

  @return vector of response objects
  )
(defn testMultipleRequestSingleHost [host port path & [timeout]]
  (if (nil? timeout)
    (def hostConnection (connectHost host port))
    (def hostConnection (connectHost host port timeout))
    )
  (def requestVectors [])
  (dotimes [i (count path)] (def requestVectors (conj requestVectors [(nth path i) hostConnection])))
  (pmap httpRequest requestVectors))

(comment
  Open multiple connections and send the same request on each connection.

  @param host Vector of hostVectors [[String host, long port]...]
  @param path String for the file path, domain or ip excluded.
  @param timeOut in ms as long (optional)

  @return vector of response objects
  )
(defn testSingleRequestMultipleHosts [hostVector path]
  (def connections [])
  (def connections (pmap connectHost hostVector))
  (def requestVectors [])
  (def failedConnections [])
  (dotimes [i (count connections)] (if (not (vector? (nth connections i)))
                                     (def requestVectors (conj requestVectors [path (nth connections i)]))
                                     (def failedConnections (conj failedConnections (nth connections i)))))
  (conj failedConnections (pmap httpRequest requestVectors))
  )

(comment
  Open multiple connections and send a request each.

  @param host Vector of hostVectors [[String host, long port] ...]
  @param path Vector of Strings for the file path, domain or ip excluded [String path_0 String path_1 ... ] .

  @return vector of response objects
  )
(defn testMultiRequestMultipleHosts [hosts path]
  (def connections (pmap connectHost hosts))
  (def connections (into [] connections))
  (def requests [])
  (if (= (count hosts) (count path))
    (do (dotimes [i (count path)] (def requests (conj requests [(nth path i) (nth connections i)])))
      (pmap httpRequest requests))
    (println "Input Error: The host and the path vectors must be of the same length!")))

(defn -main [& args]
  (println "cwik a testframework for quic")
  (println "Version 1.0.0?")
  )
#!quic.aiortc.org:443 //Currently the only working remote host

#!127.0.0.1:4433
#!python3 http3_server.py --certificate ../tests/ssl_cert.pem --private-key ../tests/ssl_key.pem


