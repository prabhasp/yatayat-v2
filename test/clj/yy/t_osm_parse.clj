(ns yy.t-osm-parse
  (:require [midje.sweet :refer :all]
            [yy.osm-parse :refer :all]
            [clojure.zip :as zip]
            [clojure.xml :as xml]
            [clojure.data.zip.xml :as zx]))

(defn zip-str [s]
  (zip/xml-zip
   (xml/parse (java.io.ByteArrayInputStream. (.getBytes s)))))

(def test-xml
  "<?xml version='1.0' encoding='UTF-8'?>
  <osm version='0.6' generator='Overpass API'>
  <note>The data included in this document is from www.openstreetmap.org. The data is made available under ODbL.</note>
  <meta osm_base='2013-07-23T23:59:02Z'/>
  <node id='31150627' lat='27.6984315' lon='85.3485208'>
    <tag k='name' v='Sinamangal'/>
    <tag k='public_transport' v='stop_position'/>
  </node>
  <node id='1347468314' lat='27.6989046' lon='85.3478461'/>
  <node id='1347468329' lat='27.6992615' lon='85.3474024'/>
  <node id='2296825748' lat='27.6992366' lon='85.3473930'/>
  <node id='1347468354' lat='27.6991855' lon='85.3474211'/>
  <node id='1347468309' lat='27.6986671' lon='85.3482406'/>
  <node id='2007675777' lat='27.6985687' lon='85.3483866'/>
  <node id='31228768' lat='27.6789154' lon='85.3495276'>
    <tag k='name' v='Koteshwor Stop'/>
    <tag k='public_transport' v='stop_position'/>
  </node>
  <node id='1274184084' lat='27.7037097' lon='85.3164292'>
    <tag k='name' v='Purano Bus Park'/>
    <tag k='public_transport' v='stop_position'/>
  </node>
  <way id='120105828'>
    <nd ref='1347468329'/>
    <nd ref='2296825748'/>
    <nd ref='1347468354'/>
    <nd ref='1347468314'/>
    <nd ref='1347468309'/>
    <nd ref='2007675777'/>
    <nd ref='31150627'/>
    <tag k='highway' v='tertiary'/>
    <tag k='name' v='Pashupati Marga'/>
  </way>
  <relation id='2277212'>
    <member type='node' ref='31228768' role='terminus'/>
    <member type='node' ref='1274184084' role='terminus'/>
    <member type='way' ref='120105828' role=''/>
    <tag k='ref' v='annapurna B'/>
    <tag k='route' v='tempo'/>
    <tag k='type' v='route'/>
  </relation>
  </osm>")

(let [root (zip-str test-xml)
      node1 (z->node (zx/xml1-> root :node))
      nmap (z->nodemap root)
      way1 (z->way nmap (zx/xml1-> root :way))
      wmap (z->waymap nmap root)
      rel1 (z->relation nmap wmap (zx/xml1-> root :relation))
      rmap (z->relationmap nmap wmap root)]
  (facts "xml->node parsing works correctly."
         node1 => {:type :node
                   :id "31150627" :lat "27.6984315" :lon "85.3485208"
                   :name "Sinamangal" :public_transport "stop_position"})
  (facts "xml->nodemap generation works correctly."
         (sort (keys nmap)) => (sort ["31150627" "1347468314" "1347468329"
                                      "2296825748" "1347468354" "1347468309"
                                      "2007675777" "31228768" "1274184084"])
         (nmap "31150627") => node1)
  (facts "xml->way parsing works correctly."
        (:highway way1) => "tertiary"
        (:name way1) => "Pashupati Marga"
        (:type way1) => :way
        (:id way1) => "120105828"
        (count (:nodes way1)) => 7
        (last (:nodes way1)) => node1)
  (facts "xml->way parsing works correctly."
         (keys wmap) => ["120105828"]
         (wmap "120105828") => way1)
  (facts "xml->relation parsing works correctly."
         (:id rel1) => "2277212"
         (:ref rel1) => "annapurna B"
         (:route rel1) => "tempo"
         (:type rel1) => :relation
         (:nodemap rel1) => (select-keys nmap #{"31228768" "1274184084"})
         (:waymap rel1) => wmap
         (keys (:roles rel1)) => ["terminus"]
         ((:roles rel1) "terminus") => [(nmap "31228768") (nmap "1274184084")])
  (facts "xml->relationmap generation works correctly."
         (keys rmap) => ["2277212"]
         (rmap "2277212") => rel1))
