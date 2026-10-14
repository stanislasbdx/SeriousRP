package fr.stan1712.wetston.seriousrp.economy;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

import java.io.File;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class AtmRegistryTest {

	@TempDir
	Path tempDir;

	@Test
	void registerUnregisterAndFilterByOwner() {
		File file = tempDir.resolve("atms.yml").toFile();
		AtmRegistry registry = new AtmRegistry(file);
		UUID owner = UUID.fromString("11111111-1111-1111-1111-111111111111");
		UUID other = UUID.fromString("22222222-2222-2222-2222-222222222222");
		World world = mock(World.class);
		when(world.getName()).thenReturn("world");
		Location location = new Location(world, 1, 2, 3);
		Location second = new Location(world, 4, 5, 6);

		try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
			bukkit.when(() -> Bukkit.getWorld("world")).thenReturn(world);
			registry.register(location, owner);
			registry.register(second, other);
			assertEquals(2, registry.all().size());
			assertEquals(1, registry.ownedBy(owner).size());
			assertEquals(owner, registry.ownedBy(owner).get(0).owner());
			assertTrue(registry.ownedBy(owner).get(0).location().isPresent());

			registry.rename(second, "Square");
			assertEquals("Square", registry.ownedBy(other).get(0).name());
			registry.unregister(location);
			assertEquals(1, registry.all().size());
			assertTrue(registry.ownedBy(owner).isEmpty());
		}

		AtmRegistry reloaded = new AtmRegistry(file);
		assertEquals(1, reloaded.all().size());
		assertEquals(other, reloaded.all().get(0).owner());
		assertEquals("Square", reloaded.all().get(0).name());
	}
}
