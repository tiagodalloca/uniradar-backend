(ns uniradar-backend.http.tables-handler)

(defn insert
  [db* where id what]
  (swap! db* assoc-in [where id] what)
  what)

(defn find-id
  [db* where id]
  (assoc (get-in @db* [where id]) :id id))

(defn find-all
  [db* where]
  (get @db* where))

(defn update-id
  [db* where id f & args]
  (swap! db* update-in [where id] #(apply f % args))
  (find-id db* where id))

(defn delete-id
  [db* where id]
  (swap! db* assoc-in [where id] nil)
  nil)

(defn make-table-handlers
  [name {db* :db/instance}]
  [[(str "/" name)
    {:get {:handler (fn table-handler-get
                      [{query-params  :query-params}]
                      {:body (doto (into []
                                         (comp (filter
                                                (fn find-all-filter [[_ m]]
                                                  (reduce (fn find-all-filter-reducer
                                                            [acc [attr val]]
                                                            (if-let [acc (and acc (= (get m attr) val))]
                                                              acc
                                                              (reduced false)))
                                                          true
                                                          query-params)))
                                               (map (fn [[id m]] (assoc m :id id))))
                                         (find-all db* name))
                               prn)})}}]

   [(str "/" name "/:id")
    {:post {:handler (fn table-handler-post-id
                       [{body-params :body-params
                         {id :id} :path-params}]
                       {:body (doto (insert db* name id body-params) prn)})}
     :get {:handler (fn table-handler-get-id
                      [{{id :id} :path-params}]
                      {:body (do #_(prn request)
                                 (find-id db* name id))})}
     :patch {:handler (fn table-handler-patch-id
                        [{body-params :body-params
                          {id :id} :path-params}]
                        {:body (update-id db* name id merge body-params)})}
     :delete {:handler (fn table-handler-delete-id
                         [{{id :id} :path-params}]
                         {:body (delete-id db* name id)})}}]])
