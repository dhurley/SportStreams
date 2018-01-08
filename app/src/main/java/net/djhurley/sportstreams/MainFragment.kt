/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package net.djhurley.sportstreams

import java.util.Timer
import java.util.TimerTask

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.support.v17.leanback.app.BackgroundManager
import android.support.v17.leanback.app.BrowseFragment
import android.support.v17.leanback.widget.ArrayObjectAdapter
import android.support.v17.leanback.widget.HeaderItem
import android.support.v17.leanback.widget.ImageCardView
import android.support.v17.leanback.widget.ListRow
import android.support.v17.leanback.widget.ListRowPresenter
import android.support.v17.leanback.widget.OnItemViewClickedListener
import android.support.v17.leanback.widget.OnItemViewSelectedListener
import android.support.v17.leanback.widget.Presenter
import android.support.v17.leanback.widget.Row
import android.support.v17.leanback.widget.RowPresenter
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.content.ContextCompat
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast

import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget
import net.djhurley.sportstreams.activities.StreamDetailsActivity
import net.djhurley.sportstreams.models.CardRow
import net.djhurley.sportstreams.models.Stream
import net.djhurley.sportstreams.presenters.CardPresenter
import net.djhurley.sportstreams.services.RedditService

class MainFragment : BrowseFragment() {

    private val mHandler = Handler()
    private lateinit var mRowsAdapter: ArrayObjectAdapter
    private lateinit var mBackgroundManager: BackgroundManager
    private var mDefaultBackground: Drawable? = null
    private lateinit var mMetrics: DisplayMetrics
    private var mBackgroundTimer: Timer? = null
    private var mBackgroundUri: String? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        Log.i(TAG, "onCreate")
        super.onActivityCreated(savedInstanceState)

        prepareBackgroundManager()

        setupUIElements()

        loadRows()

        setupEventListeners()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: " + mBackgroundTimer?.toString())
        mBackgroundTimer?.cancel()
    }

    private fun prepareBackgroundManager() {

        mBackgroundManager = BackgroundManager.getInstance(activity)
        mBackgroundManager.attach(activity.window)
        mDefaultBackground = ContextCompat.getDrawable(activity, R.drawable.default_background)
        mMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(mMetrics)
    }

    private fun setupUIElements() {
        title = getString(R.string.browse_title)
        // over title
        headersState = BrowseFragment.HEADERS_ENABLED
        isHeadersTransitionOnBackEnabled = true

        // set fastLane (or headers) background color
        brandColor = ContextCompat.getColor(activity, R.color.fastlane_background)
        // set search icon color
        searchAffordanceColor = ContextCompat.getColor(activity, R.color.search_opaque)
    }

    private fun loadRows() {
        val streamsTask = StreamsTask()
        streamsTask.execute()
    }

    private inner class StreamsTask : AsyncTask<String, Void, List<CardRow>>() {

        override fun doInBackground(params: Array<String>): List<CardRow> {
            val cardRows = ArrayList<CardRow>()

            val soccerStreams = RedditService().getSoccerStreams()
            cardRows.add(CardRow("Soccer", soccerStreams))

            return cardRows
        }

        override fun onPostExecute(cardRows: List<CardRow>) {
            mRowsAdapter = ArrayObjectAdapter(ListRowPresenter())
            val cardPresenter = CardPresenter()

            for (i in 0 until cardRows.size) {
                val cardRow = cardRows[i]
                val streams = cardRow.streams
                val listRowAdapter = ArrayObjectAdapter(cardPresenter)
                for (j in 0 until streams.size) {
                    listRowAdapter.add(streams[j])
                }
                val header = HeaderItem(i.toLong(), cardRow.category)
                mRowsAdapter.add(ListRow(header, listRowAdapter))
            }

            val gridHeader = HeaderItem((cardRows.size + 1).toLong(), "PREFERENCES")

            val mGridPresenter = GridItemPresenter()
            val gridRowAdapter = ArrayObjectAdapter(mGridPresenter)
            gridRowAdapter.add(resources.getString(R.string.settings))
            mRowsAdapter.add(ListRow(gridHeader, gridRowAdapter))

            adapter = mRowsAdapter
        }
    }

    private fun setupEventListeners() {
        setOnSearchClickedListener {
            Toast.makeText(activity, "Implement your own in-app search", Toast.LENGTH_LONG).show()
        }

        onItemViewClickedListener = ItemViewClickedListener()
        onItemViewSelectedListener = ItemViewSelectedListener()
    }

    private inner class ItemViewClickedListener : OnItemViewClickedListener {
        override fun onItemClicked(itemViewHolder: Presenter.ViewHolder, item: Any, rowViewHolder: RowPresenter.ViewHolder, row: Row) {

            if (item is Stream) {
                Log.d(TAG, "Item: " + item.toString())
                val intent = Intent(activity, StreamDetailsActivity::class.java)
                intent.putExtra(StreamDetailsActivity.STREAM, item)

                val bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        activity,
                        (itemViewHolder.view as ImageCardView).mainImageView,
                        StreamDetailsActivity.SHARED_ELEMENT_NAME).toBundle()

                activity.startActivity(intent, bundle)
            } else if (item is String) {
                Toast.makeText(activity, item, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private inner class ItemViewSelectedListener : OnItemViewSelectedListener {
        override fun onItemSelected(itemViewHolder: Presenter.ViewHolder?, item: Any?,
                                    rowViewHolder: RowPresenter.ViewHolder, row: Row) {
            if (item is Stream) {
                mBackgroundUri = item.imageUrl
                startBackgroundTimer()
            }
        }
    }

    private fun updateBackground(uri: String?) {
        val width = mMetrics.widthPixels
        val height = mMetrics.heightPixels
        Glide.with(activity)
                .load(uri)
                .centerCrop()
                .error(mDefaultBackground)
                .into<SimpleTarget<GlideDrawable>>(
                        object : SimpleTarget<GlideDrawable>(width, height) {
                            override fun onResourceReady(resource: GlideDrawable,
                                                         glideAnimation: GlideAnimation<in GlideDrawable>) {
                                mBackgroundManager.drawable = resource
                            }
                        })
        mBackgroundTimer?.cancel()
    }

    private fun startBackgroundTimer() {
        mBackgroundTimer?.cancel()
        mBackgroundTimer = Timer()
        mBackgroundTimer?.schedule(UpdateBackgroundTask(), BACKGROUND_UPDATE_DELAY.toLong())
    }

    private inner class UpdateBackgroundTask : TimerTask() {

        override fun run() {
            mHandler.post { updateBackground(mBackgroundUri) }
        }
    }

    private inner class GridItemPresenter : Presenter() {
        override fun onCreateViewHolder(parent: ViewGroup): Presenter.ViewHolder {
            val view = TextView(parent.context)
            view.layoutParams = ViewGroup.LayoutParams(GRID_ITEM_WIDTH, GRID_ITEM_HEIGHT)
            view.isFocusable = true
            view.isFocusableInTouchMode = true
            view.setBackgroundColor(ContextCompat.getColor(activity, R.color.default_background))
            view.setTextColor(Color.WHITE)
            view.gravity = Gravity.CENTER
            return Presenter.ViewHolder(view)
        }

        override fun onBindViewHolder(viewHolder: Presenter.ViewHolder, item: Any) {
            (viewHolder.view as TextView).text = item as String
        }

        override fun onUnbindViewHolder(viewHolder: Presenter.ViewHolder) {}
    }

    companion object {
        private val TAG = "MainFragment"

        private val BACKGROUND_UPDATE_DELAY = 300
        private val GRID_ITEM_WIDTH = 200
        private val GRID_ITEM_HEIGHT = 200
    }
}
