(defproject dansite "0.1.0-SNAPSHOT"
  :description "Base Webserver"
  :url "http://localhost:8080/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :min-lein-version "2.7.1"
  
  :main  dansite.system  
  
  :jar-name     "dansite.jar"
  :uberjar-name "dansite-standalone.jar"

  :dependencies [[org.clojure/clojure "1.9.0-beta4"]
               [org.clojure/clojurescript "1.9.946"]
               [http-kit "2.2.0"]
               [com.stuartsierra/component "0.3.2"]
               [compojure "1.6.0"]
               [jarohen/chord "0.8.1"]
               [org.clojure/core.async  "0.3.443"]
               [ring/ring-defaults "0.3.1"]
               [reagent "0.7.0"]
               [reagent-utils "0.2.1"]]

  :plugins [[lein-figwheel "0.5.14"]
            [lein-cljsbuild "1.1.7" :exclusions [[org.clojure/clojure]]]]

  :source-paths ["src/clj"]

  ;; Setting up nREPL for Figwheel and ClojureScript dev
  ;; Please see:
  ;; https://github.com/bhauman/lein-figwheel/wiki/Using-the-Figwheel-REPL-within-NRepl
  :profiles {:uberjar {:aot :all
                       :source-paths ["src/clj"]
                       :prep-tasks ["compile" ["cljsbuild" "once" "min"]]}
             :dev {:dependencies [[reloaded.repl "0.2.4"]
                                  [figwheel-sidecar "0.5.14"]
                                  [binaryage/devtools "0.9.4"]
                                  [com.cemerick/piggieback "0.2.2"]]
                   ;; need to add dev source path here to get user.clj loaded
                   :source-paths ["src/clj" "dev"]
                   ;; for CIDER
                   ;; :plugins [[cider/cider-nrepl "0.12.0"]]
                   :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
                   ;; need to add the compliled assets to the :clean-targets
                   :clean-targets ^{:protect false} ["resources/public/js/compiled"
                                                     :target-path]}})
