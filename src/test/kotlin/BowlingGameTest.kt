import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType

import org.junit.jupiter.api.Test
import java.lang.IllegalStateException

class BowlingGameTest {

    @Test
    internal fun negativeRollResultsInAnError() {
        val game = BowlingGame()

        assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
            game.roll(-1)
        }
    }

    @Test
    fun `game starts on frame 1 with a score of 0`() {
        val game = BowlingGame()

        assertThat(game.score()).isEqualTo(0)
        assertThat(game.currentFrame().knockedDownPins()).isEqualTo(0)
    }

    @Test
    fun `single roll is not scored until frame finishes`() {
        val game = BowlingGame()

        game.roll(4)

        assertThat(game.score()).isEqualTo(0)
        assertThat(game.currentFrame().knockedDownPins()).isEqualTo(4)
    }

    @Test
    fun `completed first frame is scored`() {
        val game = BowlingGame()

        game.rollFrame(4, 4)

        assertThat(game.score()).isEqualTo(8)
        assertThat(game.currentFrame().knockedDownPins()).isEqualTo(8)
    }

    @Test
    fun `third roll moves to next frame but doesn't impact on score until finished`() {
        val game = BowlingGame()

        game.rollFrame(4, 4)
        game.roll(1)

        assertThat(game.score()).isEqualTo(8)
        assertThat(game.currentFrame().knockedDownPins()).isEqualTo(1)
    }

    @Test
    fun `completed second frame is scored`() {
        val game = BowlingGame()

        game.rollFrame(4, 4)
        game.rollFrame(1, 6)

        assertThat(game.score()).isEqualTo(15)
        assertThat(game.currentFrame().knockedDownPins()).isEqualTo(7)
    }

    @Test
    fun `rolling a strike followed by a 5 does impact on scores until second frame is finished`() {
        val game = BowlingGame()

        game.strike()
        game.roll(5)

        assertThat(game.score()).isEqualTo(0)
        assertThat(game.currentFrame().knockedDownPins()).isEqualTo(5)
    }

    @Test
    fun `rolling a strike followed by a completed seccond frame impacts on the score`() {
        val game = BowlingGame()

        game.strike()
        game.rollFrame(4, 2)

        assertThat(game.score()).isEqualTo(22)
        assertThat(game.currentFrame().knockedDownPins()).isEqualTo(6)
    }

    @Test
    fun `rolling a strike followed by one strike doesn't change the score yet`() {
        val game = BowlingGame()

        game.strike()
        game.strike()

        assertThat(game.score()).isEqualTo(0)
        assertThat(game.currentFrame().knockedDownPins()).isEqualTo(10)
    }

    @Test
    fun `rolling a strike includes the next two rolls in the total`() {
        val game = BowlingGame()

        game.strike()
        game.rollFrame(5, 4)

        assertThat(game.score()).isEqualTo(28)
        assertThat(game.currentFrame().knockedDownPins()).isEqualTo(9)
    }

    @Test
    fun `rolling a strike followed by a spare`() {
        val game = BowlingGame()

        game.strike()
        game.rollFrame(5, 5)

        assertThat(game.score()).isEqualTo(20)
        assertThat(game.currentFrame().knockedDownPins()).isEqualTo(10)
    }

    @Test
    fun `rolling a strike followed by two strikes includes the bonus score`() {
        val game = BowlingGame()

        game.strike()
        game.strike()
        game.strike()

        assertThat(game.score()).isEqualTo(30)
        assertThat(game.currentFrame().knockedDownPins()).isEqualTo(10)
    }

    @Test
    fun `rolling more then the number of pins in a roll throws an IllegalArgumentException`() {
        val game = BowlingGame()

        game.roll(6)
        assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
            game.roll(5)
        }
    }

    @Test
    fun `rolling a spare on frame 1 is not scored yet`() {
        val game = BowlingGame()

        game.rollFrame(4, 6)

        assertThat(game.currentFrame().knockedDownPins()).isEqualTo(10)
        assertThat(game.score()).isEqualTo(0)
    }

    @Test
    fun `rolling a spare on frame 1 followed by a 5 returns the spare bonus`() {
        val game = BowlingGame()

        game.rollFrame(4, 6)
        game.rollFrame(5, 0)

        assertThat(game.currentFrame().knockedDownPins()).isEqualTo(5)
        assertThat(game.score()).isEqualTo(20)
    }

    @Test
    fun `breaking a strike streak on frame 3`() {
        val game = BowlingGame()

        game.strike()
        game.strike()
        game.roll(5)

        assertThat(game.score()).isEqualTo(25)
        assertThat(game.currentFrame().knockedDownPins()).isEqualTo(5)
    }

    @Test
    fun `Perfect game until the final frame`() {
        val game = BowlingGame()

        repeat(9) {
            game.strike()
        }

        game.roll(5)

        assertThat(game.score()).isEqualTo(235)
        assertThat(game.currentFrame().knockedDownPins()).isEqualTo(5)

        game.roll(1)

        assertThat(game.score()).isEqualTo(257)
        assertThat(game.currentFrame().knockedDownPins()).isEqualTo(6)
    }

    @Test
    fun `Game is over after final frame with no bonus rolls`() {
        val game = BowlingGame()

        repeat(9) {
            game.strike()
        }

        game.rollFrame(5, 1)

        assertThatExceptionOfType(IllegalStateException::class.java).isThrownBy {
            game.roll(5)
        }

    }

    @Test
    fun `Spare on final frame gives a bonus roll`() {
        val game = BowlingGame()

        repeat(9) {
            game.strike()
        }

        game.rollFrame(6, 4)
        game.roll(5)

        assertThat(game.score()).isEqualTo(271)
    }


    @Test
    fun `Strike on final frame gives two bonus roll`() {
        val game = BowlingGame()

        repeat(9) {
            game.strike()
        }

        game.strike()
        game.roll(5)
        game.roll(3)

        assertThat(game.score()).isEqualTo(283)
    }

    @Test
    fun `rolling the worst game ever`() {
        val game = BowlingGame()

        repeat(20) {
            game.roll(0)
        }

        assertThat(game.score()).isEqualTo(0)
        assertThat(game.currentFrame().knockedDownPins()).isEqualTo(0)
    }


    @Test
    fun `rolling an average game with some strikes and spares`() {
        val game = BowlingGame()

        //frame 1
        game.rollFrame(5, 4)

        //frame 2
        game.strike()


        // frame 3
        game.rollFrame(9, 1)


        // frame 4
        game.rollFrame(6, 1)


        // frame 5
        game.rollFrame(9, 1)


        // frame 6
        game.strike()

        // frame 7
        game.strike()

        // frame 8
        game.rollFrame(2, 1)


        // frame 9
        game.rollFrame(4, 2)

        // frame 10
        game.roll(3)
        game.roll(6)

        assertThat(game.score()).isEqualTo(125)
    }

    @Test
    fun `rolling a perfect game`() {
        val game = BowlingGame()

        repeat(12) {
            game.strike()
        }

        assertThat(game.score()).isEqualTo(300)
        assertThat(game.currentFrame().knockedDownPins()).isEqualTo(10)
    }

    private fun BowlingGame.rollFrame(roll1: Int, roll2: Int) {
        this.roll(roll1)
        this.roll(roll2)
    }

    private fun BowlingGame.strike() {
        this.roll(10)
    }


}