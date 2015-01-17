(ns songku.haiku
  (:require [clojure.math.combinatorics :as combo]
            [com.lemonodor.syllables :as syllables]
            [songku.analysis :as analysis]))


(defn sum [s]
  (reduce + s))


(def syllable-db
  (merge
   (syllables/default-syllables-db)
   {"808" #{3}
    "909" #{3}
    "abc" #{3}
    "aeon" #{2}
    "anodyne" #{3}
    "babby" #{2}
    "barbarella" #{4}
    "bigga" #{2}
    "da" #{1}
    "dayz" #{1}
    "fo" #{1}
    "fretless" #{2}
    "gonna" #{2}
    "heartbreaker" #{3}
    "hovy" #{2}
    "itunes" #{2}
    "iz" #{1}
    "jamz" #{2}
    "jerkin" #{2}
    "jigga" #{2}
    "l.a" #{2}
    "lovesick" #{2}
    "luv" #{1}
    "mammy" #{2}
    "musique" #{2}
    "nigga" #{2}
    "niggas" #{2}
    "niggaz" #{2}
    "neptunes" #{2}
    "outro" #{2}
    "papi" #{2}
    "piranhas" #{2}
    "pissing" #{2}
    "pissings" #{2}
    "remix" #{2}
    "saltines" #{2}
    "soho" #{2}
    "spanglish" #{2}
    "teardrop" #{2}
    "teardrops" #{2}
    "tha" #{1}
    "thang" #{1}
    "thugz" #{1}
    "uk" #{2}
    "u.s.a" #{3}
    "vs" #{2}
    "warz" #{1}
    "wayback" #{2}
    "wayfaring" #{3}}))


(defn syllables [text]
  (set
   (map
    sum
    (apply
     combo/cartesian-product
     (map (partial syllables/count-syllables syllable-db)
          (analysis/tokenize text))))))
