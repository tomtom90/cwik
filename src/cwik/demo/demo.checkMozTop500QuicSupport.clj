(comment "
A code loading moz top 500 web page addresses and test if a connection can be established at port 443
")

#!Get the csv from ttps://moz.com/top-500/download/?table=top500Domains
(def csvList (clojure.string/split-lines(slurp "https://moz.com/top-500/download/?table=top500Domains")))
(def hostList [])

#!Clean quotation marks from CSV and split the values
(dotimes [i (count csvList)] (def hostList (conj hostList [(nth (clojure.string/split (clojure.string/replace (nth csvList i) "\"" "") #",") 1) 443])))

#!Try to open connections
(def connectionList (pmap connectHost hostList))

#!Evaluate results and print
(def resList [])
(dotimes [i (count hostList)]
    (if (vector? (nth connectionList i))
      (def resList (conj resList [false (nth (nth hostList i) 0) (nth (nth hostList i) 1)]))
      (def resList (conj resList [true (nth (nth hostList i) 0) (nth (nth hostList i) 1)]))
      ))
(print resList)