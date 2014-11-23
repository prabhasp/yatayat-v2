(ns yy.views.layout
  (:require [hiccup.page :refer [html5 include-css include-js]]))

(defn home []
  (html5
    [:head
     [:title "Welcome to yy"]
     (include-css "/css/screen.css")]
    [:body
     [:div {:id "app"} "Loading application..."]
     (include-js "http://cdnjs.cloudflare.com/ajax/libs/react/0.11.1/react.js")
     (include-js "/out/goog/base.js")
     (include-js "/js/yy.js")
     [:script {:type "text/javascript"} "goog.require(\"yy.core\"); yy.core.init();"]]))
