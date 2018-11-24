/*
 * Copyright (c) 2018 Erik van Oosten
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.grons.metrics4.scala

private object StringUtils {

  /**
    * Collapse adjacent dots, strip leading and trailing dot and return the resulting [[String]].
    *
    * @param s the [[String]] to normalize
    * @return a normalized [[String]]
    */
  def collapseDots(s: String): String = {
    val scratchpad = s.toCharArray

    var pos, newPos = 0
    var currentChar = ' '
    var inDots = false

    while (pos < scratchpad.length) {
      currentChar = scratchpad(pos)
      if (currentChar != '.') {
        scratchpad(newPos) = currentChar
        inDots = false
        newPos += 1
      } else if (!inDots) {
        scratchpad(newPos) = '.'
        inDots = true
        newPos += 1
      }
      pos += 1
    }

    if (scratchpad(newPos - 1) == '.') {
      newPos -= 1
    }
    val offset = if (scratchpad(0) == '.') 1 else 0

    new String(scratchpad, offset, newPos - offset)
  }

  /**
    * Replace all occurrences of `searchString` in `text` with `replacement`.
    *
    * @param text the [[String]] to perform the replacements in
    * @param searchString the [[String]] that should be replaced
    * @param replacement the [[String]] that should be put in place of `searchString`
    * @return a [[String]] with all occurrences of `searchString` replaced with `replacement`
    */
  def replace(text: String, searchString: String, replacement: String): String =  {
    if (text.isEmpty || searchString.isEmpty) {
      return text
    }

    var start = 0
    var end = text.indexOf(searchString, start)
    if (end == -1) {
      return text
    }

    val replLength = searchString.length
    val increase = {
      val diff = replacement.length - replLength
      math.max(diff, 0) * 16
    }

    val sb = new StringBuilder(text.length + increase)
    while (end != -1)  {
      sb.append(text.substring(start, end)).append(replacement)
      start = end + replLength
      end = text.indexOf(searchString, start)
    }
    sb.append(text.substring(start))
    sb.toString
  }

}
