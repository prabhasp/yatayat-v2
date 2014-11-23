(ns yy2.views.layout
  (:require [hiccup.page :refer [html5 include-css]]))

(defn common [& body]
  (html5
    [:head
     [:title "Welcome to yy2"]
     (include-css "/css/screen.css")]
    [:body body]))
