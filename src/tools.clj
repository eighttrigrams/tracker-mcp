(ns tools
  (:require [clojure.edn :as edn]
            [et.vp.ds.search :as search]
            [et.vp.ds :as ds]
            [clojure.pprint :as pprint]
            tools
            [cheshire.core :as json]))

(def env :prod)

(def db (:db (edn/read-string (slurp 
                               (str (if (= :prod env)
                                      "/Users/daniel/Applications"
                                      "..")
                                    "/tracker/config.edn")))))

(def tools-list 
  {:get-items
   {:name        "get_items"
    :description 
    "When I am in Claude and ask what I think about this or that or 
     anything relating to my memory (as the current user using Claude and its tools)
     I am actually making a reference to Tracker/the Tracker system.
     
     Asks Tracker about all available items and lists 
                  them in the order
                  most recently touched first (where touched can mean a 
                  modification or just having looked at it).

                  Use the only-contexts \"true\" option as a default when starting searches. Reason:
                  When starting searches (spanning possibly multiple tool calls), it is good
                  to find items which are marked as \"contexts\" before following chains
                  via \"get-related-items\" etc. Use the \"only-contexts\" argument of the get_items tool for that. 
                  When you don't find what you search for, instantly, instead of omitting the argument as true here,
                  try first a couple of searches with that argument left as true, but different search terms.
                        
                        Now it gets even better. Once you know the \"id\" 
                        of a certain item you can find out which other items 
                        it is related to by supplying a selected-context-id.
                        The search results will then be confined to items listed 
                        as related to that context (which is also an item).
     
     Also use the q parameter more for finding generic categories than for anything too specific
     since tracker does not always contain everything in titles. Other strategies are better
     for example when searching for Quotes on the Scientific Method, try to find the context
                  for Quotes and the Context for Scientic Method, and then try
                  to make an intersection search by using get-related-items
                  with the arguments selected-context-id (passing the id for Scientic Method)
                  and secondary-contexts-items-ids (passing an array with the only item being 
                  the id of Quotes).
     
     "
    :inputSchema {:type       "object"
                  :properties {:q                {:type        "string"
                                                  :description "Query string. Obviously, when trying to find anything, we need to narrow down the search result, ideally such that 
                                          the thing we search for is the top search result. Note that normally we limit the results to 10, so you might not even see any more."}
                               :only-contexts   {:type        "string" 
                                                 :description "with a value \"true\" on this parameter, it lists only items marked as contexts. These are more often better entrypoints into a search consisting of possibly
                                                                multiple tool calls with follow ups via get_related_items than a broader search without that parameter would be. Prefer that to get the broad categories of a search right first."}}
                  :required   ["q"]}}
   #_#_:get-aggregated-contexts
   {:name "get_aggregated_contexts"
    :description "When you do a get_related_items search, you are operating within a given
                  context. And you will get a list of items associated with that context.
                  But these items are also associated to other contexts, a listing of which you
                  get by querying get_aggregated_contexts. You can use select ids of these
                  to filter in a subsequent get_related_items query"
    :inputSchema {:type       "object"
                  :properties {:selected-context-item-id 
                               {:type        "string"
                                :description "an id number to narrow down the search results to items related to that context."}}
                  :required   ["selected-context-item-id"]}}
   :get-related-items
   {:name        "get_related_items"
    :description "Asks Tracker about related items to a given context item and lists 
                  them in the order
                  most recently touched first (where touched can mean a 
                  modification or just having looked at it).
                  
                  If you can, prefer intersection search using the secondary_contexts_items_ids
                  argument, thereby narrowing down results with multiple context ids, over using
                  all to specific search strings provided under the q parameter.
                  
                  If you can get away with it, then use multiple ids in secondary_contexts_items_ids
                  to make the search even narrower defined. 

                  When doing that, make the broader categories/contexts ids be provided
                  in secondary_contexts_items_ids and the less specific id provided as
                  selected-context-item-id. For example, Quotes on the Second World War ->
                  Quotes is the broader category, so the quotes id is provided via
                  secondary_contexts_items_ids and the Second World War id is provided via
                  selected-context-item-id.
                  "
    
    :inputSchema {:type       "object"
                  :properties {:q {:type        "string"
                                   :description "Query string. 
                                                 
                                                 Before I describe this argument, note that it is often preferential to
                                                 pre-filter items by using intersection search by passing secondary-contexts-items-ids
                                                 rather than using an actual search term for q.
                                                 
                                                 Obviously, when trying to find anything, we need to narrow down the search result, ideally such that 
                                          the thing we search for is the top search result.
                                                 
                                          However, when a selected-context is given, you might want to give an empty query string to see all items available and related to a given context.
                                                 
                                                 An even better strategy within Tracker to filter for good results is to specify secondary-contexts-items-ids to 
                                                 search in intersections of contexts. This is often better to use query strings."}
                               :selected-context-item-id 
                                 {:type        "string"
                                  :description "an id number to narrow down the search results to items related to that context.
                                                when doing intersection searches, use this id for the narrowest/most specific of the contexts."}
                               :secondary-contexts-items-ids
                               {:type "array"
                                :items {:type "string"}
                                :description "A definitive success strategy in Tracker:
                                              When doing a get_related_items query that is narrowed down
                                              by additional contexts, only items are shown which are also part of these other contexts.
                                              
                                              When doing intersection searches, make broader and more generic categories  
                                              be deployed via secondary-contexts-items-ids and reserve selected-context-item-id 
                                              for the narrowest of the contexts."}}
                  :required   ["q" "selected-context-item-id"]}}
   :get-item   {:name        "get_item"
                :description "Asks Tracker about a single item, and that means, in contrast
                        to listing items with for example get_issues, that the description is included,
                        Which is what we are often also interested in, once we identified the specific 
                        item we are after.
                              
                        Also, once you have an issue that looks interesting, you should definitely
                        more often than not also check for its related item, ussing its id,
                              using get_related_items and the selected-context-item-id parameter.
                              "
                :inputSchema {:type       "object"
                              :properties {:id {:type        "string"
                                                :description "the item's id, as the issues returned from get_items and get_related_items always include."}}
                              :required   ["id"]}}})

(defn get-items [{:keys [q selected-context-item-id only-contexts] :as _arguments}]
  (when selected-context-item-id (throw (IllegalArgumentException. "shouldn't pass selected-context-item-id. For this use get-related-items")))
  (when (not (or (= "true" only-contexts) (nil? only-contexts))) 
    (throw (IllegalArgumentException. "only contexts should either be \"true\" or nil/null (omit the parameter/argument entirely when it should say anything other than true)")))
  (if (= "true" only-contexts)
    (search/search-contexts db (merge {:limit 10 :force-limit? true}))
    (search/search-issues db q (merge {:limit 10 :force-limit? true}))))

(defn get-related-items [{:keys [q selected-context-item-id :secondary-contexts-items-ids] :as _arguments}]
  (search/search-issues 
   db
   q 
   {:selected-context {:id   selected-context-item-id
                       :data {:views {:current {:secondary-contexts secondary-contexts-items-ids}}}}
    :limit 10
    :force-limit? true}))

#_(defn get-aggregated-contexts [{:keys [selected-context-item-id] :as _arguments}]
  (search/fetch-aggregated-contexts db {:selected-context {:id selected-context-item-id}}))

(defn get-item [{:keys [id] :as _arguments}]
  (ds/get-item db 
               ;; TODO make the & arg-map thing to pass in :id id without specifying map, then check whether arg is id, or title, to replace get-by-title
               {:id id}))

(defn map-tool [name]
  (case name
    "get_items" get-items
    "get_related_items" get-related-items
    ;; "get_aggregated_contexts" get-aggregated-contexts
    "get_item" get-item
    nil))

(comment
  (require '[cheshire.core :as json])
  #_(get-aggregated-contexts {:selected-context-item-id 10935})
  (search/search-contexts db "")
  (json/generate-string (get-items {:q "YouTube"}))
  (pprint/pprint (get-items {:q "YouTube"})))
