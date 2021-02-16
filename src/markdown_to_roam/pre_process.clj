(ns markdown-to-roam.pre-process
  (:require [clojure.string :as str]))

; TODO: I assume no one starts with higher/smaller headings, 
; might need to pre-process that too

; TODO: rename obsidian journal file names to Roam Daily names?

; TODO: replace obsidian specific syntax

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