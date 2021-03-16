//package userinterface.layout
//
//import userinterface.items.Item
//
//class UILayoutManager(private val layouts: ArrayList<UILayout> = ArrayList()) {
//
////    constructor(vararg layouts: UILayout) : this() {
////        this.layouts.addAll(layouts)
////    }
//
//    operator fun plusAssign(layout: UILayout) {
//        layouts += layout
//    }
//
//    fun apply(id: String, items: ArrayList<Item>) {
//        val layout = layouts.find { layout -> layout.id == id } ?: return
//        apply(layout, items)
//    }
//
//    fun apply(layout: UILayout, items: ArrayList<Item>) {
////        for (constraint in layout.constraints) {
////            constraint.a
////        }
//        layout.apply(items)
//    }
//
//}