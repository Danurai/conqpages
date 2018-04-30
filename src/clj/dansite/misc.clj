(ns dansite.misc
  (:require 
    [hiccup.page :as h]
    [cemerick.friend :as friend]
    [clojure.java.io :as io]
    [clojure.data.json :as json]
    [dansite.users :as users :refer [users]]))
    

(def cards (json/read-str (slurp (io/resource "public/js/data/wh40k_cards.json")) :key-fn keyword))

(def pretty-head
  [:head
  ;; Meta Tags
    [:meta {:charset "UTF-8"}]
    [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
  ;; jquery and popper
    [:script {:src "https://code.jquery.com/jquery-3.3.1.min.js" :integrity "sha256-FgpCb/KJQlLNfOu91ta32o/NMZxltwRo8QtmkMRdAu8=" :crossorigin "anonymous"}]
  ;; REMOVED - Required for Bootstrap Tooltips  [:script {:src "https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.12.9/umd/popper.min.js" :integrity "sha384-ApNbgh9B+Y1QKtv3Rn7W3mgPxhU9K/ScQsAP7hUibX39j7fakFPskvXusvfa0b4Q" :crossorigin "anonymous"}]
  ;; Bootstrap  
    [:link   {:rel "stylesheet" :href "https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css" :integrity "sha384-Gn5384xqQ1aoWXA+058RXPxPg6fy4IWvTNh0E263XmFcJlSAwiGgFAW/dAiS6JXm" :crossorigin "anonymous"}]
    [:script {:src "https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/js/bootstrap.min.js" :integrity "sha384-JZR6Spejh4U02d8jOt6vLEHfe/JQGiRRSQQxSfFWpi1MquVdAyjUar5+76PVCmYl" :crossorigin "anonymous"}]
  ;; Font Awesome
    [:script {:defer true :src "https://use.fontawesome.com/releases/v5.0.10/js/all.js"}]
  ;; JQuery Qtip2
    [:link   {:rel "stylesheet" :href "https://cdnjs.cloudflare.com/ajax/libs/qtip2/2.1.1/jquery.qtip.css"}]
    [:script {:src "https://cdnjs.cloudflare.com/ajax/libs/qtip2/2.1.1/jquery.qtip.js"}]
  ;; TAFFY JQuery database
    [:script {:src "https://cdnjs.cloudflare.com/ajax/libs/taffydb/2.7.2/taffy-min.js"}]
  ;; Site Specific CSS
    (h/include-css "/css/style.css")
    (h/include-css "https://fonts.googleapis.com/css?family=Exo+2")   ;; <link href="https://fonts.googleapis.com/css?family=Aldrich|Electrolize|Exo|Exo+2|Jura|Mina|Play|Rationale|Sarpanch" rel="stylesheet">
    ])

(defn- navlink
"Returns hiccup code for a navbar link"
  [req title]
  (let [uri (:uri req)
        link (str "/" (clojure.string/lower-case title))]
    [:li.nav-item
      [:a.nav-link {:href link :class (if (= link uri) "active")} title]]))
    
(defn navbar [req & args]
  [:nav.navbar.navbar-expand-lg.navbar-dark {:style "background-color: teal;"}
    [:div.container
    ;; Home Brand with Icon
      [:a.navbar-brand.mb-0.h1 {:href "/"}
        [:i.fas.fa-cog.mx-2] "Home&nbsp;"]
    ;; Collapse Button for smaller viewports
      [:button.navbar-toggler {:type "button" :data-toggle "collapse" :data-target "#navbarSupportedContent" 
                            :aria-controls "navbarSupportedContent" :aria-label "Toggle Navigation" :aria-expanded "false"}
        [:span.navbar-toggler-icon]]
    ;; Collapsable Content
      [:div#navbarSupportedContent.collapse.navbar-collapse
    ;; List of Links
        [:ul.navbar-nav.mr-auto
          (navlink req "Decks")
          (navlink req "Collection")
    ;;(navlink req "Search")
          [:li.nav-item 
            [:a.nav-link.disabled "Litmus"]]] ;; {:href "/litmus"}
    ;; Inline Search Form
          [:form.form-inline.mx-2.my-lg-0 {:action "/find" :method "get"}
            [:div.input-group
              [:input.form-control {:type "search" :placeholder "search" :name "q" :aria-label "Search"}]
              [:div.input-group-append
                [:button.btn.bg-light {:type "submit"}
                  [:i.fas.fa-search]]]]]
    ;; Login Icon
          [:span.nav-item.dropdown
            [:a#userDropdown.nav-link.dropdown-toggle.text-white {:href="#" :role "button" :data-toggle "dropdown" :aria-haspopup "true" :aria-expanded "false"}
              [:i.fas.fa-user]]
              (if-let [identity (friend/identity req)]
                [:div.dropdown-menu {:aria-labelledby "userDropdown"}
                  (if (friend/authorized? #{::users/admin} (friend/identity req))
                    [:a.dropdown-item {:href "/admin"} "Admin Console"])
                  [:a.dropdown-item {:href "/logout"} "Logout"]]
                [:div.dropdown-menu {:aria-labelledby "userDropdown"}
                  [:a.dropdown-item {:href "/login"} "Login"]])]]]])