package com.lastfom.launcher;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MainActivity extends Activity {

    private EditText edtUsername;
    private Button btnImport;
    private Button btnPlay;
    private TextView txtStatus;
    private File gameNativeLib = null;
    private static final int PICK_APK_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edtUsername = findViewById(R.id.edtUsername);
        btnImport = findViewById(R.id.btnImport);
        btnPlay = findViewById(R.id.btnPlay);
        txtStatus = findViewById(R.id.txtStatus);

        // Ação para importar o APK do Minecraft armazenado no celular
        btnImport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("application/vnd.android.package-archive");
                startActivityForResult(Intent.createChooser(intent, "Selecione o APK do Minecraft"), PICK_APK_REQUEST);
            }
        });

        // Ação para realizar o login offline e rodar o jogo importado
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = edtUsername.getText().toString().trim();

                if (username.isEmpty()) {
                    Toast.makeText(MainActivity.this, getString(R.string.error_empty_name), Toast.LENGTH_LONG).show();
                    return;
                }

                if (gameNativeLib == null || !gameNativeLib.exists()) {
                    Toast.makeText(MainActivity.this, getString(R.string.error_no_apk_selected), Toast.LENGTH_LONG).show();
                    return;
                }

                // Login Offline confirmado com o nome inserido
                Toast.makeText(MainActivity.this, getString(R.string.success_logged_in) + " " + username, Toast.LENGTH_SHORT).show();

                // Execução nativa da engine do Minecraft Bedrock compilada para armeabi-v7a
                try {
                    System.load(gameNativeLib.getAbsolutePath());
                    Toast.makeText(MainActivity.this, "Iniciando Minecraft Bedrock...", Toast.LENGTH_SHORT).show();
                    // Aqui a biblioteca nativa .so do jogo assume a execução gráfica
                } catch (UnsatisfiedLinkError e) {
                    Toast.makeText(MainActivity.this, "Erro de arquitetura: Garanta que o APK é armeabi-v7a", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    // Processa o APK selecionado pelo usuário e extrai os binários bin_v7a do jogo
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_APK_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri apkUri = data.getData();
            if (apkUri != null) {
                try {
                    InputStream is = getContentResolver().openInputStream(apkUri);
                    ZipInputStream zis = new ZipInputStream(is);
                    ZipEntry entry;
                    File outputDir = getDir("libs", MODE_PRIVATE);
                    
                    // Procura pela biblioteca nativa do Minecraft dentro do APK para sistemas de 32-bits (armeabi-v7a)
                    while ((entry = zis.getNextEntry()) != null) {
                        if (entry.getName().equals("lib/armeabi-v7a/libminecraftpe.so")) {
                            gameNativeLib = new File(outputDir, "libminecraftpe.so");
                            FileOutputStream fos = new FileOutputStream(gameNativeLib);
                            byte[] buffer = new byte[4096];
                            int length;
                            while ((length = zis.read(buffer)) > 0) {
                                fos.write(buffer, 0, length);
                            }
                            fos.close();
                            break;
                        }
                    }
                    zis.close();

                    if (gameNativeLib != null && gameNativeLib.exists()) {
                        txtStatus.setText(getString(R.string.status_apk_loaded));
                        txtStatus.setTextColor(0xFF388E3C); // Altera o texto para verde (Sucesso)
                    } else {
                        Toast.makeText(this, "Nenhuma biblioteca armeabi-v7a encontrada no APK!", Toast.LENGTH_LONG).show();
                    }

                } catch (Exception e) {
                    Toast.makeText(this, "Falha ao processar arquivo APK: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
