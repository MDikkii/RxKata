package playground

import com.github.davidmoten.rx2.RetryWhen
import com.google.common.base.Suppliers
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import kotlin.random.Random


/**
 * How to poll data with Rx depending on a given value?
 *
 * TODO:
 * - Tests
 */

typealias Username = String

typealias Token = String

class PollingExercise {

    private val memoizedTokenSupplier = Suppliers.memoizeWithExpiration({ getLoginToken() }, 2, TimeUnit.SECONDS)

    fun poll(intervalSeconds: Long) =
            Observable.interval(0, intervalSeconds, TimeUnit.SECONDS)
                    .switchMap { interval ->
                        memoizedTokenSupplier.get().switchMapSingle { loginToken ->
                            fetchData(interval.toInt()).subscribeOn(Schedulers.io())
                                    .observeOn(Schedulers.trampoline())
                                    .doOnError { println("onError $it") }
                                    .retryWhen(RetryWhen.maxRetries(3)
                                            .exponentialBackoff(100, TimeUnit.MILLISECONDS)
                                            .retryWhenInstanceOf(IllegalStateException::class.java)
                                            .build())
                                    .observeOn(Schedulers.computation())
                        }
                    }
                    .startWith(emptyList<Int>())
                    .scan { t1, t2 -> t1.union(t2).toList() }
                    .doOnNext { println("onNext $it") }

    private fun getLoginToken(): Observable<Token> = Observable.timer(300, TimeUnit.MILLISECONDS).map { Random.nextInt(100000).toString() }.doOnNext { println("Token: $it") }

    private fun fetchData(seed: Int) = Single.timer(200, TimeUnit.MILLISECONDS).map {
        Random.nextInt(10).takeIf { it > 3 }?.let { generateSequence(seed) { it + 1 }.take(10).toList() }
                ?: throw IllegalStateException()
    }
}

fun main() {
    val intervalSeconds = 1L
    PollingExercise().poll(intervalSeconds).subscribe()

    Thread.sleep(intervalSeconds * 5 * 1000)
}