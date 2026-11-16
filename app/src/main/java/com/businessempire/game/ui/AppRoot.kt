package com.businessempire.game.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.businessempire.game.BuildConfig
import com.businessempire.game.R
import com.businessempire.game.domain.Economy
import com.businessempire.game.domain.GameState
import com.businessempire.game.domain.NumberFormatter
import com.businessempire.game.domain.OfflineReport
import com.businessempire.game.domain.TutorialEngine
import com.businessempire.game.domain.content.Achievements
import com.businessempire.game.domain.content.MilestoneKind
import com.businessempire.game.domain.content.TutorialStep
import com.businessempire.game.ui.components.CashHeader
import com.businessempire.game.ui.components.EventDialog
import com.businessempire.game.ui.components.FeedbackSettings
import com.businessempire.game.ui.components.LocalFeedbackSettings
import com.businessempire.game.ui.components.LocalSoundPlayer
import com.businessempire.game.ui.components.SoundPlayer
import com.businessempire.game.ui.components.rememberGameFeedback
import com.businessempire.game.ui.components.TutorialBanner
import com.businessempire.game.ui.screens.AboutScreen
import com.businessempire.game.ui.screens.AchievementsScreen
import com.businessempire.game.ui.screens.AssetsScreen
import com.businessempire.game.ui.screens.BankScreen
import com.businessempire.game.ui.screens.BusinessScreen
import com.businessempire.game.ui.screens.CheatsScreen
import com.businessempire.game.ui.screens.CreditsScreen
import com.businessempire.game.ui.screens.MyEmpireScreen
import com.businessempire.game.ui.screens.InvestScreen
import com.businessempire.game.ui.screens.OnboardingScreen
import com.businessempire.game.ui.screens.PrestigeScreen
import com.businessempire.game.ui.screens.ProfileDestination
import com.businessempire.game.ui.screens.ProfileScreen
import com.businessempire.game.ui.screens.RichListScreen
import com.businessempire.game.ui.screens.SettingsScreen
import com.businessempire.game.ui.screens.ShopScreen
import com.businessempire.game.ui.screens.StatisticsScreen
import com.businessempire.game.ui.screens.StockDetailScreen

@Composable
fun AppRoot(vm: GameViewModel = viewModel(factory = GameViewModel.Factory)) {
    val state by vm.state.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_START) { vm.onAppStart() }
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { vm.onAppStop() }

    val soundPlayer = remember { SoundPlayer() }
    DisposableEffect(soundPlayer) { onDispose { soundPlayer.release() } }

    val current = state
    if (current == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }
    CompositionLocalProvider(
        LocalNumberStyle provides current.settings.numberStyle,
        LocalFeedbackSettings provides FeedbackSettings(current.settings.haptics, current.settings.sound),
        LocalSoundPlayer provides soundPlayer,
    ) {
        if (!current.profile.isSet) {
            OnboardingScreen(onStart = vm::setProfile)
        } else {
            GameScaffold(current, vm)
        }
    }
}

@Composable
private fun GameScaffold(state: GameState, vm: GameViewModel) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val destination = backStack?.destination
    val buyMode by vm.buyMode.collectAsStateWithLifecycle()
    val offline by vm.offlineReport.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val economy = remember(state) { Economy(state) }
    val inInvest = destination?.hierarchy?.any { it.hasRoute(InvestGraph::class) } == true

    NoticeEffect(vm, snackbar)
    LaunchedEffect(inInvest) { if (inInvest) vm.onInvestOpened() }

    Scaffold(
        topBar = { CashHeader(state.cash, economy.totalPassiveIncomePerSecond, economy.netWorth) },
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = { BottomBar(navController, destination, showProfileBadge = vm.canClaimDaily(state)) },
    ) { padding ->
        Column(Modifier.padding(padding)) {
            val step = TutorialEngine.step(state)
            AnimatedVisibility(visible = step != TutorialStep.DONE) {
                TutorialBanner(step, onSkip = vm::skipTutorial)
            }
            GameNavHost(navController, state, vm, buyMode)
        }
    }

    val event = state.event.pending
    when {
        offline != null -> WelcomeBackDialog(offline!!, vm::dismissOfflineReport)
        event != null -> EventDialog(state, event, vm::resolveEvent)
    }
}

@Composable
private fun BottomBar(navController: NavHostController, destination: androidx.navigation.NavDestination?, showProfileBadge: Boolean) {
    NavigationBar {
        TopTab.entries.forEach { tab ->
            val selected = destination?.hierarchy?.any { it.hasRoute(tab.route::class) } == true
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(tab.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    if (tab == TopTab.PROFILE && showProfileBadge) {
                        val badgeLabel = stringResource(R.string.cd_daily_ready)
                        BadgedBox(badge = { Badge() }) { Icon(tab.icon, contentDescription = badgeLabel) }
                    } else {
                        Icon(tab.icon, contentDescription = null)
                    }
                },
                label = { Text(stringResource(tab.label)) },
            )
        }
    }
}

@Composable
private fun GameNavHost(navController: NavHostController, state: GameState, vm: GameViewModel, buyMode: com.businessempire.game.domain.BuyMode) {
    val back: () -> Unit = { navController.popBackStack() }
    NavHost(navController = navController, startDestination = BusinessRoute) {
        composable<BusinessRoute> { BusinessScreen(state, buyMode, vm) }
        navigation<InvestGraph>(startDestination = InvestRoute) {
            composable<InvestRoute> {
                InvestScreen(state, vm, onOpenStock = { navController.navigate(StockDetailRoute(it)) })
            }
            composable<StockDetailRoute> { entry ->
                StockDetailScreen(state, entry.toRoute<StockDetailRoute>().stockId, vm, onBack = back)
            }
        }
        composable<BankRoute> { BankScreen(state, vm) }
        navigation<AssetsGraph>(startDestination = AssetsRoute) {
            composable<AssetsRoute> { AssetsScreen(state, vm, onOpenEmpire = { navController.navigate(MyEmpireRoute) }) }
            composable<MyEmpireRoute> { MyEmpireScreen(state, back) }
        }
        navigation<ProfileGraph>(startDestination = ProfileRoute) {
            composable<ProfileRoute> {
                ProfileScreen(state, vm) { dest ->
                    navController.navigate(
                        when (dest) {
                            ProfileDestination.RICH_LIST -> RichListRoute
                            ProfileDestination.ACHIEVEMENTS -> AchievementsRoute
                            ProfileDestination.STATISTICS -> StatisticsRoute
                            ProfileDestination.SETTINGS -> SettingsRoute
                            ProfileDestination.PRESTIGE -> PrestigeRoute
                            ProfileDestination.SHOP -> ShopRoute
                            ProfileDestination.CHEATS -> CheatsRoute
                        },
                    )
                }
            }
            composable<RichListRoute> {
                val richList by vm.richList.collectAsStateWithLifecycle()
                RichListScreen(state, richList, vm.leaderboard, back)
            }
            composable<AchievementsRoute> { AchievementsScreen(state, back) }
            composable<StatisticsRoute> { StatisticsScreen(state, back) }
            composable<SettingsRoute> { SettingsScreen(state, vm, back, onAbout = { navController.navigate(AboutRoute) }) }
            composable<AboutRoute> {
                val richList by vm.richList.collectAsStateWithLifecycle()
                AboutScreen(richList, back, onCredits = { navController.navigate(CreditsRoute) })
            }
            composable<CreditsRoute> { CreditsScreen(back) }
            composable<PrestigeRoute> { PrestigeScreen(state, vm, back) }
            // MONETIZATION: Shop is only reachable when BuildConfig.STORE_ENABLED is true.
            if (BuildConfig.STORE_ENABLED) composable<ShopRoute> { ShopScreen(back) }
            if (BuildConfig.DEBUG) composable<CheatsRoute> { CheatsScreen(state, vm, back) }
        }
    }
}

@Composable
private fun NoticeEffect(vm: GameViewModel, snackbar: SnackbarHostState) {
    val context = LocalResources.current
    val style = LocalNumberStyle.current
    val feedback = rememberGameFeedback()
    LaunchedEffect(vm, style, feedback) {
        vm.notices.collect { notice ->
            if (notice is Notice.AchievementUnlocked || notice is Notice.Prestiged || notice is Notice.DailyClaimed ||
                notice is Notice.PassedPerson || notice is Notice.WealthMilestoneReached
            ) feedback.celebrate()
            fun m(v: Double) = context.getString(R.string.money, NumberFormatter.format(v, style))
            val text = when (notice) {
                is Notice.MilestoneReached -> {
                    val name = context.getString(ContentText.businessName(notice.businessId))
                    val factor = NumberFormatter.short(notice.milestone.factor)
                    val bonus = when (notice.milestone.kind) {
                        MilestoneKind.SPEED -> context.getString(R.string.milestone_speed, factor)
                        MilestoneKind.REVENUE -> context.getString(R.string.milestone_revenue, factor)
                    }
                    context.getString(R.string.notice_milestone, name, notice.milestone.owned, bonus)
                }
                is Notice.StockNews -> {
                    val name = context.getString(InvestText.stockName(notice.item.stockId))
                    val pct = NumberFormatter.short(Math.round(kotlin.math.abs(notice.item.change * 100)).toDouble())
                    context.getString(
                        if (notice.item.change >= 0) R.string.notice_news_up else R.string.notice_news_down,
                        context.getString(InvestText.headline(notice.item.headline), name),
                        pct,
                    )
                }
                is Notice.AchievementUnlocked -> {
                    val def = Achievements.byId[notice.id] ?: return@collect
                    context.getString(
                        R.string.notice_achievement,
                        MetaText.achievementDescription(context, def, style),
                        MetaText.achievementReward(context, def),
                    )
                }
                is Notice.EventResolved -> MetaText.outcome(context, notice.outcome, notice.amount, style)
                is Notice.DailyClaimed -> context.getString(R.string.notice_daily, m(notice.amount), notice.streak)
                is Notice.Prestiged -> context.getString(R.string.notice_prestige, NumberFormatter.short(notice.points))
                is Notice.PassedPerson -> buildString {
                    append(context.getString(R.string.notice_passed, notice.person.name))
                    if (notice.others > 0) append(" ").append(context.getQuantityString(R.plurals.notice_passed_others, notice.others, notice.others))
                }
                is Notice.WealthMilestoneReached -> buildString {
                    append(context.getString(R.string.notice_wealth_milestone, context.getString(RichText.milestoneName(notice.milestone.id))))
                    notice.milestone.richerThanPercent?.let {
                        append(" ").append(context.getString(R.string.rich_list_richer_than, NumberFormatter.short(it)))
                    }
                }
            }
            snackbar.currentSnackbarData?.dismiss()
            snackbar.showSnackbar(text)
        }
    }
}

@Composable
private fun WelcomeBackDialog(report: OfflineReport, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.welcome_back_title)) },
        text = {
            Column {
                Text(stringResource(R.string.welcome_back_body, duration(report.elapsedSeconds), money(report.earned)))
                if (report.countedSeconds < report.elapsedSeconds) {
                    Text(
                        stringResource(R.string.welcome_back_capped, duration(report.capHours * 3600.0)),
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.collect)) } },
    )
}
