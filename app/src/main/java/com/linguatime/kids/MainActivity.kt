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
import com.linguatime.kids.data.AuthRepository
import com.linguatime.kids.screens.ParentHomeScreen
import com.linguatime.kids.screens.ParentPinSetupScreen
import com.linguatime.kids.screens.ParentSignInScreen
import com.linguatime.kids.screens.ParentSignUpScreen
import com.linguatime.kids.screens.RoleSelectionScreen
import com.linguatime.kids.screens.StubScreen

enum class AppScreen {
    ROLE_SELECTION,
    PARENT_SIGN_IN,
    PARENT_SIGN_UP,
    PARENT_PIN_SETUP,
    PARENT_HOME,
    CHILD_FLOW
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    val repository = remember { AuthRepository() }
                    var screen by remember { mutableStateOf(AppScreen.ROLE_SELECTION) }

                    when (screen) {
                        AppScreen.ROLE_SELECTION -> RoleSelectionScreen(
                            onParentClick = {
                                screen = if (repository.currentUser != null) {
                                    AppScreen.PARENT_HOME
                                } else {
                                    AppScreen.PARENT_SIGN_IN
                                }
                            },
                            onChildClick = { screen = AppScreen.CHILD_FLOW }
                        )
                        AppScreen.PARENT_SIGN_IN -> ParentSignInScreen(
                            repository = repository,
                            onSignedIn = { screen = AppScreen.PARENT_HOME },
                            onNeedsPinSetup = { screen = AppScreen.PARENT_PIN_SETUP },
                            onSignUpClick = { screen = AppScreen.PARENT_SIGN_UP },
                            onBack = { screen = AppScreen.ROLE_SELECTION }
                        )
                        AppScreen.PARENT_SIGN_UP -> ParentSignUpScreen(
                            repository = repository,
                            onSignedUp = { screen = AppScreen.PARENT_PIN_SETUP },
                            onBack = { screen = AppScreen.PARENT_SIGN_IN }
                        )
                        AppScreen.PARENT_PIN_SETUP -> ParentPinSetupScreen(
                            repository = repository,
                            onDone = { screen = AppScreen.PARENT_HOME },
                            onBack = { screen = AppScreen.ROLE_SELECTION }
                        )
                        AppScreen.PARENT_HOME -> ParentHomeScreen(
                            repository = repository,
                            onLoggedOut = { screen = AppScreen.ROLE_SELECTION }
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