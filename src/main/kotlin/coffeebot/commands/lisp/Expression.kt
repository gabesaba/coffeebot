package coffeebot.commands.lisp

import java.lang.Exception
import java.math.BigInteger

sealed class Expression {
    abstract fun eval(env: Env): LispObject
}

data class SExpression(val exprs: List<Expression>): Expression() {
    override fun eval(env: Env): LispObject {
        val fn = exprs[0].eval(env)
        return if (fn is Fn) {
            fn.apply(exprs.subList(1, exprs.size), env)
        } else {
            throw TypeError("Expected function, got ${fn.type()}")
        }
    }
}

data class Token(val token: String): Expression() {
    override fun eval(env: Env): LispObject {

        val num = token.toLongOrNull()

        if (num != null) {
         return LispNumber(BigInteger.valueOf(num))
        }

        val type = env.find(token)
        if (type != null) {
            return type
        } else {
            throw LispError("Not found: $token. Try (definitions)")
        }
    }

    fun isIdentifier() = token.matches("[a-zA-Z][A-Za-z0-9]*".toRegex())
}

fun s(vararg exprs: Expression) = SExpression(exprs.toList())
fun t(s: String) = Token(s)

class LispError(override val message: String): Exception()