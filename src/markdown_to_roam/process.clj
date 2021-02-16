(ns markdown-to-roam.process
  (:require [clojure.string :as str]))

; make tree from flat file

(defn remove-blanks [coll] (remove str/blank? coll))

(def highest-heading-start-re #"\n# ")
(defn split-at-highest-heading [s] (str/split s highest-heading-start-re))

(def lower-heading-re #"(\n#)(#+ )")
(defn remove-lower-heading-level [s] (str/replace s lower-heading-re "\n$2"))
(def remove-lower-level-map #(map remove-lower-heading-level %))

(defn separate-heading-and-content [s] (str/split s #"\n" 2))
(def separate-heading-map #(map separate-heading-and-content %))

(defn split-one-heading-level [s]
  (-> s
      ; assumes headings exist
      ; TODO: option to keep heading needs to be included here 
      split-at-highest-heading
      remove-lower-level-map
      remove-blanks
      separate-heading-map))

(defn trimr-map [coll] (map str/trimr coll))

(defn split-at-empty-line [s] (str/split s #"\n\n"))

(defn split-content
  [s]
  (-> s
      split-at-empty-line
      ; we use trimr because we want to preserve \t etc. in the beginning
      trimr-map
      remove-blanks))

(def block-start "* ")
(defn non-simple-text-indexes
  [coll]
  (filter #(not (nil? %))
          (map-indexed
           #(if (str/starts-with?
                 ; need to trim because it could start with \t etc.
                 (str/trim %2)
                 block-start)
              %1
              nil)
           coll)))

(defn to-buckets [v]
  (loop [index 0
         res []
         temp []
         last nil
         len (count v)]
    (if (= len index)
      (conj res temp)
      (let [cur (v index)]
        (if (and
             last
             (= (inc last) cur))
          (recur (inc index)
                 res
                 (conj temp cur)
                 cur
                 len)
          (recur (inc index)
                 (if (empty? temp)
                   res
                   (conj res temp))
                 (vector cur)
                 cur
                 len))))))

(defn map-buckets-to-indexes
  [start end buckets]
  ; end is exclusive
  ; buckets need to be ordered
  (loop [index start
         res []
         b buckets
         cb (first buckets)]
    (if (= index end)
      res
      (if (some #(= index %) cb)
        (recur (inc (last cb))
               (vec (concat res cb))
               (rest b)
               (first (rest b)))
        (if (=
             (inc index)
             (first cb))
          (recur
           (inc (last cb))
           (conj res (vector
                      index
                      (if (= 1 (count cb))
                        (cb 0)
                        cb)))
           (rest b)
           (first (rest b)))
          (recur
           (inc index)
           (conj res index)
           b
           cb))))))

(defn fit-vals-to-bucket-hierarchy
  [coll bucket-hierarchy]
  (map
   #(if (integer? %)
      ((vec coll) %)
      (fit-vals-to-bucket-hierarchy coll %))
   bucket-hierarchy))

(defn content-block-to-simple-hierarchy
  [coll]
  (let [indexes (non-simple-text-indexes coll)
        buckets (to-buckets indexes)
        hierarchy (map-buckets-to-indexes 0 (count coll) buckets)]
    (fit-vals-to-bucket-hierarchy coll hierarchy)))

(defn chars-before-block [s] (str/index-of s "* "))

(defn more-chars-before-block-indexes [coll chars]
  (filter #(not (nil? %))
          (map-indexed
           #(if
             (> chars (chars-before-block %2))
              %1
              nil)
           coll)))

(defn has-child-nodes
  [in]
  (and (coll? in)
       (coll? (in 1))))

(defn md-hierarchy-to-vec-hierarchy
  ([coll]
   (let [indexes (more-chars-before-block-indexes coll (coll 0))
         buckets (to-buckets indexes)
         hierarchy (map-buckets-to-indexes 0 (count coll) buckets)
         v (fit-vals-to-bucket-hierarchy coll hierarchy)]
     (map #(if
            (has-child-nodes %)
             (vector  (% 0)  (md-hierarchy-to-vec-hierarchy (% 1)))
             %)
          v))))

(defn simple-hierarchy-to-vec-hierarchy
  [coll]
  (map #(if
         (has-child-nodes %)
          (vector  (% 0)  (md-hierarchy-to-vec-hierarchy (% 1)))
          %)
       coll))


(defn content-block-to-hierarchy
  [s]
  (-> s
      str/split-lines
      remove-blanks
      content-block-to-simple-hierarchy
      simple-hierarchy-to-vec-hierarchy))

; TODO: tabbing hierarchy, but what if someone used empty space?

(defn process-md
  [s]
  (if (str/includes? s "\n# ")
    (map
     #(if (str/blank? (% 1))
        (process-md (% 0))
        (vector (% 0) (process-md (% 1))))
     (split-one-heading-level s))
    (if (str/includes? s "\n\n")
      (let [v (split-content s)]
        (if (empty? v)
          ""
          (map process-md v)))
      ; now we are in a single block of text
      ; possibly with already existing * or similar
      ; TODO: research all list characters of markdown
      (if (str/includes? s "\n")
        (content-block-to-hierarchy s)
        (str/trim s)))))
