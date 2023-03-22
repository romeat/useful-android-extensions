import java.util.concurrent.TimeUnit
import java.text.SimpleDateFormat
import java.util.*
import android.content.res.Resources
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/* Converts timestamp in milliseconds to HH:MM:SS format (MM:SS when time is less than 1 hour) */
fun Long.toDisplayableTimeString(): String {
    if (this <= 0) return "00:00"

    val HH = TimeUnit.MILLISECONDS.toHours(this)
    val MM = TimeUnit.MILLISECONDS.toMinutes(this) % 60
    val SS = TimeUnit.MILLISECONDS.toSeconds(this) % 60

    return if (HH > 0) {
        String.format("%d:%02d:%02d", HH, MM, SS);
    } else {
        String.format("%02d:%02d", MM, SS);
    }
}

/* Converting example: 1234 -> 1.2K, 12099 -> 12.1K */
fun Int.toStringWithThousands(): String {
    return if(this < 1000) {
        this.toString()
    } else {
        "%.1f".format(this.toDouble()/1000) + "K"
    }
}

/** Extension function for compose ui single live event 
 * source: https://stackoverflow.com/questions/69351948/android-compose-with-single-event
 * usage: 
 *    viewModel.eventsFlow.collectInLaunchedEffectWithLifecycle { event ->
 *      when (event) {
 *          is ...
 *      }
 */
@Suppress("ComposableNaming")
@Composable
fun <T> Flow<T>.collectInLaunchedEffectWithLifecycle(
    vararg keys: Any?,
    lifecycle: Lifecycle = LocalLifecycleOwner.current.lifecycle,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    collector: suspend CoroutineScope.(T) -> Unit
) {
    val flow = this
    val currentCollector by rememberUpdatedState(collector)

    LaunchedEffect(flow, lifecycle, minActiveState, *keys) {
        withContext(Dispatchers.Main.immediate) {
            lifecycle.repeatOnLifecycle(minActiveState) {
                flow.collect { currentCollector(it) }
            }
        }
    }
}


// Convert px to dp
val Int.dp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()

// Convert dp to px
val Int.px: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

    
// String utils
val String.isDigitOnly: Boolean
    get() = matches(Regex("^\\d*\$"))

val String.isAlphabeticOnly: Boolean
    get() = matches(Regex("^[a-zA-Z]*\$"))

val String.isAlphanumericOnly: Boolean
    get() = matches(Regex("^[a-zA-Z\\d]*\$"))
    
    
// Date formatting   
fun String.toDate(format: String = "yyyy-MM-dd HH:mm:ss"): Date? {
    val dateFormatter = SimpleDateFormat(format, Locale.getDefault())
    return dateFormatter.parse(this)
}

fun Date.toStringFormat(format: String = "yyyy-MM-dd HH:mm:ss"): String {
    val dateFormatter = SimpleDateFormat(format, Locale.getDefault())
    return dateFormatter.format(this)
}
