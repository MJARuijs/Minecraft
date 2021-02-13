package graphics.model

import resources.Cache

object ModelCache: Cache<Model>(ModelLoader())