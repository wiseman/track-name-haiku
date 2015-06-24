(ns songku.haiku-test
  (:require [clojure.test :refer :all]
            [songku.haiku :as haiku]))

(deftest haiku-test
  (testing "syllables"
    (is (= #{5} (haiku/syllables "territorial")))
    (is (= #{2} (haiku/syllables "pissings")))
    (is (= #{3 4} (haiku/syllables "literally")))
    (is (= #{10 11} (haiku/syllables "literally territorial pissings")))))
