(ns mw-desktop.state
  "Global state of the application."
  (:require [mw-engine.world :refer [world?]]
            [taoensso.timbre :refer [info]])
  (:import [clojure.lang Keyword]))

(def valid-statuses
  #{:init :halted :halt-requested :running})

(def state
  "Global state of the application."
  (atom {:status :init}))

(defn get-state [^Keyword key]
  (@state key))

(defn await-status
  "Pause the current process until the global status is in the state `status-value`."
  [status-value]
  (while (not= (@state :status) status-value)
    (info "Awaiting status %s" status-value)
    (Thread/sleep 10000)))

(defn update-state!
  "Update the global state of the application. The arguments should
   be key-value pairs."
  [& kvs]
  (when-not (and (even? (count kvs))
                 (every? keyword? (take-nth 2 kvs)))
    (throw (IllegalArgumentException.
            "update-state expects an even number of arguments, and that every odd-numbered argument should be a keyword")))
  (let [deltas (into {} (map #(apply vector %) (partition 2 kvs)))]
         ;; there's probably a list of checks to be made here, and we probably 
         ;; want it to be able to add checks at runtime (?)
    (when (and (:world deltas) (not (world? (:world deltas))))
      (throw (ex-info "Attempt to set an invalid world"
                      {:deltas deltas
                       :status @state})))
    ;; you can't change either the world or the rules while the engine is 
    ;; computing a new state for the world. 
    (when (and (#{:world :rules} (keys deltas)) 
               (#{:running :halt-requested} (@state :status))) 
        (await-status :halted))
    (swap! state merge deltas)
    ;; if we've got both a world and rules, and we're in state :init, we
    ;; advance to state :halted
    (when (and (= (@state :status) :init)
               (:world @state)
               (:rules @state))
      (swap! state merge {:status :halted}))))