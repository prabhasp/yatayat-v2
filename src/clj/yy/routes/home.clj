(ns yy.routes.home
  (:require [compojure.core :refer :all]
            [yy.views.layout :as layout]))

(defn home []
  (layout/home))

(defroutes home-routes
  (GET "/" [] (home)))
