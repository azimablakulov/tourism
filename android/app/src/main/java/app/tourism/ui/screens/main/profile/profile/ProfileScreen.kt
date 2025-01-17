package app.tourism.ui.screens.main.profile.profile

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.organicmaps.R
import app.tourism.Constants
import app.tourism.applyAppBorder
import app.tourism.domain.models.profile.CurrencyRates
import app.tourism.domain.models.profile.PersonalData
import app.tourism.domain.models.resource.Resource
import app.tourism.ui.ObserveAsEvents
import app.tourism.ui.common.HorizontalSpace
import app.tourism.ui.common.LoadImg
import app.tourism.ui.common.SpaceForNavBar
import app.tourism.ui.common.VerticalSpace
import app.tourism.ui.common.buttons.PrimaryButton
import app.tourism.ui.common.buttons.SecondaryButton
import app.tourism.ui.common.nav.AppTopBar
import app.tourism.ui.common.special.CountryAsLabel
import app.tourism.ui.common.special.CountryFlag
import app.tourism.ui.common.ui_state.Loading
import app.tourism.ui.screens.main.ThemeViewModel
import app.tourism.ui.theme.TextStyles
import app.tourism.ui.theme.getBorderColor
import app.tourism.ui.utils.showToast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onPersonalDataClick: () -> Unit,
    onLanguageClick: () -> Unit,
    onSignOutComplete: () -> Unit,
    profileVM: ProfileViewModel,
    themeVM: ThemeViewModel,
) {
    val context = LocalContext.current
    val personalData = profileVM.profileDataResource.collectAsState().value
    val currencyRates = profileVM.currencyRates.collectAsState().value
    val signOutResponse = profileVM.signOutResponse.collectAsState().value

    ObserveAsEvents(flow = profileVM.uiEventsChannelFlow) { event ->
        when (event) {
            is UiEvent.NavigateToAuth -> onSignOutComplete()
            is UiEvent.ShowToast -> context.showToast(event.message)
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(id = R.string.profile_tourism),
            )
        },
        contentWindowInsets = Constants.USUAL_WINDOW_INSETS
    ) { paddingValues ->
        Column(
            Modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            VerticalSpace(height = 32.dp)
            if (personalData is Resource.Success) {
                personalData.data?.let {
                    ProfileBar(it)
                    VerticalSpace(height = 32.dp)
                }
            }
            if (currencyRates != null) {
                CurrencyRates(currencyRates = currencyRates)
                VerticalSpace(height = 20.dp)
            }
            GenericProfileItem(
                label = stringResource(R.string.personal_data),
                icon = R.drawable.profile,
                onClick = onPersonalDataClick
            )
            VerticalSpace(height = 20.dp)
            GenericProfileItem(
                label = stringResource(R.string.language),
                icon = R.drawable.globe,
                onClick = onLanguageClick
            )
            VerticalSpace(height = 20.dp)
            ThemeSwitch(themeVM = themeVM)
            VerticalSpace(height = 20.dp)
            val sheetState = rememberModalBottomSheetState()
            var isSheetOpen by rememberSaveable { mutableStateOf(false) }
            GenericProfileItem(
                label = stringResource(R.string.sign_out),
                icon = R.drawable.sign_out,
                isLoading = signOutResponse is Resource.Loading,
                onClick = { isSheetOpen = true }
            )

            if (isSheetOpen) {
                ModalBottomSheet(
                    containerColor = MaterialTheme.colorScheme.background,
                    sheetState = sheetState,
                    onDismissRequest = {
                        isSheetOpen = false
                    },
                ) {
                    SignOutWarning(
                        onSignOutClick = { profileVM.signOut() },
                        onCancelClick = { isSheetOpen = false },
                    )
                }
            }

            SpaceForNavBar()
        }
    }
}

@Composable
fun ProfileBar(personalData: PersonalData) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LoadImg(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape),
            url = personalData.pfpUrl
        )
        HorizontalSpace(width = 16.dp)
        Column {
            Text(text = personalData.fullName, style = TextStyles.h2)
            CountryAsLabel(
                Modifier.fillMaxWidth(),
                personalData.country,
                contentColor = MaterialTheme.colorScheme.onBackground.toArgb(),
            )
        }
    }
}

@Composable
fun CurrencyRates(modifier: Modifier = Modifier, currencyRates: CurrencyRates) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .applyAppBorder()
            .padding(horizontal = 15.dp, vertical = 18.dp)
            .then(modifier),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CurrencyRatesItem(
            countryCode = "US",
            value = "%.2f".format(currencyRates.usd),
        )
        CurrencyRatesItem(
            countryCode = "EU",
            value = "%.2f".format(currencyRates.eur),
        )
        CurrencyRatesItem(
            countryCode = "RU",
            value = "%.2f".format(currencyRates.rub),
        )
    }
}

@Composable
fun CurrencyRatesItem(countryCode: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (countryCode == "EU")
            Row {
                Image(
                    modifier = Modifier.height(21.dp),
                    painter = painterResource(id = R.drawable.eu_flag),
                    contentDescription = null
                )
                HorizontalSpace(width = 8.dp)
            }
        else
            CountryFlag(countryCodeName = countryCode)
        Text(text = value, style = TextStyles.b1.copy(fontWeight = FontWeight.SemiBold))
    }
}

@Composable
fun GenericProfileItem(
    modifier: Modifier = Modifier,
    label: String,
    @DrawableRes icon: Int,
    onClick: () -> Unit,
    isLoading: Boolean = false,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .applyAppBorder()
            .clickable { onClick() }
            .padding(horizontal = 15.dp, vertical = 20.dp)
            .then(modifier),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = TextStyles.h4)
        if (isLoading)
            Loading(Modifier.size(22.dp))
        else
            Icon(
                modifier = Modifier.size(22.dp),
                painter = painterResource(id = icon),
                tint = getBorderColor(),
                contentDescription = label,
            )
    }
}

@Composable
fun ThemeSwitch(modifier: Modifier = Modifier, themeVM: ThemeViewModel) {
    val isDark = themeVM.theme.collectAsState().value?.code == "dark"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .applyAppBorder()
            .padding(horizontal = 15.dp, vertical = 6.dp)
            .then(modifier),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = stringResource(id = R.string.dark_theme), style = TextStyles.h4)
        Switch(
            checked = isDark,
            onCheckedChange = { isDark ->
                val themeCode = if (isDark) "dark" else "light"
                themeVM.setTheme(themeCode)
                themeVM.updateThemeOnServer(themeCode)
            },
            colors = SwitchDefaults.colors(uncheckedTrackColor = MaterialTheme.colorScheme.background)
        )
    }
}

@Composable
fun SignOutWarning(
    modifier: Modifier = Modifier,
    onSignOutClick: () -> Unit,
    onCancelClick: () -> Unit,
) {
    Column(
        Modifier
            .padding(top = 0.dp, bottom = 48.dp, start = 32.dp, end = 32.dp)
            .then(modifier),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.sign_out_title),
            style = TextStyles.h3.copy(fontWeight = FontWeight.W700)
        )
        VerticalSpace(height = 24.dp)
        Text(
            text = stringResource(id = R.string.sign_out_warning),
            style = TextStyles.h3.copy(fontWeight = FontWeight.W500),
            textAlign = TextAlign.Center
        )
        VerticalSpace(height = 32.dp)
        Row {
            SecondaryButton(
                modifier = Modifier.weight(1f),
                label = stringResource(id = R.string.cancel),
                onClick = onCancelClick,
            )
            HorizontalSpace(width = 16.dp)
            PrimaryButton(
                modifier = Modifier.weight(1f),
                label = stringResource(id = R.string.sign_out),
                onClick = onSignOutClick,
            )
        }
    }
}
