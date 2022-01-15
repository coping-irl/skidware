/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.utils

import net.ccbluex.liquidbounce.event.MoveEvent
import net.minecraft.potion.Potion
import net.minecraft.util.AxisAlignedBB
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt


object MovementUtils : MinecraftInstance() {
    @JvmStatic

    val speed: Float
        get() = Math.sqrt(mc.thePlayer.motionX * mc.thePlayer.motionX + mc.thePlayer.motionZ * mc.thePlayer.motionZ)
            .toFloat()
    @JvmStatic

    val isBlockUnder: Boolean
        get() {
            if (mc.thePlayer == null) return false
            if (mc.thePlayer.posY < 0.0) {
                return false
            }
            var off = 0
            while (off < mc.thePlayer.posY as Int + 2) {
                val bb: AxisAlignedBB = mc.thePlayer.getEntityBoundingBox().offset(0.0, (-off).toDouble(), 0.0)
                if (!mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, bb).isEmpty()) {
                    return true
                }
                off += 2
            }
            return false
        }

    @JvmStatic

    val isMoving: Boolean
        get() = mc.thePlayer != null && (mc.thePlayer.movementInput.moveForward !== 0f || mc.thePlayer.movementInput.moveStrafe !== 0f)

    @JvmStatic

    fun hasMotion(): Boolean {
        return mc.thePlayer.motionX !== 0.0 && mc.thePlayer.motionZ !== 0.0 && mc.thePlayer.motionY !== 0.0
    }

    @JvmOverloads
    @JvmStatic

    fun strafe(speed: Float = this.speed) {
        if (!isMoving) return
        val yaw = direction
        mc.thePlayer.motionX = -Math.sin(yaw) * speed
        mc.thePlayer.motionZ = Math.cos(yaw) * speed
    }

    @JvmStatic


    fun forward(length: Double) {
        val yaw = Math.toRadians(mc.thePlayer.rotationYaw.toDouble())
        mc.thePlayer.setPosition(mc.thePlayer.posX + -sin(yaw) * length, mc.thePlayer.posY, mc.thePlayer.posZ + cos(yaw) * length)
    }

    @JvmName("getSpeed1")
    @JvmStatic

    fun getSpeed(): Float {
        return Math.sqrt(mc.thePlayer.motionX * mc.thePlayer.motionX + mc.thePlayer.motionZ * mc.thePlayer.motionZ)
            .toFloat()
    }

    @JvmStatic

    fun getDirectionRotation(yaw: Float, pStrafe: Float, pForward: Float): Double {
        var rotationYaw = yaw
        if (pForward < 0f) rotationYaw += 180f
        var forward = 1f
        if (pForward < 0f) forward = -0.5f else if (pForward > 0f) forward = 0.5f
        if (pStrafe > 0f) rotationYaw -= 90f * forward
        if (pStrafe < 0f) rotationYaw += 90f * forward
        return Math.toRadians(rotationYaw.toDouble())
    }

    @JvmStatic

    fun getRawDirectionRotation(yaw: Float, pStrafe: Float, pForward: Float): Float {
        var rotationYaw = yaw
        if (pForward < 0f) rotationYaw += 180f
        var forward = 1f
        if (pForward < 0f) forward = -0.5f else if (pForward > 0f) forward = 0.5f
        if (pStrafe > 0f) rotationYaw -= 90f * forward
        if (pStrafe < 0f) rotationYaw += 90f * forward
        return rotationYaw
    }

    @JvmStatic

    fun getScaffoldRotation(yaw: Float, strafe: Float): Float {
        var rotationYaw = yaw
        rotationYaw += 180f
        val forward = -0.5f
        if (strafe < 0f) rotationYaw -= 90f * forward
        if (strafe > 0f) rotationYaw += 90f * forward
        return rotationYaw
    }

    @JvmName("getRawDirection1")
    @JvmStatic

    open fun getRawDirection(): Float {
        return getRawDirectionRotation(mc.thePlayer.rotationYaw, mc.thePlayer.moveStrafing, mc.thePlayer.moveForward)
    }

    @JvmName("getDirection1")
    @JvmStatic

    open fun getDirection(): Double {
        return getDirectionRotation(mc.thePlayer.rotationYaw, mc.thePlayer.moveStrafing, mc.thePlayer.moveForward)
    }

    @JvmName("getBaseMoveSpeed1")
    @JvmStatic

    fun getBaseMoveSpeed(): Double {
        return sqrt(mc.thePlayer.motionX * mc.thePlayer.motionX + mc.thePlayer.motionZ * mc.thePlayer.motionZ)
    }

    @JvmStatic

    val direction: Double
        get() = getDirectionRotation(mc.thePlayer.rotationYaw, mc.thePlayer.moveStrafing, mc.thePlayer.moveForward)

    @JvmStatic

    val rawDirection: Float
        get() = getRawDirectionRotation(mc.thePlayer.rotationYaw, mc.thePlayer.moveStrafing, mc.thePlayer.moveForward)

    @JvmStatic

    val baseMoveSpeed: Double
        get() = sqrt(mc.thePlayer.motionX * mc.thePlayer.motionX + mc.thePlayer.motionZ * mc.thePlayer.motionZ)

    @JvmStatic

    fun getJumpBoostModifier(baseJumpHeight: Double): Double {
        var baseJumpHeight = baseJumpHeight
        if (mc.thePlayer.isPotionActive(Potion.jump)) {
            val amplifier: Int = mc.thePlayer.getActivePotionEffect(Potion.jump).getAmplifier()
            baseJumpHeight += ((amplifier + 1).toFloat() * 0.1f).toDouble()
        }
        return baseJumpHeight
    }

    @JvmStatic

    fun setSpeed(moveEvent: MoveEvent, moveSpeed: Double) {
        setSpeed(
            moveEvent,
            moveSpeed,
            mc.thePlayer.rotationYaw,
            mc.thePlayer.movementInput.moveStrafe as Double,
            mc.thePlayer.movementInput.moveForward as Double
        )
    }

    @JvmStatic

    fun setSpeed(
        moveEvent: MoveEvent,
        moveSpeed: Double,
        pseudoYaw: Float,
        pseudoStrafe: Double,
        pseudoForward: Double
    ) {
        var forward = pseudoForward
        var strafe = pseudoStrafe
        var yaw = pseudoYaw
        if (forward == 0.0 && strafe == 0.0) {
            moveEvent.z = 0.0
            moveEvent.x = 0.0
        } else {
            if (forward != 0.0) {
                if (strafe > 0.0) {
                    yaw += (if (forward > 0.0) -45 else 45).toFloat()
                } else if (strafe < 0.0) {
                    yaw += (if (forward > 0.0) 45 else -45).toFloat()
                }
                strafe = 0.0
                if (forward > 0.0) {
                    forward = 1.0
                } else if (forward < 0.0) {
                    forward = -1.0
                }
            }
            if (strafe > 0.0) {
                strafe = 1.0
            } else if (strafe < 0.0) {
                strafe = -1.0
            }
            val cos = Math.cos(Math.toRadians((yaw + 90.0f).toDouble()))
            val sin = Math.sin(Math.toRadians((yaw + 90.0f).toDouble()))
            moveEvent.x = forward * moveSpeed * cos + strafe * moveSpeed * sin
            moveEvent.z = forward * moveSpeed * sin - strafe * moveSpeed * cos
        }
    }
}