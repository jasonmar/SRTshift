import net.cladophora.srt.{SimpleShiftRequest, ComplexShiftRequest, SrtUtil}

class SrtUtilTest {
  def main {
    val subtitles = SrtUtil.loadSrt("R:\\sub.srt")
    val csr = ComplexShiftRequest(subtitles, "00:00:10,000", "00:00:03,000", "00:10:00,000", "00:08:00,000")
    val ssr = SimpleShiftRequest(subtitles, -556000)
    val newline = "\r\n"
    SrtUtil.saveSubtitles(SrtUtil.applyComplexShift(csr), "R:\\corrected.srt", newline)
    SrtUtil.saveSubtitles(SrtUtil.applySimpleShift(ssr), "R:\\test.srt", newline)
  }
}