package hannesdorfmann.ex4

import hannesdorfmann.types.Person
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class RepositoryEx4(private val viewEx4: ViewEx4, private val backendEx4: BackendEx4) {


    fun search(): Observable<List<Person>> {

        /**
         * Implement the search call according the following criteria:
         * - The search query string must contain at least 3 characters
         * - To save backend traffic: only search if search query hasn't changed within the last 300 ms
         * - If the user is typing fast "Hannes" and than deletes and types "Hannes" again (exceeding 300 ms) the search should not execute twice.
         */

        return viewEx4.onSearchTextchanged()
                .filter { s -> s.length > 2 }
                .debounce(300, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .switchMap { s ->
                    backendEx4.searchfor(s)
                            .subscribeOn(Schedulers.io())
                }
    }
}
