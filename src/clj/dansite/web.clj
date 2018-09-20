(ns dansite.web
   (:require [clojure.java.io :as io]
             [clojure.data.json :as json]
             [compojure.core :refer [defroutes GET POST ANY context]]
             [compojure.route :refer [not-found resources]]
             [ring.util.response :refer [response resource-response content-type redirect status]]
             [ring.middleware.params :refer [wrap-params]]
             [ring.middleware.keyword-params :refer [wrap-keyword-params]]
             [ring.middleware.nested-params :refer [wrap-nested-params]]
             [ring.middleware.session :refer [wrap-session]]
             [cemerick.friend :as friend]
               (cemerick.friend [workflows :as workflows]
                              [credentials :as creds])
             [hiccup.page :as h]
             [dansite.misc :as misc]
             [dansite.pages :as pages]
             [dansite.database :as db]))

(defn- save-deck-handler [id name decklist tags notes req]
  (db/save-deck id name decklist tags notes (-> req misc/get-authentications :uid))
  (reset! misc/alert {:type "alert-info" :message "Deck saved"})
  (redirect "/decks"))
 
(defroutes deck-routes
  (GET "/" []
    pages/decklist)
  (GET "/new" []
    pages/newdeck)
  (GET "/new/:id" []
    pages/deckbuilder)
  (GET "/edit/:id" []
    pages/deckbuilder)
  (POST "/save" [deck-id deck-name deck-content deck-tags deck-notes]  
    (friend/wrap-authorize 
      #(save-deck-handler deck-id deck-name deck-content deck-tags deck-notes %) 
      #{::db/user}))
  (POST "/delete" [deletedeckuid]
    (do 
      (db/delete-deck deletedeckuid)
      (reset! misc/alert {:type "alert-warning" :message "Deck deleted"})
      (redirect "/decks"))))

(defroutes admin-routes
  (GET "/" []
    pages/useradmin)
  (POST "/updatepassword" [uid password]
    (db/updateuserpassword uid password)
    (reset! misc/alert {:type "alert-info" :message "Password updated"})
    (redirect "/admin"))
  (POST "/updateadmin" [uid admin]
    (db/updateuseradmin uid (some? admin))
    (reset! misc/alert {:type "alert-info" :message (str "Admin status " (if (some? admin) "added" "removed"))})
    (redirect "/admin"))
  (POST "/adduser" [username password admin]
    (db/adduser username password (= admin "on"))
    (reset! misc/alert {:type "alert-info" :message (str "User Account created for " username)})
    (redirect "/admin"))
  (POST "/deleteuser" [uid]
    (reset! misc/alert {:type "alert-warning" :message "User Account Deleted"})
    (db/dropuser uid)))
    
    
(defroutes app-routes
  (GET "/" []
    pages/home)
  (context "/decks" []
    (friend/wrap-authorize deck-routes #{::db/user}))
  (context "/admin" []
    (friend/wrap-authorize admin-routes #{::db/admin}))
    ;admin-routes)
  (GET "/cards" []
    pages/searchpage)  
  (GET "/collection" []
    pages/collection)
  (GET "/litmus" []
    (friend/wrap-authorize pages/litmus #{::db/user}))
  ; Login
  (GET "/login" []
    pages/login)
  (GET "/register" []
    pages/register)
  (friend/logout
    (ANY "/logout" [] (redirect "/")))
  ; TODO wrap search?
  (GET "/find" [q]
    (pages/findcards q))
  (GET "/cycle/:id" [id]
    ;(pages/findcards (str "y:" id)))
    (let [cycle_code (->> misc/cycles :data (filter #(= (:position %) (read-string id))) first :code)]
      (pages/findcards (str "e:" (->> misc/packs :data (filter #(= (:cycle_code %) cycle_code)) (map :code) (clojure.string/join "|"))))))
  (GET "/pack/:id" [id]
    (pages/findcards (str "e:" id)))
  (GET "/card/:code{[0-9]+}" [code]
    (pages/cardpage code))
  ; API
  (context "/api/data" []
    (GET "/cards" [] (content-type (response (slurp (io/resource "data/wh40k_cards.min.json"))) "application/json"))
    (GET "/packs" [] (content-type (response (slurp (io/resource "data/wh40k_packs.min.json"))) "application/json"))
    (GET "/cycles" [] (content-type (response (slurp (io/resource "data/wh40k_cycles.min.json"))) "application/json"))
    (GET "/factions" [] (content-type (response (slurp (io/resource "data/wh40k_factions.min.json"))) "application/json"))
    (GET "/types" [] (content-type (response (slurp (io/resource "data/wh40k_types.min.json"))) "application/json")))
  (resources "/"))
   
(def app 
  (-> app-routes
    (friend/authenticate 
      {:allow-anon? true
       :login-uri "/login"
       :default-landing-uri "/"
       :unauthorized-handler
          #(-> (h/html5 
                misc/pretty-head 
                [:body 
                  (misc/navbar %) 
                  [:div.container 
                    [:div.mt-2.h2 "Access Denied: " (:uri %)]]])
            response (status 401))
       :credential-fn #(creds/bcrypt-credential-fn (db/users) %)
       :workflows [(workflows/interactive-form)]})
    (wrap-keyword-params)
    (wrap-params)
    (wrap-session)
    ))
   