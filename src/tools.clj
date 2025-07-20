(ns tools
  (:require [clojure.edn :as edn]
            [et.vp.ds.search :as search]
            [et.vp.ds :as ds]
            [clojure.pprint :as pprint]
            tools
            [cheshire.core :as json]))

(def env :dev)

(def db (:db (edn/read-string (slurp 
                               (str (if (= :prod env)
                                      "/Users/daniel/Applications"
                                      "..")
                                    "/tracker/config.edn")))))

(def tools-list 
  {:get-issues 
   {:name        "get_issues"
    :description "Asks Tracker about all available items and lists 
                  them in the order
                  most recently touched first (where touched can mean a 
                  modification or just having looked at it).
                        
                        Now it gets even better. Once you know the \"id\" 
                        of a certain item you can find out which other items 
                        it is related to by supplying a selected-context id.
                        The search results will then be confined to items listed 
                        as related to that context (which is also an item)."
    
    :inputSchema {:type       "object"
                  :properties {:q                {:type        "string"
                                                  :description "Query string. Obviously, when trying to find anything, we need to narrow down the search result, ideally such that 
                                          the thing we search for is the top search result. Note that normally we limit the results to 10, so you might not even see any more.
                                          
                                          However, when a selected-context is given, you might want to give an empty query string to see all items available and related to a given context."}
                               :selected-context {:type        "string"
                                                  :description "an id number to narrow down the search results to items related to that context."}}
                  :required   ["q"]}}
   :get-item   {:name        "get_item"
                :description "Asks Tracker about a single item, and that means, in contrast
                        to listing items with for example get_issues, that the description is included,
                        Which is what we are often also interested in, once we identified the specific 
                        item we are after."
                
                :inputSchema {:type       "object"
                              :properties {:id {:type        "string"
                                                :description "the issue's id, as the issues returned from get_issues always include."}}
                              :required   ["id"]}}})

(defn get-issues [{:keys [q selected-context] :as _arguments}]
  (search/search-issues db (merge {:q q :limit 10}
                                  (when selected-context {:selected-context {:id selected-context}}))))

(defn get-item [{:keys [id] :as _arguments}]
  (ds/get-item db {:id id}))

(defn map-tool [name]
  (case name
    "get_issues" get-issues
    "get_item" get-item
    nil))

(comment
  (require '[cheshire.core :as json])
  (json/generate-string (get-issues {:q "YouTube"}))
  (pprint/pprint (get-issues {:q "YouTube"})))
