(ns mw-desktop.core
  (:require [clojure.string :refer [join]]
            [clojure.tools.cli :refer [parse-opts]]
            ;; [mw-desktop.fxui :refer [ui]]
            [mw-desktop.io :refer [load-ruleset! load-tileset! load-world!]])
  (:gen-class))

(def defaults
  "Defaults for command line arguments."
  {:ruleset "rulesets/basic.txt"
   :tileset "tilesets/world/"
   :world "heightmaps/small_hill.png"})

(def cli-options
  [["-h" "--help"]
   ["-r FILEPATH" "--ruleset FILEPATH" "The ruleset to load"]
   ["-t FILEPATH" "--tileset FILEPATH" "The tileset to load"]
   ["-w FILEPATH" "--world FILEPATH" "Choose the world to load"]])

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (join \newline errors)))

(defn validate-args
  "Validate command line arguments. Either return a map indicating the program
  should exit (with an error message, and optional ok status), or a map
  indicating the action the program should take and the options provided."
  [args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    (cond
      (:help options) ; help => exit OK with usage summary
      {:exit-message (:summary args) :ok? true}
      errors ; errors => exit with description of errors
      {:exit-message (error-msg errors)}
      ;; custom validation on arguments
      (and (= 1 (count arguments))
           (#{"start" "stop" "status"} (first arguments)))
      {:action (first arguments) :options options}
      :else ; failed custom validation => exit with usage summary
      {:exit-message (:summary args)})))

(defn -main
  "Parse options, print the banner, read the init file if any, and enter the
  read/eval/print loop."
  [& opts]
  (let [args (parse-opts opts cli-options)
        options (merge defaults (:options args))]
    
    (load-ruleset! (:ruleset options))
    (load-tileset! (:tileset options))
    (load-world! (:world options))
    
    (when (:help args) (println (:summary args)) (System/exit 0))
    ;;(ui options)
    (println options)
    ))
