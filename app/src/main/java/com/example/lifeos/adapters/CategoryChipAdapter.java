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
    private boolean isHorizontalPreview;

    public void setCategories(List<Category> list, boolean selectionMode) {
        setCategories(list, selectionMode, false);
    }

    public void setCategories(List<Category> list, boolean selectionMode, boolean isHorizontalPreview) {
        this.selectionMode = selectionMode;
        this.isHorizontalPreview = isHorizontalPreview;
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
        int layoutId = isHorizontalPreview ? R.layout.item_category_chip_selected : R.layout.item_category_chip;
        return new ChipViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(layoutId, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ChipViewHolder holder, int position) {
        holder.bind(categories.get(position), selectionMode, isHorizontalPreview, listener, position);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class ChipViewHolder extends RecyclerView.ViewHolder {
        TextView tvChip;
        View btnRemove;

        ChipViewHolder(@NonNull View itemView) {
            super(itemView);
            tvChip = itemView.findViewById(R.id.tvChip);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }

        void bind(Category category, boolean selectionMode, boolean isHorizontalPreview, ChipListener listener, int position) {
            tvChip.setText(category.getName());
            
            View container = itemView; // In horizontal mode, this is the LinearLayout
            
            if (selectionMode && category.isSelected()) {
                container.setBackgroundResource(R.drawable.bg_gradient_button);
            } else {
                container.setBackgroundResource(R.drawable.bg_card_glass);
            }

            if (btnRemove != null) {
                btnRemove.setVisibility(isHorizontalPreview ? View.VISIBLE : View.GONE);
                btnRemove.setOnClickListener(v -> {
                    if (listener != null) listener.onChipClick(position);
                });
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onChipClick(position);
            });
        }
    }
}
