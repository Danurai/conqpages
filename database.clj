(ns dansite.database
  (:require 
    [clojure.java.jdbc :as j]
    [clj-time.core :as t]
    [clj-time.coerce :as c]
    [cemerick.friend :as friend]
      (cemerick.friend [credentials :as creds])))

; Role Hierarchy
(derive ::admin ::user)

; Define sqlite for local, or system (postgresql)
(def db (or (System/getenv "DATABASE_URL")
            {:classname   "org.sqlite.JDBC"
             :subprotocol "sqlite"
             :subname     "resources/db/db.sqlite3"}))

; Local postgresql for testing
;(def db {:dbtype "postgresql"
;         :dbname "conq_db"
;         :host "localhost"
;         :port "5432"
;         :user "conq_user"
;         :password "user"})
      
(defn create-db []
  (if (= (:subprotocol db) "sqlite") ; Split for AUTOINCREMENT / NEXTVAL
  ; Create User table in SQLITE
      (try
        (j/db-do-commands db 
          (j/create-table-ddl :users
            [[:uid      :integer :primary :key :AUTOINCREMENT]
             [:username :text]
             [:password :text]
             [:admin    :boolean]
             [:created  :date]]))
        (j/insert! db :sqlite_sequence {:name "users" :seq 1000})
        (j/insert! db :users {:username "root" :password (creds/hash-bcrypt "admin") :admin true  :created (t/now)})
        (j/insert! db :users {:username "dan"  :password (creds/hash-bcrypt "user")  :admin false :created (t/now)})
        (catch Exception e (println (str "DB Error - Users: " e))))
  ; Create User Table in postgresql
      (try
        (j/db-do-commands db ["create sequence user_uid_seq minvalue 1000"])
        (j/db-do-commands db 
          (j/create-table-ddl :users
            [[:uid      :int :default "nextval ('user_uid_seq')"]
             [:username :text]
             [:password :text]
             [:admin    :boolean]
             [:created  :bigint]]))
        (j/insert! db :users {:username "root" :password (creds/hash-bcrypt "admin") :admin true  :created (c/to-long (t/now))})
        (j/insert! db :users {:username "dan"  :password (creds/hash-bcrypt "user")  :admin false :created (c/to-long (t/now))})
        (catch Exception e (println (str "DB Error - Users: " e)))))
  ; Create Database Table and a sample deck
  (try
    (j/db-do-commands db
      (j/create-table-ddl :decklists
        [[:uid         :text :primary :key]
         [:name        :text]
         [:author      :integer]
         [:data        :text]
         [:tags        :text]
         [:notes       :text]
         [:created     :bigint]
         [:updated     :bigint]]))
    ; (j/insert! db :decklists {:uid "010101" :name "Marine Corps" :author 1001
    ;  :data "{\"010001\" 1}" 
    ;  :created (c/to-long t/now) :updated (c/to-long t/now)})
    (catch Exception e (println (str "DB Error - Decklists: " e))))
  (try
    (j/db-do-commands db
      (j/create-table-ddl :version
        [[:major    :int]
         [:minor    :int]
         [:note     :text]
         [:released :bigint]]))
    (j/insert! db :version {:major 0 :minor 1 :note "dev" :released (c/to-long (t/now))})
    (catch Exception e (str "DB Error - version: " e))))

; USERS
    
(defn users []
  (->> (j/query db ["select * from users"])
       (map (fn [x] (hash-map (:username x) (-> x (dissoc :admin)(assoc :roles (if (or (= 1 (:admin x)) (true? (:admin x))) #{::admin} #{::user}))))))
       (reduce merge)))

(defn get-authentications [req]
  (#(-> (friend/identity %) :authentications (get (:current (friend/identity %)))) req))

; DECKS
  
(defn update-or-insert!
  "Updates columns or inserts a new row in the specified table"
  [db table row where-clause]
  (j/with-db-transaction [t-con db]
    (let [result (j/update! t-con table row where-clause)]
      (if (zero? (first result))
        (j/insert! t-con table row)
        result))))

(defn rnd-deckid []
  (->> (fn [] (rand-nth (seq (char-array "ABCDEF0123456789"))))
       repeatedly
       (take 6)
       (apply str)))
(defn deck-ids []
  (map (fn [x] (:uid x))(j/query db ["SELECT uid FROM decklists"])))      
(defn unique-deckid []
; get all deck IDs to compare to
  (let [uids (deck-ids)]
    (loop []
      (let [uid (rnd-deckid)]
        (if (.contains uids uid)
          (recur)
          uid)))))
            
(defn save-deck [id name decklist tags notes uid]
  (let [deckid (if (clojure.string/blank? id) (unique-deckid) id)
        qry    {:uid deckid :name name :data decklist :tags tags :notes notes :author uid :updated (c/to-long (t/now))}
        where-clause ["uid = ?" deckid]]
    (j/with-db-transaction [t-con db]
      (let [result (j/update! t-con :decklists qry where-clause)]
        (if (zero? (first result))
          (j/insert! t-con :decklists (assoc qry :created (c/to-long (t/now)) :updated (c/to-long (t/now))))
          result)))))
    
    
(defn get-user-decks [uid]
  (j/query db ["SELECT * FROM decklists WHERE author = ? ORDER BY UPDATED DESC" uid]))
  
(defn get-user-deck [deckid]
  (first (j/query db ["SELECT * FROM decklists WHERE uid = ?" deckid])))  
  
