package com.example.extensao;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Adapter para o RecyclerView do feed de posts
 */
public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<PostModel> posts;
    private OnPostClickListener listener;

    // Interface para callbacks de clique
    public interface OnPostClickListener {
        void onPostClick(PostModel post);
        void onVerDetalhesClick(PostModel post);
    }

    // Construtor
    public PostAdapter(List<PostModel> posts, OnPostClickListener listener) {
        this.posts = posts;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        PostModel post = posts.get(position);
        holder.bind(post, listener);
    }

    @Override
    public int getItemCount() {
        return posts != null ? posts.size() : 0;
    }

    // Método para atualizar a lista de posts
    public void updatePosts(List<PostModel> newPosts) {
        if (newPosts != null) {
            this.posts = newPosts;
            notifyDataSetChanged();
        }
    }

    // ViewHolder para cada item do post
    public static class PostViewHolder extends RecyclerView.ViewHolder {
        
        private ImageView imageViewPost;
        private TextView textViewTitulo;
        private TextView textViewDescricao;
        private TextView textViewData;
        private TextView textViewLocal;
        private LinearLayout layoutLocal;
        private Button buttonVerDetalhes;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            
            // Inicializar as views
            imageViewPost = itemView.findViewById(R.id.imageViewPost);
            textViewTitulo = itemView.findViewById(R.id.textViewTitulo);
            textViewDescricao = itemView.findViewById(R.id.textViewDescricao);
            textViewData = itemView.findViewById(R.id.textViewData);
            textViewLocal = itemView.findViewById(R.id.textViewLocal);
            layoutLocal = itemView.findViewById(R.id.layoutLocal);
            buttonVerDetalhes = itemView.findViewById(R.id.buttonVerDetalhes);
        }

        public void bind(PostModel post, OnPostClickListener listener) {
            if (post == null) return;
            
            // Configurar título
            textViewTitulo.setText(post.getTitulo());
            
            // Configurar descrição
            textViewDescricao.setText(post.getDescricao());
            
            // Configurar data
            textViewData.setText(post.getData());
            
            // Configurar imagem (mostrar/esconder baseado na disponibilidade)
            if (post.hasImage()) {
                imageViewPost.setVisibility(View.VISIBLE);
                // Usar ImageUtils para carregar a imagem
                ImageUtils.loadPostImage(itemView.getContext(), post.getImagemUrl(), imageViewPost);
            } else {
                imageViewPost.setVisibility(View.GONE);
            }
            
            // Configurar local (mostrar/esconder baseado na disponibilidade)
            if (post.hasLocal()) {
                layoutLocal.setVisibility(View.VISIBLE);
                textViewLocal.setText(post.getLocal());
            } else {
                layoutLocal.setVisibility(View.GONE);
            }
            
            // Configurar clique no card
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPostClick(post);
                }
            });
            
            // Configurar clique no botão "Ver detalhes"
            buttonVerDetalhes.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onVerDetalhesClick(post);
                }
            });
        }
    }
}