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
             [cemerick.friend :as friend]
               (cemerick.friend [workflows :as workflows]
                              [credentials :as creds])
             [hiccup.page :as h]
             [dansite.misc :as misc]
             [dansite.pages :as pages]
             [dansite.database :as db]))

(defn- save-deck-handler [id name decklist tags notes req]
  (db/save-deck id name decklist tags notes (-> req misc/get-authentications :uid))
  (reset! pages/alert {:type "alert-info" :message "Deck saved"})
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
      (reset! pages/alert {:type "alert-warning" :message "Deck deleted"})
      (redirect "/decks"))))
  
(defroutes app-routes
  (GET "/" req
    (h/html5 
      misc/pretty-head
      [:body
        (misc/navbar req)
        [:div.container.my-2
          [:ul
            [:li [:span.h5 "Decks: "] "Create or Edit Deck Lists"]
            [:li [:span.h5 "Cards: "] "View or search cards"]
            [:li [:span.h5 "Collection: "] "Browse collection in virtual folders"]
            [:li [:span.h5 "Litmus: "] "Test decks"]
            ]]]))
  (context "/decks" []
    ;deck-routes)
    (friend/wrap-authorize deck-routes #{::db/user}))
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
  (GET "/pack/:id" [id]
    (pages/searchattr (str "e:" id))) ;;TODO USE STANDARD pages/search response
  (GET "/card/:code{[0-9]+}" [code]
    (pages/cardpage code))
  ; API
  (context "/api/data" []
    (GET "/cards" [] (content-type (response (slurp (io/resource "data/wh40k_cards.min.json"))) "application/json"))
    (GET "/packs" [] (content-type (response (slurp (io/resource "data/wh40k_packs.min.json"))) "application/json"))
    (GET "/cycles" [] (content-type (response (slurp (io/resource "data/wh40k_cycles.min.json"))) "application/json"))
    (GET "/factions" [] (content-type (response (slurp (io/resource "data/wh40k_factions.min.json"))) "application/json"))
    (GET "/types" [] (content-type (response (slurp (io/resource "data/wh40k_types.min.json"))) "application/json")))
  
  ; TODO wrap-authorize?
  (resources "/"))
   
(def app 
  (-> app-routes
    (friend/authenticate 
      {:allow-anon? true
       :login-uri "/login"
       :default-landing-uri "/"
       :credential-fn #(creds/bcrypt-credential-fn (db/users) %)
       :workflows [(workflows/interactive-form)]})
    (wrap-keyword-params)
    (wrap-params)
    (wrap-session)
    ))
   