class ChristmasTree(var color: String) {

    fun putTreeTopper(color: String) {
        val topper = TreeTopper(color)
        topper.sparkle()
    }

    inner class TreeTopper(var color: String) {

        fun sparkle() {
            println("The sparkling $color tree topper looks stunning on the ${this@ChristmasTree.color} Christmas tree!")
        }
    }
}