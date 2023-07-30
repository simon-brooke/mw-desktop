(ns mw-desktop.ui-components.world-view
  "View of the world"
  (:require [cljfx.api :as fx]
            [clojure.java.io :refer [as-url resource]]
            [mw-desktop.io :refer [load-world!]]
            [mw-desktop.state :refer [state]]))

;; World view is essentially a grid of tiles, one per cell. In the long term 
;; I want a context menu, allowing the user to
;;
;; 1. inspect cell properties;
;; 2. change non-protected properties, including state;
;; 3. browse cell history in a user-friendly presentation.
;;
;; In the short term, just being able to scroll around the grid is enough.

(defn- tile-image [{:keys [url]}]
  {:fx/type :image-view
   :image {:url url
           :requested-width 20
           :preserve-ratio true
           :background-loading true}})

(defn assemble-tiles
  [world tiles]
  (map (fn [cell]
         {:fx/type tile-image
          :tile-pane/alignment :bottom-center
          :url (as-url (or (tiles (:state cell))
                           (resource "tilesets/world/error.png")))})
       (flatten world)))

(defn world-view
  [{{:keys [world tileset]} :state}]
  ;; assumes that by the time we get here, a tileset is a clojure map
  ;; in which the keys are the names of the tiles, without file extension, as
  ;; keywords (i.e. they're states, from the point of view of the world), and
  ;; in which the values are URLs which point to image files.
  (let [th (or (:height (first tileset)) 20)
        tw (or (:width (first tileset)) 20)
        cols 48 ;; (count (first world))
        rows 47;; (count world)
        children (assemble-tiles world tileset)]
    {:fx/type :tile-pane
     :hgap 0
     :pref-columns cols
     :pref-rows rows
     :pref-tile-height th
     :pref-tile-width tw
     :vgap 0
     :children children}))


;;; From this point on we're just constructing a test harness to launch the 
;;; component in isolation.
(load-world! "init-state/world.edn")
(defmulti handle-event :event/type)

(defmethod handle-event :default [e]
  (prn e))

(def app
  "Test purposes only"
  (fx/create-app state
                 :event-handler handle-event
                 :desc-fn (fn [_]
                            {:fx/type :stage
                             :showing true
                             :width 960
                             :height 540
                             :scene {:fx/type :scene
                                     :root {:fx/type world-view}}})))