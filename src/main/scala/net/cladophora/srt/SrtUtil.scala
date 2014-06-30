package net.cladophora.srt

object SrtUtil {
  private def parseTs (ts:String): org.joda.time.DateTime = {
    val dtf = org.joda.time.format.DateTimeFormat.forPattern("HH:mm:ss,SSS")
    org.joda.time.DateTime.parse(ts, dtf)
  }

  private def splitTs (ts: String): SrtTimeStamps = {
    val times = ts.split(" --> ")
    val start = parseTs(times(0))
    val end = parseTs(times(1))
    SrtTimeStamps(start, end)
  }

  private def parseSubtitle (rawSubtitle: String): Subtitle = {
    val lines = rawSubtitle.split("""\r?\n""").toList
    Subtitle(splitTs(lines(1)), lines.takeRight(lines.size - 2))
  }

  private def addMilliSeconds (s: Subtitle, ms: Integer): Subtitle = {
    val newTs = SrtTimeStamps(s.time.start.plusMillis(ms), s.time.end.plusMillis(ms))
    Subtitle(newTs, s.subs)
  }

  private def addSeconds (s: Subtitle, seconds: Integer): Subtitle = {
    addMilliSeconds(s, seconds * 1000)
  }

  private def addMinutes (s: Subtitle, minutes: Integer): Subtitle = {
    addMilliSeconds(s, minutes * 60 * 1000)
  }

  private def printTs (t: SrtTimeStamps): String = {
    val dtf = org.joda.time.format.DateTimeFormat.forPattern("HH:mm:ss,SSS")
    t.start.toString(dtf) + " --> " + t.end.toString(dtf)
  }

  private def printSubtitle(s:Subtitle, i: Integer, writer: java.io.PrintWriter, newline: String) = {
    writer.print((i+1).toString + newline)
    writer.print(printTs(s.time) + newline)
    s.subs.map(x => writer.print(x + newline))
    writer.print(newline)
  }

  def saveSubtitles(s:Subtitles, path: String, newline: String): Unit = {
    val writer = new java.io.PrintWriter(path,"UTF-8")
    try {
      s.subs.zipWithIndex.foreach{ x => printSubtitle(x._1, x._2, writer, newline) }
    } finally {
      writer.close()
      println("Wrote subs to " + path)
    }
  }

  private def shiftMs(t: org.joda.time.DateTime, f: (Int) => Int): org.joda.time.DateTime = {
    val x = t.getMillisOfDay
    t.plusMillis(f(x) - x)
  }

  private def simpleShift (s: Subtitles, ms: Int) : Subtitles = {
    Subtitles(s.subs.map(s => addMilliSeconds(s, ms)))
  }

  private def shiftSub(s: Subtitle, f: (Int) => Int): Subtitle = {
    Subtitle(SrtTimeStamps(shiftMs(s.time.start,f), shiftMs(s.time.end,f)), s.subs)
  }

  private def shiftSubs(s: Subtitles, f: (Int) => Int): Subtitles = {
    Subtitles(s.subs.map(s => shiftSub(s, f)).filter(_.time.start.getYear >= 1970))
  }

  def applySimpleShift(r: SimpleShiftRequest): Subtitles = {
    simpleShift(r.subs, r.ms)
  }

  def applyComplexShift (r: ComplexShiftRequest) = {
    def getMs (ts: String): Double = { parseTs(ts).getMillisOfDay.toDouble }
    val f = getTranslateFunction(getMs(r.t0), getMs(r.t0prime), getMs(r.t1), getMs(r.t1prime))
    shiftSubs(r.subs,f)
  }

  private def getTranslateFunction (x0: Double, y0: Double, x1: Double, y1: Double) = {
    val m = (y1 - y0) / (x1 - x0)
    val b = y0 - (m * x0)
    (x: Int) => Math.floor((m * x) + b).toInt
  }

  def loadSrt(path: String): Subtitles = {
    Subtitles(scala.io.Source.fromFile(path,"utf-8").mkString.split("""\r?\n\r?\n""").toList.map(parseSubtitle))
  }
}
