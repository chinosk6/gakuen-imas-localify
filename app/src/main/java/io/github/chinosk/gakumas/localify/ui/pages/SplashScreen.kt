import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import io.github.chinosk.gakumas.localify.R

@Composable
fun SplashScreen(navController: NavController) {
    /*Image(
        painter = painterResource(id = R.drawable.splash_image),
        contentDescription = null,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.FillHeight
    )*/

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(100)

        navController.navigate("main") {
            popUpTo("splash") { inclusive = true }
        }
    }
}
