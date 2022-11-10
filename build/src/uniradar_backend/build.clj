(ns uniradar-backend.build
  (:require
   [clojure.string :as string]
   [clojure.tools.build.api :as b]))

(def lib 'io.github.tiagodalloca/uniradar-backend)
(def version "0.1.0")
(def class-dir "target/classes")
(def basis (b/create-basis {:project "deps.edn"}))
(def jar-file (format "target/%s-%s.jar" (name lib) version))
(def copy-srcs ["src" "resources"])
(def uber-file (format "target/%s-%s-standalone.jar" (name lib) version))

(defn clean
  [params]
  (b/delete {:path "target"})
  params)

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn get-classpath
  [_]
  (->> (:classpath basis)
       (map (fn [[k _]] k))
       (string/join ":")
       println))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn jar
  [_]
  (b/write-pom {:class-dir class-dir
                :lib lib
                :version version
                :basis basis
                :src-dirs ["src"]})
  (b/copy-dir {:src-dirs copy-srcs
               :target-dir class-dir})
  (b/compile-clj {:basis basis
                  :src-dirs ["src"]
                  :class-dir class-dir})
  (b/jar {:class-dir class-dir
          :jar-file jar-file
          :main (str (name lib) ".main")})
  (get-classpath nil))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn uber
  [_]
  (clean nil)
  (b/copy-dir {:src-dirs copy-srcs
               :target-dir class-dir})
  (b/compile-clj {:basis basis
                  :src-dirs ["src"]
                  :class-dir class-dir})
  (b/uber {:class-dir class-dir
           :uber-file uber-file
           :basis basis
           :main (str (name lib) ".main")}))
