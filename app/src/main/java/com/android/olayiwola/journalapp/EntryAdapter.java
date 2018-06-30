package com.android.olayiwola.journalapp;

import android.content.Context;
import android.icu.text.SimpleDateFormat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.android.olayiwola.journalapp.Data.JournalEntry;

import java.text.Format;
import java.util.Date;
import java.util.List;

/**
 * Created by olayiwola on 6/28/2018.
 */

public class EntryAdapter extends RecyclerView.Adapter<EntryAdapter.EntryViewHolder> {

    private final Context mContext;
    private List<JournalEntry> mjournalEntries;
    private final CardClickListener mCardClickListener;

    public EntryAdapter(Context context, CardClickListener listener) {
        mContext = context;
        mCardClickListener = listener;
    }

    @Override
    public EntryAdapter.EntryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.entry_view,parent,false);
        EntryViewHolder entryViewHolder = new EntryViewHolder(view);
        return entryViewHolder;

    }

    @Override
    public void onBindViewHolder(EntryViewHolder holder, int position) {
        holder.mContent.setText(mjournalEntries.get(position).getContent());
        holder.mTitle.setText(mjournalEntries.get(position).getTitle());
        holder.mDate.setText(dateToString(mjournalEntries.get(position).getLastModifiedDate()));
    }

    @Override
    public int getItemCount() {
        if(mjournalEntries == null)
            return 0;
        else
            return mjournalEntries.size();
    }

    public void setData(List<JournalEntry> entries){
        mjournalEntries = entries;
        notifyDataSetChanged();
    }

    public interface CardClickListener {
        void onItemClickListener(JournalEntry entry);
    }

    /*****
     *
     * Formats Date to String, so that it can display in this format {relativeDay} at HH:mm
     * @param date
     * @return
     */
    public String dateToString(Date date){
        long timeNow = System.currentTimeMillis();
        long before = date.getTime();
        CharSequence timeDayRelative = DateUtils.getRelativeTimeSpanString(before, timeNow, DateUtils.DAY_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE);
        Format hourFormatter = new SimpleDateFormat("HH:mm");
        String timeHour = hourFormatter.format(before);
        String timeDayHour = timeDayRelative+" at "+timeHour;
        //return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(d);
        return timeDayHour;
    }

    public class EntryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView mTitle;
        TextView mContent;
        TextView mDate;
        CardView mCardView;
        public EntryViewHolder(View itemView) {
            super(itemView);
            mTitle = itemView.findViewById(R.id.entry_title);
            mContent = itemView.findViewById(R.id.entry_content);
            mDate = itemView.findViewById(R.id.entry_date);
            mCardView = itemView.findViewById(R.id.card_view);
            mCardView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            JournalEntry entry = mjournalEntries.get(getAdapterPosition());
            mCardClickListener.onItemClickListener(entry);
            Log.d("view", "view clicked");
        }
    }
}
