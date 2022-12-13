package com.example.week_5B_solution.view

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.week_5B_solution.R
import com.example.week_5B_solution.model.ImageData
import com.example.week_5B_solution.viewmodel.AppViewModel
import com.google.android.material.snackbar.Snackbar

class PathDetailActivity : AppCompatActivity() {

    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>

    private var appViewModel: AppViewModel? = null

    private var myDataset: MutableList<ImageData> = ArrayList<ImageData>()


    var showImageActivityResultContract = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result->
        result?.let{
            val position = it.data?.extras?.getInt("position")
            val delete_op = it.data?.extras?.getBoolean("deletion")
            val update_op = it.data?.extras?.getBoolean("updated")
            delete_op?.apply {
                if(delete_op == true){
                    position?.apply{
                        // Tell the adapter that the collection it is rendering has changed
                        // so it can redraw itself.
                        mAdapter.notifyItemRemoved(position)
                        mAdapter.notifyItemRangeChanged(position, MyAdapter.items.size);
                        Snackbar.make(/* view = */ mRecyclerView,
                            /* text = */ "Image deleted.",
                            /* duration = */ Snackbar.LENGTH_LONG)
                            .show()
                    }
                }
            }
            update_op?.apply {
                if(update_op == true){
                    Snackbar.make(/* view = */ mRecyclerView,
                        /* text = */ "Image detail updated.",
                        /* duration = */ Snackbar.LENGTH_LONG)
                        .show()
                }
            }
        }
    }





    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_path_detail)

        this.appViewModel = ViewModelProvider(this)[AppViewModel::class.java]

        var markerTest = findViewById<TextView>(R.id.markerTest)

        var title = this@PathDetailActivity.intent.getStringExtra("title")
        var pathID = this@PathDetailActivity.intent.getIntExtra("pathID",-1)

        initPathImages(pathID)


        markerTest.setText("$title, $pathID")


        mRecyclerView = findViewById<RecyclerView>(R.id.pathImageList)

        val numberOfColumns = 4
        mRecyclerView.layoutManager = GridLayoutManager(this, numberOfColumns)

        mAdapter = MyPathAdapter(this, myDataset) as RecyclerView.Adapter<RecyclerView.ViewHolder>
        mRecyclerView.adapter = mAdapter

    }

    fun initPathImages(id: Int){
        val pathImages = this.appViewModel!!.retrievePathImages(id)

        myDataset.addAll(pathImages)
    }

    fun onViewHolderItemClick(position: Int) {
        val intent = Intent(this, ShowImageActivity::class.java)
        intent.putExtra("position", position)
        // Start the ShowImageActivity from the ActivityResultContract registered to handle
        // the result when the Activity returns
        showImageActivityResultContract.launch(intent)
    }

}