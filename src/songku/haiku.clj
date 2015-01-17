(ns songku.haiku
  (:require [clojure.math.combinatorics :as combo]
            [com.lemonodor.syllables :as syllables]
            [songku.analysis :as analysis]))


(defn sum [s]
  (reduce + s))


(defn syllables [text]
  (set
   (map
    sum
    (apply
     combo/cartesian-product
     (map syllables/count-syllables
          (analysis/tokenize text))))))
