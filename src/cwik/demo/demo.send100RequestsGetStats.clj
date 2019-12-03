(comment "
Send 100 http requests to a server and get statistical data from the requests
")

#!Connect to remote host
(def trialConnection (connectHost ["quic.aiortc.org" 443]))

#!Send repeated requests
(def stats [[(getStatistics (repeatedRequest "/178786" trialConnection 100))] [(.getSent (.getStats trialConnection))(.getLost (.getStats trialConnection))]])
(println stats)
