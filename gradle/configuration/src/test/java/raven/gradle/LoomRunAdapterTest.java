package raven.gradle;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import org.junit.jupiter.api.Test;

class LoomRunAdapterTest {
    @Test void selectedMaxHeapIsProjectedOnceAndEmptyLocalBlockRestoresMemory() {
        String defaults = "instance.client.playerName=DevPlayer\ninstance.extraJvmArgs=[\n-Xmx128M\n-Dliteral=hello world\n]\n";
        var config = RepositoryConfigurationTest.config(defaults, "instance.memory=64M");
        assertEquals("128M", LoomRunAdapter.settings(config, RepositoryConfigurationTest.TARGET, "client").effectiveMemory());
        assertEquals(List.of("-Dliteral=hello world"), new LoomRunAdapter.InstanceArguments(config, RepositoryConfigurationTest.TARGET, "client", true).asArguments());
        var cleared = RepositoryConfigurationTest.config(defaults, "instance.memory=64M\ninstance.extraJvmArgs=[\n\n]\n");
        assertEquals("64M", LoomRunAdapter.settings(cleared, RepositoryConfigurationTest.TARGET, "client").effectiveMemory());
        assertEquals(List.of(), new LoomRunAdapter.InstanceArguments(cleared, RepositoryConfigurationTest.TARGET, "client", true).asArguments());
    }

    @Test void variantsRemainIsolatedAndNamesAreRequired() {
        var config = RepositoryConfigurationTest.config("instance.client.playerName=DevPlayer\ninstance.client-multiplayer.playerName=DevPlayer2", "");
        assertEquals(List.of("--username", "DevPlayer"), new LoomRunAdapter.InstanceArguments(config, RepositoryConfigurationTest.TARGET, "client", false).asArguments());
        assertEquals(List.of("--username", "DevPlayer2"), new LoomRunAdapter.InstanceArguments(config, RepositoryConfigurationTest.TARGET, "client-multiplayer", false).asArguments());
        assertEquals(List.of(), new LoomRunAdapter.InstanceArguments(config, RepositoryConfigurationTest.TARGET, "server", false).asArguments());
        assertThrows(RuntimeException.class, () -> LoomRunAdapter.settings(RepositoryConfigurationTest.config("", ""), RepositoryConfigurationTest.TARGET, "client"));
    }

    @Test void serverIgnoresBadClientValuesButMultiplayerChecksBothNames() {
        var bad = RepositoryConfigurationTest.config("instance.client.playerName=bad name\ninstance.client-multiplayer.playerName=DevPlayer2\ninstance.width=bad", "");
        assertDoesNotThrow(() -> LoomRunAdapter.settings(bad, RepositoryConfigurationTest.TARGET, "server"));
        assertThrows(RuntimeException.class, () -> LoomRunAdapter.settings(bad, RepositoryConfigurationTest.TARGET, "client"));
        assertThrows(RuntimeException.class, () -> LoomRunAdapter.settings(bad, RepositoryConfigurationTest.TARGET, "client-multiplayer"));
        var same = RepositoryConfigurationTest.config("instance.playerName=SamePlayer", "");
        assertDoesNotThrow(() -> LoomRunAdapter.settings(same, RepositoryConfigurationTest.TARGET, "client"));
        assertThrows(RuntimeException.class, () -> LoomRunAdapter.settings(same, RepositoryConfigurationTest.TARGET, "client-multiplayer"));
    }

    @Test void argumentsRemainAtomicAndLocalEmptyClearsOnlyExtras() {
        var config = RepositoryConfigurationTest.config("instance.client.playerName=DevPlayer\ninstance.extraGameArgs=[\"two words\",\"\",\"雪\"]\ninstance.extraJvmArgs=[\"-Dmessage=hello world\"]", "");
        assertEquals(List.of("--username", "DevPlayer", "two words", "", "雪"), new LoomRunAdapter.InstanceArguments(config, RepositoryConfigurationTest.TARGET, "client", false).asArguments());
        assertEquals(List.of("-Dmessage=hello world"), new LoomRunAdapter.InstanceArguments(config, RepositoryConfigurationTest.TARGET, "client", true).asArguments());
        var clear = RepositoryConfigurationTest.config("instance.client.playerName=DevPlayer\ninstance.extraGameArgs=[\"kept?\"]", "instance.extraGameArgs=[]");
        assertEquals(List.of("--username", "DevPlayer"), new LoomRunAdapter.InstanceArguments(clear, RepositoryConfigurationTest.TARGET, "client", false).asArguments());
    }

    @Test void multiplayerConsumesOnlyOtherClientsNameNotItsWindowOrExtras() {
        var config = RepositoryConfigurationTest.config("instance.client.playerName=DevPlayer\ninstance.client-multiplayer.playerName=DevPlayer2\ninstance.client.width=bad\ninstance.client.extraGameArgs=not-json", "");
        assertDoesNotThrow(() -> LoomRunAdapter.settings(config, RepositoryConfigurationTest.TARGET, "client-multiplayer"));
        assertThrows(RuntimeException.class, () -> LoomRunAdapter.settings(config, RepositoryConfigurationTest.TARGET, "client"));
    }
}
