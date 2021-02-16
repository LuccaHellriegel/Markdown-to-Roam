(ns markdown-to-roam.core
  (:require [markdown-to-roam.pre-process :as pre]
            [markdown-to-roam.process :as p]
            [markdown-to-roam.post-process :as post]))

(defn transform-md
  [s]
  (->
   s
   pre/pre-process-md
   p/process-md
   post/post-process-md))

(defn transform-md-save
  [s]
  (spit "test.txt"
        (transform-md s)))

; TODO: Roam supports headings - 
; maybe in the future make it configurable 
; if headings are to be removed or not?
; need tree anyways 
; but the "lowering" approach would be in the way?
