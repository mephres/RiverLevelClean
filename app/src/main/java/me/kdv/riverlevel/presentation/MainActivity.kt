package me.kdv.riverlevel.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import me.kdv.riverlevel.ui.theme.RiverLevelTheme
import me.kdv.riverlevel.ui.theme.RiversScreen
import javax.inject.Inject

class MainActivity : ComponentActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val component by lazy {
        (application as RiverApp).component
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        component.inject(this)
        super.onCreate(savedInstanceState)

        setContent {
            RiverLevelTheme {
                RiversScreen(viewModelFactory)
            }
        }
    }
}