// This line defines the "package" where this file belongs.
// A package is like a folder that groups related files together.
// Here, the package is called "com.springai.vector_store_api_basics.model".
package com.springai.vector_store_api_basics.model;

// Importing Jakarta Persistence (JPA) annotations.
// These are used to map Java classes to database tables.
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// Importing Hibernate-specific annotations.
// These allow us to work with advanced database column types (like vectors).
import org.hibernate.annotations.Array;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

// Importing Java utility class for generating unique IDs.
import java.util.UUID;

// @Entity tells JPA (Java Persistence API):
// "This class represents a table in the database."
// Each object of this class corresponds to one row in that table.
@Entity

// @Table(name = "documents") tells JPA:
// "Map this class to the database table named documents."
@Table(name = "documents")
public class VectorDocument {

    @Id
    // @Id tells JPA:
    // "This field is the primary key of the table."
    // A primary key uniquely identifies each row in the table.
    //
    // Here, we generate a random UUID string as the ID by default.
    private String id = UUID.randomUUID().toString();

    @Column(columnDefinition = "TEXT")
    // @Column(columnDefinition = "TEXT") tells JPA:
    // "Store this field as a TEXT column in the database."
    // This is useful for storing large text documents.
    private String text;

    @JdbcTypeCode(SqlTypes.VECTOR)
    // @JdbcTypeCode(SqlTypes.VECTOR) tells Hibernate:
    // "This column stores vector data (special type for embeddings)."
    @Array(length = 3072)
    // @Array(length = 3072) specifies the length of the vector (3072 dimensions).
    @Column(name="embedding", columnDefinition = "vector(3072)")
    // @Column(name="embedding", columnDefinition = "vector(3072)") tells JPA:
    // "Map this field to a database column named 'embedding' with type vector(3072)."
    //
    // This is where we store the AI-generated embedding (numerical representation of text).
    private float[] embedding;

    // Default constructor (required by JPA).
    public VectorDocument() {
    }

    // Constructor with parameters.
    // Allows creating a VectorDocument with text and embedding directly.
    public VectorDocument(String text, float[] embedding) {
        this.text = text;
        this.embedding = embedding;
    }

    // Getter and Setter for ID.
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    // Getter and Setter for text.
    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }

    // Getter and Setter for embedding.
    public float[] getEmbedding() {
        return embedding;
    }
    public void setEmbedding(float[] embedding) {
        this.embedding = embedding;
    }
}