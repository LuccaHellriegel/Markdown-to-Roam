(ns markdown-to-roam.post-process
  (:require [clojure.string :as str]))

(defn repeat-tabs
  [n]
  (apply str (repeat n "\t")))

(defn to-block
  ([s]
   (to-block s 0))
  ([s tabs] (str (repeat-tabs tabs) "* " s "\n")))

; TODO: is empty str possible? yes, find way to remove it before it goes into the vector
; remove the str/blank? checks, also sometimes empty? -> maybe check before that iteration?
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