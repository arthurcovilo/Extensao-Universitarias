package com.example.extensao;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class CadastroEventoActivity extends AppCompatActivity {

    EditText editTitulo, editData, editHorario, editLocal, editDescricao;
    Spinner spinnerCategoria;
    Button btnSalvar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_evento);

        // Botão de voltar na ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Novo Evento");
        }

        editTitulo      = findViewById(R.id.editTitulo);
        editData        = findViewById(R.id.editData);
        editHorario     = findViewById(R.id.editHorario);
        editLocal       = findViewById(R.id.editLocal);
        editDescricao   = findViewById(R.id.editDescricao);
        spinnerCategoria = findViewById(R.id.spinnerCategoria);
        btnSalvar       = findViewById(R.id.btnSalvarEvento);

        // Popula o Spinner de categorias
        ArrayAdapter<CharSequence> adapterCategoria = ArrayAdapter.createFromResource(
                this,
                R.array.categorias_evento,   // defina no strings.xml
                android.R.layout.simple_spinner_item
        );
        adapterCategoria.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoria.setAdapter(adapterCategoria);

        // Ao clicar no campo Data, abre o DatePickerDialog
        editData.setFocusable(false);
        editData.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, day) -> {
                String data = String.format("%02d/%02d/%04d", day, month + 1, year);
                editData.setText(data);
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        // Ao clicar no campo Horário, abre o TimePickerDialog
        editHorario.setFocusable(false);
        editHorario.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new TimePickerDialog(this, (view, hour, minute) -> {
                String hora = String.format("%02d:%02d", hour, minute);
                editHorario.setText(hora);
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
        });

        // Recebe a data pré-selecionada vinda da MainActivity (CalendarView)
        String dataSelecionada = getIntent().getStringExtra("dataSelecionada");
        if (dataSelecionada != null && !dataSelecionada.isEmpty()) {
            editData.setText(dataSelecionada);
        }

        btnSalvar.setOnClickListener(v -> {
            String titulo    = editTitulo.getText().toString().trim();
            String data      = editData.getText().toString().trim();
            String horario   = editHorario.getText().toString().trim();
            String local     = editLocal.getText().toString().trim();
            String descricao = editDescricao.getText().toString().trim();
            String categoria = spinnerCategoria.getSelectedItem().toString();

            // Validação básica dos campos obrigatórios
            if (titulo.isEmpty()) {
                editTitulo.setError("Informe o título do evento");
                return;
            }
            if (data.isEmpty()) {
                editData.setError("Informe a data");
                return;
            }
            if (horario.isEmpty()) {
                editHorario.setError("Informe o horário");
                return;
            }

            // Aqui você pode salvar no banco de dados (Room, Firebase, etc.)
            // Por enquanto exibe um Toast de confirmação
            Toast.makeText(this,
                    "Evento \"" + titulo + "\" salvo com sucesso!",
                    Toast.LENGTH_LONG).show();

            // Volta para a tela de Eventos após salvar
            startActivity(new Intent(this, EventosActivity.class));
            finish();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// COMO ABRIR ESTA TELA A PARTIR DE MainActivity (botão "Novo evento"):
//
//   btnNovoEvento.setOnClickListener(v -> {
//       if (dataSelecionada.isEmpty()) {
//           Toast.makeText(this, "Selecione uma data", Toast.LENGTH_SHORT).show();
//       } else {
//           Intent intent = new Intent(MainActivity.this, CadastroEventoActivity.class);
//           intent.putExtra("dataSelecionada", dataSelecionada);
//           startActivity(intent);
//       }
//   });
// ─────────────────────────────────────────────────────────────────────────────
//
// LEMBRE DE ADICIONAR NO strings.xml:
//   <string-array name="categorias_evento">
//       <item>Workshop</item>
//       <item>Palestra</item>
//       <item>Reunião</item>
//       <item>Curso</item>
//       <item>Outro</item>
//   </string-array>
// ─────────────────────────────────────────────────────────────────────────────
