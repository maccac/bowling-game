import java.lang.IllegalStateException

class BowlingGame {

    companion object {
        const val MAX_FRAMES = 10
    }

    private val frames = mutableListOf(Frame())
    private var bonusRolls = mutableListOf<Int>()

    fun roll(numberOfPins: Int) {
        if (isFinishedFinalFrame() && !hasAvailableBonusRolls()) {
            throw IllegalStateException("Game is already over")
        } else if (isFinishedFinalFrame()) {
            bonusRolls.add(numberOfPins)
        } else {
            selectNextFrame().recordRoll(numberOfPins)
        }
    }

    private fun selectNextFrame(): Frame {
        var currentFrame = currentFrame()
        if (currentFrame.isFinished()) {
            currentFrame = Frame()
            frames.add(currentFrame)
        }
        return currentFrame
    }

    private fun isFinishedFinalFrame() = (frames.size == MAX_FRAMES) && frames[MAX_FRAMES - 1].isFinished()

    private fun hasAvailableBonusRolls() =
            (frames[9].isStrike() && bonusRolls.size < 2)
            || (frames[MAX_FRAMES - 1].isSpare() && bonusRolls.size < 1)

    fun score(): Int {
        return sumOfFrames() + bonusRolls.sum()
    }

    private fun sumOfFrames() = frames
        .filter(Frame::isFinished)
        .mapIndexed { index, frame -> scoreRollForFrame(frame, index) }
        .toList()
        .sum()

    private fun scoreRollForFrame(frame: Frame, index: Int): Int {
        val subsequentRolls = getRollsAfterIndex(index)
        return when {
            frame.isFinished() && index == 9 -> frame.knockedDownPins()
            frame.isSpare() -> scoreSpare(subsequentRolls, frame)
            frame.isStrike() -> scoreStrike(subsequentRolls, frame)
            else -> frame.knockedDownPins()
        }
    }

    private fun scoreSpare(subsequentRolls: List<Int>, frame: Frame): Int {
        return if (subsequentRolls.isNotEmpty()) {
            frame.knockedDownPins() + subsequentRolls[0]
        } else {
            0
        }
    }

    private fun scoreStrike(subsequentRolls: List<Int>, frame: Frame): Int {
        return if (subsequentRolls.size >= 2) {
            frame.knockedDownPins() + subsequentRolls[0] + subsequentRolls[1]
        } else {
            0
        }
    }

    private fun getRollsAfterIndex(index: Int) =
        listOf(
            *(frames.subList(index + 1, frames.size).flatMap { it.rolls }.toTypedArray()),
            *bonusRolls.toTypedArray()
        )

    fun currentFrame() = frames[frames.size - 1]
}

class Frame {

    var rolls = mutableListOf<Int>()

    fun recordRoll(numberOfPins: Int) {
        require(numberOfPins >= 0) {"Negative rolls are not allowed."}
        require(knockedDownPins() + numberOfPins <= 10) {"Roll of $numberOfPins exceeds the number of pins in a frame"}
        rolls.add(numberOfPins)
    }

    fun knockedDownPins() = rolls.sum()

    internal fun isFinished() = (rolls.size == 2 || knockedDownPins() == 10)
    internal fun isSpare() = (rolls.size == 2 &&  knockedDownPins() == 10)
    internal fun isStrike() = (rolls.size == 1 && knockedDownPins() == 10)

}