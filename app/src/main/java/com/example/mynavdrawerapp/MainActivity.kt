package com.example.mynavdrawerapp

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.SmsManager
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    lateinit var toggle: ActionBarDrawerToggle
    lateinit var drawerLayout : DrawerLayout

    lateinit var phoneNumbers: Array<String>
    lateinit var myImageView: ImageView
    lateinit var mySpinner: Spinner
    lateinit var myTextViewheader: TextView
    lateinit var myAvailablebalance: TextView
    lateinit var myTextViewMotoHeader: TextView
    lateinit var myTextViewCarHeader: TextView
    lateinit var myRadioGroupMoto: RadioGroup
    lateinit var myRadioGroupCar: RadioGroup
    lateinit var AutoSweepEasyTripProvideMessageMoto: String
    var isMoto = false //for Moto message content
    lateinit var AutoSweepEasyTripProvideMessageCar: String
    var isCar = false //for Car message content
    lateinit var myImageGcash: ImageView
    lateinit var myImageGoTyme: ImageView
    lateinit var myImageBDOPay: ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


//        Note that if you're using a Toolbar in your activity,
//        you should also set the android:theme attribute of your <application> element to
//        Theme.AppCompat.NoActionBar in your AndroidManifest.xml file.
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)


        // get the drawer layout
        drawerLayout = findViewById(R.id.drawerLayout)

        //load Spinner items
        mySpinner = findViewById(R.id.spinner)
        myImageView = findViewById(R.id.imageView)
        myTextViewheader = findViewById(R.id.textView)
        myTextViewMotoHeader = findViewById(R.id.textView2)
        myTextViewCarHeader = findViewById(R.id.textView3)
        myAvailablebalance = findViewById(R.id.textView4)
        myRadioGroupMoto = findViewById(R.id.radioGroup)
        myRadioGroupCar = findViewById(R.id.radioGroup2)
        myImageGcash = findViewById(R.id.imageGCash)
        myImageGoTyme = findViewById(R.id.imageGoTyme)
        myImageBDOPay = findViewById(R.id.imageBDOPay)


        //load items in Spinner
        loadItemToSpinner(this)

        //Load Radio groups
        radioGroupButtonMoto()
        radioGroupButtonCar()

        // Request SMS permissions
        ActivityCompat.requestPermissions( this, arrayOf(
            Manifest.permission.SEND_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS
        ),
            1
        )

        // Register the SMS receiver
        val smsReceiver = SmsReceiver()
        val intentFilter = IntentFilter("android.provider.Telephony.SMS_RECEIVED")
        registerReceiver(smsReceiver, intentFilter)

        myImageView.setOnClickListener {
            //Toast.makeText(this, "Image Clicked", Toast.LENGTH_SHORT).show()
            val selectedPhoneNumber = phoneNumbers[mySpinner.selectedItemPosition].trim().split("-")[0]


            //Select which message to send
//            var message: String = if(AutoSweepEasyTripProvideMessageMoto != null) {
//                AutoSweepEasyTripProvideMessageMoto
//            } else{
//                AutoSweepEasyTripProvideMessageCar
//            }



            //Toast.makeText(this, "$selectedPhoneNumber $AutoSweepEasyTripProvideMessageCar", Toast.LENGTH_SHORT).show()
            var message : String

            Log.i("Vehicle Type: ", "isMoto: $isMoto isCar: $isCar")


            if (isCar){
                message = AutoSweepEasyTripProvideMessageCar
            }else{
                message = AutoSweepEasyTripProvideMessageMoto
            }

            Log.i("Message: ", "$message")
            //Toast.makeText(this, "$message", Toast.LENGTH_LONG).show()

            try {
                sendSms(selectedPhoneNumber, message)
                myAvailablebalance.text = "Balance inquiry request sent."

            } catch (e: Exception) {
                Toast.makeText(this, "Error: ${e.message} Please select Vehicle Type, RFID and Provider Number", Toast.LENGTH_SHORT).show()
            }

        }

        myImageGcash.setOnClickListener {

            openApp("com.globe.gcash.android")
            Toast.makeText(this, "GCash Clicked", Toast.LENGTH_SHORT).show()
        }
        myImageBDOPay.setOnClickListener {
            Toast.makeText(this, "BDOPay service not available", Toast.LENGTH_SHORT).show()
        }
        myImageGoTyme.setOnClickListener {
            Toast.makeText(this, "GoTyme service not available", Toast.LENGTH_SHORT).show()
        }

        // tell the ActionBar to show the hamburger icon
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // tell the ActionBar to show the home icon
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // create the toggle for the drawer
        toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            R.string.open,
            R.string.close
        )

        // add the toggle to the drawer
        drawerLayout.addDrawerListener(toggle)

        // set the state of the toggle to match the state of the drawer
        toggle.syncState()

        val navView: NavigationView = findViewById(R.id.nav_view)

        navView.setNavigationItemSelectedListener {
                it.isChecked = true
            when(it.itemId){
                R.id.home -> {
                    setMainContentVisibility(View.VISIBLE)
                    // Remove any existing fragments from the fragment container
                     replaceFragment(HomeFragment(), it.title.toString())
                    //return@setNavigationItemSelectedListener null == true
                    //null
                }
                R.id.message -> {

                    //make Visibility of Main Content GONE
                    setMainContentVisibility(View.GONE)
                    replaceFragment(MessageFragment(), it.title.toString())
                }
                R.id.settings -> {

                    //make Visibility of Main Content GONE
                    setMainContentVisibility(View.GONE)
                    replaceFragment(SettingsFragment(), it.title.toString())
                }
                R.id.login -> {

                    //make Visibility of Main Content GONE
                    setMainContentVisibility(View.GONE)
                    replaceFragment(LoginFragment(), it.title.toString())
                }
            }
            true

        }
    }

    // This function replaces the current fragment with a new one and updates the title
    private fun replaceFragment(fragment: Fragment, title: String) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        if(fragment != null) {
            //Toast.makeText(this, "NOT NUll Home", Toast.LENGTH_SHORT).show()

            fragmentTransaction.replace(R.id.frame_layout_1, fragment)
            fragmentTransaction.commit()
        }else{
            // If fragment is null, it means we're back to home, so remove any current fragment
            Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show()
            //supportFragmentManager.popBackStackImmediate()
        }

        drawerLayout.closeDrawers()
        setTitle(title) // Update the activity title with the new fragment's title
    }

    // Handle the selection of items in the options menu
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        // Check if the toggle handles the selection of the item
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        // If not, pass it to the superclass to handle it
        return super.onOptionsItemSelected(item)
    }

    private fun loadItemToSpinner(context: Context){
        phoneNumbers = arrayOf("Press to select", "09178608655-AUTOSWEEP", "09191601553-EASYTRIP")

        mySpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            phoneNumbers)

        mySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                (view as TextView).textSize = 20f // increase text size to 20sp

                //Toast.makeText(context, "Selected item: ${phoneNumbers[position]}", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }
    }

    fun radioGroupButtonMoto() {
        val radioGroup = findViewById<RadioGroup>(R.id.radioGroup)

        //AutoSweepEasyTripProvideMessageCar = null.toString()

        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            isCar = false
            
            when (checkedId) {
                R.id.radioButton -> {

                    AutoSweepEasyTripProvideMessageMoto = "AUTOSWEEP BALINQ I264933"
                    isMoto = true
                    //Card no: I264933
                    //Phone Number: 09178608655
                    //Toast.makeText(this, "AutoSweep MotorCycle was selected", Toast.LENGTH_SHORT).show()
                }

                R.id.radioButton2 -> {
                    AutoSweepEasyTripProvideMessageMoto = "BAL 540000910840"
                    isMoto = true
                    //Card no: 540000910840
                    //Phone Number: 09191601553
                    //Toast.makeText(this, "EasyTrip Motorcycle was selected", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun radioGroupButtonCar() {
        val radioGroup = findViewById<RadioGroup>(R.id.radioGroup2)

        //AutoSweepEasyTripProvideMessageMoto = null.toString()

        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            isMoto = false

            when (checkedId) {
                R.id.radioButton3 -> {

                    AutoSweepEasyTripProvideMessageCar = "AUTOSWEEP BALINQ R377212"
                    isCar = true

                    //Toast.makeText(this, "AutoSweep for CAR was selected with $AutoSweepEasyTripProvideMessageCar AutoSweepEasyTripProvideMessageMoto: $AutoSweepEasyTripProvideMessageMoto", Toast.LENGTH_SHORT).show()
                }

                R.id.radioButton4-> {
                    AutoSweepEasyTripProvideMessageCar = "BAL 540001908575"
                    isCar = true
                    //Toast.makeText(this, "EasyTrip for CAR was selected with $AutoSweepEasyTripProvideMessageCar AutoSweepEasyTripProvideMessageMoto: $AutoSweepEasyTripProvideMessageMoto", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun sendSms(phoneNumber: String, message: String) {
        // Get the default SMS manager
        val smsManager = SmsManager.getDefault()

        // Create intents for when the message is sent and when it is delivered
        val sentIntent = PendingIntent.getBroadcast(
            this,
            0,
            Intent("SMS_SENT"),
            PendingIntent.FLAG_IMMUTABLE
        )

        val deliveredIntent = PendingIntent.getBroadcast(
            this,
            0,
            Intent("SMS_DELIVERED"),
            PendingIntent.FLAG_IMMUTABLE
        )

        // Show a toast message indicating that the SMS message is being sent
        //Toast.makeText(this, "$message", Toast.LENGTH_LONG).show()

        // Send the SMS message using the SMS manager
        smsManager.sendTextMessage(phoneNumber, null, message, sentIntent, deliveredIntent)

        // Show a toast message indicating that the SMS message has been sent
        Toast.makeText(this, "SMS sent", Toast.LENGTH_SHORT).show()

        resetVehicleCondition()
    }

    private fun openApp(packageName: String)
    {

        //Toast.makeText(this, "packageName: $packageName", Toast.LENGTH_SHORT).show()
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        if (intent != null)
        {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
        else
        {
            if(packageName.equals("com.globe.gcash.android"))
                Toast.makeText(this, "GCash app is not installed", Toast.LENGTH_SHORT).show()
            else if(packageName.equals("ph.com.gotyme.android"))
                Toast.makeText(this, "Gotyme app is not installed", Toast.LENGTH_SHORT).show()
            else if (packageName.equals("com.unionbankph.online.android"))
                Toast.makeText(this, "BDOnline app is not installed", Toast.LENGTH_SHORT).show()

            //Optionally, you can direct the user to the Play Store to install the app
            val playStoreIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
            startActivity(playStoreIntent)
        }

    }

    private fun setMainContentVisibility(visibility: Int)
    {
        myImageView.visibility = visibility
        mySpinner.visibility = visibility
        myAvailablebalance.visibility = visibility
        myTextViewheader.visibility = visibility
        myRadioGroupCar.visibility = visibility
        myRadioGroupMoto.visibility = visibility
        myTextViewCarHeader.visibility = visibility
        myTextViewMotoHeader.visibility = visibility
        myImageGcash.visibility = visibility
        myImageGoTyme.visibility = visibility
        myImageBDOPay.visibility = visibility
    }
    fun resetVehicleCondition(){
        AutoSweepEasyTripProvideMessageMoto = null.toString()
        AutoSweepEasyTripProvideMessageCar = null.toString()
        isMoto = false
        isCar = false

        myRadioGroupMoto.clearCheck()
        myRadioGroupCar.clearCheck()

        mySpinner.setSelection(0)

        myAvailablebalance.text = "0.00"
    }
}