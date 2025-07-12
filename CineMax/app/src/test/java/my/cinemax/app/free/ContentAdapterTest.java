package my.cinemax.app.free;

import android.widget.Filter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import java.util.ArrayList;
import java.util.List;
import my.cinemax.app.free.model.Entry;
import my.cinemax.app.free.ui.adapters.ContentAdapter;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ContentAdapterTest {

    @Mock
    private Entry entry1;

    @Mock
    private Entry entry2;

    @Test
    public void testFilter() {
        when(entry1.getTitle()).thenReturn("Test Movie 1");
        when(entry2.getTitle()).thenReturn("Another Movie");

        List<Entry> entries = new ArrayList<>();
        entries.add(entry1);
        entries.add(entry2);

        ContentAdapter adapter = new ContentAdapter(null, entries);
        adapter.getFilter().filter("Test", new Filter.FilterListener() {
            @Override
            public void onFilterComplete(int count) {
                assertEquals(1, count);
            }
        });
    }
}
