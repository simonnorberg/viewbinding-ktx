package androidx.activity

import android.view.LayoutInflater
import androidx.annotation.MainThread
import androidx.viewbinding.ViewBinding

@MainThread
inline fun <T : ViewBinding> ComponentActivity.viewBindings(
    crossinline bindingInflater: (LayoutInflater) -> T
): Lazy<T> {
    return lazy(LazyThreadSafetyMode.NONE) {
        bindingInflater.invoke(layoutInflater)
    }
}
