/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.movement.Fly
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.*
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition

@ModuleInfo(name = "Criticals", description = "Automatically deals critical hits.", category = ModuleCategory.COMBAT)
class Criticals : Module() {

    val modeValue = ListValue(
        "Mode",
        arrayOf(
            "NewPacket",
            "Packet",
            "NCPPacket",
            "NoGround",
            "Redesky",
            "AACv4",
            "Hop",
            "TPHop",
            "Jump",
            "Visual",
            "Edit",
            "MiniPhase",
            "NanoPacket",
            "Non-Calculable",
            "Invalid",
            "FakeColide"
        ),
        "Packet"
    )
    val motionValue = ListValue("MotionMode", arrayOf("MinemoraTest"), "MinemoraTest")
    val hoverValue = ListValue("HoverMode", arrayOf("AAC4"), "AAC4")
    val hoverNoFall = BoolValue("HoverNoFall", true)
    val hoverCombat = BoolValue("HoverOnlyCombat", true)
    val delayValue = IntegerValue("Delay", 0, 0, 500)
    private val lookValue = BoolValue("UseC06Packet", false)
    private val jumpHeightValue = FloatValue("JumpHeight", 0.42F, 0.1F, 0.42F)
    private val downYValue = FloatValue("DownY", 0f, 0f, 0.1F)
    private val hurtTimeValue = IntegerValue("HurtTime", 10, 0, 10)
    private val onlyAuraValue = BoolValue("OnlyAura", false)

    val msTimer = MSTimer()
    private var readyCrits: Boolean = false
    private var canCrits: Boolean = true;
    var aacLastState = false

    override fun onEnable() {
        if (modeValue.get().equals("NoGround", ignoreCase = true))
            mc.thePlayer.jump()
        canCrits = true;
    }

    @EventTarget
    fun onAttack(event: AttackEvent) {
        if (onlyAuraValue.get() && !LiquidBounce.moduleManager[KillAura::class.java]!!.state) return

        if (event.targetEntity is EntityLivingBase) {
            val entity = event.targetEntity

            if (!mc.thePlayer.onGround || mc.thePlayer.isOnLadder || mc.thePlayer.isInWeb || mc.thePlayer.isInWater ||
                mc.thePlayer.isInLava || mc.thePlayer.ridingEntity != null || entity.hurtTime > hurtTimeValue.get() ||
                LiquidBounce.moduleManager[Fly::class.java]!!.state || !msTimer.hasTimePassed(delayValue.get().toLong())
            )
                return

            val x = mc.thePlayer.posX
            val y = mc.thePlayer.posY
            val z = mc.thePlayer.posZ

            fun sendCriticalPacket(
                xOffset: Double = 0.0,
                yOffset: Double = 0.0,
                zOffset: Double = 0.0,
                ground: Boolean
            ) {
                val x = mc.thePlayer.posX + xOffset
                val y = mc.thePlayer.posY + yOffset
                val z = mc.thePlayer.posZ + zOffset
                if (lookValue.get()) {
                    mc.netHandler.addToSendQueue(
                        C06PacketPlayerPosLook(
                            x,
                            y,
                            z,
                            mc.thePlayer.rotationYaw,
                            mc.thePlayer.rotationPitch,
                            ground
                        )
                    )
                } else {
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y, z, ground))
                }
            }

            when (modeValue.get().toLowerCase()) {
                "newpacket" -> {
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y + 0.05250000001304, z, true))
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y + 0.00150000001304, z, false))
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y + 0.01400000001304, z, false))
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y + 0.00150000001304, z, false))
                    mc.thePlayer.onCriticalHit(entity)
                }

                "packet" -> {
                    sendCriticalPacket(yOffset = 0.0625, ground = true)
                    sendCriticalPacket(ground = false)
                    sendCriticalPacket(yOffset = 1.1E-5, ground = false)
                    sendCriticalPacket(ground = false)
                }

                "ncppacket" -> {
                    sendCriticalPacket(yOffset = 0.11, ground = false)
                    sendCriticalPacket(yOffset = 0.1100013579, ground = false)
                    sendCriticalPacket(yOffset = 0.0000013579, ground = false)
                }

                "aacv4" -> {
                    mc.thePlayer.motionZ *= 0
                    mc.thePlayer.motionX *= 0
                    sendCriticalPacket(xOffset = mc.thePlayer.posX, yOffset = mc.thePlayer.posY + 3e-14, ground = true)
                    sendCriticalPacket(xOffset = mc.thePlayer.posX, yOffset = mc.thePlayer.posY + 8e-15, ground = true)
                }

                "hop" -> {
                    mc.thePlayer.motionY = 0.1
                    mc.thePlayer.fallDistance = 0.1f
                    mc.thePlayer.onGround = false
                }

                "tphop" -> {
                    sendCriticalPacket(yOffset = 0.02, ground = false)
                    sendCriticalPacket(yOffset = 0.01, ground = false)
                    mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.01, mc.thePlayer.posZ)
                }

                "jump" -> {
                    if (mc.thePlayer.onGround) {
                        mc.thePlayer.motionY = jumpHeightValue.get().toDouble()
                    } else {
                        mc.thePlayer.motionY -= downYValue.get()
                    }
                }

                "miniphase" -> {
                    sendCriticalPacket(yOffset = -0.0125, ground = false)
                    sendCriticalPacket(yOffset = 0.01275, ground = false)
                    sendCriticalPacket(yOffset = -0.00025, ground = true)
                }

                "nanopacket" -> {
                    sendCriticalPacket(yOffset = 0.00973333333333, ground = false)
                    sendCriticalPacket(yOffset = 0.001, ground = false)
                    sendCriticalPacket(yOffset = -0.01200000000007, ground = false)
                    sendCriticalPacket(yOffset = -0.0005, ground = false)
                }

                "non-calculable" -> {
                    sendCriticalPacket(yOffset = 1E-5, ground = false)
                    sendCriticalPacket(yOffset = 1E-7, ground = false)
                    sendCriticalPacket(yOffset = -1E-6, ground = false)
                    sendCriticalPacket(yOffset = -1E-4, ground = false)
                }

                "invalid" -> {
                    sendCriticalPacket(yOffset = 1E+27, ground = false)
                    sendCriticalPacket(yOffset = -1E+68, ground = false)
                    sendCriticalPacket(yOffset = 1E+41, ground = false)
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y - 1E+68, z, false))
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y + 1E+41, z, false))
                }

                "fakecollide" -> {
                    val motionX: Double
                    val motionZ: Double
                    if (MovementUtils.isMoving) {
                        motionX = mc.thePlayer.motionX
                        motionZ = mc.thePlayer.motionZ
                    } else {
                        motionX = 0.00
                        motionZ = 0.00
                    }
                    sendCriticalPacket(
                        xOffset = motionX / 3,
                        yOffset = 0.20000004768372,
                        zOffset = motionZ / 3,
                        ground = false
                    )
                    sendCriticalPacket(
                        xOffset = motionX / 1.5,
                        yOffset = 0.12160004615784,
                        zOffset = motionZ / 1.5,
                        ground = false
                    )
                }

                "visual" -> mc.thePlayer.onCriticalHit(entity)
            }

            readyCrits = true
            msTimer.reset()
        }
    }


    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (packet is C03PacketPlayer) {
            when (modeValue.get().toLowerCase()) {
                "noground" -> packet.onGround = false
                "motion" -> {
                    when (motionValue.get().toLowerCase()) {
                        "minemoratest" -> if (!LiquidBounce.combatManager.inCombat) mc.timer.timerSpeed = 1.00f
                    }
                }
                "hover" -> {
                    if (hoverCombat.get() && !LiquidBounce.combatManager.inCombat) return
                    if (packet is C05PacketPlayerLook) {
                        mc.netHandler.addToSendQueue(
                            C06PacketPlayerPosLook(
                                mc.thePlayer.posX,
                                mc.thePlayer.posY,
                                mc.thePlayer.posZ,
                                packet.yaw,
                                packet.pitch,
                                packet.onGround
                            )
                        )
                        event.cancelEvent()
                        return
                    } else if (!(packet is C04PacketPlayerPosition) && !(packet is C06PacketPlayerPosLook)) {
                        mc.netHandler.addToSendQueue(
                            C04PacketPlayerPosition(
                                mc.thePlayer.posX,
                                mc.thePlayer.posY,
                                mc.thePlayer.posZ,
                                packet.onGround
                            )
                        )
                        event.cancelEvent()
                        return
                    }
                    when (hoverValue.get().toLowerCase()) {
                        "aac4" -> {
                            if (mc.thePlayer.onGround && !aacLastState && hoverNoFall.get()) {
                                packet.onGround = mc.thePlayer.onGround
                                aacLastState = mc.thePlayer.onGround
                                packet.y += 0.000000000000136
                                return
                            }
                        }
                    }
                }
            }
        }
    }



    override val tag: String
        get() = modeValue.get()
}

