package androidx.fragment.app

import android.view.View
import androidx.annotation.MainThread
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.observe
import androidx.viewbinding.ViewBinding
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

@MainThread
fun <T : ViewBinding> Fragment.viewBindings(
    viewBindingFactory: (View) -> T,
    onDestroy: (T) -> Unit = {}
): ReadOnlyProperty<Fragment, T> {
    return FragmentViewBinding(this, viewBindingFactory, onDestroy)
}

internal class FragmentViewBinding<T : ViewBinding>(
    val fragment: Fragment,
    val viewBindingFactory: (View) -> T,
    val onDestroy: (T) -> Unit
) : ReadOnlyProperty<Fragment, T> {
    private var binding: T? = null

    init {
        fragment.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                fragment.viewLifecycleOwnerLiveData.observe(fragment) { viewLifecycleOwner ->
                    viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
                        override fun onDestroy(owner: LifecycleOwner) {
                            binding?.let(onDestroy)
                            binding = null
                        }
                    })
                }
            }
        })
    }

    override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        val currentBinding = binding
        if (currentBinding != null) {
            return currentBinding
        }
        val lifecycle = fragment.viewLifecycleOwner.lifecycle
        if (!lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED)) {
            throw IllegalStateException("Can't access binding when Fragment views are destroyed.")
        }
        return viewBindingFactory(thisRef.requireView()).also { binding = it }
    }
}
