(ns rendezvous.mailbox-test
  (:require [clojure.test :refer [deftest is testing]]
            [rendezvous.mailbox :as mailbox]))

(deftest empty-mailbox
  (testing "a mailbox with no entries returns an empty list"
    (let [store (mailbox/new-store)]
      (is (= [] (mailbox/get-entries store "abc123"))))))

(deftest add-entry
  (testing "posting to a mailbox adds the entry"
    (let [store  (mailbox/new-store)
          entry  {:id "peer1" :ip "1.2.3.4" :port 4567}
          store' (mailbox/add-entry store "abc123" entry)]
      (is (= [entry] (mailbox/get-entries store' "abc123"))))))

(deftest dedup-by-id
  (testing "posting the same id twice replaces the old entry"
    (let [store   (mailbox/new-store)
          entry1  {:id "peer1" :ip "1.2.3.4" :port 4567}
          entry2  {:id "peer1" :ip "9.9.9.9" :port 9999}
          store'  (-> store
                      (mailbox/add-entry "abc123" entry1)
                      (mailbox/add-entry "abc123" entry2))]
      (is (= [entry2] (mailbox/get-entries store' "abc123"))))))

(deftest multiple-entries
  (testing "different ids coexist in the same mailbox"
    (let [store  (mailbox/new-store)
          entry1 {:id "peer1" :ip "1.2.3.4" :port 1111}
          entry2 {:id "peer2" :ip "5.6.7.8" :port 2222}
          store' (-> store
                     (mailbox/add-entry "abc123" entry1)
                     (mailbox/add-entry "abc123" entry2))]
      (is (= #{entry1 entry2}
             (set (mailbox/get-entries store' "abc123")))))))

(deftest isolated-mailboxes
  (testing "entries in different mailboxes do not bleed into each other"
    (let [store  (mailbox/new-store)
          entry  {:id "peer1" :ip "1.2.3.4" :port 1111}
          store' (mailbox/add-entry store "abc123" entry)]
      (is (= [] (mailbox/get-entries store' "zzz999"))))))

(deftest expiry
  (testing "entries older than 90 seconds are not returned"
    (let [store     (mailbox/new-store)
          old-time  (- (System/currentTimeMillis) 91000)
          entry     {:id "peer1" :ip "1.2.3.4" :port 1111}
          store'    (mailbox/add-entry store "abc123" entry old-time)]
      (is (= [] (mailbox/get-entries store' "abc123"))))))

(deftest purge-removes-expired-from-storage
  (testing "purge removes expired entries from the store itself"
    (let [store    (mailbox/new-store)
          old-time (- (System/currentTimeMillis) 91000)
          entry    {:id "peer1" :ip "1.2.3.4" :port 1111}
          store'   (mailbox/add-entry store "abc123" entry old-time)
          purged   (mailbox/purge store')]
      (is (= {} purged)))))

(deftest purge-keeps-live-entries
  (testing "purge leaves entries that have not yet expired"
    (let [store   (mailbox/new-store)
          entry   {:id "peer1" :ip "1.2.3.4" :port 1111}
          store'  (mailbox/add-entry store "abc123" entry)
          purged  (mailbox/purge store')]
      (is (= 1 (count (mailbox/get-entries purged "abc123")))))))
