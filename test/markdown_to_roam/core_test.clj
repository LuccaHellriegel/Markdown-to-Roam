(ns markdown-to-roam.core-test
  (:require [clojure.test :refer [deftest testing is]]
            [markdown-to-roam.core :as sut]))

(deftest transform-test
  (testing
   ""
    (is (=
         (sut/transform-md "test \n# heading1\n text1 \n## heading11\n text11 \n# heading2\n text2\n## heading21\n text21 \n# heading3\n text")
         "* test\n* heading1\n\t* text1\n\t* heading11\n\t\t* text11\n* heading2\n\t* text2\n\t* heading21\n\t\t* text21\n* heading3\n\t* text\n"))))


; TODO: make file-based integration test so we can see the result correctly formatted