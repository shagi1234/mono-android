package com.mono.music.ui.utils

import android.app.Activity
import android.content.*
import android.content.Context.RECEIVER_NOT_EXPORTED
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status


@Composable
fun SmsRetreiverUserConsent(
    smsCodeLength: Int = SMS_CODE_LENGTH,
    onSMSReceived: (message: String, code: String) -> Unit
) {
    val context = LocalContext.current

    var shouldRegisterReceiver by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        Log.d(VERIFY_TAG,"Initializing Sms Retriever client")

        SmsRetriever.getClient(context)
            .startSmsUserConsent(null)
            .addOnSuccessListener {
                Log.d(VERIFY_TAG,"SmsRetriever started successfully")
                shouldRegisterReceiver = true
            }
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it?.resultCode == Activity.RESULT_OK && it.data != null) {
            val message: String? = it.data!!.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE)
            message?.let {
                Log.d(VERIFY_TAG,"Sms received: $message")
                val verificationCode = getVerificationCodeFromSms(message, smsCodeLength)
                Log.d(VERIFY_TAG,"Verification code parsed: $verificationCode")

                onSMSReceived(message, verificationCode)
            }
            shouldRegisterReceiver = false
        } else {
            Log.d(VERIFY_TAG,"Consent denied. User can type OTC manually.")
        }
    }

    if (shouldRegisterReceiver) {
        SystemBroadcastReceiver(
            systemAction = SmsRetriever.SMS_RETRIEVED_ACTION,
            broadCastPermission = SmsRetriever.SEND_PERMISSION,
        ) { intent ->
            if (intent != null && SmsRetriever.SMS_RETRIEVED_ACTION == intent.action) {
                val extras = intent.extras

                val smsRetrieverStatus = extras?.get(SmsRetriever.EXTRA_STATUS) as Status
                when (smsRetrieverStatus.statusCode) {
                    CommonStatusCodes.SUCCESS -> {
                        // Get consent intent
                        val consentIntent = extras.getParcelable<Intent>(SmsRetriever.EXTRA_CONSENT_INTENT)
                        try {
                            // Start activity to show consent dialog to user, activity must be started in
                            // 5 minutes, otherwise you'll receive another TIMEOUT intent
                            launcher.launch(consentIntent)
                        } catch (e: ActivityNotFoundException) {
                            Log.e(VERIFY_TAG, "Activity Not found for SMS consent API")
                        }
                    }

                    CommonStatusCodes.TIMEOUT -> Log.d(VERIFY_TAG,"Timeout in sms verification receiver")
                }
            }
        }
    }
}

internal fun getVerificationCodeFromSms(sms: String, smsCodeLength: Int): String =
    sms.filter { it.isDigit() }
        .substring(0 until smsCodeLength)

@Composable
fun SystemBroadcastReceiver(
    systemAction: String,
    broadCastPermission: String,
    onSystemEvent: (intent: Intent?) -> Unit
) {
    // Grab the current context in this part of the UI tree
    val context = LocalContext.current

    // Safely use the latest onSystemEvent lambda passed to the function
    val currentOnSystemEvent by rememberUpdatedState(onSystemEvent)

    // If either context or systemAction changes, unregister and register again
    DisposableEffect(context, systemAction) {
        val intentFilter = IntentFilter(systemAction)
        val broadcast = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                currentOnSystemEvent(intent)
            }
        }

        context.registerReceiver(broadcast, intentFilter, RECEIVER_NOT_EXPORTED)

        // When the effect leaves the Composition, remove the callback
        onDispose {
            context.unregisterReceiver(broadcast)
        }
    }

    DisposableEffect(context, broadCastPermission) {
        val intentFilter = IntentFilter(broadCastPermission)
        val broadcast = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                currentOnSystemEvent(intent)
            }
        }

        context.registerReceiver(broadcast, intentFilter)

        // When the effect leaves the Composition, remove the callback
        onDispose {
            context.unregisterReceiver(broadcast)
        }
    }
}

const val SMS_CODE_LENGTH = 6
const val VERIFY_TAG = "Verify Screen"