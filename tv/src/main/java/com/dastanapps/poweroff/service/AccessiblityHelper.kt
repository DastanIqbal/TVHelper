package com.dastanapps.poweroff.service

import android.accessibilityservice.AccessibilityService
import android.graphics.Point
import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo


//// below code is for supporting legacy devices as per my understanding of evia face cam source
//// this is only used for long clicks here and isn't exactly something reliable
//// leaving it in for reference just in case needed in future, because looking up face cam
//// app's source might be a daunting task
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