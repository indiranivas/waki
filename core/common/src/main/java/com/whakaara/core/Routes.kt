package com.whakaara.core

sealed class RootScreen(val route: String) {
    data object Alarm : RootScreen("alarm_root")
    data object Stopwatch : RootScreen("stopwatch_root")

    data object Timer : RootScreen("timer_root")

    data object Onboarding : RootScreen("onboarding_root")

    data object SmartAlarm : RootScreen("smart_alarm_root")

    // TODO: settings feature module?
    data object Settings : RootScreen("settings_root")
}

sealed class LeafScreen(val route: String) {
    data object Alarm : LeafScreen("alarm")
    data object Stopwatch : LeafScreen("stopwatch")
    data object Timer : LeafScreen("timer")

    data object Onboarding : LeafScreen("onboarding")

    data object SmartAlarm : LeafScreen("smart_alarm")
    data object SearchLocation : LeafScreen("search_location")
    data object SearchResults : LeafScreen("search_results")
    data object PickOnMap : LeafScreen("pick_on_map")
    data object TriggerChoice : LeafScreen("trigger_choice")
    data object LocationSetup : LeafScreen("location_setup")
    data object ArrivalSetup : LeafScreen("arrival_setup")
    data object DepartureSetup : LeafScreen("departure_setup")

    data object Settings : LeafScreen("settings")
}
