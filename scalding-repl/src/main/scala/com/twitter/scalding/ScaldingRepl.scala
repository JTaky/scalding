/*  Copyright 2013 Twitter, inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.twitter.scalding

import java.io.File

import ammonite.repl.{ Repl, Storage }

/**
 * A class providing Scalding specific commands for inclusion in the Scalding REPL.
 */
class ScaldingRepl(storage: Storage, predef: String)
  extends Repl(System.in, System.out, System.err, storage, predef) {

  prompt.update(ScaldingShell.prompt())

  override def printBanner() {
    val fc = Console.YELLOW
    val wc = Console.RED
    def wrapFlames(s: String) = s.replaceAll("[()]+", fc + "$0" + wc)
    printStream.println(fc +
      " (                                           \n" +
      " )\\ )            (   (                       \n" +
      "(()/(         )  )\\  )\\ )  (          (  (   \n" +
      " /(_)) (   ( /( ((_)(()/( )\\   (     )\\))(  \n" +
      "(_))   )\\  )( )) _   ((_)(( )  )\\ ) (( ))\\  \n".replaceAll("_", wc + "_" + fc) + wc +
      wrapFlames("/ __|((_) ((_)_ | |  _| | (_) _(_(( (_()_) \n") +
      wrapFlames("\\__ \\/ _| / _` || |/ _` | | || ' \\))/ _` \\  \n") +
      "|___/\\__| \\__,_||_|\\__,_| |_||_||_| \\__, |  \n" +
      " Ammonite Repl 0.5.7/2.10.5         |___/   ")
  }
}

object ScaldingRepl {
  def apply(storage: Storage, paths: Array[String]): ScaldingRepl = {
    val imports = "import " + Seq(
      "ammonite.ops.Path",
      "com.twitter.scalding._",
      "com.twitter.scalding.ReplImplicits._",
      "com.twitter.scalding.ReplImplicitContext._").mkString(", ")

    // interpret all files named ".scalding_repl" from the current directory up to the root
    val initScripts = findAllUpPath(".scalding_repl")
      .reverse // work down from top level file to more specific ones
      .map(f => "load.exec(Path(\"%s\"))".format(f))

    val classpaths = paths
      .filter(_.startsWith("/"))
      .map("load.cp(Path(\"%s\"))".format(_))

    val predef = (Seq(imports) ++ initScripts ++ classpaths)
      .mkString(System.lineSeparator())

    new ScaldingRepl(storage, predef)
  }

  /**
   * Search for files with the given name in all directories from current directory
   * up to root.
   */
  private def findAllUpPath(filename: String): List[File] =
    Iterator.iterate(System.getProperty("user.dir"))(new File(_).getParent)
      .takeWhile(_ != "/")
      .flatMap(new File(_).listFiles.filter(_.toString.endsWith(filename)))
      .toList
}
