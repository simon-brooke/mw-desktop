(defproject mw-desktop "0.3.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[cljfx "1.7.23"]
                 [com.novemberain/pantomime "2.11.0"]
                 [com.taoensso/timbre "6.2.2"]
                 [de.codecentric.centerdevice/javafxsvg "1.3.0"] ;; used by markdown-editor-example
                 [markdown-clj "1.11.4"]
                 [mw-engine "0.3.0-SNAPSHOT"]
                 [mw-parser "0.3.0-SNAPSHOT"]
                 [net.sourceforge.htmlcleaner/htmlcleaner "2.29"]
                 [org.clojure/clojure "1.10.3"]
                 [org.clojure/core.cache "1.0.225"]
                 [org.clojure/tools.cli "1.0.214"]
                 [org.commonmark/commonmark "0.21.0"]
                 [seesaw "1.5.0"]]
  :main ^:skip-aot mw-desktop.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
