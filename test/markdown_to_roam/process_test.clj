(ns markdown-to-roam.process-test
  (:require [clojure.test :refer [deftest testing is]]
            [markdown-to-roam.process :as sut]))

(deftest split-md-heading-empty
  (testing
   "empty coll is returned for empty str"
    (is (empty? (sut/split-at-highest-md-heading "")))))

(deftest split-md-heading-none
  (testing
   "if no heading is found the original string in a collection is returned"
    (is (= ["testString ###Text ##Text #Text"] (sut/split-at-highest-md-heading "testString ###Text ##Text #Text")))))

(deftest split-md-heading-only
  (testing
   "if just a usable heading is there the returned coll is empty"
    (is (= ["# "] (sut/split-at-highest-md-heading "# ")))))

(deftest split-md-heading-just-highest
  (testing
   "only highest and not other headings lead to splitting"
    (is
     (=
      ["Heading1\n TextText \n## Heading11 " "Heading2 \n TextText \n## Heading12\n"]
      (sut/split-at-highest-md-heading "\n# Heading1\n TextText \n## Heading11 \n# Heading2 \n TextText \n## Heading12\n")))))
