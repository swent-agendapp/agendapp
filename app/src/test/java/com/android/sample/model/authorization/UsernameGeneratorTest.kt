package com.android.sample.model.authorization

import com.android.sample.model.authentication.AnimalNames
import com.android.sample.model.authentication.UsernameGenerator
import org.junit.Assert.assertTrue
import org.junit.Test

class UsernameGeneratorTest {

  @Test
  fun `generated username is not blank and contains number`() {
    val username = UsernameGenerator.generate()

    assertTrue("Username should not be blank", username.isNotBlank())

    assertTrue("Username should contain numbers", username.any { it.isDigit() })

    val startsWithAnimal = AnimalNames.list.any { username.startsWith(it) }
    assertTrue("Username should start with a valid animal name", startsWithAnimal)
  }

  @Test
  fun `multiple generated usernames are unique`() {
    val usernames = (1..10).map { UsernameGenerator.generate() }
    val distinctUsernames = usernames.distinct()
    assertTrue(
        "Usernames should be unique across multiple generations",
        usernames.size == distinctUsernames.size)
  }
}
