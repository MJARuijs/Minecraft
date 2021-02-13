package resources

interface Loader<T: Resource> {

    fun load(path: String): T

}