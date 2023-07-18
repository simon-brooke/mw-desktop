(ns mw-desktop.core
  (:require [clojure.java.io :refer [file]]
            [clojure.tools.cli :refer [parse-opts]])
  (:gen-class))

(def cli-options
  [["-f FILEPATH" "--file-path FILEPATH"
    "Set the path to the directory for reading and writing Lisp files."
    :validate [#(and (.exists (file %))
                     (.isDirectory (file %))
                     (.canRead (file %))
                     (.canWrite (file %)))
               "File path must exist and must be a directory."]]
   ["-h" "--help"]
   ["-p PROMPT" "--prompt PROMPT" "Set the REPL prompt to PROMPT"
    :default "Sprecan::"]
   ["-r SYSOUTFILE" "--read SYSOUTFILE" "Read Lisp system from file SYSOUTFILE"
    :validate [#(and
                 (.exists (file %))
                 (.canRead (file %)))
               "Could not find sysout file"]]
   ["-s" "--strict" "Strictly interpret the Lisp 1.5 language, without extensions."]
   ["-t" "--time" "Time evaluations."]
   ["-x" "--testing" "Disable the jline reader - useful when piping input."]])

(defn -main
  "Parse options, print the banner, read the init file if any, and enter the
  read/eval/print loop."
[& opts]
(let [args (parse-opts opts cli-options)]
  (println "Hello, World!")))
