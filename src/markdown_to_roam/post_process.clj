(ns markdown-to-roam.post-process
  (:require [clojure.string :as str]))

(defn repeat-tabs
  [n]
  (apply str (repeat n "\t")))

(defn to-block
  ([s]
   (to-block s 0))
  ([s tabs] (str (repeat-tabs tabs) "* " (str/trim s) "\n")))

; TODO: dont add * if already present
; do other list markers too

(defn post-process-md
  ([v]
   (post-process-md v 0))
  ([v level]
   (if (= level 0)
     (apply str (mapv #(if (string? %)
                         (if (str/blank? %)
                           ""
                           (to-block %))
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