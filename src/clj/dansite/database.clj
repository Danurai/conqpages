(ns dansite.database
  (:require 
    [clojure.java.jdbc :as j]
    [cemerick.friend [credentials :as creds]]))

; Role Hierarchy
(derive ::admin ::user)

; Define sqlite for local, or system (postgresql)
(def db (or (System/getenv "DATABASE_URL")
            {:classname   "org.sqlite.JDBC"
             :subprotocol "sqlite"
             :subname     "resources/db/db.sqlite3"}))
      
(defn create-db []
  (try
    (j/db-do-commands db
      (j/create-table-ddl :users
        [[:uid      :int :primary :key]
         [:username :text]
         [:password :text]
         [:admin    :boolean]]))
    (j/insert! db :users {:username "root" :password (creds/hash-bcrypt "admin") :admin true :uid 10000})
    (j/insert! db :users {:username "dan" :password (creds/hash-bcrypt "user") :admin false :uid 10001})
    (catch Exception e (println (str "user table creation failed " e))))
  (try
    (j/db-do-commands db
      (j/create-table-ddl :decklists
        [[:name   :text]
         [:author :int]
         [:data   :blob]]))
    (catch Exception e (println (str "decklist table creation failed " e)))))
           
(defn users []
  (->> (j/query db ["select * from users"])
       (map (fn [x] (hash-map (:username x) (-> x (dissoc :admin)(assoc :roles (if (or (= 1 (:admin x)) (true? (:admin x))) #{::admin} #{::user}))))))
       (reduce merge)))
      
(defn update-or-insert!
  "Updates columns or inserts a new row in the specified table"
  [db table row where-clause]
  (j/with-db-transaction [t-con db]
    (let [result (j/update! t-con table row where-clause)]
      (if (zero? (first result))
        (j/insert! t-con table row)
        result))))

(defn save-deck [name deck uid]
   (update-or-insert! db :decklists {:data deck :author uid :name name} ["name = ? AND author = ?" name uid]))

(defn decks [uid]
  (j/query db ["SELECT * FROM decklists WHERE author = ?" uid]))