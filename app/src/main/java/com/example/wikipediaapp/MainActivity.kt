package com.example.wikipediaapp

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast

/**
 * MainActivity class of the application
 */
class MainActivity : AppCompatActivity() {

    // http request
    private var okHttpClient = OkHttpClient()
    var okHttpRequest = OkHttpRequest(okHttpClient)
    val GET_LANGUAGES_REQUEST = 1
    val GET_ARTICLES_REQUEST = 2

    // wikipedia api
    var requestOffset = 0
    var requestContinue = ""
    var searchPhrase = ""
    var lang = "en"

    // recyclerView
    lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var articleAdapter: ArticleAdapter
    private var articleList: ArrayList<Article> = ArrayList()
    var loadingMoreRecyclerView = false



    /**
     * onCreate method
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // if rotated, then get previous state
        if(savedInstanceState != null) {
            articleList = savedInstanceState.getParcelableArrayList("recyclerViewData")
            searchPhrase = savedInstanceState.getString("searchPhrase")
            requestOffset = savedInstanceState.getInt("requestOffset")
            requestContinue = savedInstanceState.getString("requestContinue")
            lang = savedInstanceState.getString("requestLanguage")
            tv_no_results.visibility = savedInstanceState.getInt("noResultsVisibility")
        }

        // init recyclerView layout
        linearLayoutManager = LinearLayoutManager(this)
        // init recyclerView adapter
        articleAdapter = ArticleAdapter(articleList)
        // set recyclerView components
        rv_articles.layoutManager = linearLayoutManager
        rv_articles.adapter = articleAdapter

        // load all languages to pick from
        getAllLanguages()

        // initializations of listeners
        initEditTextFocusListeners()
        initLoadMoreRecyclerViewListener()
        initQueryTextSubmitListener()
    }


    /**
     * onSaveInstanceState method to keep data on screen rotation
     *
     * @param outState  Bundle, where the current state is being saved
     */
    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // data to be saved
        outState.putParcelableArrayList("recyclerViewData", articleList)
        outState.putString("searchPhrase", searchPhrase)
        outState.putInt("requestOffset", requestOffset)
        outState.putString("requestContinue", requestContinue)
        outState.putString("requestLanguage", ss_language.selectedItem.toString())
        outState.putInt("noResultsVisibility", tv_no_results.visibility)
    }


    /**
     * onSaveInstanceState method to keep saved state, on screen rotation
     */
    private fun initLoadMoreRecyclerViewListener() {
        rv_articles.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!loadingMoreRecyclerView && linearLayoutManager.itemCount <= linearLayoutManager.findLastVisibleItemPosition() + 2) {

                    // load next data, if any
                    if (requestOffset != 0 && !requestContinue.isEmpty()) {
                        // set the flag, to determine that the data is currently loading
                        loadingMoreRecyclerView = true
                        // build new request url
                        val url = "https://${getPickedLanguage()}.wikipedia.org/w/api.php?action=query&list=search&srsearch=$searchPhrase&format=json&sroffset=$requestOffset&contiue=$requestContinue"
                        // call get request for new data from wikipedia
                        okHttpRequest.httpGet(url, getRequestCallback(GET_ARTICLES_REQUEST))
                    }

                }
            }
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
            }
        })
    }


    /**
     * Method to initialize the listener for the searchView keyboard search button click
     */
    private fun initQueryTextSubmitListener() {
        sv_article.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            // on text change
            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }

            // on text submit -> click the search button on keyboard
            override fun onQueryTextSubmit(query: String): Boolean {
                // call method to display search results
                getSearchResults()
                return false
            }

        })
    }


    /**
     * Method used to initialize focus listeners for searchView and recyclerView
     * this method calls method hideKeyboard() on lost focus of searchView field and on touch of the recyclerView
     */
    private fun initEditTextFocusListeners() {
        // if searchView article is not focused
        sv_article.setOnFocusChangeListener { view, hasFocus ->
            if(!hasFocus) {
                // hide keyboard
                hideKeyboard(view)
            }
        }
        // if recyclerView is touched
        rv_articles.setOnTouchListener { view, event ->
            // if touched
            when (event.action) {
            // hide keyboard
                MotionEvent.ACTION_DOWN -> hideKeyboard(view)
            }
            view.onTouchEvent(event)
        }
    }


    /**
     * On click method for searchView search button
     *
     * @param view  view that is being clicked
     */
    fun searchClicked(view: View) {
        // call method to display search results
        getSearchResults()
    }


    /**
     * Method that build and call get request from wikipedia, based on input search phrase
     */
    fun getSearchResults() {
        // get search phrase from searchView
        searchPhrase = sv_article.query.toString()
        // build new request url
        val url = "https://${getPickedLanguage()}.wikipedia.org/w/api.php?action=query&list=search&srsearch=$searchPhrase&format=json"
        // clear previous data
        articleAdapter.clearData()
        // call get request for new data from wikipedia
        okHttpRequest.httpGet(url, getRequestCallback(GET_ARTICLES_REQUEST))
        // remove focus
        sv_article.clearFocus()
    }



    /**
     * Method that returns picked language, and if the selection failed, then set "en" as default
     *
     * @return  picked language
     */
    fun getPickedLanguage() : String {
        // if language selection failed in some way, then default is "en"
        if (ss_language.selectedItem != null) {
            lang = ss_language.selectedItem.toString()
        }
        return lang
    }


    /**
     * Method that build and call get request from wikipedia to get all languages
     */
    private fun getAllLanguages() {
        // build request url
        val url = "https://commons.wikimedia.org/w/api.php?action=sitematrix&smtype=language&format=json"
        // call get request to get the languages
        okHttpRequest.httpGet(url, getRequestCallback(GET_LANGUAGES_REQUEST))
    }


    /**
     * Method that hides keyboard if the param view is not focused anymore
     *
     * @param view  view that is being determined
     */
    private fun hideKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }


    /**
     * Method that creates and returns the http get request callback.
     * The callback receives the response from http get request as JSON, parse it, and call method that populate the recyclerView
     * If there is any problem, inform the user
     *
     * @return  the http get request callback
     */
    private fun getRequestCallback(requestCode: Int) : Callback {
        return object: Callback {
            override fun onResponse(call: Call?, response: Response) {
                // get response data as string
                val responseData = response.body()?.string()
                // run on main thread -> display the results of search
                runOnUiThread{
                    try {
                        // parse JSON response data
                        var json = JSONObject(responseData)

                        // determine the request
                        when(requestCode) {
                            // if we are requesting the articles
                            GET_ARTICLES_REQUEST -> {
                                // if the continue property exists
                                if (json.has("continue")) {
                                    // then get required data for "load more" functionality
                                    requestOffset = json.getJSONObject("continue").getInt("sroffset")
                                    requestContinue = json.getJSONObject("continue").getString("continue")
                                }

                                // if there are any results
                                if (json.getJSONObject("query").optJSONArray("search").length() > 0) {
                                    // hide "No Results" textView
                                    tv_no_results.visibility = View.GONE
                                    // then load data and display them
                                    loadArticlesData(json.getJSONObject("query").optJSONArray("search"))
                                } else {
                                    // if there are no results, then display "No Results" textView
                                    tv_no_results.visibility = View.VISIBLE
                                }
                            }
                            // if we are requesting the languages
                            GET_LANGUAGES_REQUEST -> {
                                loadLanguagesData(json.getJSONObject("sitematrix"))
                            }
                        }
                    } catch (e: JSONException) {
                        Log.e("Json", e.message)
                    }
                }
            }

            override fun onFailure(call: Call?, e: IOException?) {
                // run on main thread -> inform the user
                runOnUiThread {
                    // if there is problem with the request
                    Toast.makeText(applicationContext, "Make sure that you are connected to network!", Toast.LENGTH_LONG).show()
                }
            }
        }
    }


    /**
     * Method that populate the recyclerView with parsed JSONArray data
     *
     * @param jsonArticles  data in JSON format that are being parsed and populated to recyclerView
     */
    private fun loadArticlesData(jsonArticles: JSONArray) {
        // create temporary list of articles
        val tmpList: ArrayList<Article> = ArrayList()
        // for each article
        for (i in 0..(jsonArticles.length() - 1)) {
            // get title
            val title = jsonArticles.getJSONObject(i).getString("title")
            // get snippet
            val snippet = jsonArticles.getJSONObject(i).getString("snippet")
            // populate temporary list of articles
            tmpList.add(Article(title, snippet))
        }
        // populate recyclerView with list of articles
        articleAdapter.addData(tmpList)
        // set flag for "load more" (indicates that we can load next 10 articles)
        loadingMoreRecyclerView = false
    }


    /**
     * Method that load languages -> populates the list used for picking the search language and set it to SearchableSpinner
     *
     * @param jsonLanguages  data in JSON format that are being parsed and populated for SearchableSpinner
     */
    private fun loadLanguagesData(jsonLanguages: JSONObject) {
        // create temporary list of languages
        val tmpList: ArrayList<String> = ArrayList()
        // for each article
        for (key in jsonLanguages.keys()) {
            if (key == "count")
                continue
            // get language
            val lang = jsonLanguages.getJSONObject(key).getString("code")
            // populate temporary list of articles
            tmpList.add(lang)
        }

        val adapter = ArrayAdapter<String>(this, R.layout.searchable_spinner_item, tmpList)
        // set up the SearchableSpinner
        ss_language.adapter = adapter
        ss_language.setSelection(adapter.getPosition(lang))
        ss_language.setTitle("Pick search language")
        ss_language.setPositiveButton("OK")
    }


}
