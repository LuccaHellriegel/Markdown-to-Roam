(ns markdown-to-roam.core
  (:require [clojure.string :as str]))

; TODO: rename obsidian journal file names to Roam Daily names?

; replace obsidian specific syntax

; make tree from flat file

(defn prepare-first-heading
  "make sure first heading is ready for regex"
  [s]
  (if (str/starts-with? s "# ")
    (str "\n" s)
    s))

(defn prepare-last-heading
  "file could end with heading that doesnt end with \\n"
  [s]
  (str s "\n"))

(defn remove-empty-space-before-newline
  "make sure that there is no empty space after the heading text"
  [s]
  (str/replace s #"( +)\n" "\n"))

; TODO: remove all extra empty space? like "  +" -> " "
; are there any hierarchy use cases for the empty space?

(defn remove-extra-empty-space-after-tag
  [s]
  (str/replace s #"# ( +)" "# "))

(defn remove-empty-space-before-heading-line
  "make sure there is never empty space between \\n and #"
  [s]
  (str/replace s #"(\n)( +)#" "\n#"))

(defn remove-empty-space
  [s]
  (-> s
      remove-empty-space-before-newline
      remove-extra-empty-space-after-tag
      remove-extra-empty-space-after-tag))

(defn add-newline-before-headings
  "need an empty line between hierarchy levels"
  [s]
  (str/replace s #"(\w+| +)(\n#+ )" "$1\n$2"))

; TODO: never use trim before the end conversion -> Java thinks newlines are whitespace

(defn pre-process-headings
  [s]
  (-> s
      prepare-first-heading
      prepare-last-heading
      add-newline-before-headings
      remove-empty-space))

; TODO: research where \r is used an how this might interact here

(defn remove-extra-newlines
  [s]
  ; two \n is an empty space
  ; more than that is not necessary to preserve hierarchy
  (str/replace s #"\n\n\n+" "\n\n"))

(defn pre-process-md
  [s]
  (-> s
      remove-extra-newlines
      pre-process-headings))

(defn empty-str?
  [s]
  (= (str/trim s) ""))

(defn remove-empty-strs
  [coll]
  ; we dont trim for real because it might destroy further split-logic
  (remove empty-str? coll))

(def highest-heading-start-re #"\n# ")

(defn split-at-highest-md-heading
  [s]
  (remove-empty-strs
   (str/split s highest-heading-start-re)))

; after splitting at higher heading,
; we remove one level of the lower ones
; so we have a higher one again
(def lower-heading-re #"(\n#)(#+ )")

(defn remove-lower-heading-level
  [s]
  (str/replace s lower-heading-re "\n$2"))

(defn splitted-heading-str-to-tree-vec
  [s]
  (str/split s #"\n" 2))

; TODO: I assume no one starts with higher/smaller headings, 
; might need to pre-process that too

(defn process-md
  [s]
  (if (str/includes? s "\n# ")
    (let [heading-content (mapv
                           remove-lower-heading-level
                           (split-at-highest-md-heading s))
          heading-content-vec (remove empty?
                                      (mapv splitted-heading-str-to-tree-vec heading-content))]
      ;TODO: case if the s only consists of the hierarchy elemtns?
      (if (empty? heading-content-vec)
        ""
        (mapv
         #(if (empty-str? (% 1))
            (process-md (% 0))
            (vector (% 0) (process-md (% 1))))
         heading-content-vec)))
    (str/trim s)))

(defn repeat-tabs
  [n]
  (apply str (repeat n "\t")))

(defn to-block
  ([s]
   (to-block s 0))
  ([s tabs] (str (repeat-tabs tabs) "* " s "\n")))

(defn post-process-md
  ([v]
   (post-process-md v 0))
  ([v level]
   (if (= level 0)
     (apply str (mapv #(if (string? %)
                         (to-block %)
                         (post-process-md % 1)) v))
     (let [first (first v)
           second (second v)]
       (str
        (to-block first (dec level))
        (if (string? second)
          (to-block second level)
          (apply str (mapv #(if (string? %)
                              (to-block % level)
                              (post-process-md % (inc level))) second))))))))

(defn transform-md
  [s]
  (->
   s
   pre-process-md
   process-md;))
   post-process-md))

(defn transfor-md-save
  [s]
  (spit "test.txt"
        (transform-md s)))

; TODO: Roam supports headings - 
; maybe in the future make it configurable 
; if headings are to be removed or not?
; need tree anyways 

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

