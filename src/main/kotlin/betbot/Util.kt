package betbot

import java.util.Optional

fun <T> Optional<T>.getOptional(): T? {
    return this.orElse(null)
}
