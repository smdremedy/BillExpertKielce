package pl.szkoleniaandroid.billexpert

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.squareup.picasso.Picasso


@BindingAdapter("imageUrl")
fun imageUrl(view: ImageView, url: String) {

    Picasso.get().load(url).into(view)

}