package my.cinemax.app.free.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Picasso;
import java.util.List;
import my.cinemax.app.free.R;
import my.cinemax.app.free.model.Entry;

public class ContentAdapter extends RecyclerView.Adapter<ContentAdapter.ViewHolder> implements Filterable {

    private List<Entry> entries;
    private List<Entry> entriesFiltered;
    private Context context;

    public ContentAdapter(Context context, List<Entry> entries) {
        this.context = context;
        this.entries = entries;
        this.entriesFiltered = entries;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_content, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Entry entry = entriesFiltered.get(position);
        holder.title.setText(entry.getTitle());
        holder.year.setText(String.valueOf(entry.getYear()));
        Picasso.get().load(entry.getPoster()).into(holder.poster);

        holder.itemView.setOnClickListener(v -> {
            if (entry.getServers() != null && !entry.getServers().isEmpty()) {
                String videoUrl = entry.getServers().get(0).getUrl();
                Intent intent = new Intent(context, my.cinemax.app.free.ui.activities.SimplePlayerActivity.class);
                intent.putExtra(my.cinemax.app.free.ui.activities.SimplePlayerActivity.VIDEO_URL_EXTRA, videoUrl);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return entriesFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String charString = constraint.toString();
                if (charString.isEmpty()) {
                    entriesFiltered = entries;
                } else {
                    List<Entry> filteredList = new ArrayList<>();
                    for (Entry row : entries) {
                        if (row.getTitle().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }
                    entriesFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = entriesFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                entriesFiltered = (ArrayList<Entry>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView poster;
        public TextView title;
        public TextView year;

        public ViewHolder(View itemView) {
            super(itemView);
            poster = itemView.findViewById(R.id.image_view_poster);
            title = itemView.findViewById(R.id.text_view_title);
            year = itemView.findViewById(R.id.text_view_year);
        }
    }
}
