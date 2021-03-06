(ns lumo.classpath
  (:require [clojure.string :as string]))

(defn- directory?
  [path]
  (. (js/LUMO_STAT path) isDirectory))

(defn- file?
  [path]
  (and (. (js/LUMO_STAT path) isFile) (or (string/ends-with? path ".cljs")
                                          (string/ends-with? path ".cljc")
                                          (string/ends-with? path ".clj"))))

(defn- jarfile?
  [path]
  (string/ends-with? path ".jar"))

(defn- filenames
  [path]
  (if (or (identical? "" path) (jarfile? path))
    path
    (let [root (js/LUMO_READDIR path)
          root-files (filter #(file? (str path "/" %)) root)
          sub-dirs (map #(str path "/" %) (filter #(directory? (str path "/" %)) root))
          sub-files (mapcat filenames sub-dirs)]
      (mapcat identity [root-files sub-files]))))

(defn classpath
  "Returns a JS array of strings listing all folders on the classpath"
  []
  (js/LUMO_READ_SOURCES))

(defn classpath-files
  "Returns a list of all usable files on the classpath"
  []
  (mapcat filenames (classpath)))

(defn filenames-in-jar
  "Returns a list of all filenames in a jarfile"
  [jar-file]
  (let [zip (.load (js/LUMO_JSZIP.) (js/LUMO_READFILE jar-file))]
    (filter #(re-find #".*\.clj.*" %) (js/Object.keys zip.files))))

(defn classpath-jarfiles
  "Returns a list of all .jar files on the classpath"
  []
  (filter jarfile? (classpath)))

(defn add-source!
  "Mutates the classpath by adding a source directory"
  [path]
  (js/LUMO_ADD_SOURCES #js [path]))

(defn remove-source!
  "Mutates the classpath by removing a source directory"
  [path]
  (js/LUMO_REMOVE_SOURCE path))
