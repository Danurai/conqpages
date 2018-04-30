(ns dansite.web
   (:require [clojure.java.io :as io]
             [clojure.data.json :as json]
             [compojure.core :refer [defroutes GET POST ANY context]]
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
             [dansite.pages :as pages]
             [dansite.users :as users :refer [users]]))
   
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
 
(defroutes deck-routes
  (GET "/" []
    pages/decklist)
  (GET "/new" []
    pages/newdeck)
  (GET "/edit" []
    pages/deckbuilder))
  
(defroutes app-routes
  (GET "/" req
    (h/html5 
      misc/pretty-head
      [:body
        (misc/navbar req)]))
  (context "/decks" []
    ; (friend/wrap-authorize deck-routes #{::users/user}))
    deck-routes)
  (GET "/collection" []
    pages/collection)
  (GET "/find" [q]
    (pages/search q))
  (GET "/pack/:id" [id]
    (pages/searchattr (str "e:" id))) ;;TODO USE STANDARD pages/search response
  (GET "/card/:code{[0-9]+}" [code]
    (pages/card code))
  (context "/api/data" []
    (GET "/cards" [] 
      (content-type (response (slurp (io/resource "public/js/data/wh40k_cards.json"))) "application/json")))
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
       :credential-fn #(creds/bcrypt-credential-fn @users %)
       :workflows [(workflows/interactive-form)]})
    (wrap-keyword-params)
    (wrap-params)
    (wrap-session)
    ))
   