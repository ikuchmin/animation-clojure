(require '[cljs.build.api :as b])

(b/watch "src"
  {:main 'core.core
   :output-to "out/core.js"
   :output-dir "out"})
