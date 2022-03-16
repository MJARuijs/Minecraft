package messages

abstract class MessageClient(private var topics: ArrayList<MessageTopics> = ArrayList()) {

    private lateinit var messageBus: MessageBus

    protected val data = HashMap<String, Any?>()

    fun register(messageBus: MessageBus) {
        this.messageBus = messageBus
    }

    fun register(messageBus: MessageBus, topics: ArrayList<MessageTopics>) {
        this.messageBus = messageBus
        this.topics = topics
    }

    fun isInterestedInTopic(topic: MessageTopics): Boolean {
        return topics.contains(topic)
    }

    fun sendMessage(message: Message) {
        messageBus.addMessage(message)
    }

    abstract fun receiveMessage(message: Message)

}