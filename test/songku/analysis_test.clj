(ns songku.analysis-test
  (:require [clojure.test :refer :all]
            [songku.analysis :as analysis]))

(deftest analyze-test
  (testing "tokenize"
    (let [tokenizer (analysis/make-tokenizer)]
      (is (= ["hello" "beyonce"]
             (analysis/tokenize tokenizer "hello Beyoncé")))
      (is (= ["hello" "5beyonce"]
             (analysis/tokenize tokenizer "hello 5Beyoncé")))
      (is (= []
             (analysis/tokenize tokenizer ""))))
    (is (= ["hello" "beyonce"]
           (analysis/tokenize "hello Beyoncé")))
    (is (= ["hello" "5beyonce"]
           (analysis/tokenize "hello 5Beyoncé")))
    (is (= ["hello" "and" "what" "up" "5beyonce"]
           (analysis/tokenize "hello, & what-up? 5Beyoncé?")))
    (is (= ["beyonce's"]
           (analysis/tokenize "Beyoncé's")))
    (is (= ["jamming" "me"]
           (analysis/tokenize "Jammin' me")))
    (is (= ["u.s.a"]
           (analysis/tokenize "U.S.A.")))
    (is (= []
           (analysis/tokenize "")))))
