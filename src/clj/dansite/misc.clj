(ns dansite.misc
  (:require 
    [hiccup.page :as h]
    [cemerick.friend :as friend]
    [clojure.java.io :as io]
    [clojure.data.json :as json]
    [dansite.users :as users :refer [users]]))
    

(def cards (json/read-str (slurp (io/resource "data/wh40k_cards.min.json")) :key-fn keyword))
(def packs (json/read-str (slurp (io/resource "data/wh40k_packs.min.json")) :key-fn keyword))
(def cycles (json/read-str (slurp (io/resource "data/wh40k_cycles.min.json")) :key-fn keyword))

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
          (navlink req "Cards")
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

                  
(defn get-authentications [req]
  (#(-> (friend/identity %) :authentications (get (:current (friend/identity %)))) req))
                  
(defn signature-id [id]
  (->> cards
      :data
      (filter #(= (:code %) id))
      (map :signature_squad)
      first))
                  
(defn sig-squad [id]
  (let [sig (signature-id id)]
    (json/write-str
      (->> cards 
           :data 
           (filter #(= (:signature_squad %) sig))
           (sort-by :code)
           (map #(assoc {:code (:code %)} :qty (:quantity %)))))))

(defn signature-squad-decklist [id]
  (let [signature (signature-id id)]
    (zipmap 
      (->> cards
           :data
           (filter #(= (:signature_squad %) signature))
           (sort-by :code)
           (map :code))
      (->> cards
           :data
           (filter #(= (:signature_squad %) signature))
           (sort-by :code)
           (map :quantity)))))

(defn faction-code [id]
  (->> cards :data (filter #(= (:code %) id)) first :faction_code))        
         
(def svg 
  {
    :astra_militarum "M 67.00,10.91 C 67.00,10.91 97.00,10.91 97.00,10.91 97.00,10.91 115.00,10.00 115.00,10.00 117.95,10.04 128.48,10.16 130.35,11.75 132.17,13.30 137.53,26.96 138.68,30.00 138.68,30.00 155.06,70.00 155.06,70.00 156.76,74.35 161.09,81.54 160.76,85.99 160.45,90.12 157.00,92.01 154.09,94.42 150.03,97.78 138.14,107.01 136.21,111.04 135.34,112.86 135.48,114.08 135.62,116.00 135.62,116.00 138.13,153.00 138.13,153.00 138.68,156.24 139.98,165.87 138.13,168.40 136.36,170.44 131.41,170.04 129.00,170.00 120.85,169.85 121.01,167.21 121.00,160.00 121.00,160.00 121.00,152.00 121.00,152.00 120.96,148.23 120.49,134.96 119.26,132.06 118.15,129.44 115.12,127.18 112.61,129.62 110.89,131.29 111.31,135.65 111.20,138.00 111.20,138.00 112.38,163.00 112.38,163.00 112.96,171.21 107.23,170.11 101.00,170.00 99.38,169.97 96.94,170.04 95.60,168.98 93.62,167.41 94.00,163.30 94.00,161.00 94.00,161.00 94.00,138.00 94.00,138.00 93.99,134.50 94.11,126.62 88.14,128.66 84.48,129.90 85.01,134.93 85.00,138.00 85.00,138.00 85.00,162.00 85.00,162.00 84.99,163.78 85.17,166.82 84.01,168.26 82.17,170.53 70.77,170.70 68.60,168.98 66.31,167.17 67.00,159.83 67.00,157.00 67.00,157.00 67.90,145.00 67.90,145.00 68.07,142.66 68.68,132.67 67.90,131.19 66.27,128.02 62.16,127.82 60.60,131.19 60.60,131.19 58.09,157.00 58.09,157.00 57.43,166.70 60.29,169.86 50.00,170.00 47.94,170.02 43.33,170.30 41.66,169.01 38.61,166.48 41.06,154.83 41.66,151.00 41.66,151.00 43.00,135.00 43.00,135.00 43.00,135.00 43.00,129.00 43.00,129.00 43.07,123.38 45.59,113.76 42.40,109.09 42.40,109.09 24.58,93.87 24.58,93.87 22.57,92.09 19.41,89.64 18.70,86.99 17.78,83.51 23.56,74.08 24.81,70.00 24.81,70.00 41.05,29.00 41.05,29.00 43.23,23.19 45.73,14.05 51.10,10.91 55.94,9.56 61.66,10.99 67.00,10.91 Z M 110.97,74.50 C 109.44,75.56 106.23,77.50 105.72,79.33 105.06,81.67 107.45,83.26 109.06,84.41 112.60,86.93 118.52,91.46 123.00,90.45 125.92,89.79 136.57,83.54 137.68,80.82 139.05,77.46 132.47,67.59 129.70,65.61 127.93,64.36 127.82,64.76 126.00,64.57 122.27,65.84 114.74,71.89 110.97,74.50 Z M 41.61,79.90 C 41.75,84.60 55.65,91.51 60.00,90.49 63.33,89.70 73.37,83.94 73.39,80.17 73.41,77.77 69.73,75.65 68.00,74.41 63.18,70.97 57.34,64.93 51.18,65.64 48.25,67.45 41.51,76.42 41.61,79.90 Z"
    :chaos           "M 90.00,48.00 C 89.98,45.59 90.12,43.39 89.00,41.17 87.55,38.29 84.90,37.39 85.86,33.00 85.86,33.00 95.09,14.02 95.09,14.02 96.05,11.88 97.09,8.83 99.93,8.74 102.62,8.67 103.79,12.01 104.73,14.00 104.73,14.00 114.06,33.95 114.06,33.95 114.69,37.61 111.49,37.79 110.01,40.30 108.99,42.01 109.04,44.07 109.00,46.00 109.00,46.00 109.00,72.00 109.00,72.00 109.00,72.00 112.70,70.57 112.70,70.57 112.70,70.57 129.40,54.91 129.40,54.91 129.40,54.91 132.60,46.93 132.60,46.93 132.60,46.93 141.00,44.00 141.00,44.00 141.00,44.00 162.00,38.00 162.00,38.00 163.26,44.75 157.91,54.65 155.14,61.00 154.25,63.04 153.11,67.01 151.49,68.36 148.90,70.52 147.11,68.18 143.83,69.92 139.77,72.07 128.39,82.94 126.00,87.00 126.00,87.00 127.00,88.00 127.00,88.00 127.00,88.00 149.00,87.00 149.00,87.00 149.00,87.00 159.70,85.98 159.70,85.98 159.70,85.98 163.00,81.00 163.00,81.00 163.00,81.00 184.00,90.36 184.00,90.36 186.00,91.29 190.88,93.03 191.55,95.23 192.45,98.22 188.04,100.10 186.00,101.08 179.95,103.97 168.00,110.36 162.00,111.00 162.00,111.00 158.85,105.02 158.85,105.02 158.85,105.02 153.00,104.00 153.00,104.00 153.00,104.00 129.14,104.60 129.14,104.60 129.14,104.60 126.00,108.00 126.00,108.00 134.57,111.66 136.54,116.75 143.00,121.77 147.67,125.40 149.89,123.17 152.28,125.00 153.71,126.09 157.64,136.70 158.46,139.00 159.58,142.12 165.02,152.69 160.69,154.30 158.30,155.18 149.69,151.78 147.00,150.86 144.25,149.91 135.06,147.45 133.33,146.07 130.17,143.55 132.75,141.38 129.65,137.00 126.53,132.60 114.85,122.07 110.00,119.00 108.23,126.27 108.98,143.66 109.00,152.00 109.00,154.46 108.77,157.43 110.00,159.63 111.22,161.80 113.73,162.72 113.58,166.04 113.47,168.66 109.06,176.29 107.64,179.00 106.48,181.21 102.83,189.98 101.32,190.95 99.25,192.28 97.60,190.53 96.61,188.85 96.61,188.85 89.04,174.00 89.04,174.00 88.05,172.02 85.65,167.16 85.70,165.09 85.76,162.53 87.79,161.07 88.84,158.91 90.12,156.30 89.98,153.81 90.00,151.00 90.00,151.00 90.00,119.00 90.00,119.00 85.20,121.23 71.34,132.90 69.21,137.42 68.74,139.01 68.98,142.21 69.21,144.00 62.13,144.58 55.54,147.07 49.00,149.16 46.54,149.95 41.76,151.53 39.32,150.39 34.60,148.19 40.97,138.08 42.34,135.00 43.75,131.79 45.91,123.52 47.97,121.59 50.69,119.04 52.68,122.10 56.91,119.86 60.43,117.99 70.44,108.29 73.00,105.00 67.62,103.69 54.87,103.99 49.00,104.39 46.87,104.46 42.33,103.73 40.94,104.39 38.50,105.72 38.28,108.61 38.00,111.00 29.59,110.42 24.29,105.67 17.00,102.18 11.59,99.59 8.98,100.52 7.00,94.00 7.00,94.00 25.00,86.06 25.00,86.06 25.00,86.06 33.96,81.91 33.96,81.91 33.96,81.91 41.21,85.99 41.21,85.99 41.21,85.99 47.00,87.00 47.00,87.00 47.00,87.00 75.00,87.00 75.00,87.00 72.81,82.39 63.49,73.05 58.83,70.58 55.75,68.95 53.71,70.20 51.43,68.36 49.64,66.93 48.91,64.08 48.14,62.00 46.81,58.40 41.29,44.94 41.61,42.14 42.32,35.85 53.35,41.05 57.00,42.14 57.00,42.14 70.67,47.09 70.67,47.09 73.93,49.67 70.97,52.02 73.56,55.72 73.56,55.72 89.00,71.00 89.00,71.00 90.59,64.44 90.05,54.95 90.00,48.00 Z"
    :dark_eldar      "M 138.31,3.60 C 138.31,3.60 139.02,10.00 139.02,10.00 139.02,10.00 139.02,22.00 139.02,22.00 139.02,22.00 119.92,48.00 119.92,48.00 119.92,48.00 111.04,61.00 111.04,61.00 111.04,61.00 112.00,74.00 112.00,74.00 112.00,74.00 126.28,61.20 126.28,61.20 126.28,61.20 143.99,45.42 143.99,45.42 148.68,41.28 151.50,35.54 158.00,37.00 156.81,41.58 155.43,45.16 155.09,50.00 154.80,54.11 155.76,59.20 153.26,62.71 153.26,62.71 128.99,82.12 128.99,82.12 125.78,84.41 120.91,87.34 118.86,90.72 117.35,93.18 118.11,97.02 117.83,100.00 117.19,106.94 115.62,116.49 109.89,121.06 103.58,126.10 94.76,126.78 87.00,126.02 82.37,125.24 76.42,121.03 72.95,126.02 68.45,132.95 81.64,132.24 85.00,132.41 88.28,132.83 88.93,132.49 92.00,132.41 101.99,131.61 107.00,131.15 115.00,123.90 120.89,118.56 124.75,113.83 126.57,106.00 126.57,106.00 128.60,92.00 128.60,92.00 130.25,87.52 144.61,76.56 149.00,74.31 150.94,73.32 151.94,73.29 154.00,73.00 148.34,83.05 140.97,87.91 139.17,91.17 138.18,92.97 137.36,101.50 137.17,104.00 136.43,113.68 142.44,111.41 147.00,119.00 137.32,118.22 137.86,116.75 131.00,125.00 131.00,125.00 137.00,134.00 137.00,134.00 137.00,134.00 127.00,132.25 127.00,132.25 124.24,132.39 120.68,134.79 118.00,135.96 111.39,138.83 106.34,138.05 106.79,145.00 106.87,146.24 107.22,148.76 107.73,149.85 109.76,154.19 114.29,152.05 118.00,152.08 122.10,152.11 124.93,153.25 127.89,156.13 135.83,163.85 139.87,187.06 140.00,198.00 140.09,206.21 142.07,216.50 139.00,224.00 137.92,210.72 134.46,188.00 129.28,176.00 127.30,171.43 122.76,162.99 117.00,162.85 111.99,162.72 108.31,169.54 102.00,173.00 100.41,168.11 99.24,163.09 98.44,158.00 97.93,154.78 98.14,150.90 96.69,148.02 91.00,136.75 84.27,148.25 83.00,155.00 81.81,154.08 81.61,154.22 80.83,152.71 80.09,151.26 76.81,135.74 71.84,143.22 71.84,143.22 70.63,146.01 70.63,146.01 70.63,146.01 69.42,150.00 69.42,150.00 69.42,150.00 68.00,156.00 68.00,156.00 61.84,152.00 65.20,144.37 61.41,140.60 59.12,138.32 53.99,138.85 51.00,139.00 51.00,139.00 50.00,152.00 50.00,152.00 50.00,152.00 48.00,152.00 48.00,152.00 46.08,148.66 42.02,142.83 42.50,139.00 43.07,134.39 51.27,125.51 54.42,121.72 54.42,121.72 78.86,89.00 78.86,89.00 80.23,87.06 87.62,76.40 87.89,75.02 88.52,71.85 85.36,70.33 83.00,69.20 73.49,64.62 66.18,67.48 65.00,62.77 64.47,60.68 66.36,56.96 67.21,55.00 67.21,55.00 73.05,40.00 73.05,40.00 77.50,26.72 74.85,16.57 76.00,9.00 77.74,10.35 78.86,10.99 79.98,13.04 83.97,20.36 81.19,39.51 81.00,48.07 81.00,48.07 97.00,48.07 97.00,48.07 99.17,48.39 101.92,49.62 103.91,48.86 107.14,47.63 110.76,40.07 112.68,37.00 112.68,37.00 134.00,2.00 134.00,2.00 134.00,2.00 138.31,3.60 138.31,3.60 Z M 82.00,100.00 C 89.15,96.32 94.39,92.49 95.00,84.00 88.79,85.41 83.46,94.08 82.00,100.00 Z"
    :eldar           "M 32.00,126.00 C 29.18,126.01 20.43,126.88 18.65,125.11 15.87,122.32 21.84,116.24 23.53,114.00 23.53,114.00 38.93,92.00 38.93,92.00 40.12,90.19 43.47,86.15 43.55,84.18 43.64,81.73 40.74,81.67 38.99,81.55 35.74,81.33 26.79,81.63 24.31,79.98 20.74,77.59 21.00,67.40 25.15,65.60 27.56,64.55 45.07,65.01 49.00,65.00 62.13,64.98 61.60,59.95 69.20,49.00 69.20,49.00 87.93,23.00 87.93,23.00 87.93,23.00 97.29,10.13 97.29,10.13 98.71,8.48 100.31,6.76 102.72,7.62 105.62,8.67 111.74,19.02 113.74,22.00 113.74,22.00 140.45,60.96 140.45,60.96 141.84,62.59 143.29,64.28 145.43,64.95 145.43,64.95 173.00,64.95 173.00,64.95 175.12,65.04 177.78,64.97 179.40,66.60 181.51,68.74 181.41,77.41 179.40,79.60 177.32,81.87 172.85,81.03 170.00,81.00 167.29,80.98 158.42,80.33 156.87,82.60 155.29,84.92 158.81,89.10 160.07,91.00 160.07,91.00 175.20,113.04 175.20,113.04 176.92,115.41 181.78,120.79 180.70,123.77 179.57,126.90 175.62,126.67 173.00,126.20 173.00,126.20 150.00,126.20 150.00,126.20 151.14,134.17 157.08,138.21 160.57,145.00 163.10,149.91 160.96,151.74 156.00,151.38 152.58,151.14 148.52,151.44 146.09,148.69 146.09,148.69 132.59,127.60 132.59,127.60 129.64,125.17 119.99,125.97 116.00,126.00 113.65,126.02 110.39,125.81 108.60,127.60 106.68,129.52 107.01,133.46 107.00,136.00 107.00,136.00 107.00,182.00 107.00,182.00 107.00,184.11 107.22,187.81 105.98,189.58 104.17,192.10 94.79,192.00 93.02,189.58 91.79,187.91 92.00,184.06 92.00,182.00 92.00,182.00 92.00,136.00 92.00,136.00 91.99,133.46 92.32,129.52 90.40,127.60 88.76,125.95 86.15,126.06 84.00,125.98 79.75,125.82 70.58,125.16 67.18,127.60 62.30,131.11 55.22,146.83 52.37,149.26 50.24,151.08 40.89,152.40 38.87,150.22 36.57,147.75 39.17,144.14 40.59,142.00 44.00,136.87 49.59,131.46 49.00,125.00 44.27,126.33 37.11,125.98 32.00,126.00 Z M 107.16,39.00 C 105.87,37.13 103.67,33.62 101.08,33.61 98.56,33.59 96.09,37.20 94.73,39.00 91.01,43.97 84.85,50.87 84.00,57.00 96.34,54.68 103.90,53.20 116.00,58.00 117.34,51.48 110.78,44.25 107.16,39.00 Z M 79.00,88.77 C 90.54,94.33 109.73,94.44 120.99,88.20 127.17,84.78 132.78,78.30 129.16,71.00 127.30,67.26 125.88,66.17 121.82,65.00 122.14,68.19 123.06,72.10 121.82,74.98 115.97,86.63 91.11,87.60 81.30,79.37 75.77,74.72 77.62,71.91 76.00,66.00 63.86,72.69 68.44,83.68 79.00,88.77 Z M 147.58,99.00 C 145.55,95.76 138.54,83.52 135.00,83.16 131.73,82.82 126.68,88.93 126.82,92.00 126.93,94.21 129.28,97.16 130.49,99.00 132.17,101.57 135.29,107.46 137.39,109.11 140.81,111.80 148.67,111.00 153.00,111.00 152.25,105.60 150.41,103.53 147.58,99.00 Z M 60.62,109.83 C 62.73,108.51 64.40,105.24 65.81,103.17 71.20,95.26 76.48,86.96 63.00,83.00 63.00,83.00 46.00,111.00 46.00,111.00 49.54,111.78 57.50,111.78 60.62,109.83 Z M 90.40,109.98 C 92.68,108.17 93.14,97.73 88.86,97.05 83.67,96.23 78.95,107.02 78.00,111.00 80.66,111.00 88.51,111.48 90.40,109.98 Z M 108.47,109.98 C 109.86,111.00 112.33,110.99 114.00,110.99 115.17,110.99 117.49,111.00 118.64,110.43 120.72,109.20 119.58,106.73 118.64,105.00 116.74,100.73 114.35,97.28 109.31,97.66 106.66,99.79 105.30,107.66 108.47,109.98 Z" 
    :necrons         "M 85.34,16.96 C 89.29,21.58 94.85,24.41 101.00,23.89 106.37,23.44 111.31,20.13 114.61,15.98 118.08,11.63 117.40,6.74 124.00,6.35 125.37,5.98 127.59,6.05 128.85,6.35 135.93,9.67 128.50,21.03 126.16,25.00 125.17,26.67 123.67,28.66 124.32,30.72 124.32,30.72 131.21,39.00 131.21,39.00 135.83,45.43 136.42,51.55 140.04,54.07 142.18,55.56 146.26,54.69 149.00,54.90 149.00,54.90 172.00,54.90 172.00,54.90 174.19,55.00 177.84,54.79 179.69,56.02 182.97,58.21 182.97,65.79 179.69,67.98 177.84,69.21 174.19,69.00 172.00,69.00 172.00,69.00 148.00,69.00 148.00,69.00 145.66,69.00 142.26,68.80 140.22,70.02 137.20,71.84 133.72,80.66 134.92,84.00 134.92,84.00 150.74,105.00 150.74,105.00 152.86,107.64 156.89,112.15 154.93,115.77 152.44,120.39 147.98,117.68 145.44,114.94 145.44,114.94 133.59,100.00 133.59,100.00 132.17,98.10 129.85,94.07 127.70,93.16 124.99,92.01 121.40,94.83 119.00,96.03 110.69,100.18 109.02,96.88 109.00,108.00 109.00,108.00 109.00,174.00 109.00,174.00 109.00,188.91 111.27,194.50 101.00,194.96 88.50,195.52 90.28,189.60 90.06,180.00 90.06,180.00 90.06,171.00 90.06,171.00 90.06,171.00 90.06,121.00 90.06,121.00 90.06,121.00 90.96,107.00 90.96,107.00 90.49,98.84 85.04,98.75 79.09,95.27 77.14,94.13 74.50,91.87 72.13,92.59 69.69,93.33 67.57,97.01 66.13,99.00 66.13,99.00 55.03,113.00 55.03,113.00 53.74,114.68 51.85,117.67 49.79,118.34 46.95,119.28 39.78,116.47 44.94,109.01 44.94,109.01 50.42,102.72 50.42,102.72 50.42,102.72 60.49,90.00 60.49,90.00 62.79,87.06 65.57,83.94 64.77,80.00 64.14,76.90 61.74,71.61 58.89,70.02 56.88,68.90 54.24,69.02 52.00,69.00 52.00,69.00 28.00,69.00 28.00,69.00 25.37,68.99 21.57,69.32 19.56,67.40 17.20,65.23 17.04,58.03 19.56,56.02 21.20,54.76 25.00,55.00 27.00,55.00 27.00,55.00 50.00,55.00 50.00,55.00 52.83,55.00 56.41,55.21 58.94,53.83 62.59,51.84 65.60,42.56 68.89,38.09 72.03,33.81 75.53,32.49 74.72,28.96 73.39,23.12 61.35,12.22 69.15,6.74 81.54,3.56 79.55,10.20 85.34,16.96 Z M 79.96,48.28 C 70.26,61.40 77.56,80.91 93.00,85.07 94.47,85.46 100.37,85.74 102.00,85.67 117.53,85.06 132.02,63.31 116.99,46.02 112.80,41.20 109.43,39.03 103.00,38.47 93.00,39.23 86.58,39.34 79.96,48.28 Z" 
    :orks            "M 71.12,108.00 C 71.12,108.00 58.62,82.10 58.62,82.10 54.19,74.61 40.04,74.12 36.95,68.78 36.33,66.98 36.74,62.27 36.95,60.04 36.95,60.04 36.95,54.72 36.95,54.72 36.33,50.74 33.58,41.60 34.69,38.02 35.92,34.00 41.47,31.40 44.83,29.20 44.83,29.20 69.04,12.80 69.04,12.80 73.02,9.91 76.82,6.17 82.00,6.14 82.00,6.14 97.00,7.91 97.00,7.91 101.83,8.24 114.89,8.25 119.00,9.72 123.10,11.19 133.07,19.70 137.00,22.79 137.00,22.79 147.69,31.18 147.69,31.18 152.51,36.85 147.33,41.10 150.46,46.90 152.36,50.44 166.98,61.05 171.00,64.00 169.59,65.55 169.04,66.43 166.96,67.26 164.52,68.23 143.24,69.03 139.27,72.51 135.84,75.52 135.17,81.74 134.11,86.00 134.11,86.00 125.00,118.00 125.00,118.00 125.00,118.00 123.00,118.00 123.00,118.00 123.00,118.00 116.00,104.00 116.00,104.00 116.00,104.00 114.00,104.00 114.00,104.00 114.00,104.00 107.00,121.00 107.00,121.00 107.00,121.00 105.00,121.00 105.00,121.00 105.00,121.00 100.00,98.00 100.00,98.00 100.00,98.00 97.16,101.94 97.16,101.94 91.29,110.43 89.56,99.20 87.00,95.00 87.00,95.00 82.48,113.00 82.48,113.00 82.48,113.00 80.00,122.00 80.00,122.00 75.34,119.07 73.28,112.87 71.12,108.00 Z M 59.43,45.00 C 59.30,48.27 64.32,62.04 66.70,64.27 69.17,66.58 78.63,68.68 81.96,67.40 85.16,66.17 86.93,62.76 88.68,60.00 89.96,57.97 91.72,55.35 92.27,53.00 93.07,49.51 90.69,40.25 88.28,37.65 86.67,35.91 82.29,34.26 80.00,33.15 76.00,31.22 74.58,30.23 70.04,30.69 66.37,32.66 59.61,40.69 59.43,45.00 Z M 114.11,53.21 C 114.11,53.21 111.00,56.13 111.00,56.13 115.10,56.54 127.24,59.68 130.01,56.13 131.45,54.47 131.00,47.48 131.00,45.00 131.00,45.00 114.11,53.21 114.11,53.21 Z M 97.36,67.72 C 94.58,72.89 92.16,73.54 93.00,80.00 96.23,78.91 97.14,78.24 99.98,76.43 101.13,75.70 103.19,74.63 103.89,73.46 105.86,70.14 102.06,64.49 100.00,62.00 100.00,62.00 97.36,67.72 97.36,67.72 Z M 161.00,96.00 C 161.00,96.00 162.00,116.00 162.00,116.00 162.00,116.00 163.00,133.00 163.00,133.00 162.98,145.20 158.56,145.46 150.28,153.00 150.28,153.00 118.00,181.25 118.00,181.25 114.93,183.75 104.24,192.30 101.00,192.97 97.60,193.68 93.80,191.09 91.00,189.39 91.00,189.39 70.00,175.58 70.00,175.58 70.00,175.58 41.00,155.86 41.00,155.86 37.69,153.56 31.11,149.92 30.02,145.96 29.14,142.75 30.78,137.22 31.50,134.00 31.50,134.00 34.54,117.00 34.54,117.00 35.78,110.00 34.86,101.51 40.00,96.00 40.00,96.00 46.69,124.00 46.69,124.00 47.06,125.95 47.54,133.05 50.83,132.38 53.19,131.90 53.71,126.89 54.12,125.00 54.12,125.00 60.00,103.00 60.00,103.00 60.00,103.00 62.00,103.00 62.00,103.00 62.00,103.00 71.77,124.00 71.77,124.00 74.42,130.09 76.87,136.62 82.00,141.00 82.00,141.00 94.00,117.00 94.00,117.00 97.70,120.75 98.08,129.65 98.85,135.00 99.11,136.79 99.33,140.80 101.94,140.80 103.97,140.80 105.60,137.54 106.37,136.00 109.15,130.39 108.84,125.89 115.00,123.00 115.00,123.00 123.00,141.00 123.00,141.00 123.00,141.00 125.00,141.00 125.00,141.00 125.00,141.00 131.67,119.00 131.67,119.00 131.67,119.00 137.00,107.00 137.00,107.00 137.00,107.00 144.00,128.00 144.00,128.00 144.00,128.00 146.00,128.00 146.00,128.00 146.00,128.00 148.49,118.00 148.49,118.00 148.49,118.00 152.46,97.00 152.46,97.00 152.46,97.00 156.00,80.00 156.00,80.00 162.35,83.49 160.99,89.64 161.00,96.00 Z" 
    :space_marines   "M 123.79,52.60 C 119.57,50.89 114.55,54.42 111.06,53.31 107.34,52.13 106.31,47.58 110.23,45.74 112.51,44.66 119.19,45.05 122.00,45.00 123.48,44.97 125.49,44.98 126.87,44.40 128.96,43.52 129.80,42.00 130.60,39.98 134.03,31.25 127.51,24.71 126.57,20.91 125.32,15.90 128.12,12.33 133.00,11.34 138.77,10.18 146.65,12.72 145.64,20.00 144.91,25.29 137.80,30.45 141.55,39.98 142.35,41.99 143.04,43.50 145.14,44.40 146.50,44.98 148.53,44.97 150.00,45.00 152.82,45.05 159.49,44.54 161.63,46.02 164.11,47.66 164.08,51.14 161.63,52.54 158.27,54.68 152.42,51.75 149.10,52.54 146.80,52.85 143.26,57.01 140.00,59.00 144.37,60.43 151.33,62.04 155.00,64.22 164.31,69.76 164.01,74.45 163.83,84.00 164.00,86.04 164.05,89.01 163.83,90.96 162.96,94.60 161.28,95.57 161.36,97.51 161.36,97.51 165.00,108.00 165.00,108.00 165.00,108.00 166.87,96.00 166.87,96.00 168.22,92.16 170.70,90.26 171.67,85.00 172.78,78.99 171.87,71.08 176.39,66.34 180.53,62.00 188.26,60.82 194.00,61.04 194.00,61.04 218.00,63.75 218.00,63.75 218.00,63.75 231.00,65.91 231.00,65.91 236.24,66.25 238.32,65.24 243.00,65.04 243.00,65.04 257.00,65.97 257.00,65.97 257.00,65.97 272.00,65.00 272.00,65.00 270.51,73.88 259.05,74.93 252.00,77.00 256.30,77.57 260.47,77.22 263.00,81.00 263.00,81.00 253.00,84.64 253.00,84.64 253.00,84.64 239.00,86.00 239.00,86.00 239.00,86.00 264.00,90.00 264.00,90.00 260.82,94.67 259.97,93.28 255.09,94.26 251.06,95.07 251.24,96.39 246.00,96.25 238.94,96.06 235.73,93.15 229.00,95.00 236.78,96.66 237.15,98.67 243.00,100.24 249.01,101.85 251.30,99.36 256.00,104.00 256.00,104.00 230.00,110.00 230.00,110.00 239.82,112.13 236.52,116.29 232.95,116.68 232.95,116.68 215.00,113.00 215.00,113.00 218.27,118.67 221.10,117.87 225.85,120.63 227.14,121.38 228.86,122.75 227.83,124.44 226.80,126.15 223.66,125.92 222.00,125.67 216.33,124.82 211.53,121.38 206.00,120.00 206.00,120.00 219.00,131.00 219.00,131.00 210.35,133.37 203.29,127.90 196.00,124.00 197.68,128.74 199.74,130.99 199.00,136.00 199.00,136.00 187.00,130.00 187.00,130.00 187.00,130.00 191.00,140.00 191.00,140.00 191.00,140.00 190.00,141.00 190.00,141.00 182.81,137.72 181.47,135.28 177.00,129.00 177.00,129.00 174.00,140.00 174.00,140.00 168.62,136.34 170.84,134.49 169.20,129.00 169.20,129.00 166.21,122.00 166.21,122.00 165.09,119.05 164.48,111.62 164.00,108.00 164.00,108.00 157.86,115.44 157.86,115.44 155.87,117.04 153.09,117.59 151.64,119.43 149.68,121.90 151.15,124.52 149.89,126.69 148.05,129.83 141.23,128.97 138.00,129.00 134.25,129.05 124.51,130.74 122.04,127.41 120.19,124.91 122.71,121.23 120.36,118.74 118.32,116.57 111.15,117.28 108.92,109.00 107.41,103.39 110.26,99.54 109.43,95.00 108.15,87.93 104.89,80.11 108.45,73.00 114.13,61.67 125.72,61.86 134.00,58.00 125.88,57.34 128.32,54.44 123.79,52.60 Z M 27.00,65.00 C 27.00,65.00 40.00,65.79 40.00,65.79 40.00,65.79 77.00,61.01 77.00,61.01 83.52,60.87 93.22,61.78 96.84,68.09 99.01,71.87 99.41,81.37 100.52,86.00 102.15,92.74 106.74,97.93 106.98,105.00 107.07,107.47 106.93,116.96 106.44,119.00 106.44,119.00 102.02,131.00 102.02,131.00 102.02,131.00 100.00,142.00 100.00,142.00 100.00,142.00 98.00,142.00 98.00,142.00 96.44,136.98 95.07,135.32 96.00,130.00 88.41,133.56 90.03,138.09 80.00,140.00 80.00,140.00 85.00,130.00 85.00,130.00 79.67,132.50 75.94,136.23 70.00,135.00 70.00,135.00 75.00,124.00 75.00,124.00 68.16,127.94 60.15,133.22 52.00,131.00 52.00,131.00 68.00,118.00 68.00,118.00 60.73,120.06 52.76,127.96 42.00,125.00 42.00,125.00 58.00,112.00 58.00,112.00 52.08,114.22 40.04,117.33 34.00,116.00 36.58,111.47 37.45,111.28 42.00,109.00 42.00,109.00 16.00,105.00 16.00,105.00 16.00,105.00 16.00,102.00 16.00,102.00 28.10,99.83 29.86,102.50 41.00,94.00 32.18,94.09 33.03,95.54 28.00,95.88 24.92,96.09 12.59,93.89 10.29,92.15 8.83,91.04 8.79,90.44 8.00,89.00 16.43,89.00 24.19,88.64 32.00,85.00 19.49,84.96 19.60,83.16 9.00,81.00 12.43,75.91 18.41,77.59 24.00,76.00 16.01,76.00 1.68,74.84 0.00,65.00 0.00,65.00 27.00,65.00 27.00,65.00 Z M 115.61,103.27 C 112.70,107.12 115.84,110.77 120.02,111.17 128.19,111.96 136.48,102.52 124.00,99.85 120.94,100.05 117.62,100.63 115.61,103.27 Z M 147.00,110.08 C 150.63,111.52 156.29,111.74 157.16,106.96 157.65,105.73 157.58,104.49 157.16,103.39 154.87,99.85 146.51,98.85 143.56,101.60 142.13,102.93 141.59,105.20 141.00,107.00 141.00,107.00 147.00,110.08 147.00,110.08 Z M 133.00,116.00 C 133.00,116.00 135.00,118.00 135.00,118.00 135.00,118.00 136.00,118.00 136.00,118.00 136.00,118.00 138.00,116.00 138.00,116.00 138.00,116.00 138.00,115.00 138.00,115.00 138.00,115.00 136.00,113.00 136.00,113.00 136.00,113.00 133.00,116.00 133.00,116.00 Z M 143.98,136.31 C 145.49,138.58 145.00,146.97 145.00,150.00 145.00,160.66 147.19,178.79 138.29,186.39 135.38,188.87 133.38,186.28 131.67,183.95 128.13,179.13 127.08,176.94 127.00,171.00 127.00,171.00 127.00,147.00 127.00,147.00 127.00,142.88 126.27,138.45 128.74,134.99 132.06,133.55 141.68,132.87 143.98,136.31 Z" 
    :tau             "M 122.69,24.17 C 124.61,26.31 128.11,31.44 129.03,34.17 130.26,37.77 130.02,43.14 130.00,47.00 129.95,57.41 124.45,67.51 115.00,72.47 108.46,75.90 104.14,76.08 97.00,76.00 74.82,75.73 61.94,51.61 70.53,32.00 74.40,23.17 83.01,17.19 92.00,14.53 103.39,12.67 114.77,15.39 122.69,24.17 Z M 141.00,25.82 C 145.37,25.89 157.39,34.51 161.00,37.46 179.61,52.72 188.43,72.56 191.05,96.00 191.05,96.00 191.92,104.00 191.92,104.00 191.92,110.19 189.54,118.08 187.77,124.00 181.42,145.16 169.41,163.06 150.00,174.39 142.57,178.72 120.37,187.02 112.06,185.84 107.23,185.15 108.01,180.80 108.00,177.00 108.00,177.00 108.00,101.00 108.00,101.00 108.00,98.54 107.76,94.50 109.17,92.43 110.71,90.19 115.40,89.36 118.00,88.39 124.03,86.12 128.87,83.91 133.71,79.49 147.92,66.50 146.22,42.35 137.00,27.00 138.62,26.29 139.01,25.79 141.00,25.82 Z M 59.43,29.15 C 59.57,30.77 57.72,35.19 57.13,37.00 55.11,43.19 54.08,45.32 54.20,52.00 53.97,54.62 53.87,59.61 54.20,62.00 56.41,71.40 63.86,79.90 72.00,84.57 76.19,86.97 88.82,90.61 90.08,94.14 91.15,96.47 90.04,106.72 90.08,110.00 90.08,110.00 90.08,171.00 90.08,171.00 90.00,174.12 90.99,182.11 89.08,184.42 86.74,186.90 79.23,185.08 76.00,184.42 65.28,182.89 48.31,175.44 40.00,168.56 24.68,155.86 16.29,143.05 10.59,124.00 9.06,118.88 6.81,110.25 7.18,105.00 7.79,96.47 8.11,87.26 10.43,79.00 16.58,57.07 33.37,36.69 54.00,26.86 56.25,26.41 59.13,25.79 59.43,29.15 Z" 
    :tyranids        "M 11.00,99.15 C 8.41,99.32 4.82,100.01 3.77,96.77 3.14,94.82 5.60,86.02 5.95,83.00 6.40,79.11 6.96,74.68 8.33,71.00 15.14,52.62 33.56,40.54 53.00,41.01 56.09,41.09 67.92,43.50 71.00,44.45 73.31,45.17 77.39,46.79 78.23,49.27 78.97,51.43 76.78,57.67 81.23,59.92 84.91,61.79 88.13,57.36 91.00,55.59 93.82,53.86 95.84,54.02 99.00,54.00 102.09,53.98 106.20,53.69 109.00,55.01 112.05,56.44 115.54,60.65 118.85,59.59 123.61,58.06 120.57,50.99 123.60,47.65 126.67,44.25 143.11,41.37 148.00,41.09 152.76,40.82 156.46,41.99 161.00,43.13 173.90,46.37 185.75,55.50 191.05,68.00 192.94,72.45 193.43,76.37 194.31,81.00 195.10,85.18 198.34,93.74 196.40,97.76 194.86,100.81 183.00,99.49 180.47,97.76 179.16,96.98 177.76,94.69 176.59,93.41 176.59,93.41 166.43,83.14 166.43,83.14 165.11,81.65 163.81,78.97 161.86,78.43 159.21,77.70 156.02,80.34 154.41,82.18 153.28,83.48 152.80,84.54 152.00,86.00 152.00,86.00 159.96,88.81 159.96,88.81 163.96,90.71 170.83,96.19 173.00,100.00 161.51,105.32 155.20,100.81 146.00,94.50 140.98,91.05 137.50,90.32 137.00,84.00 129.49,84.53 121.44,88.88 129.46,96.37 132.56,99.26 137.99,100.80 142.00,102.00 138.20,105.75 128.01,106.80 123.09,104.70 121.09,103.84 120.32,102.57 119.00,101.00 118.07,108.08 116.16,121.13 127.00,121.75 130.15,121.93 133.38,119.60 139.00,119.00 137.53,125.57 134.70,128.31 134.38,130.11 133.74,133.76 140.77,137.27 143.44,135.27 145.52,133.72 145.39,129.52 147.52,128.07 152.29,124.83 153.87,137.55 154.00,139.79 154.00,139.79 163.00,139.79 163.00,139.79 163.00,139.79 179.19,130.91 179.19,130.91 179.19,130.91 190.00,123.00 190.00,123.00 188.61,130.63 181.15,142.58 174.96,147.21 170.48,150.57 167.26,152.48 162.00,150.00 159.15,157.70 156.63,156.61 150.00,153.00 146.92,160.15 143.81,159.21 137.00,158.00 137.00,158.00 137.00,153.00 137.00,153.00 128.35,156.78 124.30,159.76 121.00,148.00 121.00,148.00 118.00,152.00 118.00,152.00 110.13,150.57 103.46,146.78 108.00,138.00 108.00,138.00 100.00,139.45 100.00,139.45 100.00,139.45 92.00,138.00 92.00,138.00 92.91,139.62 93.93,141.03 93.97,143.00 94.07,147.17 88.82,150.81 85.00,151.10 85.00,151.10 77.00,150.00 77.00,150.00 79.40,157.76 70.47,159.15 67.00,153.00 67.00,153.00 65.00,153.00 65.00,153.00 64.55,154.34 64.09,156.21 63.10,157.26 59.11,161.52 53.36,158.04 51.00,154.00 40.90,158.57 42.56,154.60 35.96,151.69 32.96,150.37 31.71,150.73 28.00,148.52 19.56,143.49 12.49,132.64 11.00,123.00 17.57,125.68 24.31,134.83 35.00,138.81 38.34,140.06 44.58,140.97 46.98,137.57 46.98,137.57 50.00,127.00 50.00,127.00 55.99,129.11 53.91,132.97 57.41,135.19 60.18,136.94 65.56,134.00 65.99,130.90 66.35,128.29 63.16,125.11 62.00,119.00 67.15,119.70 70.30,122.19 74.00,121.73 83.80,120.53 83.90,107.98 81.00,101.00 79.98,102.49 79.51,103.60 77.73,104.40 73.53,106.29 62.30,104.57 58.00,103.00 62.37,100.04 67.78,99.60 71.65,95.62 78.70,88.40 68.36,84.16 62.00,84.00 63.30,89.90 59.57,90.64 55.01,93.81 44.37,101.21 41.93,104.39 28.00,101.00 29.86,96.39 35.71,91.75 40.04,89.29 41.89,88.24 45.11,87.50 46.26,85.69 48.67,81.88 41.58,76.95 38.14,77.88 35.91,78.48 34.32,81.67 32.87,83.42 29.84,87.09 25.51,92.54 21.00,94.00 20.93,100.01 15.60,98.85 11.00,99.15 Z"
  })    