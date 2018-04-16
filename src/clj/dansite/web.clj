(ns dansite.web
   (:require [chord.http-kit :refer [with-channel]]
            ;; [clojure.java.io :as io]
            [compojure.core :refer [defroutes GET POST]]
            [compojure.route :refer [not-found resources]]
            [ring.util.response :refer [resource-response content-type]]
            [clojure.core.async :refer [>! <! chan go go-loop mult tap]]))
           
   
(defroutes app
   (GET "/" [] (content-type (resource-response "index.html" {:root "public"}) "text/html"))
   (resources "/"))
   