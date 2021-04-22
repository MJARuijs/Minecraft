package player

class Jump(val height: Float) : Action() {

    override fun perform(player: Player, delta: Float) {
        if (player.getYLevel() < height) {
            player.position.y += delta
        } else {
            finish(player)
        }

        if (player.position.y >= height) {
            finish(player)
        }
    }

    override fun finish(player: Player) {

    }

    override fun interrupt(player: Player) {

    }
}