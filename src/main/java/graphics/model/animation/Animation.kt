package graphics.model.animation

class Animation(val name: String, val loop: Boolean, val keyFrames: List<Pair<Int, Pose>>) {

    fun getLength(): Int {
        return keyFrames.last().first
    }

}