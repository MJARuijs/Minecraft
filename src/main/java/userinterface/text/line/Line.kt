package userinterface.text.line

class Line(private val maxLineWidth: Float, val words: ArrayList<Word> = ArrayList()) {
    
    constructor(maxLineWidth: Float, vararg words: Word) : this(maxLineWidth) {
        this.words += words
    }
    
    fun addWord(newWord: Word, spaceWidth: Float): Boolean {
        
        var currentLineWidth = 0.0f
        for (word in words) {
            currentLineWidth += word.getWidth() + spaceWidth
        }
        
        if (currentLineWidth + newWord.getWidth() > maxLineWidth) {
            return false
        }
        
        words += newWord
        return true
    }
}