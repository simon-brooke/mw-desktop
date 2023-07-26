(ns mw-desktop.swing-ui
  (:require [clojure.java.io :refer [resource]]
            [clojure.string :refer [join]]
            [markdown.core :refer [md-to-html-string]]
            [mw-desktop.state :refer [get-state update-state!]]
            [seesaw.core :refer [border-panel editor-pane frame
                                 left-right-split menu menu-item menubar native! pack!
                                 scrollable separator show! tabbed-panel table text]])
  (:import [org.htmlcleaner CleanerProperties HtmlCleaner SimpleHtmlSerializer]))

;; This is probably a dead end. Its performance is terrible; the fxui version looks
;; much more promising.

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

(update-state! :world-view (table :model [:columns [{:key :name, :text "Name"} :likes]
                                          :rows '[["Bobby" "Laura Palmer"]
                                                  ["Agent Cooper" "Cherry Pie"]
                                                  {:likes "Laura Palmer" :name "James"}
                                                  {:name "Big Ed" :likes "Norma Jennings"}]])
               :rule-editor (text :editable? true
                                  :id :rule-editor
                                  :multi-line? true
                                  :text ";; This is where you will write your rules.")
               :error-panel (text :editable? false
                                  :id :error-panel
                                  :foreground "maroon"
                                  :multi-line? true
                                  :text ";; Errors will be shown here."))

(defn markdown->html
  "`md-to-html-string` returns an HTML fragment that `editor-pane` chokes on. 
   This is an attempt to do better. It sort-of works -- produces nice clean
   HTML -- but the performance of `editor-pane` is still unacceptably poor"
  [md-text]
  (let [props (CleanerProperties.)]
    (.setOmitDoctypeDeclaration props false)
    (.setOmitDeprecatedTags props true)
    (.setOmitUnknownTags props true)
    (.setOmitXmlDeclaration props true)
    (.getAsString (SimpleHtmlSerializer. props)
                  (.clean (HtmlCleaner. props) (md-to-html-string md-text)))))

(defn make-multi-view
  "Make the right hand multi-view panel."
  []
  (tabbed-panel
   :tabs [{:title "Rules"
           :content (border-panel
                     :center (scrollable
                              (border-panel
                               :center (get-state :rule-editor)
                               :west (text :columns 4
                                           :editable? false
                                           :foreground "cornflowerblue"
                                           :id :line-numbers
                                           :multi-line? true
                                           :text (join "\n"
                                                       (map str (range 1 1000)))
                                           :wrap-lines? false)))
                     :south (scrollable
                             (get-state :error-panel)))}
          {:title "Grammar"
           :content (scrollable
                     ;; the performance of laying out HTML in an editor-pane 
                     ;; is painful! RTF is better but not good, and unreliable.
                     (editor-pane :editable? false
                           ;; :multi-line? true
                           :content-type "text/plain"
                           :text (slurp
                                  (resource "doc/grammar.md"))
                           ;; :wrap-lines? true
                           ))}]))

(update-state! :multi-view (make-multi-view))

(defn create-app-window
  "Create the app window."
  []
  (native!)
  (update-state! :app-window
                 (pack!
                  (frame :title "MicroWorld"
                       ;; :size  [600 :by 600]
                       ;; :on-close :exit
                         :menubar (menubar
                                   :items
                                   [(menu :text "World" :items
                                          [(menu-item :text "Run World"  :enabled? false)
                                           (menu-item :text "Halt Run" :enabled? false)
                                           (separator)
                                           (menu :text "Create World..." :items
                                                 [(menu-item :text "From Heightmap...")
                                                  (menu-item :text "From Coordinates...")])
                                           (menu-item :text "Load World File...")
                                           (menu-item :text "Save World File As...")
                                           (separator)
                                           (menu-item :text "Import Tile Set...")])
                                    (menu :text "Rules" :items
                                          [(menu-item :text "New Rule Set")
                                           (menu-item :text "Open Rule Set...")
                                           (menu-item :text "Save Rule Set")
                                           (menu-item :text "Save Rule Set As...")
                                           (separator)
                                           (menu-item :text "Compile Rules")])
                                    (menu :text "Help" :items
                                          [(menu-item :text "About MicroWorld")])])
                         :content (left-right-split (scrollable (get-state :world-view))
                                                    (scrollable (get-state :multi-view))
                                                    :divider-location 8/10)))))

(defn show-app-window [] (show! (get-state :app-window)))
