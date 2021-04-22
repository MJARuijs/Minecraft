package userinterface.layout

import userinterface.UIColor
import userinterface.items.backgrounds.ColorType
import userinterface.layout.constraints.ConstraintSet

class UILayout(val id: String, private val itemChanges: HashMap<String, Pair<ConstraintSet, Pair<UIColor, ColorType>?>> = HashMap()) {

    operator fun plusAssign(itemChange: Triple<String, ConstraintSet, Pair<UIColor, ColorType>>) {
        itemChanges[itemChange.first] = Pair(itemChange.second, itemChange.third)
    }

    operator fun plusAssign(itemChange: Pair<String, ConstraintSet>) {
        itemChanges[itemChange.first] = Pair(itemChange.second, null)
    }

    fun getConstraintsChange(id: String) = itemChanges[id]?.first

    fun getColorChange(id: String) = itemChanges[id]?.second

}