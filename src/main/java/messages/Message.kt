package messages

class Message(val topic: MessageTopics, val data: HashMap<String, Any?> = HashMap())