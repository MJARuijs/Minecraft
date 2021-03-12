package player

abstract class Action {

    abstract fun perform(player: Player, delta: Float)

    abstract fun finish(player: Player)

    abstract fun interrupt(player: Player)

}