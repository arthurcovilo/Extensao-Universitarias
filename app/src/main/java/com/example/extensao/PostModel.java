package com.example.extensao;

/**
 * Modelo de dados para representar um post do feed
 */
public class PostModel {
    
    private String id;
    private String titulo;
    private String descricao;
    private String imagemUrl;
    private String data;
    private String local;
    
    /**
     * Construtor completo
     */
    public PostModel(String id, String titulo, String descricao, String imagemUrl, String data, String local) {
        super(); // Explicit super constructor call
        this.id = id;
        this.titulo = titulo;
        this.descricao = descricao;
        this.imagemUrl = imagemUrl;
        this.data = data;
        this.local = local;
    }
    
    /**
     * Construtor vazio
     */
    public PostModel() {
        super(); // Explicit super constructor call
    }
    
    // Getters
    public String getId() {
        return id;
    }
    
    public String getTitulo() {
        return titulo;
    }
    
    public String getDescricao() {
        return descricao;
    }
    
    public String getImagemUrl() {
        return imagemUrl;
    }
    
    public String getData() {
        return data;
    }
    
    public String getLocal() {
        return local;
    }
    
    // Setters
    public void setId(String id) {
        this.id = id;
    }
    
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }
    
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
    
    public void setImagemUrl(String imagemUrl) {
        this.imagemUrl = imagemUrl;
    }
    
    public void setData(String data) {
        this.data = data;
    }
    
    public void setLocal(String local) {
        this.local = local;
    }
    
    // Métodos utilitários
    public boolean hasImage() {
        return imagemUrl != null && !imagemUrl.trim().isEmpty();
    }
    
    public boolean hasLocal() {
        return local != null && !local.trim().isEmpty();
    }
    
    @Override
    public String toString() {
        return "PostModel{" +
                "id='" + id + '\'' +
                ", titulo='" + titulo + '\'' +
                ", descricao='" + descricao + '\'' +
                ", imagemUrl='" + imagemUrl + '\'' +
                ", data='" + data + '\'' +
                ", local='" + local + '\'' +
                '}';
    }
}