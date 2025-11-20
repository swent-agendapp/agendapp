package com.android.sample.model.authentication

// Default list of animal names for generating user nicknames.
object AnimalNames {
  val list =
      listOf(
          "lion",
          "tiger",
          "wolf",
          "eagle",
          "bear",
          "fox",
          "panda",
          "otter",
          "falcon",
          "lynx",
          "leopard",
          "rabbit",
          "deer",
          "boar",
          "hawk",
          "whale",
          "shark",
          "koala",
          "camel",
          "buffalo",
          "caterpillar",
          "dolphin",
          "elephant",
          "giraffe",
          "hippo")
}

// Utility object to generate random usernames by combining an animal name with a random number.
object UsernameGenerator {
  fun generate(): String {
    val animal = AnimalNames.list.random()
    val number = (1000..9999).random()
    return "$animal$number"
  }
}
