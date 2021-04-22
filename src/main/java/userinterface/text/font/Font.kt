package userinterface.text.font

import graphics.textures.ImageMap
import resources.Resource
import userinterface.text.line.Character

class Font(val textureAtlas: ImageMap, private val characters: ArrayList<Character>) : Resource {

    fun getSpaceWidth(): Float {
        val spaceCharacter = getCharacter(32.toChar())
        return spaceCharacter.advance
    }
    
    fun getCharacter(id: Char): Character {
        return characters.find { char -> char.id == id } ?: throw NonExistingCharacterException("Character with id: $id could not be found in this font..")
    }
    
    override fun destroy() {
        textureAtlas.destroy()
    }
}