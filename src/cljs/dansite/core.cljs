(ns dansite.core
  (:require [reagent.core :as r]))
  
(defn- dansite-navbar
"Twitter Bootstrap 4.1.0 Navbar 
used on all pages. #page# defines active nav item"
  []
  [:nav.navbar.navbar-expand-lg.navbar-dark {:style {:background-color "teal"}}
    ;; Home Brand with Icon
    [:span.navbar-brand.mb-0.h1
      [:i.fas.fa-cog.mx-2] 
      "Home"]
    ;; Collapse Button for smaller viewports
    [:button.navbar-toggler {:type "button" :data-toggle "collapse" :data-target "#navbarSupportedContent" 
                          :aria-controls "navbarSupportedContent" :aria-label "Toggle Navigation" :aria-expanded "false"}
      [:span.navbar-toggler-icon]]
    ;; Collapsable Content
    [:div#navbarSupportedContent.collapse.navbar-collapse
      ;; List of Links
      [:ul.navbar-nav.mr-auto
        [:li.nav-item 
          [:a.nav-link {:href "/decks"} "Decks"]]
        [:li.nav-item 
          [:a.nav-link {:href "/folders"} "Collection"]]
        [:li.nav-item 
          [:a.nav-link.disabled "Litmus"]]] ;; {:href "/litmus"}
      ;; Inline Search Form
        [:form.form-inline.my-2.my-lg-0
          [:div.input-group
            [:input.form-control {:type "search" :placeholder "search" :aria-label "Search"}]
            [:div.input-group-append
              [:button.btn {:type "submit"}
                [:i.fas.fa-search]]]]]
      ;; Login Icon
        [:a.nav-item.nav-link.text-white.active {:href "/login" :title "Login/Logout"}
          [:i.fas.fa-user]]]
  ])
  
(defn- dansite-login  
"Twitter Bootstrap 4.1.0 Login Form"
  []
  [:div.card
    [:div.card-header "Login"]
    [:div.card-body
      [:form {:action "/login" :method "post"}
        [:div.form-group
          [:label {:for "username"} "Name"]
          [:input#username.form-control {:type "text" :placeholder "Username or email address" :auto-focus true}]]
        [:div.form-group
          [:label {:for "userpassword"} "Password"]
          [:input#userpassword.form-control {:type "password" :placeholder "Password"}]]
        [:button.btn.btn-warning.float-right {:type "submit"} "Login"]
      ]]])

(defn- dansite-app []
  [:div
    [dansite-navbar]
    [:div.container
      [:div.row.my-2
        [:div.col-sm-6.offset-3
          [dansite-login]]]]])

;;(defn ^:export main []
  (r/render [dansite-app] (js/document.getElementById "app"))
;;)
  
  