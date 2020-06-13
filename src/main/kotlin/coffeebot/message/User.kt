package coffeebot.message

data class User(val name: String, private val id: Long) {
    override fun toString(): String {
        return this.name
    }

    fun getMentionString(): String {
        return "<@${this.id}>"
    }
}
