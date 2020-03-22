package coffeebot.commands.lisp

fun parseLisp(s: String): Expression {

    val tokens = lex(s).toMutableList()
    val lisp = syntax(tokens)

    return if (tokens.isEmpty()) {
        lisp
    } else {
        throw SyntaxError()
    }
}

fun lex(lisp: String): List<String> {
    if (lisp == "") {
        throw SyntaxError()
    }

    return lisp.replace("(", " ( ")
            .replace(")", " ) ")
            .replace("\\s+".toRegex(), " ")
            .trim()
            .split(" ")
}

fun <T> MutableList<T>.pop(): T {
    return this.removeAt(0)
}

fun <T> MutableList<T>.unpop(t: T) {
    this.add(0, t)
}

fun syntax(tokens: MutableList<String>): Expression {

    when (val car = tokens.pop()) {
        ")" -> throw SyntaxError()
        "(" -> {
            val sExp = mutableListOf<Expression>()
            while (tokens.size > 0) {
                val car = tokens.pop()
                when (car) {
                    ")" -> return SExpression(sExp)
                    "(" -> {
                        tokens.unpop(car)
                        sExp.add(syntax(tokens))
                    }
                    else -> sExp.add(Token(car))
                }
            }
        }
        else -> return Token(car)
    }
    throw SyntaxError()
}

class SyntaxError: Exception()
