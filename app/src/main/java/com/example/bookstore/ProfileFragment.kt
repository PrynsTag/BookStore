package com.example.bookstore

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.setupWithNavController
import kotlinx.android.synthetic.main.fragment_profile.*

class ProfileFragment : Fragment(), View.OnClickListener {
    private lateinit var db: DatabaseHelper
    private lateinit var userData: UserData
    private lateinit var navController: NavController

    private val args: ProfileFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        db = DatabaseHelper(activity!!)
        userData = db.getUserData(args.username, args.password)
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        inp_name.text = Editable.Factory.getInstance().newEditable(userData.name)
        inp_username.text = Editable.Factory.getInstance().newEditable(userData.username)
        inp_email.text = Editable.Factory.getInstance().newEditable(userData.email)

        bottom_nav.setupWithNavController(navController)
        bottom_nav.setOnNavigationItemSelectedListener { item ->
            val bundle = bundleOf("username" to args.username, "password" to args.password)
            navController.navigate(item.itemId, bundle)
            true
        }

        view.findViewById<Button>(R.id.btn_update).setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.btn_update -> {
                val updatedData = db.updateUserData(
                    inp_name.text.toString(),
                    inp_username.text.toString(),
                    inp_email.text.toString(),
                    args.username,
                    args.password
                )
                if (updatedData) {
                    Toast.makeText(
                        activity,
                        "user ${userData.username} has been updated.",
                        Toast.LENGTH_SHORT
                    ).show()
                    ProfileFragmentDirections.actionProfileFragmentToLoginFragment().apply {
                        navController.navigate(this)
                    }
                } else {
                    Toast.makeText(
                        activity,
                        "user ${userData.username} has NOT been updated.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                //  Hide keyword when button clicked
                val inputManager =
                    context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputManager.hideSoftInputFromWindow(v.windowToken, 0)
            }
        }
    }
}