class Stats {
  var messages: Int = 0
  var allocated: Int = 0
  var checks: Int = 0
  var touches: Int = 0
  var multicasts: Int = 0
  var multicastsReceived: Int = 0
  var errors: Int = 0

  def += (right: Stats): Stats = {
    messages += right.messages
    allocated += right.allocated
    checks += right.checks
    touches += right.touches
    multicasts += right.multicasts
    multicastsReceived += right.multicastsReceived

    errors += right.errors
    this
  }

  override def toString(): String = {
    s"Stats msgs=$messages joins=$checks leaves=$touches multicasts=$multicasts received=$multicastsReceived err=$errors"
  }
}
