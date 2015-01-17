(ns songku.analysis
  (:require [clojure.string :as string])
  (:import (java.io StringReader)
           (org.apache.lucene.analysis TokenStream Tokenizer)
           (org.apache.lucene.analysis.core LowerCaseFilter WhitespaceTokenizer)
           (org.apache.lucene.analysis.miscellaneous ASCIIFoldingFilter)
           (org.apache.lucene.analysis.standard StandardFilter StandardTokenizer)
           (org.apache.lucene.analysis.synonym SynonymFilter SynonymMap$Builder)
           (org.apache.lucene.analysis.tokenattributes CharTermAttribute
                                                       OffsetAttribute)
           (org.apache.lucene.util CharsRef Version)))


(defn make-synonym-map [rules]
  (let [^SynonymMap$Builder builder (SynonymMap$Builder. true)]
    (doseq [[^String from ^String to] rules]
      (.add builder (CharsRef. from) (CharsRef. to) false))
    (.build builder)))


(def synonyms
  { "&" "and"})


(defn make-first-stage-tokenizer []
  (let [^Tokenizer tokenizer (WhitespaceTokenizer.
                              Version/LUCENE_48
                              (StringReader. ""))
        lc-filt (LowerCaseFilter. Version/LUCENE_48 tokenizer)
        syn-filt (SynonymFilter. lc-filt (make-synonym-map synonyms) true)
        ascii-filt (ASCIIFoldingFilter. syn-filt false)]
    {:tokenizer tokenizer
     :token-stream ascii-filt}))


(defn make-second-stage-tokenizer []
  (let [^Tokenizer tokenizer (StandardTokenizer.
                              Version/LUCENE_48
                              (StringReader. ""))
        standard-filt (StandardFilter. Version/LUCENE_48 tokenizer)]
    {:tokenizer tokenizer
     :token-stream standard-filt}))


(defn make-tokenizer []
  [(make-first-stage-tokenizer) (make-second-stage-tokenizer)])



(defn tokenize-stage [stage text]
  (.setReader ^Tokenizer (:tokenizer stage) (StringReader. text))
  (let [^TokenStream token-stream (:token-stream stage)
        ^OffsetAttribute offset-attr (.addAttribute
                                      token-stream OffsetAttribute)
        ^CharTermAttribute char-term-attr (.addAttribute
                                           token-stream CharTermAttribute)]
    (.reset token-stream)
    (try
      (loop [more-tokens (.incrementToken token-stream)
             token-accum []]
        (if more-tokens
          (let [start-offset (.startOffset offset-attr)
                end-offset (.endOffset offset-attr)
                term (str char-term-attr)]
            (recur
             (.incrementToken token-stream)
             (cons term token-accum)))
          (reverse token-accum)))
      (finally
        (.close token-stream)))))


(defn tokenize
  ([text]
   (tokenize (make-tokenizer) text))
  ([tokenizer text]
   (->> text
        (tokenize-stage (first tokenizer))
        (string/join " ")
        (tokenize-stage (second tokenizer)))))
