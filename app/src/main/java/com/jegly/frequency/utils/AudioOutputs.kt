package com.jegly.frequency.utils

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager

object AudioOutputs {

    data class Device(val id: Int, val name: String, val type: Int)

    fun listOutputs(context: Context): List<Device> {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return am.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            .filter { it.isSink }
            .distinctBy { it.id }
            .map { Device(it.id, friendlyName(it), it.type) }
    }

    fun findById(context: Context, id: Int): AudioDeviceInfo? {
        if (id == AppSettings.AUDIO_DEVICE_SYSTEM) return null
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return am.getDevices(AudioManager.GET_DEVICES_OUTPUTS).firstOrNull { it.id == id }
    }

    private fun friendlyName(info: AudioDeviceInfo): String {
        val product = info.productName?.toString()?.takeIf { it.isNotBlank() }
        val typeLabel = when (info.type) {
            AudioDeviceInfo.TYPE_BUILTIN_SPEAKER       -> "Phone speaker"
            AudioDeviceInfo.TYPE_BUILTIN_EARPIECE      -> "Earpiece"
            AudioDeviceInfo.TYPE_WIRED_HEADPHONES      -> "Wired headphones"
            AudioDeviceInfo.TYPE_WIRED_HEADSET         -> "Wired headset"
            AudioDeviceInfo.TYPE_USB_HEADSET           -> "USB headset"
            AudioDeviceInfo.TYPE_USB_DEVICE            -> "USB device"
            AudioDeviceInfo.TYPE_USB_ACCESSORY         -> "USB accessory"
            AudioDeviceInfo.TYPE_BLUETOOTH_A2DP        -> "Bluetooth"
            AudioDeviceInfo.TYPE_BLUETOOTH_SCO         -> "Bluetooth (call)"
            AudioDeviceInfo.TYPE_HDMI                  -> "HDMI"
            AudioDeviceInfo.TYPE_HDMI_ARC              -> "HDMI ARC"
            AudioDeviceInfo.TYPE_LINE_ANALOG           -> "Line out"
            AudioDeviceInfo.TYPE_LINE_DIGITAL          -> "Digital out"
            AudioDeviceInfo.TYPE_DOCK                  -> "Dock"
            AudioDeviceInfo.TYPE_AUX_LINE              -> "Aux"
            else                                       -> "Output ${info.type}"
        }
        return if (product != null && product != typeLabel) "$typeLabel — $product" else typeLabel
    }
}
