package com.example.wikipediaapp

import android.support.v4.text.HtmlCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.recyclerview_articles_item.view.*

/**
 * Class used to manage the recyclerView of articles
 */
class ArticleAdapter (var mData: ArrayList<Article>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // inflates the row layout from xml when needed
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_articles_item, parent, false)
        return ArticleViewHolder(view)
    }

    // binds the data to the TextView in each row
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val article = mData[position]
        // determine viewHolder
        val articleViewHolder = holder as ArticleViewHolder
        // bind the data
        articleViewHolder.onBind(article.title, article.snippet)
    }

    // total number of rows
    override fun getItemCount(): Int {
        return mData.size
    }

    /**
     * Method that add data to recyclerView and notifies it about changes
     *
     * @param List  list of articles
     */
    fun addData(articleList: List<Article>) {
        this.mData.addAll(articleList)
        notifyDataSetChanged()
    }

    /**
     * Method that clears all data in the recyclerView
     */
    fun clearData() {
        this.mData.clear()
        notifyDataSetChanged()
    }


    /**
     * Class that stores and recycles views as they are scrolled off screen
     */
    class ArticleViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView) {

        val tvTitle = itemView.tv_title
        val tvSnippet = itemView.tv_snippet

        /**
         * Method that binds the content -> title and snippet to current item view in recyclerView
         *
         * @param title     title of the current article
         * @param snippet   snippet of the current article
         */
        fun onBind(title: String, snippet: String) {
            tvTitle.text = title
            tvSnippet.text = HtmlCompat.fromHtml(snippet, HtmlCompat.FROM_HTML_MODE_LEGACY)
        }
    }
}