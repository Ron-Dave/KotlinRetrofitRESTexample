package codevinci.com.kotlinretrofitrestexample.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import codevinci.com.kotlinretrofitrestexample.R
import codevinci.com.kotlinretrofitrestexample.adapter.RecyclerViewAdapter
import codevinci.com.kotlinretrofitrestexample.githubpapi.ApiClient
import codevinci.com.kotlinretrofitrestexample.githubpapi.GithubInterface
import codevinci.com.kotlinretrofitrestexample.model.RecyclerViewItems
import codevinci.com.kotlinretrofitrestexample.model.User
import codevinci.com.kotlinretrofitrestexample.util.NetworkConection
import codevinci.com.kotlinretrofitrestexample.util.picasso
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_user_details.*
import kotlinx.android.synthetic.main.content_user_details.*
import kotlinx.android.synthetic.main.profile_view.*
import org.jetbrains.anko.design.snackbar
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserDetailsActivity : AppCompatActivity() {
    lateinit var mAdapter: RecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_details)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val accessToken: String = intent.getStringExtra("access_token")
                ?: ""//get access token from previous activity
        Log.d("accesstoken_test", accessToken)
        initViews()
        fabSearch.setOnClickListener { view->initViews() }
        btSearch.setOnClickListener { view ->
            val username: String = etUsername.text.toString()
            if (username.isNotEmpty()) {
                if (NetworkConection.isNetworkConnected(applicationContext)) {
                    getUserDetails(accessToken, username)
                } else {
                    snackbar(view, getString(R.string.error_internet))
                }
            } else {
                etUsername?.error = getString(R.string.error_enter_username)
            }
        }

    }

    @SuppressLint("RestrictedApi")
//hide profile view features and only show search bar
    fun initViews() {
        llSearchView.visibility = View.VISIBLE
        llProfileView.visibility = View.GONE
        fabSearch.visibility = View.GONE
        rvMoreDetails.layoutManager = LinearLayoutManager(this)
        rvMoreDetails.setHasFixedSize(true)
    }

    @SuppressLint("RestrictedApi")
    fun showResultsView() {
        llSearchView.visibility = View.GONE
        llProfileView.visibility = View.VISIBLE
        fabSearch.visibility = View.VISIBLE
    }

    fun getUserDetails(accessToken: String, username: String) {
        val dialog = indeterminateProgressDialog(message = "Please wait a bit…", title = "Fetching data")
        val githubServices = ApiClient.client.create(GithubInterface::class.java)
        val accessTokenWithBearer = "Bearer $accessToken"

        val call = githubServices.getUserDetails(accessTokenWithBearer, username)

        call.enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                dialog.dismiss()
                Log.d("fullresponse", response.toString())
                if (response.code() == 200) {
                    val user = response.body()
                    populateViews(user!!)
                } else {
                    toast(response.message())
                }

            }

            override fun onFailure(call: Call<User>?, t: Throwable?) {
                dialog.dismiss()
                toast(getString(R.string.error_server))
            }
        })
    }

    fun populateViews(user: User?) {
        showResultsView()
        picasso.load(user?.avatar_url).into(ivAvatar)
        tvFullName.text = user?.name
        tvUsername.text = user?.login
        tvLocation.text = user?.location?:"NA"
        tvFollowers.text = user?.followers.toString()
        tvRepos.text = user?.public_repos.toString()
        tvFollowing.text = user?.following.toString()

        //build a list of items to be showcased in the recycler view
        var email = user?.email?:"Unknown"
        var moreItemsList: ArrayList<RecyclerViewItems> = ArrayList()
        moreItemsList.add(RecyclerViewItems("Email: $email",null))
        moreItemsList.add(RecyclerViewItems("Subscriptions",user?.subscriptions_url))
        moreItemsList.add(RecyclerViewItems("Organizations",user?.organizations_url))
        moreItemsList.add(RecyclerViewItems("Received Events",user?.received_events_url))
        moreItemsList.add(RecyclerViewItems("Full Profile",user?.html_url))

        mAdapter = RecyclerViewAdapter(this,moreItemsList)
        rvMoreDetails.adapter = mAdapter

    }

}
