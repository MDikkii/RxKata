package operators

import io.reactivex.schedulers.TestScheduler
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

class TradingExercisesTest {

    private lateinit var exercises: TradingExercises
    private lateinit var testScheduler: TestScheduler

    @Before
    fun setUp() {
        exercises = TradingExercises()
        testScheduler = TestScheduler()
    }

    @Test
    fun noEventEmitted() {
        val test = exercises.noEventEmitted(testScheduler).test()
        test.assertSubscribed()
        testScheduler.advanceTimeBy(1, TimeUnit.HOURS)
        test.assertNoValues()
    }
}