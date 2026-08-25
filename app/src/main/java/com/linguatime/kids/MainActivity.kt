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
import com.linguatime.kids.data.ChildRepository
import com.linguatime.kids.data.DeviceStorage
import com.linguatime.kids.screens.ChildHomeScreen
import com.linguatime.kids.screens.ChildLoginScreen
import com.linguatime.kids.screens.ParentAddChildScreen
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
    PARENT_ADD_CHILD,
    PARENT_HOME,
    CHILD_LOGIN,
    CHILD_HOME,
    CHILD_FLOW
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val deviceStorage = DeviceStorage(this)
        setContent {
            MaterialTheme {
                Surface {
                    val repository = remember { AuthRepository() }
                    val childRepository = remember { ChildRepository() }
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
                            onChildClick = {
                                screen = if (deviceStorage.childId() != null) {
                                    AppScreen.CHILD_HOME
                                } else {
                                    AppScreen.CHILD_LOGIN
                                }
                            }
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
                        AppScreen.PARENT_ADD_CHILD -> ParentAddChildScreen(
                            repository = repository,
                            childRepository = childRepository,
                            onDone = { screen = AppScreen.PARENT_HOME },
                            onBack = { screen = AppScreen.PARENT_HOME }
                        )
                        AppScreen.PARENT_HOME -> ParentHomeScreen(
                            repository = repository,
                            childRepository = childRepository,
                            onAddChild = { screen = AppScreen.PARENT_ADD_CHILD },
                            onLoggedOut = { screen = AppScreen.ROLE_SELECTION }
                        )
                        AppScreen.CHILD_LOGIN -> ChildLoginScreen(
                            childRepository = childRepository,
                            deviceStorage = deviceStorage,
                            onLoggedIn = { screen = AppScreen.CHILD_HOME },
                            onBack = { screen = AppScreen.ROLE_SELECTION }
                        )
                        AppScreen.CHILD_HOME -> ChildHomeScreen(
                            childId = deviceStorage.childId() ?: "",
                            childRepository = childRepository,
                            onLoggedOut = {
                                deviceStorage.saveChildId(null)
                                screen = AppScreen.ROLE_SELECTION
                            }
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