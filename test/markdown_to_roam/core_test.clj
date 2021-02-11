(ns markdown-to-roam.core-test
  (:require [clojure.test :refer [deftest testing is]]
            [markdown-to-roam.core :as sut]))

(deftest trim-front-empty
  (testing
   "empty string is returned"
    (is (= "" (sut/trim-newline-front "")))))

(deftest trim-front-only
  (testing
   "all newlines and returns are removed if only they exist"
    (is (= "" (sut/trim-newline-front "\n\n\r\n\r\r\n")))))

(deftest trim-front-none
  (testing
   "original string without newlines and returns is returned"
    (is (= "testString" (sut/trim-newline-front "testString")))))

(deftest trim-front-couple
  (testing
   "a couple mixed newlines and returns get removed from the front"
    (is (= "testString" (sut/trim-newline-front "\n\n\r\n\r\r\ntestString")))))

(deftest trim-front-single-return
  (testing
   "a single return gets removed from the front"
    (is (= "testString" (sut/trim-newline-front "\rtestString")))))

(deftest trim-front-single-newline
  (testing
   "a single newline gets removed from the front"
    (is (= "testString" (sut/trim-newline-front "\ntestString")))))