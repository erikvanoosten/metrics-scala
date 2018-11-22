package nl.grons.metrics4.scala

private object StringUtils {

  /**
    * Normalize dots and return the resulting [[String]].
    *
    * Normalizing dots means collapsing all adjacent occurrences of '.' to a single
    * character and stripping leading and trailing '.'.
    *
    * @param s the [[String]] to normalize
    * @return a normalized [[String]]
    */
  def normalizeDots(s: String): String = {
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

    new String(scratchpad, offset, newPos)
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
    if (isEmpty(text) || isEmpty(searchString) || replacement == null) {
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

  /**
    * Determine if given [[String]] is `null` or has length equal to 0.
    *
    * @param s the [[String]] to check
    * @return a [[Boolean]], true iff `s` is null or has length equal to 0.
    */
  def isEmpty(s: String): Boolean = {
    s == null || s.length == 0
  }

}
