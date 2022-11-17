package com.example.week_5B_solution

import android.net.Uri

class ImageElement {
    var image: Int? = null
    var file_uri: Uri?=null

    constructor(image: Int) {
        this.image = image
    }
    constructor(uri: Uri?) {
        file_uri = uri
    }
}