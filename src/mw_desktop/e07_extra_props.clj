(ns mw-desktop.e07-extra-props
  (:require [cljfx.api :as fx]
            [clojure.java.io :refer [as-url resource]]
            [mw-desktop.io :refer [load-ruleset! load-tileset! load-world!]]
            [mw-desktop.state :refer [get-state state update-state!]]))

(def anchor-pane
  {:fx/type :anchor-pane
   :children [{:fx/type :label
               :anchor-pane/left 10
               :anchor-pane/bottom 10
               :text "bottom-left"}
              {:fx/type :label
               :anchor-pane/top 10
               :anchor-pane/right 10
               :text "top-right"}
              {:fx/type :label
               :anchor-pane/left 100
               :anchor-pane/top 100
               :anchor-pane/right 100
               :anchor-pane/bottom 100
               :style {:-fx-background-color :lightgray
                       :-fx-alignment :center}
               :text "Try resizing window too!"}]})

(def border-pane
  {:fx/type :border-pane
   :top {:fx/type :label
         :border-pane/alignment :center
         :border-pane/margin 10
         :text "top header"}
   :left {:fx/type :label
          :border-pane/margin 10
          :text "left sidebar"}
   :right {:fx/type :label
           :border-pane/margin 10
           :text "right sidebar"}
   :center {:fx/type :label
            :border-pane/margin 10
            :text "center content"}
   :bottom {:fx/type :label
            :border-pane/margin 10
            :text "bottom footer"}})

(def flow-pane
  {:fx/type :flow-pane
   :vgap 5
   :hgap 5
   :padding 5
   :children (repeat 100 {:fx/type :rectangle :width 25 :height 25})})

(def grid-pane
  {:fx/type :grid-pane
   :children (concat
              (for [i (range 16)]
                {:fx/type :label
                 :grid-pane/column i
                 :grid-pane/row i
                 :grid-pane/hgrow :always
                 :grid-pane/vgrow :always
                 :text "boop"})
              [{:fx/type :label
                :grid-pane/row 2
                :grid-pane/column 3
                :grid-pane/column-span 2
                :text "I am a long label spanning 2 columns"}])})

(def h-box
  {:fx/type :h-box
   :spacing 5
   :children [{:fx/type :label
               :text "just label"}
              {:fx/type :label
               :h-box/hgrow :always
               :max-width Double/MAX_VALUE
               :style {:-fx-background-color :lightgray}
               :text "expanded label"}
              {:fx/type :label
               :h-box/margin 100
               :text "label with big margin"}]})

(def stack-pane
  {:fx/type :stack-pane
   :children [{:fx/type :rectangle
               :width 200
               :height 200
               :fill :lightgray}
              {:fx/type :label
               :stack-pane/alignment :bottom-left
               :stack-pane/margin 5
               :text "stacked label"}
              {:fx/type :text-field
               :stack-pane/alignment :top-right
               :stack-pane/margin 5
               :max-width 300
               :text "Text field in top-right corner"}]})

(def rules (load-ruleset! "rulesets/basic.txt"))
(def tiles (load-tileset! "tilesets/world"))
(def world (load-world! "init-state/world.edn"))

(defn tile-image [{:keys [url]}]
  {:fx/type :image-view
   :image {:url url
           :requested-width 20
           :preserve-ratio true
           :background-loading true}})

;; {:fx/type tile-image
;;  :tile-pane/alignment :bottom-center
;;  :url "https://i.imgur.com/oy91jyx.gif"}

(defn assemble-tiles
  [world tiles]
  (map #(assoc {}
               :fx/type tile-image
               :tile-pane/alignment :bottom-center
               :url (as-url (or (tiles (:state %))
                                (resource "tilesets/world/error.png"))))
       (flatten world)))

(def tile-pane
  (let [world (get-state :world)
        tiles (get-state :tileset)
        cols (count (first world))
        children (assemble-tiles world tiles)]
    {:fx/type :scroll-pane
     :fit-to-width false
     :content {:fx/type :tile-pane
               :pref-columns cols
               :hgap 1
               :vgap 1
               :children children
               }}))

(def v-box
  {:fx/type :v-box
   :spacing 5
   :fill-width true
   :alignment :top-center
   :children [{:fx/type :label :text "just label"}
              {:fx/type :label
               :v-box/vgrow :always
               :style {:-fx-background-color :lightgray}
               :max-height Double/MAX_VALUE
               :max-width Double/MAX_VALUE
               :text "expanded label"}]})

(def button-bar
  {:fx/type :button-bar
   :button-min-width 100
   :buttons [{:fx/type :button
              :button-bar/button-data :yes
              :text "Yes"}
             {:fx/type :button
              :button-bar/button-data :no
              :text "No"}]})

(def split-pane
  {:fx/type :split-pane
   :divider-positions [0.5]
   :items [{:fx/type :label
            :split-pane/resizable-with-parent false
            :padding 50
            :text "This is label that is NOT resizable with parent"}
           {:fx/type :label
            :padding 50
            :text "This is a label resizable with parent"}]})


(fx/on-fx-thread
 (fx/create-component
  {:fx/type :stage
   :showing true
   :title "Pane examples"
   :scene {:fx/type :scene
           :root {:fx/type :tab-pane
                  :pref-width 960
                  :pref-height 540
                  :tabs [{:fx/type :tab
                          :text "Anchor Pane"
                          :closable false
                          :content anchor-pane}
                         {:fx/type :tab
                          :text "Border Pane"
                          :closable false
                          :content border-pane}
                         {:fx/type :tab
                          :text "Flow Pane"
                          :closable false
                          :content flow-pane}
                         {:fx/type :tab
                          :text "Grid Pane"
                          :closable false
                          :content grid-pane}
                         {:fx/type :tab
                          :text "HBox"
                          :closable false
                          :content h-box}
                         {:fx/type :tab
                          :text "Stack Pane"
                          :closable false
                          :content stack-pane}
                         {:fx/type :tab
                          :text "Tile Pane"
                          :closable false
                          :content tile-pane}
                         {:fx/type :tab
                          :text "VBox"
                          :closable false
                          :content v-box}
                         {:fx/type :tab
                          :text "Button Bar"
                          :closable false
                          :content button-bar}
                         {:fx/type :tab
                          :text "SplitPane"
                          :closable false
                          :content split-pane}]}}}))

