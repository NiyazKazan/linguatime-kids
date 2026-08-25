package com.linguatime.kids

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.linguatime.kids.screens.RoleSelectionScreen
import com.linguatime.kids.screens.StubScreen

enum class AppScreen {
    ROLE_SELECTION,
    PARENT_FLOW,
    CHILD_FLOW
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    var screen by remember { mutableStateOf(AppScreen.ROLE_SELECTION) }

                    when (screen) {
                        AppScreen.ROLE_SELECTION -> RoleSelectionScreen(
                            onParentClick = { screen = AppScreen.PARENT_FLOW },
                            onChildClick = { screen = AppScreen.CHILD_FLOW }
                        )
                        AppScreen.PARENT_FLOW -> StubScreen(
                            title = "Зона родителя (в разработке)",
                            onBack = { screen = AppScreen.ROLE_SELECTION }
                        )
                        AppScreen.CHILD_FLOW -> StubScreen(
                            title = "Зона ребёнка (в разработке)",
                            onBack = { screen = AppScreen.ROLE_SELECTION }
                        )
                    }
                }
            }
        }
    }
}