(ns rendezvous.core-test
  (:require [clojure.test :refer [deftest is testing]]
            [rendezvous.core :as core]))

(deftest parse-args-interface
  (testing "-i flag sets the interface"
    (is (= "eth0" (:interface (core/parse-args ["-i" "eth0"]))))))

(deftest parse-args-missing-interface
  (testing "missing -i flag throws"
    (is (thrown? Exception (core/parse-args [])))))

(deftest parse-args-missing-value
  (testing "-i with no value throws"
    (is (thrown? Exception (core/parse-args ["-i"])))))
