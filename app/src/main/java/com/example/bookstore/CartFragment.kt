package com.example.bookstore


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.card_item.view.*
import kotlinx.android.synthetic.main.fragment_cart.*

class CartFragment : Fragment() {

    lateinit var username: String
    lateinit var password: String
    lateinit var handler: DatabaseHelper
    lateinit var navController: NavController
    private var adapter: RecyclerView.Adapter<RecyclerAdapter.ViewHolder>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        username = arguments!!.getString("username").toString()
        password = arguments!!.getString("password").toString()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_cart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        val container = activity!!.findViewById<FrameLayout>(R.id.container)
        val rootView = LayoutInflater.from(activity!!).inflate(R.layout.card_item, container, false)

        // Set Visibility of Button in CartFragment to GONE ERROR!
        rootView.btn_add.visibility = View.GONE // NOT FIXED

        val layoutManager = LinearLayoutManager(activity!!)
        recycler_cart_view.layoutManager = layoutManager

        handler = DatabaseHelper(activity!!)
        val (title, author, numPages, imageUrl, price) = handler.getBookData(username, password)

        adapter = RecyclerAdapter(
            activity!!,
            title,
            author,
            numPages,
            imageUrl,
            price,
            username,
            password,
            navController,
            R.layout.card_delete
        )
        recycler_cart_view.adapter = adapter
    }
}
