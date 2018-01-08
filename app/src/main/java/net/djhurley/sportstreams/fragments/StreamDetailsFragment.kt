package net.djhurley.sportstreams.fragments

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.AsyncTask
import android.os.Bundle
import android.support.v17.leanback.app.DetailsFragment
import android.support.v17.leanback.app.DetailsFragmentBackgroundController
import android.support.v17.leanback.widget.*
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget
import net.djhurley.sportstreams.*
import net.djhurley.sportstreams.MainActivity
import net.djhurley.sportstreams.activities.StreamDetailsActivity
import net.djhurley.sportstreams.models.Stream
import net.djhurley.sportstreams.presenters.DetailsDescriptionPresenter
import net.djhurley.sportstreams.services.RedditService

/**
 * Created by DJHURLEY on 07/01/2018.
 */
class StreamDetailsFragment : DetailsFragment() {

    private var mSelectedStream: Stream? = null

    private lateinit var mDetailsBackground: DetailsFragmentBackgroundController
    private lateinit var mPresenterSelector: ClassPresenterSelector
    private lateinit var mAdapter: ArrayObjectAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate StreamDetailsFragment")
        super.onCreate(savedInstanceState)

        mDetailsBackground = DetailsFragmentBackgroundController(this)

        mSelectedStream = activity.intent.getSerializableExtra(StreamDetailsActivity.STREAM) as Stream
        if (mSelectedStream != null) {
            mPresenterSelector = ClassPresenterSelector()
            mAdapter = ArrayObjectAdapter(mPresenterSelector)
            setupDetailsOverviewRow()
            setupDetailsOverviewRowPresenter()
            adapter = mAdapter
            initializeBackground(mSelectedStream)
            onItemViewClickedListener = ItemViewClickedListener()
        } else {
            val intent = Intent(activity, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun initializeBackground(stream: Stream?) {
        mDetailsBackground.enableParallax()
        Glide.with(activity)
                .load(stream?.imageUrl)
                .asBitmap()
                .centerCrop()
                .error(R.drawable.default_background)
                .into<SimpleTarget<Bitmap>>(object : SimpleTarget<Bitmap>() {
                    override fun onResourceReady(bitmap: Bitmap,
                                                 glideAnimation: GlideAnimation<in Bitmap>) {
                        mDetailsBackground.coverBitmap = bitmap
                        mAdapter.notifyArrayItemRangeChanged(0, mAdapter.size())
                    }
                })
    }

    private fun setupDetailsOverviewRow() {
        val linksTask = LinksTask()
        linksTask.execute(mSelectedStream?.url)
    }

    private inner class LinksTask : AsyncTask<String, Void, List<String>>() {

        override fun doInBackground(params: Array<String>): List<String> {
            return RedditService().getSoccerStreamLinks(params[0])
        }

        override fun onPostExecute(links: List<String>) {
            Log.d(TAG, "doInBackground: " + mSelectedStream?.toString())
            val row = DetailsOverviewRow(mSelectedStream)
            row.imageDrawable = ContextCompat.getDrawable(activity, R.drawable.default_background)
            val width = convertDpToPixel(activity, DETAIL_THUMB_WIDTH)
            val height = convertDpToPixel(activity, DETAIL_THUMB_HEIGHT)
            Glide.with(activity)
                    .load(mSelectedStream?.imageUrl)
                    .centerCrop()
                    .error(R.drawable.default_background)
                    .into<SimpleTarget<GlideDrawable>>(object : SimpleTarget<GlideDrawable>(width, height) {
                        override fun onResourceReady(resource: GlideDrawable,
                                                     glideAnimation: GlideAnimation<in GlideDrawable>) {
                            Log.d(TAG, "details overview card image url ready: " + resource)
                            row.imageDrawable = resource
                            mAdapter.notifyArrayItemRangeChanged(0, mAdapter.size())
                        }
                    })

            var id = 0L
            links.forEach {
                val actionAdapter = ArrayObjectAdapter()
                actionAdapter.add(Action(id, it))
                row.actionsAdapter = actionAdapter
                id++
            }

            mAdapter.add(row)
        }
    }

    private fun setupDetailsOverviewRowPresenter() {
        // Set detail background.
        val detailsPresenter = FullWidthDetailsOverviewRowPresenter(DetailsDescriptionPresenter())
        detailsPresenter.backgroundColor =
                ContextCompat.getColor(activity, R.color.selected_background)

        // Hook up transition element.
        val sharedElementHelper = FullWidthDetailsOverviewSharedElementHelper()
        sharedElementHelper.setSharedElementEnterTransition(
                activity, StreamDetailsActivity.SHARED_ELEMENT_NAME)
        detailsPresenter.setListener(sharedElementHelper)
        detailsPresenter.isParticipatingEntranceTransition = true

        detailsPresenter.onActionClickedListener = OnActionClickedListener { action ->
            Toast.makeText(activity, action.toString(), Toast.LENGTH_SHORT).show()
        }
        mPresenterSelector.addClassPresenter(DetailsOverviewRow::class.java, detailsPresenter)
    }

    private fun convertDpToPixel(context: Context, dp: Int): Int {
        val density = context.applicationContext.resources.displayMetrics.density
        return Math.round(dp.toFloat() * density)
    }

    private inner class ItemViewClickedListener : OnItemViewClickedListener {
        override fun onItemClicked(itemViewHolder: Presenter.ViewHolder?, item: Any?,
                                   rowViewHolder: RowPresenter.ViewHolder, row: Row) {
            if (item is Stream) {
                Log.d(TAG, "Item: " + item.toString())
                val intent = Intent(activity, StreamDetailsActivity::class.java)
                intent.putExtra(resources.getString(R.string.stream), mSelectedStream)

                val bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        activity,
                        (itemViewHolder?.view as ImageCardView).mainImageView,
                        StreamDetailsActivity.SHARED_ELEMENT_NAME).toBundle()
                activity.startActivity(intent, bundle)
            }
        }
    }

    companion object {
        private val TAG = "StreamDetailsFragment"

        private val DETAIL_THUMB_WIDTH = 274
        private val DETAIL_THUMB_HEIGHT = 274
    }
}