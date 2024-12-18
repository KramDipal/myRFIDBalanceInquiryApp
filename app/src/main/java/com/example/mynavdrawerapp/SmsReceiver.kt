package com.example.mynavdrawerapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.SmsMessage
import android.widget.TextView


// This class is a BroadcastReceiver that listens for SMS messages sent to the device.
// When an SMS is received, it extracts the message body and checks if it contains the
// string "balance". If it does, it extracts the balance from the message body (assuming
// it is in the format "Balance: xxx") and updates the TextView in the MainActivity with
// the balance.
class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val bundle: Bundle? = intent.extras
        val pdus = bundle?.get("pdus") as Array<*>?
        if (pdus != null) {
            for (pdu in pdus) {
                val format = bundle?.getString("format")
                val message = SmsMessage.createFromPdu(pdu as ByteArray, format)
                val messageBody = message.messageBody

                // Assuming the message body contains the balance in the format "Balance: xxx"
                if (messageBody.contains("balance")) {
                    val balance = messageBody.substringAfter("balance").trim()
                    // Update the TextView with the balance
                    val textViewResponse = (context as MainActivity).findViewById<TextView>(R.id.textView4)
                    textViewResponse.text = "Balance: $balance"
                }
            }
        }
    }
}
