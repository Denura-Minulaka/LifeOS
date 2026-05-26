package com.example.lifeos.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lifeos.R;
import com.example.lifeos.models.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryChipAdapter extends RecyclerView.Adapter<CategoryChipAdapter.ChipViewHolder> {

    public interface ChipListener {
        void onChipClick(int position);
    }

    private final List<Category> categories = new ArrayList<>();
    private ChipListener listener;
    private boolean selectionMode;

    public void setCategories(List<Category> list, boolean selectionMode) {
        this.selectionMode = selectionMode;
        categories.clear();
        if (list != null) categories.addAll(list);
        notifyDataSetChanged();
    }

    public void setListener(ChipListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChipViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ChipViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_chip, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ChipViewHolder holder, int position) {
        holder.bind(categories.get(position), selectionMode, listener, position);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class ChipViewHolder extends RecyclerView.ViewHolder {
        TextView tvChip;

        ChipViewHolder(@NonNull View itemView) {
            super(itemView);
            tvChip = itemView.findViewById(R.id.tvChip);
        }

        void bind(Category category, boolean selectionMode, ChipListener listener, int position) {
            tvChip.setText(category.getName());
            if (selectionMode && category.isSelected()) {
                tvChip.setBackgroundResource(R.drawable.bg_gradient_button);
            } else {
                tvChip.setBackgroundResource(R.drawable.bg_card_glass);
            }
            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onChipClick(position);
            });
        }
    }
}
