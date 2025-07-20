(ns tools
  (:require [clojure.edn :as edn]
            [et.vp.ds.search :as search]
            [et.vp.ds :as ds]
            [clojure.pprint :as pprint]
            tools
            [cheshire.core :as json]))

(defonce db (:db (edn/read-string (slurp "../tracker/config.edn"))))

(defn get-issues [{:keys [q selected-context] :as _arguments}]
  (search/search-issues db (merge {:q q :limit 10}
                                  (when selected-context {:selected-context {:id selected-context}}))))

(defn get-item [{:keys [id] :as _arguments}]
  (ds/get-item db {:id id}))

(comment
  (require '[cheshire.core :as json])
  (json/generate-string (get-issues {:q "YouTube"}))
  (pprint/pprint (get-issues {:q "YouTube"})))
