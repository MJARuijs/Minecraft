package userinterface.items

import devices.Mouse
import userinterface.UniversalParameters
import userinterface.UniversalParameters.SWITCH_THUMB_OFF_BACKGROUND
import userinterface.UniversalParameters.SWITCH_THUMB_ON_BACKGROUND
import userinterface.UniversalParameters.SWITCH_TRACK_OFF_BACKGROUND
import userinterface.UniversalParameters.SWITCH_TRACK_ON_BACKGROUND
import userinterface.animation.ColorAnimation
import userinterface.animation.animationtypes.ColorAnimationType
import userinterface.items.backgrounds.ColorType
import userinterface.items.backgrounds.ColoredBackground
import userinterface.layout.constraints.ConstraintDirection
import userinterface.layout.constraints.ConstraintSet
import userinterface.layout.constraints.constrainttypes.AspectRatioConstraint
import userinterface.layout.constraints.constrainttypes.CenterConstraint
import userinterface.layout.constraints.constrainttypes.PixelConstraint
import userinterface.layout.constraints.constrainttypes.RelativeConstraint

class Switch(id: String, constraints: ConstraintSet, private var switchOn: Boolean = false,
             private var onStateChanged: (Boolean) -> Unit = {},
             private val trackOnBackground: ColoredBackground = SWITCH_TRACK_ON_BACKGROUND(),
             private val thumbOnBackground: ColoredBackground = SWITCH_THUMB_ON_BACKGROUND(),
             private val trackOffBackground: ColoredBackground = SWITCH_TRACK_OFF_BACKGROUND(),
             private val thumbOffBackground: ColoredBackground = SWITCH_THUMB_OFF_BACKGROUND())
    : Item(id, constraints, trackOffBackground) {

    private var thumb: Item

    private val switchOffConstraints = ConstraintSet(
        PixelConstraint(ConstraintDirection.TO_LEFT),
        CenterConstraint(ConstraintDirection.VERTICAL),
        RelativeConstraint(ConstraintDirection.VERTICAL, 1.5f),
        AspectRatioConstraint(ConstraintDirection.HORIZONTAL, 1.0f)
    )
    
    private val switchOnConstraints = ConstraintSet(
        PixelConstraint(ConstraintDirection.TO_RIGHT),
        CenterConstraint(ConstraintDirection.VERTICAL),
        RelativeConstraint(ConstraintDirection.VERTICAL, 1.5f),
        AspectRatioConstraint(ConstraintDirection.HORIZONTAL, 1.0f)
    )
    
    private var previousState = switchOn
    
    init {
        if (switchOn) {
            background = ColoredBackground(trackOnBackground)
            thumb = UIButton("${id}_thumb", switchOnConstraints, { toggle() }, background = ColoredBackground(thumbOnBackground))
        } else {
            background = ColoredBackground(trackOffBackground)
            thumb = UIButton("${id}_thumb", switchOffConstraints, { toggle() }, background = ColoredBackground(thumbOffBackground))
        }
        allowChildToIgnoreBounds(thumb.id)
        children += thumb
    }

    fun turnOn(duration: Float = UniversalParameters.ANIMATION_DURATION) {
        switchOn = true
        animateTrack(duration)
        animateThumb(duration)
    }

    fun turnOff(duration: Float = UniversalParameters.ANIMATION_DURATION) {
        switchOn = false
        animateTrack(duration)
        animateThumb(duration)
    }

    fun toggle(duration: Float = UniversalParameters.ANIMATION_DURATION) {
        if (switchOn) {
            turnOff(duration)
        } else {
            turnOn(duration)
        }
    }
    
    override fun update(mouse: Mouse, aspectRatio: Float, deltaTime: Float): Boolean {
        if (previousState != switchOn) {
            onStateChanged(switchOn)
        }
        previousState = switchOn
        return super.update(mouse, aspectRatio, deltaTime)
    }
    
    private fun animateTrack(duration: Float) {
        val newColor = if (switchOn) {
            trackOnBackground.outlineColor
        } else {
            trackOffBackground.outlineColor
        }
        animator.apply(null, constraints, duration, ColorAnimation(duration, this, newColor, ColorAnimationType.CHANGE_TO_COLOR, ColorType.OUTLINE_COLOR))
    }
    
    private fun animateThumb(duration: Float) {
        val newColor = if (switchOn) {
            thumbOnBackground.color
        } else {
            thumbOffBackground.color
        }
        
        val newConstraints = if (switchOn) {
            switchOnConstraints
        } else {
            switchOffConstraints
        }
        
        thumb.animator.apply(this, newConstraints, duration, ColorAnimation(duration, thumb, newColor, ColorAnimationType.CHANGE_TO_COLOR, ColorType.BACKGROUND_COLOR))
    }
}