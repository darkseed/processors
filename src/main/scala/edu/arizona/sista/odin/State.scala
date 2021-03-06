package edu.arizona.sista.odin

class State(val lookUpTable: MentionLUT = Map.empty) {
  def update(mentions: Seq[Mention]): State = new State(mergeLuts(mkLut(mentions)))

  private def mkLut(mentions: Seq[Mention]): MentionLUT = {
    val pairs = for {
      m <- mentions
      i <- m.tokenInterval.toSeq
      key = (m.sentence, i)
    } yield (key, m)
    pairs groupBy (_._1) mapValues (_ map (_._2))
  }

  private def mergeLuts(newLut: MentionLUT): MentionLUT = {
    val merged = for {
      key <- lookUpTable.keySet ++ newLut.keySet
      mentions = lookUpTable.getOrElse(key, Nil) ++ newLut.getOrElse(key, Nil)
    } yield (key -> mentions.distinct)
    merged.toMap
  }

  // returns all mentions contained in the state
  def allMentions: Seq[Mention] =
    lookUpTable.values.toSeq.flatten.distinct filter (_.keep)

  // checks if a mention is already contained in the state
  def contains(m: Mention): Boolean =
    mentionsFor(m.sentence, m.start) exists (_ == m)

  def mentionsFor(sent: Int, tok: Int): Seq[Mention] =
    lookUpTable.getOrElse((sent, tok), Nil)

  def mentionsFor(sent: Int, tok: Int, label: String): Seq[Mention] =
    mentionsFor(sent, tok) filter (_ matches label)

  def mentionsFor(sent: Int, tok: Int, labels: Seq[String]): Seq[Mention] =
    labels flatMap (l => mentionsFor(sent, tok, l))

  def mentionsFor(sent: Int, toks: Seq[Int]): Seq[Mention] =
    toks flatMap (t => mentionsFor(sent, t))

  def mentionsFor(sent: Int, toks: Seq[Int], label: String): Seq[Mention] =
    toks flatMap (t => mentionsFor(sent, t, label))

  def mentionsFor(sent: Int, toks: Seq[Int], labels: Seq[String]): Seq[Mention] =
    toks flatMap (t => mentionsFor(sent, t, labels))
}
