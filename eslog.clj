(ns lazybot.plugins.eslog
  (:use lazybot.registry)
  (:require [clojurewerkz.elastisch.rest :as esr]
            [clojurewerkz.elastisch.rest.index :as esi]
            [clojurewerkz.elastisch.rest.document :as esd]
            [compojure.core :only [GET POST]]))

(def es-host "http://127.0.0.1:9200")

(defn get-connection []
  (esr/connect! es-host))

(defn create-schema []
  (let [mapping-types {:message
                       {:properties {:user       {:type "string" :store "yes"}
                                     :content    {:type "string" :store "yes" :analyzer "snowball"}
                                     :channel    {:type "string" :store "yes"}
                                     :timestamp  {:type "date" }}}}]
    (get-connection)
    (esi/create "lunatech-irc-messages" :mappings mapping-types)))


;; (create-schema)

(defn log-message [{:keys [com bot nick channel message action?]}]
  (if (.startsWith channel "#" )
    (esd/create "lunatech-irc-messages"
                :message
                {:user nick
                 :content message
                 :channel channel
                 :timestamp (java.util.Date.)})))


(defplugin
  (:hook :on-message #'log-message)
  (:routes (GET "/lunatictacs" req (handler req)))
  (:cmd
   "Say hello!"
   #{"eshi"}
   (fn [com-m]
     (send-message com-m "Hello there -- ElasticSearch Logger Plugin"))))
