(ns lazybot.plugins.iamat
  (:use lazybot.registry
        [useful.map :only [keyed]]
        [somnium.congomongo :only [fetch-one insert! update!]]) )


(defn- record-nick-location [{:keys [com bot nick channel message action?]}]
  (let [attrs (keyed [nick])]
    (println "Noted that" nick " was at "
             (clojure.string/replace-first message #"@iamat " "")
             " at " (java.util.Date.))
    (update! :locations attrs
             (assoc attrs
               :location (clojure.string/replace-first message #"@iamat " "")
               :timestamp (java.util.Date.)))))

(defn- get-nick-location [nick]
  (fetch-one :locations
             :where {:nick nick}))

(defplugin
  (:cmd
   "Tell bot where you are."
   #{"iamat"}
   #'record-nick-location)
  (:cmd
   "Ask where a nick is"
   #{"whereis"}
   (fn [{:keys [com bot channel args] :as com-m}]
     (let [nick (first args)]
       (println nick)
       (send-message com-m
                     (if-let [loc (get-nick-location nick)]
                       (str nick " said he was at _" (:location loc) "_ around " (:timestamp loc))
                       (str "I've no memory of where " nick " is. I wasn't informed! *frown*")))))))
