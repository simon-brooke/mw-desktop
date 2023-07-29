(ns mw-desktop.fxui
  (:require [cljfx.api :as fx]
            [clojure.core.cache :refer [lru-cache-factory]]
            [clojure.java.io :refer [resource]]
            [clojure.string :refer [join lower-case starts-with?]]
            [mw-desktop.state :refer [get-state state update-state!]]))

;; OK, the basic idea here is we have a window divided vertically 
;; into two panes. The user can drag the division between the panes 
;; left and right. In the left pane is always the world; in the right, a 
;; number of pages can be displayed.
;;
;; 1. Documentation;
;; 2. The rule editor;
;; 3. The log;
;; 4. Data on what states are in use (and how many of each);
;; 5. Some way to get data on other properties (for the mutual aid model, we
;;    want to see how much food in total there is in the world, how much the
;;    richest centile has, how much the poorest, and how that's changing over
;;    time; but whether I have the skill to make that something the user can
;;    configure is another matter).
;;
;; There is a File menu with options to:
;;
;; 1. Save the world as an EDN file;
;; 2. Load the world from an EDN file;
;; 3. Create a world from a height map;
;; 4. Load a rules file;
;; 5. Save a rules file;
;; 4. Load (? or register?) a tile set (probably as a jar file?);
;;
;; There is a World menu with options to:
;;
;; 1. Start the world running;
;; 2. Pause/Stop the run;
;; 3. Select a tile set;
;; 
;; There is a View menu with options to change the display in the right 
;; hand pane.
;;
;; 1. The rule editor;
;; 2. The documentation;
;; 3. Stats displays (but this needs more thought and experimentation)
;; 
;; One thought is I might define new rule language to create graphs and charts.
;;
;; 'timeseries total food where state is house group by decile'
;; 'timeseries total food where state is house, fertility where state is pasture or crop or fallow'
;; 'graph fertility by altitude'
;; 'barchart fertility by state'
;; 'piechart count group by state'
;; 
;; In which case you probably have one graph page per rule.

(defn- tile-image [{:keys [url]}]
  {:fx/type :image-view
   :image {:url url
           :requested-width 20
           :preserve-ratio true
           :background-loading true}})

(defn world-view [{:keys [world tileset]}]
  ;; assumes that by the time we get here, a tileset is a clojure map
  ;; in which the keys are the names of the tiles, without file extension, as
  ;; keywords (i.e. they're states, from the point of view of the world), and
  ;; in which the values are just java images (bitmaps), or else maps which
  ;; wrap java images with some other related data for example dimensions.
  (let [th (or (:height (first tileset)) 20)
        tw (or (:width (first tileset)) 20)
        cols (count (first world))
        rows (count world)]
   {:fx/type :tile-pane
    :hgap 0
    :pref-columns cols
    :pref-rows rows
    :pref-tile-height th
    :pref-tile-width tw
    :vgap 0
    :children (map (fn [cell]{:fx/type tile-image
                      :tile-pane/alignment :bottom-center
                      :url (resource (format "%s/%s.png" tileset (:state cell)))}) 
                   (flatten world))}))

(defn root-view [{{:keys [world rules]} :state}]
  {:fx/type :stage
   :showing true
   :scene {:fx/type :scene
           :root {:fx/type :split-pane
                  :items [{:fx.type :scroll-pane
                           :content {:fx/type world-view}}]}}})


(defmulti handle-event :event/type)

(defmethod handle-event :default [e]
  (prn e))

(defmethod handle-event ::type-text [{:keys [fx/event fx/context]}]
  {:context (fx/swap-context context assoc :typed-text event)})

(defmulti event-handler 
  "Multi-method event handler cribbed from e12-interactive-development"
  :event/type)

(def renderer
  "Renderer cribbed from e12-interactive-development"
  (fx/create-renderer
   :middleware (fx/wrap-map-desc (fn [state]
                                   {:fx/type root-view
                                    :state state}))
   :opts {:fx.opt/map-event-handler event-handler}))

(fx/mount-renderer state renderer)

;; (defn ui
;;   [_config]
;;   (update-state! :ui
;;              (fx/create-app state
;;                  :event-handler event-handler
;;                  :desc-fn (fn [state]
;;                             {:fx/type root-view
;;                              :state state}))))