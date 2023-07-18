(ns mw-desktop.state
  "Global state of the application."
  (:require [mw-engine.utils :refer [member?]]
            [mw-engine.world :refer [world?]])
  (:import [clojure.lang Keyword]))

(def valid-states
  #{:init :halted :halt-requested :running})

(def state
  "Global state of the application."
  (atom {:state :init}))

(defn get-state [^Keyword key]
  (@state key))

(defn await-state
  "Pause the current process until the global status is in the state `state-value`."
  [state-value]
  (while (not= (@state :state) state-value)
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
                       :state @state})))
    ;; you can't change either the world or the rules while the engine is computing
    ;; a new status for the world.
    (when-not (= (@state :state) :init)
      (when (or (member? (keys deltas) :world)
              (member? (keys deltas) :rules))
      (await-state :halted)) )
    (swap! state merge deltas)
    (when (and (= (@state :state) :init)
               (:world state)
               (:rules state))
      (swap! state merge {:state :halted}))))