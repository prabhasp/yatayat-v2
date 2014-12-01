(ns yy.osm-parse
  (require [clojure.xml :as xml]
           [clojure.zip :as zip]
           [clojure.data.zip.xml :as zx]))

(defn as-xml-zip [x]
  "Takes a string or filename and produces an xml-zipper out of it."
  (zip/xml-zip (xml/parse x)))

(defn kv-map [zpr]
  "Take a zipped thing derived from osm-xml, and returns k->v map."
  (zipmap (map keyword (zx/xml-> zpr :tag (zx/attr :k)))
          (zx/xml-> zpr :tag (zx/attr :v))))

(defn z->node [n]
  "Take a node in zip form and makes it into a yy node"
  (merge (kv-map n) (:attrs (first n)) {:type :node}))

(defn z->way [nodemap w]
  "Take a way in zip form and make it into a yy way.
   Also needs a nodemap that maps ids to node objects."
  (let [nodes (map nodemap (zx/xml-> w :nd (zx/attr :ref)))]
    (merge  (kv-map w) (:attrs (first w)) {:type :way :nodes nodes})))

(defn z->relation [nodemap waymap r]
  "Take a way in zip form and make it into a yy relation.
   Also needs nodemap, waymap that map from id to node, way."
  (let [mems (zx/xml-> r :member)
        rf #(zx/attr % :ref)
        rl #(if ((zx/attr= :role "") %) nil (zx/attr % :role))
        el #(if ((zx/attr= :type "node") %) (nodemap (rf %)) (waymap (rf %)))
        role->mem-list (for [m mems :when (rl m)]
                       (hash-map (rl m) [(el m)]))
        roles (apply merge-with concat role->mem-list)
        ref-ids (map #(zx/attr % :ref) mems)
        nodemap (select-keys nodemap ref-ids)
        waymap (select-keys waymap ref-ids)]
    (merge (kv-map r) (:attrs (first r))
           {:type :relation :nodemap nodemap :waymap waymap :roles roles})))

(defn- z->map [zpr typ f]
  "Take a zipper from xml, and extracts a map for typ :node|:way|:relation,
   and f which takes zpr and maps to the required object (eg. z->node)."
    (into {}
          (for [t (zx/xml-> zpr typ)]
            (vector (zx/attr t :id) (f t)))))
(defn z->nodemap [zpr]
  (z->map zpr :node z->node))
(defn z->waymap [nodemap zpr]
  (z->map zpr :way (partial z->way nodemap)))
(defn z->relationmap [nodemap waymap zpr]
  (z->map zpr :relation (partial z->relation nodemap waymap)))

(defn xml->yy [fname]
  "INCOMPLETE function to map from xml to a clojure-idiomatic data structure."
  (let [root (-> fname xml/parse zip/xml-zip)
        nodemap (z->nodemap root)
        waymap (z->waymap nodemap root)
        relationmap (z->relationmap nodemap waymap root)]
    {:waymap waymap :relationmap relationmap :nodemap nodemap}))
