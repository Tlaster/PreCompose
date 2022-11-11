@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "EXPOSED_PARAMETER_TYPE", "CANNOT_OVERRIDE_INVISIBLE_MEMBER")
package moe.tlaster.precompose

import androidx.compose.runtime.Composable
import androidx.compose.ui.createSkiaLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.native.ComposeLayer
import androidx.compose.ui.node.LayoutNode
import androidx.compose.ui.platform.AccessibilityController
import androidx.compose.ui.platform.EmptyFocusManager
import androidx.compose.ui.platform.MacosTextInputService
import androidx.compose.ui.platform.Platform
import androidx.compose.ui.platform.ViewConfiguration
import androidx.compose.ui.platform.WindowInfoImpl
import androidx.compose.ui.semantics.SemanticsOwner
import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.useContents
import platform.AppKit.NSBackingStoreBuffered
import platform.AppKit.NSWindow
import platform.AppKit.NSWindowDelegateProtocol
import platform.AppKit.NSWindowDidChangeBackingPropertiesNotification
import platform.AppKit.NSWindowDidResizeNotification
import platform.AppKit.NSWindowStyleMaskClosable
import platform.AppKit.NSWindowStyleMaskFullSizeContentView
import platform.AppKit.NSWindowStyleMaskMiniaturizable
import platform.AppKit.NSWindowStyleMaskResizable
import platform.AppKit.NSWindowStyleMaskTitled
import platform.AppKit.NSWindowTitleHidden
import platform.AppKit.NSWindowWillCloseNotification
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSMakeRect
import platform.Foundation.NSNotification
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSSelectorFromString
import platform.darwin.NSObject

internal class ComposeWindow(
    hideTitleBar: Boolean = false,
    initialTitle: String,
    private val onCloseRequest: () -> Unit = {},
) : NSObject(), NSWindowDelegateProtocol {
    private val macosTextInputService = MacosTextInputService()
    private val platform: Platform = object : Platform {
        override val windowInfo = WindowInfoImpl().apply {
            // true is a better default if platform doesn't provide WindowInfo.
            // otherwise UI will be rendered always in unfocused mode
            // (hidden textfield cursor, gray titlebar, etc)
            isWindowFocused = true
        }

        override val focusManager = EmptyFocusManager

        override fun requestFocusForOwner() = false

        override fun accessibilityController(owner: SemanticsOwner) = object : AccessibilityController {
            override fun onSemanticsChange() = Unit
            override fun onLayoutChange(layoutNode: LayoutNode) = Unit
            override suspend fun syncLoop() = Unit
        }

        override fun setPointerIcon(pointerIcon: PointerIcon) = Unit
        override val viewConfiguration = object : ViewConfiguration {
            override val longPressTimeoutMillis: Long = 500
            override val doubleTapTimeoutMillis: Long = 300
            override val doubleTapMinTimeMillis: Long = 40
            override val touchSlop: Float = 18f
        }
        override val textInputService = macosTextInputService
    }

    val layer = ComposeLayer(
        layer = createSkiaLayer(),
        platform = platform,
        getTopLeftOffset = { Offset.Zero },
        input = macosTextInputService.input
    )
    val title: String
        get() = nsWindow.title()

    fun setTitle(title: String) {
        nsWindow.setTitle(title)
    }

    private val windowStyle =
        (
            NSWindowStyleMaskTitled or
                NSWindowStyleMaskMiniaturizable or
                NSWindowStyleMaskClosable or
                NSWindowStyleMaskResizable
            ).let {
            if (hideTitleBar) {
                it or NSWindowStyleMaskFullSizeContentView
            } else {
                it
            }
        }

    private val contentRect = NSMakeRect(0.0, 0.0, 640.0, 480.0)

    private val nsWindow = NSWindow(
        contentRect = contentRect,
        styleMask = windowStyle,
        backing = NSBackingStoreBuffered,
        defer = true,
    ).apply {
        center()
        setFrameAutosaveName(initialTitle)
        if (hideTitleBar) {
            titlebarAppearsTransparent = true
            titleVisibility = NSWindowTitleHidden
        }
    }

    init {
        layer.layer.attachTo(nsWindow)
        nsWindow.orderFrontRegardless()
        nsWindow.delegate = this
        NSNotificationCenter.defaultCenter().addObserver(
            this,
            selector = NSSelectorFromString("windowDidResize:"),
            name = NSWindowDidResizeNotification,
            `object` = nsWindow,
        )
        NSNotificationCenter.defaultCenter().addObserver(
            this,
            selector = NSSelectorFromString("windowWillClose:"),
            name = NSWindowWillCloseNotification,
            `object` = nsWindow,
        )
        NSNotificationCenter.defaultCenter().addObserver(
            this,
            selector = NSSelectorFromString("windowDidChangeBackingProperties:"),
            name = NSWindowDidChangeBackingPropertiesNotification,
            `object` = nsWindow,
        )
        updateLayerSize()
        setTitle(initialTitle)
    }

    @ObjCAction
    override fun windowDidChangeBackingProperties(notification: NSNotification) {
        updateLayerSize()
    }

    @ObjCAction
    override fun windowDidResize(notification: NSNotification) {
        updateLayerSize()
    }

    @ObjCAction
    override fun windowWillClose(notification: NSNotification) {
        onCloseRequest.invoke()
    }

    private fun updateLayerSize() {
        val (w, h) = nsWindow.contentView!!.frame.useContents {
            size.width to size.height
        }
        layer.setSize(w.toInt(), h.toInt())
        layer.layer.nsView.frame = CGRectMake(0.0, 0.0, w, h)
        layer.layer.redrawer?.syncSize()
        layer.layer.redrawer?.redrawImmediately()
    }

    /**
     * Sets Compose content of the ComposeWindow.
     *
     * @param content Composable content of the ComposeWindow.
     */
    fun setContent(
        content: @Composable () -> Unit
    ) {
        layer.setContent(
            content = content
        )
    }

    // TODO: need to call .dispose() on window close.
    // When calling dispose() it will throw kotlin.IllegalStateException which indicate that the layer is already disposed.
    fun dispose() {
        layer.dispose()
    }
}
