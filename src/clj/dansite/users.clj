(ns dansite.users
  (:require 
    [clojure.java.jdbc :as j]
    [cemerick.friend [credentials :as creds]]))
      
; a dummy in-memory user "database"
(def users {"root" {:username "root"
                    :password (creds/hash-bcrypt "admin")
                    :roles #{::admin}}
            "dan"  {:username "dan"
                    :password (creds/hash-bcrypt "user")
                    :roles #{::user}}})


(derive ::admin ::user)