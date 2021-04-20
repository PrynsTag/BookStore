package com.example.bookstore


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_cart.*

class CartFragment : Fragment(), RecyclerAdapter.OnItemClickListener, View.OnClickListener {
    private lateinit var userBook: MutableList<BookInfo>

    private lateinit var adapter: RecyclerView.Adapter<RecyclerAdapter.ViewHolder>
    private lateinit var navController: NavController
    private lateinit var db: DatabaseHelper

    private val args: CartFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_cart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bundle = bundleOf("username" to args.username, "password" to args.password)
        navController = Navigation.findNavController(view)

        bottom_nav.setupWithNavController(navController)
        bottom_nav.setOnNavigationItemSelectedListener { item ->
            navController.navigate(item.itemId, bundle)
            true
        }

        db = DatabaseHelper(activity!!)
        userBook = db.getBookData(args.username, args.password)

        txv_totalAmount.text = userBook.sumBy { it.price }.toString() // Calculate Total Price

        adapter = initRecycler(activity!!, userBook, this, recycler_cart_view, R.layout.card_delete)

        ItemTouchHelper(object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                deleteItem(viewHolder.adapterPosition)
            }
        }).attachToRecyclerView(recycler_cart_view)

        view.findViewById<Button>(R.id.btn_pay).setOnClickListener(this)
    }

    override fun onItemClick(position: Int) {
        deleteItem(position)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.btn_pay -> {
                userBook.clear()
                db.checkOut(args.username, args.password).apply {
                    adapter.notifyItemRangeRemoved(0, this)
                }

                txv_totalAmount.text = "0"

                Snackbar.make(
                    activity!!.findViewById(android.R.id.content),
                    "Checkout success",
                    Snackbar.LENGTH_SHORT
                ).apply {
                    this.setAction("View Checkout") {
                        CartFragmentDirections.actionCartFragmentToCheckoutFragment().apply {
                            navController.navigate(this)
                        }
                        this.dismiss()
                    }.show()
                }
            }
        }
    }

    private fun deleteItem(position: Int) {
        val currentItem = userBook[position]
        val (id, title, _, _, _, _) = currentItem

        val numDeleted = db.deleteBookData(id, title, args.username, args.password)
        if (numDeleted > 0) {
            userBook.removeAt(position).apply {
                // Update Total Price
                txv_totalAmount.text =
                    (Integer.parseInt(txv_totalAmount.text.toString()) - this.price).toString()
            }
            adapter.notifyItemRemoved(position)

            Snackbar.make(
                activity!!.findViewById(android.R.id.content),
                "$title has been deleted to cart.",
                Snackbar.LENGTH_SHORT
            ).apply {
                this.setAction("Undo") {
                    insertItem(position, currentItem)
                }.show()
            }

        } else {
            Snackbar.make(
                activity!!.findViewById(android.R.id.content),
                "$title not deleted",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    private fun insertItem(position: Int, item: BookInfo) {
        userBook.add(position, item)
        db.insertBookData(item, args.username, args.password)
        adapter.notifyItemInserted(position)

        // Update Total Price
        txv_totalAmount.text =
            (Integer.parseInt(txv_totalAmount.text.toString()) + item.price).toString()

        Toast.makeText(
            activity,
            "${item.title} restored.",
            Toast.LENGTH_SHORT
        ).show()
    }
}
