package com.dastanapps.poweroff.service

import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.graphics.Rect
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.accessibility.AccessibilityNodeInfo
import com.dastanapps.poweroff.MainApp.Companion.log


fun Context.openAccessibilitySettings(packageName: String = "com.dastanapps.poweroff.tv") {
    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)

    val bundle = Bundle()
    val componentName = ComponentName(
        packageName,
        TVHelperService::class.java.name
    ).flattenToString()
    bundle.putString(":settings:fragment_args_key", componentName)

    intent.putExtra(":settings:fragment_args_key", componentName)
    intent.putExtra(":settings:show_fragment_args", bundle)

    try {
        startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

/**
 *
 * @param serviceName for eg : "application.Id/package.path.Service"
 */
fun isAccessibilitySettingsOn(context: Context, serviceName: String): Boolean {
    var accessibilityEnable = 0

    val serviceName = serviceName
    try {
        accessibilityEnable = Settings.Secure.getInt(
            context.contentResolver,
            Settings.Secure.ACCESSIBILITY_ENABLED
        )
    } catch (e: Exception) {
        log("get accessibility enable failed, the err:" + e.message)
    }
    if (1 == accessibilityEnable) {
        val mStringColonSplitter = TextUtils.SimpleStringSplitter(':')
        val settingValue: String = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        mStringColonSplitter.setString(settingValue)
        while (mStringColonSplitter.hasNext()) {
            val accessibilityService = mStringColonSplitter.next()
            if (accessibilityService.equals(serviceName, ignoreCase = true)) {
                log("accessibility service: $serviceName  is on.")
                return true
            }
        }
    } else {
        log("accessibility service disable.")
    }
    return false
}

fun findNode(
    service: AccessibilityService,
    node: AccessibilityNodeInfo?,
    pInt: Point
): List<AccessibilityNodeInfo>? {
    var node: AccessibilityNodeInfo? = node
    if (node == null) {
        node = service.rootInActiveWindow
    }
    val nodeInfos = ArrayList<AccessibilityNodeInfo>()
    node = findNodeHelper(node, pInt, nodeInfos)
    return nodeInfos
}

fun findNodeHelper(
    node: AccessibilityNodeInfo?,
    pInt: Point,
    nodeList: ArrayList<AccessibilityNodeInfo>
): AccessibilityNodeInfo? {
    if (node == null) {
        return null
    }
    val tmp = Rect()
    node.getBoundsInScreen(tmp)
    if (!tmp.contains(pInt.x, pInt.y)) {
        // node doesn't contain cursor
        return null
    }
    nodeList.add(node)
    var result: AccessibilityNodeInfo? = null
    result = node
    val childCount = node.childCount
    for (i in 0 until childCount) {
        val child = findNodeHelper(node.getChild(i), pInt, nodeList)
        if (child != null) {
            // always picks the last innermost clickable child
            result = child
        }
    }
    return result
}

fun findScrollableNode(
    root: AccessibilityNodeInfo,
    scrollDirection: AccessibilityNodeInfo.AccessibilityAction
): AccessibilityNodeInfo? {
    val deque: ArrayDeque<AccessibilityNodeInfo> = ArrayDeque()
    deque.add(root)
    while (!deque.isEmpty()) {
        val node = deque.removeFirst()
        if (node.actionList.contains(scrollDirection)) {
            return node
        }
        for (i in 0 until node.childCount) {
            deque.addLast(node.getChild(i))
        }
    }
    return null
}

fun findEditable(nodeInfo: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
    val focusInput = nodeInfo?.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
    var currentFocusInput: AccessibilityNodeInfo? = null

    if (focusInput?.isEditable == true) {
        currentFocusInput = focusInput

        log("Find Focus ${focusInput.className} ${focusInput.text}")

    } else if (currentFocusInput == null && nodeInfo != null && nodeInfo.childCount > 0) {
        loop@ for (index in 0 until nodeInfo.childCount) {
            val item = nodeInfo.getChild(index)
            if (item.isEditable) {
                currentFocusInput = item

                log("Node：${item.className} ${item.text}")
                break@loop
            }
        }
    }

    return currentFocusInput
}