(ns markdown-to-roam.core
  (:require [clojure.string :as str]))

; replace obsidian specific syntax

; make tree from flat file
(defn trim-newline-front
  [s]
  (loop [index 0 length (.length s)]
    (if (= index length)
      ""
      (let [ch (.charAt s index)]
        (if (or (= ch \newline) (= ch \return))
          (recur (inc index) length)
          (.. s (subSequence index length) toString))))))

(defn trim-newline-front-and-back
  [s]
  (-> s trim-newline-front str/trim-newline))

; str/index-of

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

