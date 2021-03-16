package userinterface.window

import userinterface.UniversalParameters
import userinterface.items.Item
import userinterface.items.UIButton
import userinterface.items.backgrounds.Background
import userinterface.layout.constraints.ConstraintDirection
import userinterface.layout.constraints.ConstraintSet
import userinterface.layout.constraints.constrainttypes.AspectRatioConstraint
import userinterface.layout.constraints.constrainttypes.CenterConstraint
import userinterface.layout.constraints.constrainttypes.PixelConstraint
import userinterface.layout.constraints.constrainttypes.RelativeConstraint

class TitleBar(id: String = "TitleBar", constraints: ConstraintSet, background: Background, closeButtonAlignment: ButtonAlignment, onClick: () -> Unit = {}) : Item(id, constraints, background) {
    
    constructor(id: String, height: Float, background: Background, closeButtonAlignment: ButtonAlignment, onClick: () -> Unit = {}) : this(
        id,
        ConstraintSet(
            CenterConstraint(ConstraintDirection.VERTICAL),
            PixelConstraint(ConstraintDirection.TO_TOP, 0.0f),
            RelativeConstraint(ConstraintDirection.HORIZONTAL, 1.0f),
            RelativeConstraint(ConstraintDirection.VERTICAL, height)
        ),
        background,
        closeButtonAlignment,
        onClick
    )

    constructor(id: String, titleBarData: TitleBarData, onClick: () -> Unit = {}) : this(id, titleBarData.height, titleBarData.background, titleBarData.closeButtonAlignment, onClick)

    init {
        if (closeButtonAlignment != ButtonAlignment.HIDDEN) {
            if (closeButtonAlignment == ButtonAlignment.RIGHT) {
                val buttonConstraints = ConstraintSet(
                    PixelConstraint(ConstraintDirection.TO_TOP, 0.0f),
                    PixelConstraint(ConstraintDirection.TO_RIGHT, 0.0f),
                    RelativeConstraint(ConstraintDirection.VERTICAL, 1.0f),
                    AspectRatioConstraint(ConstraintDirection.HORIZONTAL, 1.0f)
                )
                
                add(UIButton("close_button", buttonConstraints, onClick, background = UniversalParameters.CLOSE_BUTTON_BACKGROUND()))
            } else if (closeButtonAlignment == ButtonAlignment.LEFT) {
                val buttonConstraints = ConstraintSet(
                    PixelConstraint(ConstraintDirection.TO_TOP, 0.0f),
                    PixelConstraint(ConstraintDirection.TO_LEFT, 0.0f),
                    RelativeConstraint(ConstraintDirection.VERTICAL, 1.0f),
                    AspectRatioConstraint(ConstraintDirection.HORIZONTAL, 1.0f)
                )

                add(UIButton("close_button", buttonConstraints, onClick, background = UniversalParameters.CLOSE_BUTTON_BACKGROUND()))
            }
        }
    }
}