package com.burcuerdogan.artbookfragment;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.burcuerdogan.artbookfragment.databinding.RecyclerRowBinding;

import java.util.List;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ArtHolder> {

    List<Art> artList;

    public ListAdapter(List<Art> artList) {

        this.artList = artList;
    }

    class ArtHolder extends RecyclerView.ViewHolder {

        private RecyclerRowBinding binding;

        public ArtHolder(RecyclerRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    @NonNull
    @Override
    public ArtHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerRowBinding binding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return  new ArtHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ArtHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.binding.rowTextView.setText(artList.get(position).artname);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListFragmentDirections.ActionListFragmentToDetailsFragment action = ListFragmentDirections.actionListFragmentToDetailsFragment("old");
                action.setArtId(artList.get(position).id);
                action.setInfo("old");
                Navigation.findNavController(v).navigate(action);
            }
        });
    }

    public int getItemCount() {
        
        return artList.size();
    }
}
