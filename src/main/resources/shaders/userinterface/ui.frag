#version 450 core

const float SMOOTH_DISTANCE = 0.002f;

in vec2 position;
in vec2 passTexCoords;

uniform sampler2D sampler;
uniform vec4 color;
uniform vec4 overlayColor;
uniform vec4 outlineColor;
uniform float outline;

uniform bool textured;
uniform bool hasBackground;
uniform bool hasOverlay;

uniform float cornerRadius;

uniform float aspectRatio;
uniform vec2 viewPort;

uniform vec2 translation;
uniform vec2 scale;
uniform vec2 parentTranslation;
uniform vec2 parentScale;
uniform bool allowedToOverdraw;

out vec4 outColor;

void checkCorner(vec2 currentPoint, float x, float y, float scaledCornerRadius) {
    float distance = distance(currentPoint, vec2(x, y));
    float minScale = min(scale.x, scale.y);
    float scaledOutline = outline * 2 * minScale;

    if (cornerRadius > 0.0f) {
        if (distance > scaledCornerRadius) {
            outColor.a = 0.0f;
        } else if (distance > scaledCornerRadius - scaledOutline) {
            outColor = outlineColor;
            if (scaledCornerRadius < scaledOutline) {
                if (distance - (scaledCornerRadius - scaledOutline) < SMOOTH_DISTANCE) {
                    float factor = (distance - (scaledCornerRadius - scaledOutline)) / SMOOTH_DISTANCE;
                    outColor.a = factor;
                } else if ((scaledCornerRadius-distance) < SMOOTH_DISTANCE) {
                    outColor.a = (scaledCornerRadius-distance ) / SMOOTH_DISTANCE;
                }
            } else {
                if (distance - (scaledCornerRadius - scaledOutline) < SMOOTH_DISTANCE) {

                    float mixFactor = (distance - (scaledCornerRadius - scaledOutline)) / SMOOTH_DISTANCE;
                    outColor = mix(outlineColor, outColor, mixFactor);

                    outColor.a = mixFactor;
                } else if ((scaledCornerRadius-distance) < SMOOTH_DISTANCE) {
                    float mixFactor = (scaledCornerRadius-distance ) / SMOOTH_DISTANCE;
                    outColor = mix(outColor, outlineColor, mixFactor);
                    outColor.a = mixFactor;
                }
            }
        }
    }
}

void main() {

    if (!allowedToOverdraw) {
        float parentMinX = parentTranslation.x - parentScale.x;
        float parentMaxX = parentTranslation.x + parentScale.x;
        float parentMinY = parentTranslation.y - parentScale.y;
        float parentMaxY = parentTranslation.y + parentScale.y;

        vec2 currentPoint = gl_FragCoord.xy;
        currentPoint /= viewPort;
        currentPoint = (currentPoint * 2.0) - 1.0;
        currentPoint.x *= aspectRatio;

        if (currentPoint.x < parentMinX || currentPoint.x > parentMaxX) {
            discard;
        }
        if (currentPoint.y < parentMinY || currentPoint.y > parentMaxY) {
            discard;
        }
    }

    if (textured) {
        vec4 textureColor = texture(sampler, passTexCoords);
        outColor = textureColor;
//        if (outColor.a <= 0.1) {
//            outColor = vec4(1.0, 0.0, 0.0, 0);
//        }

        if (hasBackground) {
            if (hasOverlay) {
                outColor = textureColor.a * overlayColor + (1.0 - textureColor.a) * color;
            } else {
                outColor.rgb = textureColor.a * textureColor.rgb + (1.0 - textureColor.a) * color.rgb;
            }
        } else {
            if (hasOverlay) {
                outColor.rgb = textureColor.a * overlayColor.rgb + (1.0 - textureColor.a) * textureColor.rgb;
            }
        }
    } else {
        outColor = color;
    }

    if (cornerRadius > 0.0f) {
        vec2 currentPoint = gl_FragCoord.xy;
        currentPoint.x /= viewPort.x;
        currentPoint.y /= viewPort.y;
        currentPoint.x = (currentPoint.x * 2.0) - 1.0;
        currentPoint.y = (currentPoint.y * 2.0) - 1.0;

        currentPoint.x *= aspectRatio;

        float minScale = min(scale.x, scale.y);
        float scaledOutline = outline * 2 * minScale;

        float scaledCornerRadius = cornerRadius / 90.0;
        scaledCornerRadius *= minScale;

        float leftBound = translation.x - scale.x + scaledCornerRadius;
        float topBound = translation.y + scale.y - scaledCornerRadius;
        float rightBound = translation.x + scale.x - scaledCornerRadius;
        float bottomBound = translation.y - scale.y + scaledCornerRadius;

        bool outOfLeftBound = false;
        bool outOfTopBound = false;
        bool outOfRightBound = false;
        bool outOfBottomBound = false;

        if (currentPoint.x > rightBound) {
            outOfRightBound = true;
        }
        if (currentPoint.x < leftBound) {
            outOfLeftBound = true;
        }
        if (currentPoint.y > topBound) {
            outOfTopBound = true;
        }
        if (currentPoint.y < bottomBound) {
            outOfBottomBound = true;
        }

        if (outOfTopBound) {
            if (outOfLeftBound) {
                checkCorner(currentPoint, leftBound, topBound, scaledCornerRadius);
            } else if (outOfRightBound) {
                checkCorner(currentPoint, rightBound, topBound, scaledCornerRadius);
            }
        }

        if (outOfBottomBound) {
            if (outOfLeftBound) {
                checkCorner(currentPoint, leftBound, bottomBound, scaledCornerRadius);
            } else if (outOfRightBound) {
                checkCorner(currentPoint, rightBound, bottomBound, scaledCornerRadius);
            }
        }

        if (outline > 0.0f) {
            if (outOfRightBound && !outOfTopBound && !outOfBottomBound) {
                if (currentPoint.x > translation.x + scale.x - scaledOutline) {
                    checkCorner(vec2(currentPoint.x, 0.0), rightBound, 0.0, scaledCornerRadius);
//                    outColor = outlineColor;
                }
            }

            if (outOfLeftBound && !outOfTopBound && !outOfBottomBound) {
                if (currentPoint.x < translation.x - scale.x + scaledOutline) {
                    checkCorner(vec2(currentPoint.x, 0.0), leftBound, 0.0, scaledCornerRadius);
                }
            }

            if (outOfTopBound && !outOfLeftBound && !outOfRightBound) {
                if (currentPoint.y > translation.y + scale.y - scaledOutline) {
                    checkCorner(vec2(0.0, currentPoint.y), 0.0, topBound, scaledCornerRadius);
                }
            }

            if (outOfBottomBound && !outOfLeftBound && !outOfRightBound) {
                if (currentPoint.y < translation.y - scale.y + scaledOutline) {
                    checkCorner(vec2(0.0, currentPoint.y), 0.0, bottomBound, scaledCornerRadius);
                }
            }
        }
    } else if (outline > 0.0f) {
//        outColor.r = 1.0f;
//        outColor.a = 1.0f;

        float minScale = min(scale.x, scale.y);

        float scaledOutline = outline * 2 * minScale;
        vec2 currentPoint = gl_FragCoord.xy;
        currentPoint.x /= viewPort.x;
        currentPoint.y /= viewPort.y;
        currentPoint.x = (currentPoint.x * 2.0) - 1.0;
        currentPoint.y = (currentPoint.y * 2.0) - 1.0;

        currentPoint.x *= aspectRatio;

        if (currentPoint.x > translation.x + scale.x - scaledOutline) {
            outColor = outlineColor;
        }

        if (currentPoint.x < translation.x - scale.x + scaledOutline) {
            outColor = outlineColor;
        }

        if (currentPoint.y > translation.y + scale.y - scaledOutline) {
            outColor = outlineColor;
        }

        if (currentPoint.y < translation.y - scale.y + scaledOutline) {
            outColor = outlineColor;
        }
    }
}