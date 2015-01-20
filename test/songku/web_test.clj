(ns songku.web-test
  (:require [clojure.test :refer :all]
            [songku.web :as web]))

(deftest web-test
  (testing "parse-log"
    (is (= 5 (web/parse-long "5"))))
  (testing "fix-html-entities"
    (is (= "&" (web/fix-html-entities "&amp;")))
    (is (= "M&M" (web/fix-html-entities "M&amp;M")))))
