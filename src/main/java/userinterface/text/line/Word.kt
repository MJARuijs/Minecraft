package userinterface.text.line

class Word(private val fontSize: Float, val characters: ArrayList<Character> = ArrayList()) {
    
    fun getWidth(): Float {
        var width = 0.0f
        
        for (character in characters) {
            width += character.advance * fontSize
        }
        
        return width
    }
    
    operator fun plusAssign(character: Character) {
        characters += character
    }
    
    override fun toString(): String {
        
        var string = ""
        
        for (char in characters) {
            string += char.id
        }
        
        return string
    }
    
}