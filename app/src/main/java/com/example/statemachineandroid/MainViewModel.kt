package com.example.statemachineandroid

import androidx.lifecycle.ViewModel
import ru.nsk.kstatemachine.DefaultState
import ru.nsk.kstatemachine.Event
import ru.nsk.kstatemachine.FinalState

data class User(val id: Int, val name: String)
data class Group(val id: Int, val name: String)
enum class AppMode {
    ONLINE,
    OFFLINE
}

open class CallDetails(open val channelId: String, val micMuted: Boolean = true)
data class GroupCallDetails(
    val id: Int,
    val name: Int,
    override val channelId: String,
    val groupMembers: List<Int>,
    val isSuspended: Boolean,
    val joinedUsersCount: Int,
    val totalUsersCount: Int,
) : CallDetails(channelId)

data class OneOnOneCallDetails(
    val peerId: Int,
    val peerName: Int,
    override val channelId: String,
    val channelName: String
) : CallDetails(channelId)

data class IncomingCallDetails(val peerId: Int, val peerName: Int)

data class CallState(
    val incomingCallDetails: IncomingCallDetails,
    val currentCallDetails: CallDetails,
    val isRemoconModeActive: Boolean
)

open class AppStates(val activeStates: List<AppState> = listOf())
data class AppData(
    val userList: List<User>,
    val groupsList: List<Group>,
    val currentUser: User?,
    val appMode: AppMode = AppMode.ONLINE,
    val callState: CallState
) : AppStates()

sealed class AppState: DefaultState() {
    object LoginState : AppState()
    object LoginProgressState: AppState()
    object ChangePasswordState: AppState()
    object RequestPermissionState: AppState()
    object GroupCallState: AppState()
    object GroupCallSuspendedState: AppState()
    object OneOnOneCallState: AppState()
    object IncomingOneOnOneCallState: AppState()
    object AppInOfflineState: AppState()

    // Machine finishes when enters final state
    object LogoutState: AppState(), FinalState
    object AppExitState: AppState(), FinalState
}

sealed interface AppEvent: Event {
    object SubmitLoginDetailsEvent : AppEvent
    object LoginFailedEvent: AppEvent
    object LoginSuccessEvent: AppEvent
    object ChangePasswordEvent: AppEvent
    object ChangePassowordSuccessEvent: AppEvent
    object ChangePassowordFailedEvent: AppEvent
    object PermissionDeniedEvent: AppEvent
    object PermissionGrantedEvent: AppEvent
    object SwitchGroupEvent: AppEvent
    object StartOneOnOneCallEvent: AppEvent
    object EndOneOnOneCallEvent: AppEvent
    object MuteMicEvent: AppEvent
    object UnmuteMicEvent: AppEvent
    object OneOnOneCallNotificationReceivedEvent: AppEvent
    object RejectOneOnOneCallNotificationEvent: AppEvent
    object OneOnOneCallNotificationExpiredEvent: AppEvent
    object AcceptOneOnOneCallEvent: AppEvent
    object MakeAppOfflineEvent: AppEvent
    object MakeAppOnlineEvent: AppEvent
    object LogoutEvent: AppEvent
    object ExitAppEvent: AppEvent
}

class MainViewModel : ViewModel() {
}