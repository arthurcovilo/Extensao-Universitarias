package com.example.extensao;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
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
    private TextView txtEmptyIcon;
    private TextView txtEmptyMessage;
    private Button btnPublicarAviso;
    private BottomNavigationView bottomNavigationView;

    // Componentes
    private SessionManager sessionManager;
    private PostApiClient postApiClient;
    private PostAdapter postAdapter;
    private List<PostModel> posts;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        sessionManager = new SessionManager(this);
        postApiClient = new PostApiClient(sessionManager);

        // Inicializar views
        initViews();

        // Configurar RecyclerView
        setupRecyclerView();

        // Configurar SwipeRefresh
        setupSwipeRefresh();

        // Configurar Bottom Navigation
        setupBottomNavigation();

        // Botão de publicar aviso (apenas ADMIN)
        setupPublicar();

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
        txtEmptyIcon = findViewById(R.id.txtEmptyIcon);
        txtEmptyMessage = findViewById(R.id.txtEmptyMessage);
        btnPublicarAviso = findViewById(R.id.btnPublicarAviso);
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
        swipeRefreshLayout.setOnRefreshListener(() -> refreshPosts(true));
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
     * Mostrar o botão "Publicar" apenas para ADMIN
     */
    private void setupPublicar() {
        if (sessionManager.isAdmin()) {
            btnPublicarAviso.setVisibility(View.VISIBLE);
            btnPublicarAviso.setOnClickListener(v -> mostrarDialogoPublicar());
        }
    }

    /**
     * Carregar posts do feed (com tela de loading)
     */
    private void loadPosts() {
        showLoading(true);

        executor.execute(() -> {
            try {
                List<PostModel> loadedPosts = postApiClient.getPosts(sessionManager.getAccessToken());

                runOnUiThread(() -> {
                    showLoading(false);
                    updatePosts(loadedPosts);
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    showLoading(false);
                    showError();
                });
            }
        });
    }

    /**
     * Atualizar posts via pull-to-refresh ou após publicar/excluir
     */
    private void refreshPosts(boolean showToast) {
        executor.execute(() -> {
            try {
                List<PostModel> loadedPosts = postApiClient.getPosts(sessionManager.getAccessToken());

                runOnUiThread(() -> {
                    swipeRefreshLayout.setRefreshing(false);
                    updatePosts(loadedPosts);
                    if (showToast) {
                        Toast.makeText(this, R.string.feed_atualizado, Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    swipeRefreshLayout.setRefreshing(false);
                    showError();
                });
            }
        });
    }

    /**
     * Recarregar a lista depois de publicar ou excluir um aviso
     */
    private void recarregarPosts() {
        swipeRefreshLayout.setRefreshing(true);
        refreshPosts(false);
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
        swipeRefreshLayout.setEnabled(!show);
        layoutEmpty.setVisibility(View.GONE);
    }

    /**
     * Mostrar/esconder estado vazio
     */
    private void showEmpty(boolean show) {
        txtEmptyIcon.setText("📭");
        txtEmptyMessage.setText(R.string.feed_vazio);
        layoutEmpty.setOnClickListener(null);
        layoutEmpty.setClickable(false);
        layoutEmpty.setVisibility(show ? View.VISIBLE : View.GONE);
        layoutLoading.setVisibility(View.GONE);
    }

    /**
     * Mostrar erro: sem posts na tela, vira o estado de erro com "tentar novamente";
     * com posts já carregados, mantém a lista e avisa com um Toast.
     */
    private void showError() {
        layoutLoading.setVisibility(View.GONE);
        if (posts == null || posts.isEmpty()) {
            txtEmptyIcon.setText("⚠️");
            txtEmptyMessage.setText(R.string.feed_erro_tentar_novamente);
            layoutEmpty.setOnClickListener(v -> loadPosts());
            layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(this, R.string.feed_erro, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Diálogo de publicação de aviso (ADMIN)
     */
    private void mostrarDialogoPublicar() {
        View view = getLayoutInflater().inflate(R.layout.dialog_publicar_post, null);
        EditText editTitulo = view.findViewById(R.id.editTituloPost);
        EditText editTexto = view.findViewById(R.id.editTextoPost);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.post_dialog_titulo)
                .setView(view)
                .setPositiveButton(R.string.post_botao_publicar, null)
                .setNegativeButton(R.string.post_botao_cancelar, null)
                .create();

        // Sobrescreve o clique do "Publicar" para o diálogo não fechar quando a validação falha
        dialog.setOnShowListener(d -> {
            Button btnPublicar = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            btnPublicar.setOnClickListener(v -> {
                String titulo = editTitulo.getText().toString().trim();
                String texto = editTexto.getText().toString().trim();

                if (titulo.isEmpty()) {
                    editTitulo.setError(getString(R.string.post_titulo_obrigatorio));
                    editTitulo.requestFocus();
                    return;
                }
                if (texto.isEmpty()) {
                    editTexto.setError(getString(R.string.post_texto_obrigatorio));
                    editTexto.requestFocus();
                    return;
                }

                // Evita publicar duas vezes com toque duplo
                btnPublicar.setEnabled(false);
                executor.execute(() -> {
                    EventApiClient.ApiResult result = postApiClient.createPost(
                            titulo, texto, sessionManager.getAccessToken());
                    runOnUiThread(() -> {
                        if (isFinishing() || isDestroyed()) return;
                        if (result.success) {
                            dialog.dismiss();
                            Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show();
                            recarregarPosts();
                        } else {
                            btnPublicar.setEnabled(true);
                            Toast.makeText(this, result.message, Toast.LENGTH_LONG).show();
                        }
                    });
                });
            });
        });

        dialog.show();
    }

    /**
     * Diálogo com o texto completo do aviso; ADMIN também pode excluir
     */
    private void mostrarDetalhes(PostModel post) {
        String mensagem = post.getDescricao();
        if (post.getData() != null && !post.getData().isEmpty()) {
            mensagem += "\n\n" + getString(R.string.icone_data) + " " + post.getData();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(post.getTitulo())
                .setMessage(mensagem)
                .setPositiveButton(R.string.post_botao_fechar, null);

        if (sessionManager.isAdmin()) {
            builder.setNegativeButton(R.string.post_botao_excluir, (d, which) -> confirmarExclusao(post));
        }

        builder.show();
    }

    private void confirmarExclusao(PostModel post) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.post_excluir_titulo)
                .setMessage(getString(R.string.post_excluir_mensagem, post.getTitulo()))
                .setPositiveButton(R.string.post_excluir_confirmar, (dialog, which) -> {
                    executor.execute(() -> {
                        EventApiClient.ApiResult result = postApiClient.deletePost(
                                post.getId(), sessionManager.getAccessToken());
                        runOnUiThread(() -> {
                            if (isFinishing() || isDestroyed()) return;
                            Toast.makeText(this, result.message,
                                    result.success ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG).show();
                            if (result.success) {
                                recarregarPosts();
                            }
                        });
                    });
                })
                .setNegativeButton(R.string.post_excluir_cancelar, null)
                .show();
    }

    // Implementação dos callbacks do PostAdapter
    @Override
    public void onPostClick(PostModel post) {
        mostrarDetalhes(post);
    }

    @Override
    public void onVerDetalhesClick(PostModel post) {
        mostrarDetalhes(post);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}
