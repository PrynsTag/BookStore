package com.example.bookstore

import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.setupWithNavController
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_book_store.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class BookStoreFragment : Fragment(), RecyclerAdapter.OnItemClickListener {
    private val bookList = mutableListOf<BookInfo>()

    private lateinit var navController: NavController
    private lateinit var bundle: Bundle

    private val args: BookStoreFragmentArgs by navArgs()

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.nav_cart, menu)
        return super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.cart -> {
                BookStoreFragmentDirections.actionBookStoreFragmentToCartFragment(
                    args.username,
                    args.password
                ).apply {
                    navController.navigate(this)
                }
            }
            R.id.acb_btnLogout -> {
                BookStoreFragmentDirections.actionBookStoreFragmentToLoginFragment().apply {
                    navController.navigate(this)
                }
            }
            R.id.acb_profile -> {
                BookStoreFragmentDirections.actionBookStoreFragmentToProfileFragment(
                    args.username,
                    args.password
                ).apply {
                    navController.navigate(this)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        bundle = bundleOf("username" to args.username, "password" to args.password)
        return inflater.inflate(R.layout.fragment_book_store, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        navController = findNavController(view)

        bottom_nav.setupWithNavController(navController)
        bottom_nav.setOnNavigationItemSelectedListener { item ->
            navController.navigate(item.itemId, bundle)
            true
        }

        getBookData()
    }

    override fun onItemClick(position: Int) {
        val currentItem = bookList[position]
        val (id, title, author, page, image, price) = currentItem
        val db = DatabaseHelper(context!!)
        val insertedBook =
            db.insertBookData(
                BookInfo(id, title, author, page, image, price),
                args.username,
                args.password
            )

        if (insertedBook) {
            Snackbar.make(
                activity!!.findViewById(android.R.id.content),
                "$title has been added to cart.",
                Snackbar.LENGTH_LONG
            ).apply {
                this.setAction("View Cart") {
                    BookStoreFragmentDirections.actionBookStoreFragmentToCartFragment(
                        args.username,
                        args.password
                    ).apply {
                        navController.navigate(this)
                    }

                    this.dismiss()
                }.show()
            }

        } else {
            Snackbar.make(
                activity!!.findViewById(android.R.id.content),
                "$title not added",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    private fun getBookData() {
        val queue = Volley.newRequestQueue(activity)
        val url =
            "https://raw.githubusercontent.com/CWGGa/Book_API/main/book_api.json?token=ANOGHMZIP4S5GQF2DAS2UFLAPRWKE"

        val stringRequest = StringRequest(
            Request.Method.GET, url, { response ->
                try {
                    val jsonObject = JSONObject(response.toString())
                    val arrItems: JSONArray = jsonObject.getJSONArray("book")

                    for (i in 0 until arrItems.length()) {
                        val item: JSONObject = arrItems.getJSONObject(i)
                        bookList.add(
                            BookInfo(
                                0,
                                item.getString("title"),
                                item.getJSONArray("authors")[0] as String,
                                item.getString("pageCount"),
                                item.getString("thumbnailUrl"),
                                (100..999).random()
                            )
                        )
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                initRecycler(activity!!, bookList, this, book_store_rv, R.layout.card_item)
            }, {
                repeat(5) {
                    bookList.add(
                        BookInfo(
                            0,
                            "That didn't work!",
                            "That didn't work!",
                            "That didn't work!",
                            "https://homepages.cae.wisc.edu/~ece533/images/cat.png",
                            0
                        )
                    )
                }
            })

        queue.add(stringRequest)
    }
}

