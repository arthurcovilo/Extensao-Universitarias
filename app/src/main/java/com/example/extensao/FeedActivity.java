package com.example.extensao;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Activity responsável por exibir o feed de novidades da ONG
 */
public class FeedActivity extends AppCompatActivity implements PostAdapter.OnPostClickListener {

    // Views
    private RecyclerView recyclerViewPosts;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout layoutEmpty;
    private LinearLayout layoutLoading;
    private BottomNavigationView bottomNavigationView;

    // Componentes
    private PostAdapter postAdapter;
    private List<PostModel> posts;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        // Inicializar views
        initViews();
        
        // Configurar RecyclerView
        setupRecyclerView();
        
        // Configurar SwipeRefresh
        setupSwipeRefresh();
        
        // Configurar Bottom Navigation
        setupBottomNavigation();
        
        // Carregar posts
        loadPosts();
    }

    /**
     * Inicializar todas as views
     */
    private void initViews() {
        recyclerViewPosts = findViewById(R.id.recyclerViewPosts);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        layoutLoading = findViewById(R.id.layoutLoading);
        bottomNavigationView = findViewById(R.id.bottomNavigation);
    }

    /**
     * Configurar o RecyclerView
     */
    private void setupRecyclerView() {
        posts = new ArrayList<>();
        postAdapter = new PostAdapter(posts, this);
        
        recyclerViewPosts.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewPosts.setAdapter(postAdapter);
    }

    /**
     * Configurar o SwipeRefreshLayout
     */
    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::refreshPosts);
        swipeRefreshLayout.setColorSchemeResources(R.color.primary_purple);
    }

    /**
     * Configurar a Bottom Navigation
     */
    private void setupBottomNavigation() {
        // Definir item selecionado
        bottomNavigationView.setSelectedItemId(R.id.nav_feed);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_feed) {
                return true; // Já estamos na tela de Feed
            } else if (id == R.id.nav_calendario) {
                startActivity(new Intent(FeedActivity.this, MainActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_eventos) {
                startActivity(new Intent(FeedActivity.this, EventosActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_perfil) {
                startActivity(new Intent(FeedActivity.this, PerfilActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_contato) {
                startActivity(new Intent(FeedActivity.this, ContatoActivity.class));
                finish();
                return true;
            }

            return false;
        });
    }

    /**
     * Carregar posts do feed
     */
    private void loadPosts() {
        showLoading(true);
        
        executor.execute(() -> {
            try {
                // Simular carregamento de dados
                // TODO: Implementar chamada real para API
                List<PostModel> loadedPosts = loadPostsFromApi();
                
                runOnUiThread(() -> {
                    showLoading(false);
                    updatePosts(loadedPosts);
                });
                
            } catch (Exception e) {
                runOnUiThread(() -> {
                    showLoading(false);
                    showError("Erro ao carregar novidades");
                });
            }
        });
    }

    /**
     * Atualizar posts via pull-to-refresh
     */
    private void refreshPosts() {
        executor.execute(() -> {
            try {
                // TODO: Implementar chamada real para API
                List<PostModel> loadedPosts = loadPostsFromApi();
                
                runOnUiThread(() -> {
                    swipeRefreshLayout.setRefreshing(false);
                    updatePosts(loadedPosts);
                    Toast.makeText(this, "Feed atualizado", Toast.LENGTH_SHORT).show();
                });
                
            } catch (Exception e) {
                runOnUiThread(() -> {
                    swipeRefreshLayout.setRefreshing(false);
                    showError("Erro ao atualizar feed");
                });
            }
        });
    }

    /**
     * Simular carregamento de posts da API
     * TODO: Substituir por implementação real
     */
    private List<PostModel> loadPostsFromApi() {
        // Simular delay de rede
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        List<PostModel> mockPosts = new ArrayList<>();
        
        mockPosts.add(new PostModel(
            "1",
            "Nova Campanha de Arrecadação",
            "Estamos iniciando uma nova campanha para arrecadar alimentos não perecíveis para famílias em situação de vulnerabilidade. Sua ajuda é fundamental para levarmos esperança a quem mais precisa.",
            null,
            "15/11/2025",
            "Centro Comunitário - São Paulo"
        ));

        mockPosts.add(new PostModel(
            "2",
            "Projeto Educação Digital",
            "Lançamos o projeto que visa ensinar informática básica para crianças e adolescentes da comunidade. As aulas acontecem aos sábados e são totalmente gratuitas.",
            "https://example.com/image2.jpg",
            "12/11/2025",
            null
        ));

        mockPosts.add(new PostModel(
            "3",
            "Mutirão de Limpeza",
            "Convocamos todos os voluntários para participar do mutirão de limpeza da praça central. Juntos podemos fazer a diferença no nosso bairro!",
            null,
            "10/11/2025",
            "Praça Central"
        ));

        mockPosts.add(new PostModel(
            "4",
            "Agradecimento aos Doadores",
            "Queremos agradecer a todos que contribuíram com nossa última campanha. Graças à generosidade de vocês, conseguimos ajudar mais de 100 famílias.",
            "https://example.com/image4.jpg",
            "08/11/2025",
            null
        ));

        return mockPosts;
    }

    /**
     * Atualizar lista de posts
     */
    private void updatePosts(List<PostModel> newPosts) {
        this.posts = newPosts;
        postAdapter.updatePosts(newPosts);
        
        // Mostrar estado vazio se não houver posts
        showEmpty(newPosts.isEmpty());
    }

    /**
     * Mostrar/esconder loading
     */
    private void showLoading(boolean show) {
        layoutLoading.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerViewPosts.setVisibility(show ? View.GONE : View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);
    }

    /**
     * Mostrar/esconder estado vazio
     */
    private void showEmpty(boolean show) {
        layoutEmpty.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerViewPosts.setVisibility(show ? View.GONE : View.VISIBLE);
        layoutLoading.setVisibility(View.GONE);
    }

    /**
     * Mostrar erro
     */
    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        showEmpty(true);
    }

    // Implementação dos callbacks do PostAdapter
    @Override
    public void onPostClick(PostModel post) {
        // TODO: Implementar navegação para tela de detalhes do post
        Toast.makeText(this, "Post clicado: " + post.getTitulo(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onVerDetalhesClick(PostModel post) {
        // TODO: Implementar navegação para tela de detalhes do post
        Toast.makeText(this, "Ver detalhes: " + post.getTitulo(), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}