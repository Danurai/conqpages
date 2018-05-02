(ns dansite.system
   (:gen-class)
   (:require [org.httpkit.server :refer [run-server]]
            [com.stuartsierra.component :as component]
            [dansite.web :refer [app]]
            [dansite.database :as db :refer [create-db]]))

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
    (db/create-db)
    (assoc this :server (start-server #'app (Integer/parseInt (get (System/getenv) "PORT" "9009")))))
  (stop [this]
    (stop-server (:server this))
    (dissoc this :server)))

(defn create-system []
  (AppRecord.))

(defn -main [& args]   
  (.start (create-system)))