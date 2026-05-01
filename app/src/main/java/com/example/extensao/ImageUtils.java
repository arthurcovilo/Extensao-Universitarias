package com.example.extensao;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;

/**
 * Classe utilitária para carregamento de imagens
 */
public class ImageUtils {
    
    private static final int DEFAULT_TIMEOUT = 10000; // 10 segundos
    private static final int DEFAULT_CORNER_RADIUS = 24; // 24dp
    
    /**
     * Construtor privado para prevenir instanciação
     */
    private ImageUtils() {
        super(); // Explicit super constructor call
        // Utility class - não deve ser instanciada
    }
    
    /**
     * Carrega uma imagem com configurações padrão para posts
     */
    public static void loadPostImage(Context context, String imageUrl, ImageView imageView) {
        Glide.with(context)
                .load(imageUrl)
                .centerCrop()
                .transform(new RoundedCorners(DEFAULT_CORNER_RADIUS))
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .timeout(DEFAULT_TIMEOUT)
                .into(imageView);
    }
    
    /**
     * Carrega uma imagem com raio de borda customizado
     */
    public static void loadPostImage(Context context, String imageUrl, ImageView imageView, int cornerRadius) {
        Glide.with(context)
                .load(imageUrl)
                .centerCrop()
                .transform(new RoundedCorners(cornerRadius))
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .timeout(DEFAULT_TIMEOUT)
                .into(imageView);
    }
    
    /**
     * Limpa a imagem do ImageView
     */
    public static void clearImage(Context context, ImageView imageView) {
        Glide.with(context).clear(imageView);
    }
}