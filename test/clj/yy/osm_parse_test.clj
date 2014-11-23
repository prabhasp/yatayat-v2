(ns yy.osm-parse-test
  (:require [midje.sweet :refer :all]
            [yy.osm-parse :refer :all]
            [clojure.zip :as zip]
            [clojure.xml :as xml]))


(defn zip-str [s]
  (zip/xml-zip
   (xml/parse (java.io.ByteArrayInputStream. (.getBytes s)))))

(facts "xml->node works correctly."
       (let [nodexml "<node id=\"31150627\" lat=\"27.6984315\" lon=\"85.3485208\">
             <tag k=\"name\" v=\"Sinamangal\"/>
             <tag k=\"public_transport\" v=\"stop_position\"/>
             </node>"
             n (zip-str nodexml)]
         (z->node n) => {:type :node
                         :id "31150627" :lat "27.6984315" :lon "85.3485208"
                         :name "Sinamangal" :public_transport "stop_position"}))
