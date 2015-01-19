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
    "beez" #{1}
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
    "legit" #{2}
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
    "playtime" #{2}
    "remix" #{2}
    "saltines" #{2}
    "soho" #{2}
    "spanglish" #{2}
    "starships" #{2}
    "teardrop" #{2}
    "teardrops" #{2}
    "tha" #{1}
    "thang" #{1}
    "thugz" #{1}
    "uk" #{2}
    "u.s.a" #{3}
    "va" #{1}
    "voom" #{1}
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


(defn haikus [tracks]
  (let [track-info (map (fn [track]
                          {:name track :syllables (syllables track)})
                        tracks)
        fives (combo/combinations (filter #((:syllables %) 5) track-info) 2)
        sevens (combo/combinations (filter #((:syllables %) 7) track-info) 1)]
    (map (fn [[fives sevens]]
           (map :name [(first fives) (first sevens) (second fives)]))
         (combo/cartesian-product fives sevens))))
