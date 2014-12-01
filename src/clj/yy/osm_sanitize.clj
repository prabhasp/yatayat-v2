(ns yy.osm-sanitize
  (require [yy.osm-parse :as parse]))

(defn name-or-id-with-prefix [x pre]
  (if (:name x)
    (str pre " " (:name x))
    (str pre " " (:id x))))

(defmulti pprinter (juxt :type type))
(defmethod pprinter [:way clojure.lang.PersistentArrayMap] [w]
  (str (name-or-id-with-prefix w "W") " "
       (-> w :nodes first :id) "->" (-> w :nodes last :id)))
(defmethod pprinter [:way clojure.lang.PersistentHashMap] [w]
  (str (name-or-id-with-prefix w "W") " "
       (-> w :nodes first :id) "->" (-> w :nodes last :id)))
(defmethod pprinter [:node clojure.lang.PersistentArrayMap]  [n]
  (str "N: " (:id n)))
(defmethod pprinter [:relation clojure.lang.PersistentArrayMap] [r]
  (name-or-id-with-prefix r "R"))
(defmethod pprinter ["route" clojure.lang.PersistentArrayMap] [r]
  (name-or-id-with-prefix r "R"))

(defmethod pprinter [nil clojure.lang.LazySeq] [v]
  (map pprinter v))
(defmethod pprinter [nil clojure.lang.PersistentVector] [v]
  (map pprinter v))
(defmethod pprinter [nil clojure.lang.PersistentArrayMap] [v]
  (map pprinter v))
(defmethod pprinter [nil clojure.lang.PersistentHashMap] [v]
  (map pprinter v))
(defmethod pprinter [nil clojure.lang.MapEntry] [v]
  (map pprinter v))
(defmethod pprinter [nil clojure.lang.Keyword] [v] v)
(defmethod pprinter [nil java.lang.String] [v] v)


(defn w->ew-entry [w]
  "Creates an 'end-to-way' entry for a way. This is a map from a node at the
   either end of a way (front or back) along with an additional tag."
  {(-> w :nodes first) {:am-beg-of (-> w)}
    (-> w :nodes last) {:am-end-of (-> w )}})

(defn ws->ew-map [ways]
  "Given a seq of ways as input, creates a map of end nodes to ways.
   Each entry is a ew-entry."
  (apply merge-with merge (map w->ew-entry ways)))

(defn combine [ew-map]
  "Takes an ew-map, and 'combines' it in the following way: for any node that
   is the beginning of way 1 and end of way 2, replaces the two ew-entries w/
   a new entry, corresponding to the way which is a concat of way2 and way1."
  (let [new-ew-map (remove #(<= (count (second %)) 1) ew-map)]
    (if (empty? new-ew-map)
      ew-map
      (let [ew-entry (first new-ew-map)
            [n {:keys [am-end-of am-beg-of] :as wm}] ew-entry
            combined-way {:id (str (:id am-end-of) "-" (:id am-beg-of))
                          :name (str (:name am-end-of) " - " (:name am-beg-of))
                          :type :way
                          :nodes (concat (:nodes am-end-of) (:nodes am-beg-of))}
            result-ew-map (dissoc ew-map n)]
        (merge-with merge
                    result-ew-map
                    (w->ew-entry combined-way))))))

(defn connect-connected-ways [ways]
  "Runs combine over and over until the ew-map data structure doesn't change.
   The effect is to combine all connected ways together."
  (->> (loop [ewm (ws->ew-map ways)]
          (let [next-ewm (combine ewm)]
            (if (= ewm next-ewm)
              ewm(recur next-ewm))))
       vals
       (map vals)
       flatten distinct))

(defn diff-from-connected [rel]
  (- (* 2
        (->> (ws->ew-map rel)
             (map keys)
             flatten
             (map #(dissoc % :which))
             distinct
             count))
     (->> (ws->ew-map rel)
          (map keys)
          flatten
          distinct
          count)
     2)) ; 2 for the 2 ends
