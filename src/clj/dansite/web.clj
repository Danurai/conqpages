(ns dansite.web
   (:require [clojure.java.io :as io]
             [clojure.data.json :as json]
             [compojure.core :refer [defroutes GET POST ANY]]
             [compojure.route :refer [not-found resources]]
             [ring.util.response :refer [response resource-response content-type redirect]]
             [ring.middleware.params :refer [wrap-params]]
             [ring.middleware.keyword-params :refer [wrap-keyword-params]]
             [ring.middleware.nested-params :refer [wrap-nested-params]]
             [ring.middleware.session :refer [wrap-session]]
             [clojure.java.jdbc :as j]
             [cemerick.friend :as friend]
               (cemerick.friend [workflows :as workflows]
                              [credentials :as creds])
             [hiccup.page :as h]
             [dansite.misc :as misc]
             [dansite.pages :as pages]))
   
(def cards (json/read-str (slurp (io/resource "public/js/data/wh40k_cards.json")) :key-fn keyword))
            
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
                  :password (creds/hash-bcrypt "admin")
                  :roles #{::admin}}
           "dan"  {:username "dan"
                  :password (creds/hash-bcrypt "user")
                  :roles #{::user}}})          

(derive ::admin ::user)
   
(defroutes app-routes
  (GET "/" req
    (h/html5 
      misc/pretty-head
      [:body
        (misc/navbar req)]))
  (GET "/decks" []
    (friend/authorize #{::user}
      pages/deckbuilder))
  (GET "/collection" []
    pages/collection)
  (GET "/find" [q]
    (h/html5 
      misc/pretty-head
      [:body
        (misc/navbar nil)
        [:div.container.my-2
          [:table.table.table-hover
            [:thead [:tr [:td "Name"][:td "Faction"][:td "Type"][:td "Cost"][:td [:i.fas.fa-cog]][:td "Set"]]]
            [:tbody
              (map (fn [r]
                [:tr
                  [:td [:a.card-tooltip {:href (str "/card/" (:code r)) :data-code (:code r)} (:name r)]]
                  [:td (:faction r)]
                  [:td (:type r)]
                  [:td (:cost r)]
                  [:td {:title (:signature_loyal r)} (case (:signature_loyal r) "Signature" [:i.fas.fa-cog.icon-sig] "Loyal" [:i.fas-fa-crosshairs.icon-loyal] "")]
                  [:td (str (:pack r) " #" (-> r :position Integer.))]
                 ])
                (->> cards :data (filter #(some? (re-find (re-pattern (str "(?i)" q)) (:name %))))))
            ]]]]))
  (GET "/card/:code{[0-9]+}" [code]
    (h/html5 
      misc/pretty-head
      [:body
        (misc/navbar nil)
        [:div.container.my-2
          ((fn [r]
            [:div.row
              [:div.col-sm
                [:div.card  
                  [:div.card-header [:h2 (:name r)]]
                  [:div.card-body (:text r)]
                  [:div.card-footer.text-muted.d-flex.justify-content-between
                    [:span (:faction r)]
                    [:span (str (:pack r) " #" (-> r :position Integer.))]]]]
              [:div.col-sm
                [:img {:src (:img r) :alt (:name r)}]]])
            (->> cards :data (filter #(= (:code %) code)) first))
        ]]))
  (GET "/login" req
    (h/html5
      misc/pretty-head
      [:body  
        (misc/navbar req)
        [:div.container
          [:div.row.my-2
            [:div.col-sm-6.offset-3
              [:div.card.mt-2
                [:div.card-header "Login"]
                [:div.card-body
                  [:form {:action "login" :method "post"}
                    [:div.form-group
                      [:label {:for "username"} "Name"]
                      [:input#username.form-control {:type "text" :name "username" :placeholder "Username or email address" :auto-focus true}]]
                    [:div.form-group
                      [:label {:for "password"} "Password"]
                      [:input#userpassword.form-control {:type "password" :name "password" :placeholder "Password"}]]
                    [:button.btn.btn-warning.float-right {:type "submit"} "Login"]]]]]]]]))
  (friend/logout
    (ANY "/logout" [] (redirect "/")))
  (POST "/decks/save" [deck-content deck-name]  (save-deck-handler deck-name deck-content) )
  ;; (GET "/card/:code{[0-9]+}" [code] (str "Hello " code))
  (resources "/"))
   
(def app 
  (-> app-routes
    (friend/authenticate 
      {:allow-anon? true
       :login-uri "/login"
       :default-landing-uri "/"
       :credential-fn #(creds/bcrypt-credential-fn users %)
       :workflows [(workflows/interactive-form)]})
    (wrap-keyword-params)
    (wrap-params)
    (wrap-session)
    ))
   