@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "EXPOSED_PARAMETER_TYPE", "CANNOT_OVERRIDE_INVISIBLE_MEMBER")

package moe.tlaster.precompose

import androidx.compose.runtime.Composable
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.native.ComposeLayer
import androidx.compose.ui.platform.MacosTextInputService
import androidx.compose.ui.platform.PlatformContext
import androidx.compose.ui.platform.WindowInfoImpl
import androidx.compose.ui.unit.Density
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.useContents
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkikoInput
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

@OptIn(ExperimentalForeignApi::class)
internal class ComposeWindow(
    hideTitleBar: Boolean = false,
    initialTitle: String,
    private val onCloseRequest: () -> Unit = {},
    private val onMinimizeRequest: () -> Unit = {},
    private val onDeminiaturizeRequest: () -> Unit = {},
) : NSObject(), NSWindowDelegateProtocol {

    private val density by lazy {
        Density(
            density = nsWindow.backingScaleFactor.toFloat(),
        )
    }
    private val macosTextInputService = MacosTextInputService()
    private val _windowInfo = WindowInfoImpl().apply {
        isWindowFocused = true
    }

    @OptIn(InternalComposeUiApi::class)
    private val platformContext: PlatformContext =
        object : PlatformContext by PlatformContext.Empty {
            override val windowInfo get() = _windowInfo
            override val textInputService get() = macosTextInputService
        }

    @OptIn(InternalComposeUiApi::class)
    private val layer = ComposeLayer(
        layer = SkiaLayer(),
        platformContext = platformContext,
        input = SkikoInput.Empty,
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

    @OptIn(BetaInteropApi::class)
    @ObjCAction
    override fun windowDidChangeBackingProperties(notification: NSNotification) {
        updateLayerSize()
    }

    @OptIn(BetaInteropApi::class)
    @ObjCAction
    override fun windowDidResize(notification: NSNotification) {
        updateLayerSize()
    }

    @OptIn(BetaInteropApi::class)
    @ObjCAction
    override fun windowWillClose(notification: NSNotification) {
        onCloseRequest.invoke()
    }

    @OptIn(BetaInteropApi::class)
    @ObjCAction
    override fun windowWillMiniaturize(notification: NSNotification) {
        onMinimizeRequest.invoke()
    }

    @OptIn(BetaInteropApi::class)
    @ObjCAction
    override fun windowDidDeminiaturize(notification: NSNotification) {
        onDeminiaturizeRequest.invoke()
    }

    private fun updateLayerSize() {
        val (w, h) = nsWindow.contentView!!.frame.useContents {
            size.width to size.height
        }
        layer.setSize(w.toInt() * density.density.toInt(), h.toInt() * density.density.toInt())
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
        content: @Composable () -> Unit,
    ) {
        layer.setDensity(density)
        layer.setContent(
            content = content,
        )
    }

    // TODO: need to call .dispose() on window close.
    // When calling dispose() it will throw kotlin.IllegalStateException which indicate that the layer is already disposed.
    fun dispose() {
        layer.dispose()
    }
}
