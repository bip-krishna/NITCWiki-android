package org.wikipedia.onboarding

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import org.wikipedia.Constants
import org.wikipedia.R
import org.wikipedia.NITCWikiApp
import org.wikipedia.activity.BaseActivity
import org.wikipedia.analytics.testkitchen.TestKitchenAdapter
import org.wikipedia.compose.theme.BaseTheme
import org.wikipedia.feed.personalization.PersonalizationActivity
import org.wikipedia.language.AppLanguageState
import org.wikipedia.settings.Prefs
import org.wikipedia.settings.languages.NITCWikiLanguagesActivity
import org.wikipedia.theme.Theme
import org.wikipedia.util.DeviceUtil
import org.wikipedia.util.ResourceUtil

class InitialOnboardingActivity : BaseActivity() {

    private val appLanguageCodesState = mutableStateOf(NITCWikiApp.instance.languageState.appLanguageCodes.toList())

    private val languagesLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        appLanguageCodesState.value = NITCWikiApp.instance.languageState.appLanguageCodes.toList()
        Prefs.homeLanguageCode = NITCWikiApp.instance.languageState.appLanguageCode
        setResult(RESULT_LANGUAGE_CHANGED)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DeviceUtil.setEdgeToEdge(this)

        _instrument = TestKitchenAdapter.client.getInstrument("apps-onboarding")
            .setDefaultActionSource("initial_onboarding")
            .startFunnel("initial_onboarding")

        setContent {
            var currentTheme by remember { mutableStateOf(Theme.BLACK) }
            var currentNavigationBarColor by remember { mutableIntStateOf(ContextCompat.getColor(window.context, android.R.color.black)) }
            DeviceUtil.setLightSystemUiVisibility(this, !currentTheme.isDark)
            setNavigationBarColor(currentNavigationBarColor)
            BaseTheme(
                currentTheme = currentTheme
            ) {
                AppOnboardingScreen(
                    languageState = NITCWikiApp.instance.languageState,
                    appLanguageCodes = appLanguageCodesState.value,
                    isNewUser = Prefs.isInitialOnboardingEnabled,
                    onAddLanguageClick = {
                        languagesLauncher.launch(NITCWikiLanguagesActivity.newIntent(this, Constants.InvokeSource.ONBOARDING_DIALOG))
                    },
                    onUpdateTheme = {
                        currentTheme = NITCWikiApp.instance.currentTheme
                        currentNavigationBarColor = ResourceUtil.getThemedColor(this, R.attr.paper_color)
                    },
                    onFinish = {
                        Prefs.isInitialOnboardingEnabled = false
                        Prefs.isExploreFeedUpdatePromptShown = true
                        startActivity(PersonalizationActivity.newIntent(this))
                        finish()
                    }
                )
            }
        }
    }

    companion object {
        // TODO: need to refresh the language state at the final screen
        const val RESULT_LANGUAGE_CHANGED = 1
        fun newIntent(context: Context): Intent {
            return Intent(context, InitialOnboardingActivity::class.java)
        }
    }
}

enum class OnboardingScreen {
    INTRO,
    DATA_PRIVACY,
    LANGUAGES
}

@Composable
fun AppOnboardingScreen(
    modifier: Modifier = Modifier,
    languageState: AppLanguageState?,
    appLanguageCodes: List<String>,
    isNewUser: Boolean,
    onAddLanguageClick: () -> Unit,
    onUpdateTheme: () -> Unit,
    onFinish: () -> Unit
) {
    var showIntroScreen by remember { mutableStateOf(isNewUser) }
    if (showIntroScreen) {
        InitialOnboardingScreen(
            modifier = modifier,
            onboardingScreens = listOf(OnboardingScreen.INTRO, OnboardingScreen.DATA_PRIVACY, OnboardingScreen.LANGUAGES),
            languageState = languageState,
            appLanguageCodes = appLanguageCodes,
            onAddLanguageClick = {
                onAddLanguageClick()
            },
            onNextClick = {
                onUpdateTheme()
            },
            onFinishClick = {
                showIntroScreen = false
            }
        )
    } else {
        // TODO: interest selection
        onFinish()
    }
}
