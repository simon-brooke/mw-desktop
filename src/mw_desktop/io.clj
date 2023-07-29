(ns mw-desktop.io
  "Handle loading (and, ultimately, saving) files for the MicroWorld desktop
   app."
  (:require [clojure.java.io :refer [as-file file input-stream resource]]
            [clojure.string :refer [split starts-with?]]
            [mw-desktop.state :refer [update-state!]]
            [mw-engine.heightmap :refer [apply-heightmap]]
            [mw-engine.world :refer [world?]]
            [mw-parser.declarative :refer [compile]]
            [pantomime.mime :refer [mime-type-of]])
  (:import [java.net URL]))

(defn identify-resource
  "Identify whether `path` represents a file, a resource (preferring the file
   if both exist) or URL, identify the MIME type of the content, and return a
   map with keys `:stream` whose value is an open stream on the content and
   `type` whose value is the MIME type of the content."
  [path]
  (let [f (.exists (file path))
        r (resource path)
        u (when-not (or f r) (URL. path))
        p' (cond (and f r) (file path)
                 r r
                 u u
                 :else path)]
    {:path p'
     :stream (input-stream p')
     :type (mime-type-of p')}))

(defn load-ruleset!
  "Load a ruleset from `path`, which may be either a file path or a resource
   path, and should indicate a text file of valid MicroWorld rules.

   Where a file and resource with this `path` both exist, the file is 
   preferred. Updates global state."
  [path]
  (update-state!
   :rules
   (doall (compile (slurp (:stream (identify-resource path)))))))

(defn assemble-tile-set
  "Return a map of image files in the directory at this `dir-path`, keyed by
   keywords formed from their basename without extension."
  [dir-path]
  (let [tiles (file-seq (as-file dir-path))]
    (into {}
          (map
           #(vector
             (keyword
              (first
               (split (.getName %) #"\.")))
             %)
           (filter #(starts-with? (mime-type-of %) "image")
                   (remove #(.isDirectory %) tiles))))))

(defn load-tileset!
  "Load a tileset from `path`, which may be either a file path or a resource
   path, and should indicate a directory containing same-size image files.

   Where a file and resource with this `path` both exist, the file is 
   preferred. Updates global state."
  [path]
  (let [{p' :path} (identify-resource path)
        dir-path (as-file p')]
    (if (.isDirectory dir-path)
      (update-state! :tileset (assemble-tile-set dir-path))
      (throw (ex-info "Tileset should be a directory of image files"
                      {:path path
                       :expanded dir-path}))))

  (update-state!
   :tileset
   (doall (compile (slurp (:stream (identify-resource path)))))))

(defn load-world!
  "Load a world from `path`, which may be either a file path or a resource
   path, and may indicate either a world file (EDN) or a heightmap (image).

   Where a file and resource with this `path` both exist, the file is 
   preferred. Updates global state."
  [path]
  (let [data (identify-resource path)
        {type :type stream :stream} data
        world (try (if (starts-with? type "image/")
                     (apply-heightmap stream)
                     (read-string (slurp stream)))
                   (catch Exception any
                     (throw
                      (ex-info
                       (format
                        "Failed to read `%s` as either EDN or heightmap."
                        path)
                       (merge data {:path path})
                       any))))]
    (if (world? world) (do (update-state! :world world) world)
        (throw (ex-info "Invalid world file?"
                        (merge data {:path path
                                     :data world}))))))
