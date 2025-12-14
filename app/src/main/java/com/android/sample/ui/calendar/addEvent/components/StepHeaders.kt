import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.android.sample.ui.theme.PaddingSmallMedium
import com.android.sample.ui.theme.SpacingLarge
import com.android.sample.ui.theme.SpacingSmall
import com.android.sample.ui.theme.heightSmall

@Composable
fun StepHeader(
    stepText: String,
    title: String,
    subtitle: String,
    icon: @Composable () -> Unit,
    progress: Float,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Box(
                modifier = Modifier.padding(PaddingSmallMedium),
                contentAlignment = Alignment.Center
            ) { icon() }
        }

        Spacer(modifier = Modifier.width(SpacingSmall))
        Text(
            text = stepText,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Spacer(modifier = Modifier.height(SpacingSmall))

    Text(
        text = title,
        style = MaterialTheme.typography.headlineLarge,
        textAlign = TextAlign.Start,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(SpacingSmall))

    if (subtitle.isNotBlank()) {
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(SpacingLarge))
    } else {
        Spacer(modifier = Modifier.height(SpacingLarge))
    }

    Spacer(modifier = Modifier.height(SpacingLarge))

    Surface(
        modifier = Modifier.fillMaxWidth().height(heightSmall),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(progress),
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.primary
        ) {}
    }
}