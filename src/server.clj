(ns server
  (:require [cheshire.core :as json]
            [clojure.java.io :as io]))

(def weather-responses
  ["The weather is delightfully sunny with a gentle breeze rustling through the leaves."
   "It's a perfectly dreary day with soft rain tapping against the window panes."
   "The sky is painted with fluffy white clouds drifting lazily across a brilliant blue canvas."
   "A mystical fog has rolled in, creating an enchanting atmosphere throughout the valley."
   "The air is crisp and clear, with snowflakes dancing gracefully in the winter sunlight."])

(defn get-weather [location]
  (rand-nth weather-responses))

(defn handle-request [request]
  (let [method (:method request)
        params (:params request)]
    (cond
      (= method "initialize")
      {:jsonrpc "2.0"
       :result  {:protocolVersion "2024-11-05"
                 :capabilities    {:tools {}}  ;; Just declare "we have tools"
                 :serverInfo      {:name    "weather-mcp"
                                   :version "1.0.0"}}}
      
      (= method "tools/list")
      {:jsonrpc "2.0"
       :result {:tools [{:name "get_weather"
                        :description "Get weather information for a location"
                        :inputSchema {:type "object"
                                    :properties {:location {:type "string"
                                                          :description "The location to get weather for"}}
                                    :required ["location"]}}]}}
      
      (= method "tools/call")
      (let [tool-name (get-in params [:name])
            arguments (get-in params [:arguments])]
        (if (= tool-name "get_weather")
          {:jsonrpc "2.0"
           :result {:content [{:type "text"
                              :text (get-weather (:location arguments))}]}}
          {:jsonrpc "2.0"
           :error {:code -32601
                  :message "Unknown tool"}}))
      
      :else
      {:jsonrpc "2.0"
       :error {:code -32601
              :message "Unknown method"}})))

(defn process-line [line]
  (when-not (empty? line)
    (try
      (let [request (json/parse-string line true)
            response (handle-request request)
            response-with-id (assoc response :id (:id request))]
        (println (json/generate-string response-with-id))
        (flush))
      (catch Exception e
        (println (json/generate-string {:jsonrpc "2.0"
                                        :error {:code -32700
                                               :message "Parse error"}
                                       :id nil}))
        (flush)))))

(defn -main [& _args]
  (doseq [line (line-seq (java.io.BufferedReader. *in*))]
    (process-line line)))
