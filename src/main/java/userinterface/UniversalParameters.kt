package userinterface

import math.Color
import userinterface.items.backgrounds.ColoredBackground
import userinterface.items.backgrounds.TexturedBackground
import userinterface.text.font.Font

object UniversalParameters {
    
    private val PRIMARY_COLOR = UIColor.BLUE
    private val PRIMARY_BACKGROUND_COLOR = UIColor.GREY_DARK
    
    var aspectRatio: Float = 1.0f
        private set
    
    lateinit var defaultFont: Font
        private set
    
    fun init(aspectRatio: Float, font: Font) {
        this.aspectRatio = aspectRatio
        this.defaultFont = font
    }

    const val ANIMATION_DURATION = 0.1f
    
    fun TEXT_COLOR() = UIColor.WHITE

    fun ITEM_BACKGROUND() = ColoredBackground(UIColor.BLUE)
    
    fun SWITCH_THUMB_OFF_BACKGROUND() = ColoredBackground(Color(185, 185, 185), 90f)
    fun SWITCH_THUMB_ON_BACKGROUND() = ColoredBackground(Color(65, 162, 239), 90f)
    
    fun SWITCH_TRACK_OFF_BACKGROUND() = ColoredBackground(UIColor.TRANSPARENT, 90f, 0.09f, Color(74, 74, 74))
    fun SWITCH_TRACK_ON_BACKGROUND() = ColoredBackground(UIColor.TRANSPARENT, 90f, 0.09f, Color(33, 73, 107))
    
    fun BUTTON_BACKGROUND() = ColoredBackground(PRIMARY_COLOR)
    fun BUTTON_ON_HOVER_BACKGROUND() = ColoredBackground(PRIMARY_COLOR, 0.0f, 0.09f, UIColor.WHITE)
    
    fun PROGRESS_BAR_BACKGROUND() = ColoredBackground(UIColor.GREY)
    fun PROGRESS_BAR_COLOR() = PRIMARY_COLOR
    fun PROGRESS_BAR_PAUSED_COLOR() = UIColor.YELLOW_LIGHT
    fun PROGRESS_BAR_COMPLETED_COLOR() = UIColor.GREEN_LIGHT
    
    fun CLOSE_BUTTON_BACKGROUND() = TexturedBackground("textures/userinterface/close_button.png", overlayColor = UIColor.WHITE)
    fun CLOSE_BUTTON_HOVERED_COLOR() = UIColor.GREY
    
    fun CHECK_BOX_UNCHECKED_BACKGROUND() = ColoredBackground(UIColor.TRANSPARENT, 15f, 0.1f, UIColor.WHITE)
    fun CHECK_BOX_CHECKED_BACKGROUND() = TexturedBackground("textures/userinterface/check_mark.png", PRIMARY_COLOR, UIColor.WHITE, 15f, 0.1f, UIColor.WHITE)
    
    fun SCROLL_PANE_BACKGROUND() = ColoredBackground(PRIMARY_BACKGROUND_COLOR)
    
    fun TEXTBOX_BACKGROUND() = ColoredBackground(UIColor.TRANSPARENT)
    
    fun MENU_BACKGROUND() = ColoredBackground(UIColor.TRANSPARENT)
    
}