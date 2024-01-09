package com.example.statemachineandroid

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import ru.nsk.kstatemachine.DefaultState
import ru.nsk.kstatemachine.Event
import ru.nsk.kstatemachine.FinalState
import ru.nsk.kstatemachine.StateMachine
import ru.nsk.kstatemachine.addInitialState
import ru.nsk.kstatemachine.createStateMachine
import ru.nsk.kstatemachine.createStdLibStateMachine
import ru.nsk.kstatemachine.initialState
import ru.nsk.kstatemachine.onEntry
import ru.nsk.kstatemachine.onExit
import ru.nsk.kstatemachine.onTriggered
import ru.nsk.kstatemachine.transition
import ru.nsk.kstatemachine.visitors.exportToPlantUml


sealed class States : DefaultState() {
        object PreLoginState : States()
        object PostLoginState : States()
        object InGroupCallState : States()
        object SuspendedState : States()
        object InOneOnOneCallState : States()
        object AppOfflineState : States()
        object LoggedOutState : States(), FinalState
    }

    sealed class Events {
        object LoginSuccessEvent: Event
        object GroupCallStartEvent: Event
        object SuspendEvent: Event
        object JoinOneOnOneCallEvent: Event
        object TerminateOneOnOneCallEvent : Event
        object AppOfflineEvent : Event
        object AppOnlineEvent : Event
    }

class MainActivity : AppCompatActivity() {

    lateinit var machine: StateMachine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initStateMachine()
    }

    fun login(view: View) {
        runBlocking {
            machine.processEvent(Events.LoginSuccessEvent)
        }
    }

    fun moveToOneOnOneCall(view: View) {
        runBlocking {
            machine.processEvent(Events.JoinOneOnOneCallEvent)
        }
    }

    fun endOneOnOneCall(view: View) {
        runBlocking {
            machine.processEvent(Events.TerminateOneOnOneCallEvent)
        }
    }

    fun switchGroup(view: View) {
        runBlocking {
        }
    }

    fun exit(view: View) {

    }

    fun show(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun initStateMachine() {
        machine = createStdLibStateMachine("States") {
            addInitialState(States.PreLoginState) {
                onEntry {
                    Log.d("StateMachine", "In initial state")
                    show("In initial state")
                }

                transition<Events.LoginSuccessEvent> {
                    targetState = States.InGroupCallState
                    onTriggered {
                        Log.d("StateMachine", "Transitioning to group call state")
                        show("Transitioning to group call state")
                    }
                }
            }

            addState(States.InGroupCallState) {
                onEntry {
                    Log.d("StateMachine", "Entered group call state")
                    show("Entered group call state")
                }

                transition<Events.JoinOneOnOneCallEvent> {
                    targetState = States.InOneOnOneCallState
                    onTriggered {
                        Log.d("StateMachine", "transitioned to 1:1 call")
                        show("transitioned to 1:1 call")
                    }
                }

                onExit {
                    Log.d("StateMachine", "Exiting In group call state")
                    show("Exiting In group call state")
                }
            }

            addState(States.InOneOnOneCallState) {
                onEntry {
                    Log.d("StateMachine", "Entered 1:1 call state")
                    show("Entered 1:1 call state")
                }

                transition<Events.TerminateOneOnOneCallEvent>() {
                    targetState = States.InGroupCallState
                    onTriggered {
                        Log.d("StateMachine", "moved to group call")
                        show("moved to group call")
                    }
                }

                onExit {
                    Log.d("StateMahine", "Exiting 1:1 call")
                    show("Exiting 1:1 call")
                }
            }
        }
        runBlocking {
            println(machine.exportToPlantUml())
        }
    }
}