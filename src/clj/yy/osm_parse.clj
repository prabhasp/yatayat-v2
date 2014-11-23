(ns yy.osm-parse
  (require [clojure.xml :as xml]
           [clojure.zip :as zip]
           [clojure.data.zip.xml :as zx]))

(defn as-xml-zip [x]
  "Takes a string or filename and produces an xml-zipper out of it."
  (zip/xml-zip (xml/parse x)))

(defn z->node [n]
  "Take a node in zip form and makes it into a yy node"
  (let [ks (map keyword (zx/xml-> n :tag (zx/attr :k)))
        vs (zx/xml-> n :tag (zx/attr :v))]
    (merge {:type :node}
           (:attrs (first n))
           (zipmap ks vs))))

(defn xml->yy [fname]
  "INCOMPLETE function to map from xml to a clojure-idiomatic data structure."
  (let [root (-> fname xml/parse zip/xml-zip)
        nodes (zx/xml-> root :node)
        nodemap (into {}
                      (for [n nodes]
                        (vector (zx/attr n :id) (z->node n))))
        ways (zx/xml-> root :way)
        w->way (fn [w] (zx/xml-> w :nd (zx/attr :ref) #(get nodemap %)))
        waymap (into {}
                     (for [w ways]
                       (vector (zx/attr w :id)
                               (merge {:type :way}
                                      (:attrs (first w))
                                      {:nodes (w->way w)}))))
        relations (zx/xml-> root :relation)]
    (str (count nodes) " nodes, " (count ways) " ways,"
         " and " (count relations) " relations present.")
    {:ways ways :relations relations :nodes nodes :nodemap nodemap :waymap waymap}))

(def test-obj (xml->yy "resources/public/data/transit.stable.xml"))
