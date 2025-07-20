(ns server
  (:require [cheshire.core :as json] 
            tools))

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

(defn handle-request [request]
  (let [method (:method request)
        params (:params request)]
    (merge 
     {:jsonrpc "2.0"}
     (cond
       (= method "initialize")
       {:result {:protocolVersion "2024-11-05"
                 :capabilities    {:tools {}}  ;; Just declare "we have tools"
                 :serverInfo      {:name    "tracker-mcp"
                                   :version "1.0.0"}}}
       
       (= method "tools/list")
       {:jsonrpc "2.0"
        :result  {:tools (mapv second tools-list)}}
       (= method "tools/call")
       (let [tool-name (get-in params [:name])
             arguments (get-in params [:arguments])]
         (cond
           (= tool-name "get_issues")
           {:jsonrpc "2.0"
            :result  {:content [{:type "text"
                                 :text (json/generate-string (tools/get-issues arguments))}]}}
           (= tool-name "get_item")
           {:jsonrpc "2.0"
            :result  {:content [{:type "text"
                                 :text (json/generate-string (tools/get-item arguments))}]}}
           :else
           {:jsonrpc "2.0"
            :error   {:code    -32601
                      :message "Unknown tool"}}))
       
       :else
       {:jsonrpc "2.0"
        :error   {:code    -32601
                  :message "Unknown method"}}))))

(defn process-line [line]
  (when-not (empty? line)
    (try
      (let [request (json/parse-string line true)
            response (handle-request request)
            response-with-id (assoc response :id (:id request))]
        (println (json/generate-string response-with-id))
        (flush))
      (catch Exception _e
        (println (json/generate-string {:jsonrpc "2.0"
                                        :error {:code -32700
                                                :message "Parse error"}
                                        :id nil}))
        (flush)))))

(defn -main [& _args]
  (doseq [line (line-seq (java.io.BufferedReader. *in*))]
    (process-line line)))
