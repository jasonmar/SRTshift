Scala Subtitle Utility
----------------------

Published June 30, 2014



Introduction
------------

This is a Scala utility that provides methods to load and an SRT file from disk, apply time shifting, and save the modified SRT back to disk.


### Load an SRT file ###

val subtitles = SrtUtil.loadSrt("R:\\sub.srt")


### ComplexShiftRequest ###

Generates a translation function based on two correction points and shifts all subtitles using this function.

val csr = ComplexShiftRequest(subtitles, "00:00:10,000", "00:00:03,000", "01:23:00,000", "01:32:00,000")



### SimpleShiftRequest ###

Simply shifts all subtitles by specified milliseconds.

val ssr = SimpleShiftRequest(subtitles, -556000)



### Generate and Save the modified SRT ###

val newline = "\r\n"
SrtUtil.saveSubtitles(SrtUtil.applyComplexShift(csr), "/path/to/sub0.srt", newline)
SrtUtil.saveSubtitles(SrtUtil.applySimpleShift(ssr), "/path/to/sub1.srt", newline)