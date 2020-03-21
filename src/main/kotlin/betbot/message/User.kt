package betbot.message

data class User(val name: String) {
    override fun toString(): String {
        return this.name
    }
}
