(ns markdown-to-roam.core-test
  (:require [clojure.test :refer [deftest testing is]]
            [markdown-to-roam.core :as sut]))

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

(deftest transform-test
  (testing
   ""
    (is (=
         (sut/transform-md "test \n# heading1\n text1 \n## heading11\n text11 \n# heading2\n text2\n## heading21\n text21 \n# heading3\n text")
         "* test\n* heading1\n\t* text1\n\t* heading11\n\t\t* text11\n* heading2\n\t* text2\n\t* heading21\n\t\t* text21\n* heading3\n\t* text\n"))))


; TODO: make file-based integration test so we can see the result correctly formatted