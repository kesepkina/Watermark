fun composition(value: Int, y: (Int) -> Int, g: (Int) -> Int): Int {
    // TODO: provide implementation here
    return y(g(value))
}