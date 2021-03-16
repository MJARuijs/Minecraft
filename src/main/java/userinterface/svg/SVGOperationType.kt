package userinterface.svg

enum class SVGOperationType(val id: Char) {
    MOVE('m'),
    HORIZONTAL_LINE('h'),
    VERTICAL_LINE('v'),
    LINE('l'),
    CLOSE_PATH('z'),
    
    BEZIER_CURVE('c'),
    BEZIER_CURVE_EXTENSION('s'),
    QUADRATIC_CURVE('q'),
    QUADRATIC_CURVE_EXTENSION('t');
    
    companion object {
        fun fromId(char: Char): SVGOperationType {
            val type = values().find { type -> type.id.toLowerCase() == char.toLowerCase() }
            return type ?: throw NoSuchSVGOperationType("No SVGOperationType exists for char: $char")
        }
    }
}