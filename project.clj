(defproject yy "0.1.0-SNAPSHOT"
  :description "Yatayat: v2"
  :url "http://github.com/yatayat-v2"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [compojure "1.1.6"]
                 [hiccup "1.0.5"]
                 [ring-server "0.3.1"]
                 ;; cljs
                 [org.clojure/clojurescript "0.0-2371"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [com.facebook/react "0.11.1"]
                 [om "0.7.3"]]
  :plugins [[lein-ring "0.8.12"]
            [lein-cljsbuild "1.0.4-SNAPSHOT"]]
  :ring {:handler yy.handler/app
         :init yy.handler/init
         :destroy yy.handler/destroy}
  :hooks [leiningen.cljsbuild]
  :profiles
  {:uberjar {:aot :all}
   :production
   {:ring
    {:open-browser? false, :stacktraces? false, :auto-reload? false}}
   :dev
   {:dependencies [[ring-mock "0.1.5"] [ring/ring-devel "1.3.1"]]}}
  :source-paths ["src/clj"]
  :test-paths ["test/clj"]
  :cljsbuild {
    :builds [{:id "yy"
              :source-paths ["src/cljs"]
              :preamble ["react/react.js"]
              :compiler {
                :output-to "resources/public/js/yy.js"
                :output-dir "resources/public/js/out/"
                :optimizations :none
                :pretty-print true
                :source-map "resources/public/js/yy.js.map"}}]})
