(ns markdown-to-roam.process
  (:require [clojure.string :as str]))

; make tree from flat file

(def highest-heading-start-re #"\n# ")

(defn split-at-highest-md-heading
  [s]
  (remove str/blank?
          (str/split s highest-heading-start-re)))


(def lower-heading-re #"(\n#)(#+ )")

(defn remove-lower-heading-level
  "after splitting at higher heading,
   we remove one level of the lower ones
   so we have a higher one again"
  [s]
  (str/replace s lower-heading-re "\n$2"))

(defn splitted-heading-str-to-tree-vec
  [s]
  (str/split s #"\n" 2))

(defn index-of-starts-with-block
  ([v] (index-of-starts-with-block v 0))
  ([v start]
   (loop [index start
          len (count v)]
     (if (> index len)
       nil
       (if (str/starts-with? (v index) "* ")
         index
         (recur (inc index) len))))))

(def block-start "* ")

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
  "buckets need to be ordered"
  [start end buckets]
  (loop [index start
         res []
         b buckets
         cb (first buckets)]
    (if (= index end)
      res
      (if (some #(= index %) cb)
        (recur (inc (last cb))
               (vec (conj res cb))
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

(defn process-md
  [s]
  (if (str/includes? s "\n# ")
    (let [heading-content (mapv
                           remove-lower-heading-level
                           (split-at-highest-md-heading s))
          heading-content-vec (remove empty?
                                      (mapv splitted-heading-str-to-tree-vec heading-content))]
      (if (empty? heading-content-vec)
        ""
        (mapv
         #(if (str/blank? (% 1))
            (process-md (% 0))
            ; TODO: tail-recursion via vector-arg to func? 
            (vector (% 0) (process-md (% 1))))
         heading-content-vec)))
; TODO: dont really need to do it this way -> just call the empty-space func in the map
    (if (str/includes? s "\n\n")
      (let [v (remove str/blank?
                      (mapv str/trim
                            (str/split s #"\n\n")))]
        (if (empty? v)
          ""
          (mapv process-md v)))
      ; now we are in a single block of text
      ; possibly with already existing * or similar
      ; TODO: research all list characters of markdown
      (if (str/includes? s "\n")
        (let [v
              ; TODO: be more smart about LazySeq, 
              ; maybe it can be stacked so that I only doall at the end?
              (vec (remove str/blank?
                           (str/split
                            (str/trim s) #"\n")))]
          (if (empty? v)
            ""
            (if (= (count v) 1)
              (str/trim (v 0))
              (let [block-indexes (filterv #(not (nil? %))
                                           (map-indexed
                                            #(if (str/starts-with?
                                                  ; need to trim because it could start with \t
                                                  (str/trim %2)
                                                  block-start)
                                               %1
                                               nil)
                                            v))]
                (if (empty? block-indexes)
                  ; in this case no blocks
                  v
                  ; TODO use map-buckets-to-indexes
                  ; next step would be to 
                  ;                   make the already indented things stay that way
                  ;                                     what about numbered lists?  
                  (str/trim s))))))
        (str/trim s)))))
