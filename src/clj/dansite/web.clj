(ns dansite.web
   (:require [clojure.java.io :as io]
            [compojure.core :refer [defroutes GET POST]]
            [compojure.route :refer [not-found resources]]
            [ring.util.response :refer [response resource-response content-type redirect]]
            [ring.middleware.params :refer [wrap-params]]
            [clojure.java.jdbc :as j]
            [cemerick.friend :as friend]
              (cemerick.friend [workflows :as workflows]
                             [credentials :as creds])))
;; Database for saving and loading decks     
(def db
  {:classname   "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname     "resources/db/conquestdb.sqlite3"
   })

(defn- create-db []
   (try (j/db-do-commands db
            (j/create-table-ddl :decks
               [[:name :text]
                [:author :int]
                [:data :blob]]))
        (catch Exception e ())))
           
(defn update-or-insert!
  "Updates columns or inserts a new row in the specified table"
  [db table row where-clause]
  (j/with-db-transaction [t-con db]
    (let [result (j/update! t-con table row where-clause)]
      (if (zero? (first result))
        (j/insert! t-con table row)
        result))))

        
(defn- save-data [name deck]
   (create-db)
   (update-or-insert! db :decks {:data deck :author 0 :name name} ["name = ?" name]))
           
(defn- save-deck-handler [name deck]
  (save-data name deck)
  (redirect "/decks")
  )
           
; a dummy in-memory user "database"
(def users {"root" {:username "root"
                  :password (creds/hash-bcrypt "admin_password")
                  :roles #{::admin}}
           "dan"  {:username "dan"
                  :password (creds/hash-bcrypt "user_password")
                  :roles #{::user}}})          
   
(defroutes app-routes
   (GET "/" [] (slurp (io/resource "public/index.html")))
   (GET "/decks" []  (slurp (io/resource "public/deckbuilder.html")))
   (GET "/folders" [] (slurp (io/resource "public/40kfolders.html")))
   (GET "/login" [] (slurp (io/resource "public/login.html")))
   
   (POST "/decks/save" [deck-content deck-name]  (save-deck-handler deck-name deck-content) ) 
   
   ;; (GET "/card/:code{[0-9]+}" [code] (str "Hello " code))
   ;; (POST "/login" [] (response "You tried to log in...."))
   (resources "/"))
   
(def app (wrap-params app-routes))
   