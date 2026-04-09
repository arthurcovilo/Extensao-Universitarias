package com.example.extensao;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class DetalhesEventoActivity extends AppCompatActivity {

    TextView txtTitulo, txtCategoria, txtData, txtHorario, txtLocal, txtOrganizador, txtDescricao;
    Button btnConfirmar;

    // Chaves para receber dados via Intent
    public static final String EXTRA_TITULO       = "titulo";
    public static final String EXTRA_CATEGORIA    = "categoria";
    public static final String EXTRA_DATA         = "data";
    public static final String EXTRA_HORARIO      = "horario";
    public static final String EXTRA_LOCAL        = "local";
    public static final String EXTRA_ORGANIZADOR  = "organizador";
    public static final String EXTRA_DESCRICAO    = "descricao";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes_evento);

        // Botão de voltar na ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Detalhes do Evento");
        }

        txtTitulo      = findViewById(R.id.txtDetalheTitulo);
        txtCategoria   = findViewById(R.id.txtDetalheCategoria);
        txtData        = findViewById(R.id.txtDetalheData);
        txtHorario     = findViewById(R.id.txtDetalheHorario);
        txtLocal       = findViewById(R.id.txtDetalheLocal);
        txtOrganizador = findViewById(R.id.txtDetalheOrganizador);
        txtDescricao   = findViewById(R.id.txtDetalheDescricao);
        btnConfirmar   = findViewById(R.id.btnConfirmarPresenca);

        // Recebe os dados enviados pela EventosActivity
        Intent intent = getIntent();
        txtTitulo.setText(intent.getStringExtra(EXTRA_TITULO));
        txtCategoria.setText(intent.getStringExtra(EXTRA_CATEGORIA));
        txtData.setText(intent.getStringExtra(EXTRA_DATA));
        txtHorario.setText(intent.getStringExtra(EXTRA_HORARIO));
        txtLocal.setText(intent.getStringExtra(EXTRA_LOCAL));
        txtOrganizador.setText("Organizador: " + intent.getStringExtra(EXTRA_ORGANIZADOR));
        txtDescricao.setText(intent.getStringExtra(EXTRA_DESCRICAO));

        btnConfirmar.setOnClickListener(v ->
                Toast.makeText(this, "Presença confirmada!", Toast.LENGTH_SHORT).show()
        );
    }

    // Faz o botão "←" da ActionBar voltar para a tela anterior
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// COMO ABRIR ESTA TELA A PARTIR DE EventosActivity:
//
//   listViewEventos.setOnItemClickListener((parent, view, position, id) -> {
//       Evento evento = listaEventos.get(position);
//       Intent intent = new Intent(EventosActivity.this, DetalhesEventoActivity.class);
//       intent.putExtra(DetalhesEventoActivity.EXTRA_TITULO,      evento.titulo);
//       intent.putExtra(DetalhesEventoActivity.EXTRA_CATEGORIA,   evento.categoria);
//       intent.putExtra(DetalhesEventoActivity.EXTRA_DATA,        evento.data);
//       intent.putExtra(DetalhesEventoActivity.EXTRA_HORARIO,     evento.horario);
//       intent.putExtra(DetalhesEventoActivity.EXTRA_LOCAL,       evento.local);
//       intent.putExtra(DetalhesEventoActivity.EXTRA_ORGANIZADOR, evento.organizador);
//       intent.putExtra(DetalhesEventoActivity.EXTRA_DESCRICAO,   evento.descricao);
//       startActivity(intent);
//   });
// ─────────────────────────────────────────────────────────────────────────────
