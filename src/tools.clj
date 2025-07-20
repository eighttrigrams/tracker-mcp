(ns tools
  (:require [clojure.edn :as edn]
            [et.vp.ds.search :as search]
            [clojure.pprint :as pprint]
            tools))

(defonce db (:db (edn/read-string (slurp "../tracker/config.edn"))))

(def weather-responses
  ["The weather is delightfully sunny with a gentle breeze rustling through the leaves."
   "It's a perfectly dreary day with soft rain tapping against the window panes."
   "The sky is painted with fluffy white clouds drifting lazily across a brilliant blue canvas."
   "A mystical fog has rolled in, creating an enchanting atmosphere throughout the valley."
   "The air is crisp and clear, with snowflakes dancing gracefully in the winter sunlight."])

(defn get-weather [_arguments]
  #_(:location arguments)
  (rand-nth weather-responses))

(comment
  (pprint/pprint (search/search-contexts db {:q "YouTube"})))
