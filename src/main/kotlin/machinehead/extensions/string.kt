package machinehead.extensions

fun CharSequence?.isNotNullOrEmpty(): Boolean {
    return !this.isNullOrEmpty()
}
