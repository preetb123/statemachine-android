package com.example.statemachineandroid

import android.app.Application
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import ru.nsk.kstatemachine.ChildMode
import ru.nsk.kstatemachine.IState
import ru.nsk.kstatemachine.StateMachine
import ru.nsk.kstatemachine.addInitialState
import ru.nsk.kstatemachine.createStateMachine
import ru.nsk.kstatemachine.onEntry
import ru.nsk.kstatemachine.onExit
import ru.nsk.kstatemachine.onTriggered
import ru.nsk.kstatemachine.state
import ru.nsk.kstatemachine.transition
import ru.nsk.kstatemachine.transitionOn
import ru.nsk.kstatemachine.visitors.exportToPlantUml

class MyApplication : Application() {
    private val TAG = "MyApplication"
    lateinit var machine: StateMachine
    override fun onCreate() {
        super.onCreate()
        GlobalScope.launch {
            createMachineAndStart()
        }

    }

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    suspend fun createMachineAndStart() {
        machine = createStateMachine(applicationScope, "NysnoState", ChildMode.PARALLEL) {
            state("AppState") {
                addInitialState(AppState.LoginState) {
                    onEntry {
                        Log.d(TAG, "onEntry loginState")
                    }

                    transition<AppEvent.SubmitLoginDetailsEvent>("submit login details") {
                        targetState = AppState.LoginProgressState
                        onTriggered {
                            Log.d(TAG, "transitioning to login progress state")
                        }
                    }

                    onExit {
                        Log.d(TAG, "onExit loginState")
                    }
                }

                addState(AppState.ChangePasswordState) {
                    transitionOn<AppEvent.ChangePassowordSuccessEvent>("password changed") {
                        val permissonGranted = true
                        targetState = { if(permissonGranted) AppState.GroupCallState else AppState.RequestPermissionState }
                        onTriggered {
                            Log.d(TAG, "transitioning to ${targetState.javaClass.canonicalName.substringAfterLast(".")}")
                        }
                    }

                    transition<AppEvent.ChangePassowordFailedEvent>("failed changing password") {
                        targetState = AppState.ChangePasswordState
                        onTriggered {
                            Log.d(TAG, "transitioning to Change password screen")
                        }
                    }
                }

                addState(AppState.RequestPermissionState) {
                    transition<AppEvent.PermissionGrantedEvent>("permission grated") {
                        targetState = AppState.GroupCallState
                        onTriggered {
                            Log.d(TAG, "transitioning to group call state")
                        }
                    }

                    transition<AppEvent.PermissionDeniedEvent>("permission denied") {
                        targetState = AppState.AppExitState
                        onTriggered {
                            Log.d(TAG, "transitioning to exit state")
                        }
                    }
                }

                addState(AppState.GroupCallState) {
                    transition<AppEvent.SwitchGroupEvent>("switch group") {
                        onTriggered {
                            // we don't change the state but stay in the same state and change values
                            Log.d(TAG, "group switch request triggered")
                        }
                    }
                    transition<AppEvent.StartOneOnOneCallEvent>("start 1:1 call") {
                        targetState = AppState.OneOnOneCallState
                        onTriggered {
                            Log.d(TAG, "transition to 1:1 call manually")
                        }
                    }
                    transition<AppEvent.MakeAppOfflineEvent>("app offline") {
                        targetState = AppState.AppInOfflineState
                        onTriggered {
                            Log.d(TAG, "transition to offline state")
                        }
                    }
                    transition<AppEvent.ExitAppEvent>("exit app & terminate group call") {
                        targetState = AppState.AppExitState
                        onTriggered {
                            Log.d(TAG, "transition to exit state")
                        }
                    }
                }

                addState(AppState.OneOnOneCallState) {
                    transition<AppEvent.EndOneOnOneCallEvent>("end 1:1 call") {
                        targetState = AppState.GroupCallState
                        onTriggered {
                            Log.d(TAG, "transition to GroupCall state")
                        }
                    }

                    transition<AppEvent.AcceptOneOnOneCallEvent>() {
                        onTriggered {
                            Log.d(TAG, "accepting the incoming call")
                        }
                    }
                }

                addState(AppState.AppInOfflineState) {
                    transition<AppEvent.MakeAppOnlineEvent>("make app online") {
                        targetState = AppState.GroupCallState
                        onTriggered {
                            Log.d(TAG, "transition to GroupCall state")
                        }
                    }

                    transition<AppEvent.ExitAppEvent>("exit app") {
                        targetState = AppState.AppExitState
                        onTriggered {
                            Log.d(TAG, "transition to exit state")
                        }
                    }
                }

                addState(AppState.LoginProgressState) {
                    transitionOn<AppEvent.LoginSuccessEvent>("login success") {
                        val permissonGranted = true
                        targetState = { if(permissonGranted) AppState.GroupCallState else AppState.RequestPermissionState }
                        onTriggered {
                            Log.d(TAG, "transitioning to request permission state")
                        }
                    }
                    transition<AppEvent.LoginFailedEvent>("login failed") {
                        targetState = AppState.LoginState
                        onTriggered {
                            Log.d(TAG, "transitioning to login state, resetting the form")
                        }
                    }
                    transition<AppEvent.ChangePasswordEvent>("login success, please change password") {
                        targetState = AppState.ChangePasswordState
                        onTriggered {
                            Log.d(TAG, "transitioning to changePasswordState")
                        }
                    }
                }

                addState(AppState.AppExitState) {
                    onEntry {
                        Log.d(TAG, "exiting state machine")
                    }
                }
            }
        }

        var plantUmlDiagramString = machine.exportToPlantUml()
        plantUmlDiagramString = generatePlantUmlString(plantUmlDiagramString, machine.states)
        println(plantUmlDiagramString)
    }

    private fun generatePlantUmlString(plantUmlStr: String, states: Set<IState>): String {
        var str = plantUmlStr
        for (state in states) {
            if(state.states.isNotEmpty()){
                str = generatePlantUmlString(str, state.states)
            }else{
                val stateName = state.javaClass.canonicalName.substringAfterLast(".")
                val hashCode = state.hashCode()
                str = str.replace("State$hashCode", "$stateName")
            }
        }
        return str
    }
}