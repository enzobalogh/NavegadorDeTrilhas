package com.example.trilhasapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trilhasapp.R;
import com.example.trilhasapp.model.Trilha;

import java.util.List;

public class TrilhaAdapter extends RecyclerView.Adapter<TrilhaAdapter.TrilhaViewHolder> {

    public interface OnTrilhaActionListener {
        void onConsultar(Trilha trilha);
        void onEditar(Trilha trilha);
        void onApagar(Trilha trilha);
    }

    private final List<Trilha> trilhas;
    private final OnTrilhaActionListener listener;

    public TrilhaAdapter(List<Trilha> trilhas, OnTrilhaActionListener listener) {
        this.trilhas = trilhas;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TrilhaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_trilha, parent, false);
        return new TrilhaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrilhaViewHolder holder, int position) {
        Trilha trilha = trilhas.get(position);
        holder.bind(trilha, listener);
    }

    @Override
    public int getItemCount() {
        return trilhas.size();
    }

    static class TrilhaViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvNome;
        private final TextView tvData;
        private final TextView tvDistancia;
        private final TextView tvDuracao;
        private final ImageButton btnConsultar;
        private final ImageButton btnEditar;
        private final ImageButton btnApagar;

        TrilhaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNome       = itemView.findViewById(R.id.tv_trilha_nome);
            tvData       = itemView.findViewById(R.id.tv_trilha_data);
            tvDistancia  = itemView.findViewById(R.id.tv_trilha_distancia);
            tvDuracao    = itemView.findViewById(R.id.tv_trilha_duracao);
            btnConsultar = itemView.findViewById(R.id.btn_consultar);
            btnEditar    = itemView.findViewById(R.id.btn_editar);
            btnApagar    = itemView.findViewById(R.id.btn_apagar);
        }

        void bind(Trilha trilha, OnTrilhaActionListener listener) {
            tvNome.setText(trilha.getNome());

            // Data de início formatada
            String dataInicio = trilha.getDataInicio() != null ? trilha.getDataInicio() : "—";
            // Exibe apenas a parte de data (yyyy-MM-dd HH:mm:ss -> dd/MM/yyyy HH:mm)
            tvData.setText(formatarData(dataInicio));

            tvDistancia.setText(String.format(java.util.Locale.getDefault(),
                    "%.2f km", trilha.getDistanciaTotal()));
            tvDuracao.setText(trilha.getDuracaoFormatada());

            btnConsultar.setOnClickListener(v -> listener.onConsultar(trilha));
            btnEditar.setOnClickListener(v -> listener.onEditar(trilha));
            btnApagar.setOnClickListener(v -> listener.onApagar(trilha));

            // Clique no card inteiro também abre consulta
            itemView.setOnClickListener(v -> listener.onConsultar(trilha));
        }

        private String formatarData(String iso) {
            // Entrada: "yyyy-MM-dd HH:mm:ss"  Saída: "dd/MM/yyyy  HH:mm"
            try {
                if (iso.length() >= 16) {
                    String[] parts = iso.split(" ");
                    String[] ymd = parts[0].split("-");
                    String hm = parts[1].substring(0, 5);
                    return ymd[2] + "/" + ymd[1] + "/" + ymd[0] + "  " + hm;
                }
            } catch (Exception ignored) {}
            return iso;
        }
    }
}
