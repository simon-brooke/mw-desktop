(ns mw-desktop.ui-components.rule-editor 
  (:require [cljfx.api :as fx]
            [clojure.string :refer [join]]
            [mw-desktop.state :refer :all]))

(defn rule-editor 
  "Rule editor comprises a scrolling container, with within it,
   
   1. name of loaded rule file (non editable, visually distinguished) at the
      top;
   2. line numbers in a column on the left;
   3. editable rules source text in the middle;
   4. parser/compiler errors in a panel as the bottom."
  [{{:keys [rules-file rules-src parse-errors]} :state}]
  {:fx/type :scroll-pane
   :content {:fx/type :border-pane
   :top {:fx/type :label
         :border-pane/alignment :center
         :border-pane/margin 2
         :text rules-file}
   :left {:fx/type :label
          :border-pane/margin 2
          :text (join "\n" 
                      (map #(format "%5d" %) 
                           (range 1 (inc (count (re-seq #"\n" rules-src))))))}
   :center {:fx/type :text-area
            :style-class "input"
            :text (fx/sub-val rules-src :typed-text)
            :on-text-changed {:event/type ::type-text :fx/sync true}}
   :bottom {:fx/type :label
            :border-pane/margin 2
            :text (or parse-errors "error messages will appear here.")}}
   }
  )
