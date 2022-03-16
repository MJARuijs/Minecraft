package messages

class MessageBus {

    private val clients = ArrayList<MessageClient>()

    private val messages = ArrayDeque<Message>()

    fun registerClient(client: MessageClient) {
        client.register(this)
        clients += client
    }

    fun registerClient(client: MessageClient, topics: ArrayList<MessageTopics>) {
        client.register(this, topics)
        clients += client
    }

    fun addMessage(message: Message) {
        messages += message
    }

    fun update() {
        while (messages.isNotEmpty()) {
            val message = messages.removeFirst()

            for (client in clients) {
                if (client.isInterestedInTopic(message.topic)) {
                    client.receiveMessage(message)
                }
            }
        }
    }

}