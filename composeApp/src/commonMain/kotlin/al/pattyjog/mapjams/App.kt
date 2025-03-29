package al.pattyjog.mapjams

import al.pattyjog.mapjams.ui.AppNavigation
import androidx.compose.runtime.Composable
import al.pattyjog.mapjams.ui.theme.AppTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinContext

@Composable
@Preview
fun App() {
    KoinContext {
        AppTheme {
            AppNavigation()
        }
    }
}