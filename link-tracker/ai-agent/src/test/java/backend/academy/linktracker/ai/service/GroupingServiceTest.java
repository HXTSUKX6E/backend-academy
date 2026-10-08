package backend.academy.linktracker.ai.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import backend.academy.linktracker.ai.kafka.ProcessedUpdateProducer;
import com.example.notification.ProcessedUpdateEvent;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GroupingServiceTest {

    private GroupingService groupingService;

    @BeforeEach
    void setUp() {
        groupingService = new GroupingService(mock(ProcessedUpdateProducer.class));
    }

    @Test
    void tc21_shouldGroupMultipleUpdatesForSameChatId() {
        var update1 = new PrioritizedUpdate(1L, "https://github.com/a", "First update", List.of(111L), Priority.MEDIUM);
        var update2 = new PrioritizedUpdate(2L, "https://github.com/b", "Second update", List.of(111L), Priority.HIGH);

        groupingService.buffer(update1);
        groupingService.buffer(update2);

        List<ProcessedUpdateEvent> events = groupingService.groupAndFlush();

        assertThat(events).hasSize(1);
        ProcessedUpdateEvent event = events.get(0);
        assertThat(event.getDescription()).contains("1. First update");
        assertThat(event.getDescription()).contains("2. Second update");
        assertThat(event.getPriority()).isEqualTo("HIGH");
        assertThat(event.getTgChatIds()).containsExactly(111L);
    }

    @Test
    void tc21_shouldUseMaxPriorityAmongGroupedUpdates() {
        var low = new PrioritizedUpdate(1L, "https://github.com/a", "Low prio", List.of(111L), Priority.LOW);
        var medium = new PrioritizedUpdate(2L, "https://github.com/b", "Medium prio", List.of(111L), Priority.MEDIUM);
        var high = new PrioritizedUpdate(3L, "https://github.com/c", "High prio", List.of(111L), Priority.HIGH);

        groupingService.buffer(low);
        groupingService.buffer(medium);
        groupingService.buffer(high);

        List<ProcessedUpdateEvent> events = groupingService.groupAndFlush();

        assertThat(events).hasSize(1);
        assertThat(events.get(0).getPriority()).isEqualTo("HIGH");
    }

    @Test
    void tc22_shouldNotGroupSingleUpdate() {
        var update = new PrioritizedUpdate(1L, "https://github.com/a", "Single update", List.of(111L), Priority.LOW);

        groupingService.buffer(update);

        List<ProcessedUpdateEvent> events = groupingService.groupAndFlush();

        assertThat(events).hasSize(1);
        ProcessedUpdateEvent event = events.get(0);
        assertThat(event.getDescription()).isEqualTo("Single update");
        assertThat(event.getPriority()).isEqualTo("LOW");
        assertThat(event.getTgChatIds()).containsExactly(111L);
    }

    @Test
    void tc22_shouldPreserveOriginalChatIdsForSingleUpdate() {
        var update = new PrioritizedUpdate(1L, "https://github.com/a", "Update", List.of(111L, 222L), Priority.MEDIUM);

        groupingService.buffer(update);

        List<ProcessedUpdateEvent> events = groupingService.groupAndFlush();

        assertThat(events).hasSizeGreaterThanOrEqualTo(1);
        assertThat(events.stream().anyMatch(e -> e.getTgChatIds().contains(111L)))
                .isTrue();
    }

    @Test
    void shouldGroupSeparatelyForDifferentChatIds() {
        var update1 =
                new PrioritizedUpdate(1L, "https://github.com/a", "Update for 111", List.of(111L), Priority.MEDIUM);
        var update2 = new PrioritizedUpdate(2L, "https://github.com/b", "Update for 222", List.of(222L), Priority.HIGH);

        groupingService.buffer(update1);
        groupingService.buffer(update2);

        List<ProcessedUpdateEvent> events = groupingService.groupAndFlush();

        assertThat(events).hasSize(2);
    }

    @Test
    void shouldReturnEmptyListWhenBufferIsEmpty() {
        List<ProcessedUpdateEvent> events = groupingService.groupAndFlush();
        assertThat(events).isEmpty();
    }

    @Test
    void shouldClearBufferAfterFlush() {
        var update = new PrioritizedUpdate(1L, "https://github.com/a", "Some update", List.of(111L), Priority.MEDIUM);
        groupingService.buffer(update);

        groupingService.groupAndFlush();
        List<ProcessedUpdateEvent> secondFlush = groupingService.groupAndFlush();

        assertThat(secondFlush).isEmpty();
    }
}
