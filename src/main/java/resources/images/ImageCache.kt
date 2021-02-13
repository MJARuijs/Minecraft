package resources.images

import resources.Cache

object ImageCache: Cache<ImageData>(ImageLoader())