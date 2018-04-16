(ns dansite.system
   (:gen-class)
   (:require [org.httpkit.server :refer [run-server]]
            [com.stuartsierra.component :as component]
            [dansite.web :refer [app]]))

(defn- start-server [handler port]
  (let [server (run-server handler {:port port})]
    (println (str "Server started on http://localhost:" port))
    server)) ;; return itself

(defn- stop-server [server]
  (when server
    (server))) ;; run-server returns a fn that stops itself
    
(defrecord AppRecord []
  component/Lifecycle
  (start [this]
    (assoc this :server (start-server #'app 8080)))
  (stop [this]
    (stop-server (:server this))
    (dissoc this :server)))

(defn create-system []
  (AppRecord.))

(defn -main [& args]   
  (.start (create-system)))