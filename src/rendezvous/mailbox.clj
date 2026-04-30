(ns rendezvous.mailbox)

(def ttl-ms 90000)

(defn new-store []
  ;; map of mailbox-code -> list of {:id :ip :port :added-at}
  {})

(defn- now [] (System/currentTimeMillis))

(defn add-entry
  ([store code entry]
   (add-entry store code entry (now)))
  ([store code entry timestamp]
   (let [stamped (assoc entry :added-at timestamp)
         entries (get store code [])
         pruned  (remove #(= (:id %) (:id entry)) entries)]
     (assoc store code (conj (vec pruned) stamped)))))

(defn get-entries [store code]
  (let [cutoff  (- (now) ttl-ms)
        entries (get store code [])]
    (->> entries
         (remove #(< (:added-at %) cutoff))
         (map #(dissoc % :added-at))
         vec)))

(defn purge [store]
  (let [cutoff (- (now) ttl-ms)]
    (->> store
         (reduce-kv (fn [m code entries]
                      (let [live (vec (remove #(< (:added-at %) cutoff) entries))]
                        (if (empty? live) m (assoc m code live))))
                    {}))))
